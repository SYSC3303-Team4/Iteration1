/**
*Class:             TFTPWriterThread.java
*Project:           TFTP Project - Group 4
*Author:            Nathaniel Charlebois                                            
*Date of Update:    29/09/2016                                              
*Version:           1.0.0                                                      
*                                                                                    
*Purpose:           Handles the WRQs by:
*						-Receiving the WRQ
*						-Sending the special 0 ACK
*						-Receiving the next DATA
*						-Sending an ACK
*
*To do:
*	-Clean up code
*	-Test functionality with other components
*	-Use the UI
*
*					
* 
* 
*Update Log:    	v1.1.1
*                       - null
*/

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.swing.JTextArea;

class TFTPWriteThread implements Runnable
{
    /**
     * The text area where this thread's output will be displayed.
     */
    JTextArea transcript;
    private DatagramPacket sendPacket;
    private DatagramSocket sendSocket;
    private DatagramSocket receiveSocket;
    private DatagramPacket receivePacket;
    private int blockNumber = 0;
    private String threadNumber;
    public static final byte[] response = {0, 4, 0, 0};
    
    

    public TFTPWriteThread(JTextArea transcript, DatagramPacket receivePacketInfo,String thread) {
        this.transcript = transcript;
        receivePacket = receivePacketInfo;
        threadNumber = thread;
    }

    public void run() {

       int len, j=0;
	   len = sendPacket.getLength();
	
	   //Parsing Data for filename and mode
	   ByteArrayOutputStream filename = new ByteArrayOutputStream();
	   ByteArrayOutputStream mode = new ByteArrayOutputStream();
	   boolean change = false; 
	   for(int i = 2; i<receivePacket.getData().length;i++){
		   if(receivePacket.getData()[i]>=32){
			   if(change == false){
				   filename.write(receivePacket.getData()[i]);
			   }
			   else{
				   mode.write(receivePacket.getData()[i]);
			   }
		   }
		   if(receivePacket.getData()[i]!=0){
			   if(receivePacket.getData()[i+1] == 0){
				   change = true;
				   i++;
			   }
			}
	   }
	   System.out.println("Request parsed for:");
	   System.out.println("	Filename: " + new String(filename.toByteArray(),0,filename.toByteArray().length));
	   System.out.println("	Mode: " + new String(mode.toByteArray(),0,mode.toByteArray().length) + "\n");
	   
	   //Build and send the first ACK reply in format:
	   /*
          2 bytes    2 bytes
          -------------------
   ACK   | 04    |   Block #  |
          --------------------
	    */
	   if(blockNumber == 0){
		   sendPacket = new DatagramPacket(response, response.length,
	               receivePacket.getAddress(), receivePacket.getPort());
	
	       System.out.println("Server: Sending packet:");
	       System.out.println("To host: " + sendPacket.getAddress());
	       System.out.println("Destination host port: " + sendPacket.getPort());
	       len = sendPacket.getLength();
	       System.out.println("Length: " + len);
	       System.out.println("Containing: ");
	       for (j=0;j<len;j++) {
	    	   System.out.println("byte " + j + " " + response[j]);
	       }
			
	       // Send the datagram packet to the client via a new socket.
	       try {
	    	   // Construct a new datagram socket and bind it to any port
	    	   // on the local host machine. This socket will be used to
	    	   // send UDP Datagram packets.
	    	   sendSocket = new DatagramSocket();
	       } catch (SocketException se) {
	    	   se.printStackTrace();
	    	   System.exit(1);
	       }
			
	       try {
	    	   sendSocket.send(sendPacket);
	       } catch (IOException e) {
	    	   e.printStackTrace();
	    	   System.exit(1);
	       }
	       System.out.println("Server: packet sent using port " + sendSocket.getLocalPort());
	       System.out.println();
	   }
		
       //Wait for next DATA datagram in format:
       /*
          2 bytes    2 bytes       n bytes
          ---------------------------------
   DATA  | 03    |   Block #  |    Data    |
          ---------------------------------
        */
       
       System.out.println("Server: Waiting for packet.");
       // Block until a datagram packet is received from receiveSocket.
       try {
          receiveSocket.receive(receivePacket);
       } catch (IOException e) {
          e.printStackTrace();
          System.exit(1);
       }
       
       byte[] data = new byte[receivePacket.getLength()-4];
       //Parse data from DATA packet
       for(int i = 4; i < receivePacket.getLength();i++){
    	   data[i-4] = receivePacket.getData()[i];
       }

	    
       //Write file to directory
       TFTPWriter writer = new TFTPWriter();
       try {
			writer.write(data,filename.toString("UTF_8"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	   
       //Sending the ACK for previous DATA packet in format:
       /*
          2 bytes    2 bytes
          -------------------
   ACK   | 04    |   Block #  |
          --------------------
        */
       
	   response[2]=receivePacket.getData()[2];
	   response[3]=receivePacket.getData()[3];
	   blockNumber++;
	   
	       
       sendPacket = new DatagramPacket(response, response.length,
                             receivePacket.getAddress(), receivePacket.getPort());

       System.out.println("Server: Sending packet:");
       System.out.println("To host: " + sendPacket.getAddress());
       System.out.println("Destination host port: " + sendPacket.getPort());
       len = sendPacket.getLength();
       System.out.println("Length: " + len);
       System.out.println("Block Number: " + blockNumber);
       System.out.println("Containing: ");
       for (j=0;j<len;j++) {
          System.out.println("byte " + j + " " + response[j]);
       }

       // Send the datagram packet to the client via a new socket.

       try {
          // Construct a new datagram socket and bind it to any port
          // on the local host machine. This socket will be used to
          // send UDP Datagram packets.
          sendSocket = new DatagramSocket();
       } catch (SocketException se) {
          se.printStackTrace();
          System.exit(1);
       }

       try {
          sendSocket.send(sendPacket);
       } catch (IOException e) {
          e.printStackTrace();
          System.exit(1);
       }

       System.out.println("Server: packet sent using port " + sendSocket.getLocalPort());
       System.out.println();

       run();
       // We're finished with this socket, so close it.
       //sendSocket.close();
	
       //transcript.append(Thread.currentThread() + " finished\n");
    }
}
