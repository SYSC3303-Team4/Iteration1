/**
*Class:             TFTPReadThread.java
*Project:           TFTP Project - Group 4
*Author:            Nathaniel Charlebois                                            
*Date of Update:    29/09/2016                                              
*Version:           1.1.0                                                      
*                                                                                    
*Purpose:           Handles the RRQs by:
*						-Sending DATA
*						-Receiving ACK
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
import java.util.*;

import javax.swing.JTextArea;

class TFTPReadThread implements Runnable
{
    /**
     * The text area where this thread's output will be displayed.
     */
    private JTextArea transcript;
    private DatagramPacket receivePacket;
    private DatagramPacket sendPacket;
    private DatagramSocket sendSocket;
    private DatagramSocket receiveSocket;
    private int blockNumber = 1;
    private String threadNumber;
    public static final byte[] response = {0, 3, 0, 0};
    

    public TFTPReadThread(JTextArea transcript, DatagramPacket receivePacketInfo, String thread) {
        this.transcript = transcript;
        receivePacket = receivePacketInfo;
        threadNumber  = thread;
    }

    public void run() {
    	while(true){
		int len, j=0;
		len = sendPacket.getLength();

		//Parsing Data for filename
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



		//Encode the block number into the response block 
		    response[2]=(byte)(blockNumber & 0xFF);
		    response[3]=(byte)((blockNumber >> 8)& 0xFF);
		    blockNumber++;

		    TFTPReader reader = new TFTPReader();
		    //Building datagram		
			LinkedList<byte[]> data = null;
			try {
				data = reader.readAndSplit("testFile.dat");
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}



			//Builds the datagram in format
			/*
		2 bytes    2 bytes       n bytes
		---------------------------------
	 DATA  | 03    |   Block #  |    Data    |
		---------------------------------
		*/
			byte dataPrime[] = Arrays.copyOf(response, response.length + data.get(blockNumber).length); 
			System.arraycopy(data.get(blockNumber), 0, dataPrime, response.length, data.get(blockNumber).length);


		sendPacket = new DatagramPacket(dataPrime, dataPrime.length,
				      receivePacket.getAddress(), receivePacket.getPort());

		System.out.println("Server: Sending packet:");
		System.out.println("To host: " + sendPacket.getAddress());
		System.out.println("Destination host port: " + sendPacket.getPort());

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

		//Waiting to receive ACK
		try {
		    receiveSocket.receive(receivePacket);
		 } catch (IOException e) {
		    e.printStackTrace();
		    System.exit(1);
		 }
		//Check for ACK in format
		/*
		2 bytes    2 bytes
		-------------------
	 ACK   | 04    |   Block #  |
		--------------------
		*/
		if(receivePacket.getData()[0] == 0 && receivePacket.getData()[1] == 4){
			blockNumber++;

		}
        }
        
        
        // We're finished with this socket, so close it.
        sendSocket.close();
		
        transcript.append(Thread.currentThread() + " finished\n");
    }
    
}
