package main;

import data.*;


import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * The ClackClient class contains the following variables:
 * <ul>
 * <li>userName - String representing name of the client</li>
 * <li>hostName - String representing name of the computer representing the server</li>
 * <li>port - Integer representing port number on server connected to</li>
 * <li>closeConnection - boolean representing whether connection is closed or not</li>
 * <li>dataToSendToServer - ClackData object representing data sent to server</li>
 * <li>dataToReceiveFromServer - ClackData object representing data received from the server</li>
 * <li>inFromStd - Scanner object representing standard input</li>
 * <li>inFromServer - ObjectInputStream to receive information from server</li>
 * <li>outToServer - ObjectOutputStream to send information to server</li>
 * </ul>
 * @author Iselda Aiello, Sydney DeCyllis
 */
public class ClackClient{
  //default port number
  final static int DEFAULT_PORT = 7000;
  final static String KEY = "follow me @xxiaie on twitter.com";

  private Scanner inFromStd;
  private String userName;
  private String hostName;
  private int port;
  private boolean closeConnection;
  private ClackData dataToSendToServer;
  private ClackData dataToReceiveFromServer;
  private ObjectInputStream inFromServer;
  private ObjectOutputStream outToServer;

  /**
   * Constructor for username, host name, and port, connection should be set to be open <br>
   * Should set dataToSendToServer and dataToReceiveFromServer as null
   * @param userName sets the username of the user
   * @param hostName sets the hostname of the server
   * @param port sets the port to be connected to on the server
   */
  public ClackClient(String userName,String hostName,int port){
    if(userName == null) throw new IllegalArgumentException("Username cannot be null");
    if(hostName == null) throw new IllegalArgumentException("Hostname cannot be null");
    if(port < 1024) throw new IllegalArgumentException("Port must be 1024 or greater");
    this.userName = userName;
    this.hostName = hostName;
    this.port = port;
    this.closeConnection = false;
    this.dataToSendToServer = null;
    this.dataToReceiveFromServer = null;
    this.outToServer = null;
    this.inFromServer = null;
  }

  /**
   * Constructor to set up port to default port number 7000
   * @param userName sets the username of the user
   * @param hostName sets the hostname of the server
   */
  public ClackClient(String userName,String hostName){
    this(userName,hostName,DEFAULT_PORT);
  }

  /**
   * Constructor that sets host name to be localhost
   * @param userName sets the username of the user
   */
  public ClackClient(String userName){
    this(userName,"localhost");
  }

  /**
   * Default constructor that sets anonymous user
   */
  public ClackClient(){
    this("Anon");
  }

  /**
   * Does not return anything, but starts the connection from the client side
   */
  public void start(){

    try {
      Socket local = new Socket(hostName, port);
      local.setSoTimeout(400000);

      outToServer = new ObjectOutputStream(local.getOutputStream());
      inFromServer = new ObjectInputStream(local.getInputStream());

      ClientSideServerListener clientSideServerListener = new ClientSideServerListener(this);
      Thread thread = new Thread(clientSideServerListener);
      thread.start();

      //adds this user to the user list in ClackServer
      this.dataToSendToServer = new MessageClackData(this.userName, "init",ClackData.CONSTANT_LISTUSERS);
      sendData();
      this.dataToSendToServer = null;

      this.inFromStd = new Scanner(System.in);
      while(!this.closeConnection){
        readClientData();
        if(!this.closeConnection) thread.interrupt();
        sendData();
      }
      local.close();
      outToServer.close();
      inFromServer.close();
      inFromStd.close();
    } catch(UnknownHostException uhe){
      System.out.println("Unknown host Exception: " + uhe.getMessage());
    } catch(IOException ioe){
      System.out.println("IOException: " + ioe.getMessage() );
    }
    //this.inFromStd.close();
  }

