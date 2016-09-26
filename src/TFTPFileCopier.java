import java.io.*;

/**
 * FileCopier.java - Demonstrates how to use Java's byte stream I/O
 * classes to copy a file.
 */
public class TFTPFileCopier
{
	
	private String clientFilepath;
	private String serverFilepath;
	
	public TFTPFileCopier(){
		clientFilepath = new String("C:"+ File.separator +"Users"+ File.separator+"Public"+ File.separator +"Documents");
		serverFilepath = new String("C:"+ File.separator +"Users"+ File.separator+"Public"+ File.separator +"Documents");
		
	}
	
    public void transfer(byte[] filename)
    throws FileNotFoundException, IOException
    {
        /*
         * A FileInputStream object is created to read the file
         * as a byte stream. A BufferedInputStream object is wrapped
         * around the FileInputStream, which may increase the
         * efficiency of reading from the stream.
         */
        BufferedInputStream in = 
            new BufferedInputStream(new FileInputStream(clientFilepath + File.separator + new String(filename)));

        /*
         * A FileOutputStream object is created to write the file
         * as a byte stream. A BufferedOutputStream object is wrapped
         * around the FileOutputStream, which may increase the
         * efficiency of writing to the stream.
         */
        BufferedOutputStream out =
            new BufferedOutputStream(new FileOutputStream(serverFilepath + File.separator + new String(filename)));

        byte[] data = new byte[512];
        int n;
        
        /* Read the file in 512 byte chunks. */
        while ((n = in.read(data)) != -1) {
            /* 
             * We just read "n" bytes into array data. 
             * Now write them to the output file. 
             */
            out.write(data, 0, n);
        }
        in.close();
        out.close();
    }
    
    public void setClientFilepath(String Filepath){
    	clientFilepath = Filepath;
    }
    public String getCLientFilepath(){
    	return clientFilepath;
    }
    
}