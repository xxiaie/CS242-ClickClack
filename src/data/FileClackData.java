package data;

import java.io.*;

/**
 * The class MessageClackData is a subclass of ClackData and contains the following variables:
 * <ul>
 * <li>fileName - String representing name of file</li>
 * <li>fileContents - String representing contents of file</li>
 * </ul>
 * @author Iselda Aiello, Sydney DeCyllis
 */
public class FileClackData extends ClackData{

  private String fileName;
  private String fileContents;

  /**
   * Constructor to set up username, fileName, and type
   * @param userName sets the username of the user
   * @param fileName sets the name of the file
   * @param type sets the kind of data exchanged between client and server
   */
  public FileClackData(String userName,String fileName,int type){
    super(userName,type);
    this.fileName = fileName;
    this.fileContents = null;
  }

  /**
   * Default constructor
   */
  public FileClackData(){
    super();
    this.fileName = null;
    this.fileContents = null;
  }

  /**
   * Sets the file name in this object
   * @param fileName the new file name
   */
  public void setFileName(String fileName){
    this.fileName = fileName;
  }

  /**
   * Returns the file name
   * @return file name of the object
   */
  public String getFileName(){
    return this.fileName;
  }

  /**
   * Implemented here to return file contents
   * @return the contents of the file
   */
  public String getData(){
    return this.fileContents;
  }

  /**
   * overloaded here to return decrypted file contents
   * @param key the decryption key used to decrypt file contents
   * @return the decrypted file contents
   */
  public String getData(String key){
    return decrypt(this.fileContents, key);
  }

  /**
   * Performs a non-secure file read<br>
   * Reads in the data from the file called fileName and writes it
   * to the instance variable fileContents
   * @throws IOException when opening, reading, and closing the file
   */
  public void readFileContents() throws IOException{
    try {
      File file = new File (this.fileName);
      BufferedReader bufferedReader = new BufferedReader( new FileReader(file) );
      StringBuilder stringBuilder = new StringBuilder(500);
      String nextLine = bufferedReader.readLine();
      while(nextLine != null) {
        stringBuilder.append(nextLine);
        if ((nextLine = bufferedReader.readLine()) != null) {
          stringBuilder.append(System.getProperty("line.separator"));
        }
      }
      this.fileContents = stringBuilder.toString();
      bufferedReader.close();
    } catch(FileNotFoundException fnfe) {
      System.err.println("The file " + this.fileName + " does not exist");
    } catch(IOException ioe) {
      System.err.println("Error in reading or closing the file " + this.fileName);
    }
  }

  /**
   * Performs a secure file read<br>
   * Reads in the data from the file called fileName, encrypts it,
   * and writes it to the instance variable fileContents
   * @param key for encrypting fileContents
   * @throws IOException when opening, reading, and closing the file
   */
  public void readFileContents(String key) throws IOException{
    try {
      File file = new File (this.fileName);
      BufferedReader bufferedReader = new BufferedReader( new FileReader(file) );
      StringBuilder stringBuilder = new StringBuilder(500);
      String nextLine = bufferedReader.readLine();
      while(nextLine != null){
        stringBuilder.append(nextLine);
        if( (nextLine = bufferedReader.readLine()) != null) {
          stringBuilder.append(System.getProperty("line.separator"));
        }
      }
      this.fileContents = stringBuilder.toString();
      this.fileContents = encrypt(this.fileContents, key);
      bufferedReader.close();
    } catch(FileNotFoundException fnfe) {
      System.err.println("The file " + this.fileName + " does not exist");
    } catch(IOException ioe) {
      System.err.println("Error in reading or closing the file " + this.fileName);
    }
  }

  /**
   * Performs a non-secure file write<br>
   * Writes the data from the instance variable fileContents to
   * a file called fileName
   */
  public void writeFileContents(){
    try {
      File file = new File(this.fileName);
      BufferedWriter bufferedWriter = new BufferedWriter( new FileWriter(file) );
      bufferedWriter.write(this.fileContents);
      bufferedWriter.close();
    } catch(FileNotFoundException fnfe) {
      System.err.println("The file " + this.fileName + " does not exist");
    } catch(IOException ioe) {
      System.err.println("Error in reading or closing the file " + this.fileName);
    }
  }

  /**
   * Performs a secure file write<br>
   * Writes the data from the instance variable fileContents to
   * a file called fileName
   * @param key for decrypting fileContents
   */
  public void writeFileContents(String key){
    try {
      File file = new File(this.fileName);
      BufferedWriter bufferedWriter = new BufferedWriter( new FileWriter(file) );
      bufferedWriter.write(decrypt(this.fileContents,key));
      bufferedWriter.close();
    } catch(FileNotFoundException fnfe) {
      System.err.println("The file " + this.fileName + " does not exist");
    } catch(IOException ioe) {
      System.err.println("Error in reading or closing the file " + this.fileName);
    }
  }

  @Override
  public int hashCode(){
    return this.toString().hashCode();
  }
  
  @Override
  public boolean equals(Object other){
    if(!(other instanceof FileClackData)) return false;
    FileClackData otherFileClackData = (FileClackData)other;
    return this.username.equals(otherFileClackData.username) &&
            this.type == otherFileClackData.type && 
            this.date.equals(otherFileClackData.date) &&
            this.fileName.equals(otherFileClackData.fileName) &&
            this.fileContents.equals(otherFileClackData.fileContents);
  }

  @Override
  public String toString(){
    return "The username is: " + this.username + '\n' +
            "The date is: " + this.date.toString() + '\n' +
            "The file name is: " + this.fileName + '\n' +
            "The file contents are: " + this.fileContents;
  }

}