  /**
   * Reads the data from the client, does not return anything
   */
  public void readClientData(){
    String userInput = this.inFromStd.next();
    if (userInput.equals("DONE")){
      this.closeConnection = true;
      this.dataToSendToServer = new MessageClackData(this.userName,userInput,KEY,ClackData.CONSTANT_LOGOUT);
    } else if (userInput.equals("SENDFILE")) {
      String fileName = this.inFromStd.next();
      this.dataToSendToServer = new FileClackData(this.userName, fileName, ClackData.CONSTANT_SENDFILE);
      try{
        ((FileClackData) this.dataToSendToServer).readFileContents(KEY);
      } catch(IOException ioe) {
        this.dataToSendToServer = null;
        System.err.println("Error reading the file: " + ioe.getMessage() );
      }
    } else if (userInput.equals("LISTUSERS")){
      this.dataToSendToServer = new MessageClackData(this.userName, "", ClackData.CONSTANT_LISTUSERS);
    } else {
      String message = userInput + this.inFromStd.nextLine();
      this.dataToSendToServer = new MessageClackData(this.userName,message,KEY,ClackData.CONSTANT_SENDMESSAGE);
    }
  }

  /**
   * Sends data to server, does not return anything
   */
  public void sendData(){
    try{
      outToServer.writeObject(dataToSendToServer);
    }
    catch(IOException ioe){
      System.err.println("ObjectInputStream Exception");
    }


  }

  /**
   * Receives data from the server, does not return anything
   */
  public void receiveData(){
    try{
      dataToReceiveFromServer = (ClackData) inFromServer.readObject();
    }
    catch(ClassNotFoundException cfe){
      System.err.println("Class could not be found: " + cfe.getMessage() );
    }
    catch(IOException ioe){
      System.err.println("ObjectInputStream Exception: " + ioe.getMessage() );
    }

  }

  /**
   * Prints the received data to standard output
   */
  public void printData(){
    //only prints user list
    if(this.dataToReceiveFromServer.getType() == ClackData.CONSTANT_LISTUSERS){
      System.out.println( this.dataToReceiveFromServer.getData() );
    } else {
      System.out.println("User: " + this.dataToReceiveFromServer.getUserName());
      System.out.println("Date: " + this.dataToReceiveFromServer.getDate());
      System.out.println("Data: " + this.dataToReceiveFromServer.getData(KEY) );
    }
  }

  /**
   * Returns the username
   * @return username of the user
   */
  public String getUserName(){
    return this.userName;
  }

  /**
   * Returns the hostname
   * @return hostname of the server
   */
  public String getHostName(){
    return this.hostName;
  }

  /**
   * Returns the port
   * @return port of the server
   */
  public int getPort(){
    return this.port;
  }

  /**
   * Returns the close connection variable
   * @return whether the connection is closed
   */
  public boolean getCloseConnection() { return this.closeConnection; }

  /**
   * Initializes a ClackClient object and connects to a server
   * @param args command line arguments
   */
  public static void main(String []args){
    String parse;
    ClackClient clackClient;
    try {
      if (args.length == 0) {
        clackClient = new ClackClient();
      } else {
        parse = args[0];
        if (parse.contains("@")) {
          String[] title = parse.split("@");
          parse = title[0];
          if (title[1].contains(":")) {
            title = title[1].split(":");
            clackClient = new ClackClient(parse, title[0], Integer.parseInt(title[1]));
          } else {
            clackClient = new ClackClient(parse, title[1]);
          }
        } else {
          clackClient = new ClackClient(parse);
        }
      }
      clackClient.start();
    } catch(NumberFormatException nfe){
      System.err.println("Excepted type int after ':'");
    }
  }

  @Override
  public int hashCode(){
    return this.toString().hashCode();
  }

  @Override
  public boolean equals(Object other){
    if(!(other instanceof ClackClient)) return false;
    ClackClient otherClackClient = (ClackClient)other;
    return this.userName.equals(otherClackClient.userName) &&
            this.hostName.equals(otherClackClient.hostName) &&
            this.port == otherClackClient.port &&
            this.closeConnection == otherClackClient.closeConnection &&
            this.dataToSendToServer.equals(otherClackClient.dataToSendToServer) &&
            this.dataToReceiveFromServer.equals(otherClackClient.dataToReceiveFromServer);
  }

  @Override
  public String toString(){
    String connection = this.closeConnection ? "closed" : "open";
    return "The username is: " + this.userName + "\n" +
            "The hostname is: " + this.hostName + "\n" +
            "The port number is: " + this.port + "\n" +
            "The connection is " + connection;
  }

}