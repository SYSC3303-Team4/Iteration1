/**
*Class:             TFTPClient.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    28/09/2016                                              
*Version:           1.1.2                                                      
*                                                                                   
*Purpose:           Generates a datagram following the format of [0,R/W,STR1,0,STR2,0],
					in which R/W signifies read (1) or write (2), STR1 is a filename,
					and STR2 is the mode. Sends this datagram to the IntermediateHost
					and waits lfor response from intermediateHost. Repeats this
					process ten times, then sends a datagram packet that DOES NOT
					follow the expected format stated above. Waits for response from
					IntermediateHost. We DO NOT expect a response to the badly formated
					packet. Datagram can be 512B max
* 
* 
*Update Log:        v1.1.2
*						- now implements TFTPReader class
*						- can read data from file
*						- DOES NOT SEND ACKS
*						- DOES NOT HAVE BLOCK NUMS
*						- bug with java.nullpointer exception in send() method fixed
*						- some methods renamed
*						- generateDatagram(..) replaced with 2 new methods:
*								generateRWRQ(...) | for RRQ or WRQ
*								generateDATA()	  | for DATA
*						- sendAndEcho() is now private, only called from master send() method
*						- a ton of constants added (see class constants section)
*						- 
*					v1.1.0
*						- verbose mode added (method added)
*						- client can now send datagrams to errorSim OR
*						  directly to server (ie method testMode)
*						- renamed constant PORT --> IN_PORT_ERRORSIM
*						- added cnst IN_PORT_SERVER
*						- added var outPort
*						- added method testMode(...)
*						- added var verbose
*						- close method added
						- name changed from 'Client' to 'TFTPClient'
						 (are you happy now Sarah??!?!?!?!?!)
*					v1.0.0
*                       - null
*/


//import external libraries
import java.io.*;
import java.net.*;
import java.util.*;


public class TFTPClient 
{
	//declaring local instance variables
	private DatagramPacket sentPacket;
	private DatagramPacket recievedPacket;
	private DatagramSocket generalSocket;
	private boolean verbose;
	private int outPort;
	private TFTPReader reader ;
	private byte[] blockNum ;
	
	//declaring local class constants
	private static final int IN_PORT_ERRORSIM = 23;
	private static final int IN_PORT_SERVER = 69;
	private static final int MAX_SIZE = 512;
	private static final byte[] OPCODE_RRQ =  {0,1}; 
	private static final byte[] OPCODE_WRQ =  {0,2};
	private static final byte[] OPCODE_DATA = {0,3};
	
	
	//generic constructor
	public TFTPClient()
	{
		//construct a socket, bind to any local port
		try
		{
			generalSocket = new DatagramSocket();
		}
		//enter if socket creation results in failure
		catch (SocketException se)
		{
			se.printStackTrace();
			System.exit(1);
		}
		//initialize echo --> off
		verbose = false;
		//initialize test mode --> off
		outPort = IN_PORT_SERVER ;
		//make an empty reader
		reader = new TFTPReader();
	}
	
	
	//generic accessors and mutators
	public DatagramPacket getSentPacket()
	{
		return sentPacket;
	}
	public DatagramPacket getRecievedPacket()
	{
		return recievedPacket;
	}
	public DatagramSocket getGeneralSocket()
	{
		return generalSocket;
	}
	public void setSentPacket(DatagramPacket dp)
	{
		sentPacket = dp;
	}
	public void setRecievedPacket(DatagramPacket dp)
	{
		recievedPacket = dp;
	}
	public void setGeneralSocket(DatagramSocket gs)
	{
		generalSocket = gs;
	}
	
	
	//enable/disable verbose mode
	public void verboseMode(boolean v)
	{
		verbose = v;
	}
	
	
	//enable/disable test mode
	public void testMode(boolean t)
	{
		//test mode ON
		if (t)
		{
			outPort = IN_PORT_ERRORSIM;
		}
		//test mode OFF
		else
		{
			outPort = IN_PORT_SERVER;
		}
	}
	
	
	//close client properly
	//***FUNCTIONALITY OF CLIENT WILL CEASE ONCE CALLED***
	public void close()
	{
		//close sockets
		generalSocket.close();
	}
	
	
	//generate DatagramPacket, save as sentPacket
	//type: DATA
	public void generateDATA()
	{
		if(verbose)
		{
			System.out.println("Client: Prepping DATA packet #" + "NULL");
		}
		
		//construct array to hold data
		byte[] data = reader.pop();
		byte[] toSend = new byte[data.length + 4];
		
		//constuct array
		for(int i=0; i<2; i++)
		{
			toSend[i] = OPCODE_DATA[i];
		}
		for(int i = 2; i < 4; i++)
		{
			toSend[i] = 0x09 ;
		}
		for(int i = 0; i < data.length; i++)
		{
			toSend[i+4] = data[i] ;
		}
		
		//generate and save datagram packet
		try
		{
			sentPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), outPort);
			if(verbose)
			{
				System.out.println("Client: Packet successfully created");
			}
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
	}
	
	
	//generate DatagramPacket, save as sentPacket
	//type: RRW or WRQ
	public void generateRWRQ(String fileName, String mode, byte RWval)
	{
		//generate the data to be sent in datagram packet
		if(verbose)
		{
			System.out.println("Client: Prepping packet containing '" + fileName + "'...");
		}	
		//convert various strings to Byte arrays
		byte[] fileNameBA = fileName.getBytes();
		byte[] modeBA = mode.getBytes();
			
		//compute length of data being sent (metadata include) and create byte array
		byte[] data = new byte[fileNameBA.length + modeBA.length + 4];
		int i = 2;
			
		//add first 2 bytes of metadata
		data[0] = 0x00;
		data[1] = RWval;
		//add text
		for(int c=0; c<fileNameBA.length; c++, i++)
		{
			data[i] = fileNameBA[c];
		}
		//add pesky 0x00
		data[i] = 0x00;
		i++;
		//add mode
		for(int c=0; c<modeBA.length; c++, i++)
		{
			data[i] = modeBA[c];
		}
		//add end metadata
		data[i] = 0x00;
			
		
		//generate and save datagram packet
		try
		{
			sentPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), outPort);
			if(verbose)
			{
				System.out.println("Client: Packet successfully created");
			}
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	
	//send datagram, recieve ACKs
	public void send(String file, String mode, byte RWval)
	{
		//read and split file
		try
		{
			reader.readAndSplit(file);
			
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		//prep RRQ/RRW to send
		generateRWRQ(file, mode, RWval);
		//send RRQ/RRW
		sendAndEcho();
		//wait for ACK
		//_INSERT ACK HERE_
		
		//send DATA
		while ( !(reader.isEmpty()) )
		{
			generateDATA();
			sendAndEcho();
			// _INSERT ACK HERE_
		}
	}
	
	
	//send and echo the datagram
	private void sendAndEcho()
	{
		//print packet info IF in verbose
		if(verbose)
		{
			byte[] data = sentPacket.getData();
			int packetSize = sentPacket.getLength();
			System.out.println("Client: Sending packet...");
			System.out.println("        Host:  " + sentPacket.getAddress());
			System.out.println("        Port:  " + sentPacket.getPort());
			System.out.println("        Bytes: " + sentPacket.getLength());
			System.out.printf("%s", "        Cntn:  ");
			for(int i = 0; i < packetSize; i++)
			{
				System.out.printf("0x%02X", data[i]);
				System.out.printf("%-2c", ' ');
			}
			System.out.println("");
			System.out.println("        Cntn:  " + (new String(data,0,packetSize)));
			
		}
		//send packet
		try
		{
			generalSocket.send(sentPacket);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Client: Packet Sent");
	}
	
	
	//receive and echo received packet
	public void receiveAndEcho()
	{
		//prep for response
		byte[] response = new byte[MAX_SIZE];
		recievedPacket = new DatagramPacket(response, response.length);
				
		//wait for response
		System.out.println("Client: Waiting for response...");
		try
		{
			generalSocket.receive(recievedPacket);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("Client: Packet received");
		
		//Process and print the response
		if(verbose)
		{
			byte[] data = recievedPacket.getData();
			int packetSize = recievedPacket.getLength();
			System.out.println("        Source: " + recievedPacket.getAddress());
			System.out.println("        Port:   " + recievedPacket.getPort());
			System.out.println("        Bytes:  " + packetSize);
			System.out.println("        Cntn:  " + (new String(data,0,packetSize)));
			System.out.printf("%s", "        Cntn:  ");
			for(int i = 0; i < packetSize; i++)
			{
				System.out.printf("0x%02X", data[i]);
				System.out.printf("%-2c", ' ');
			}
			System.out.printf("%-2c", '\n');
		}
	}
	
	
	public static void main (String[] args) 
	{
		//declaring local variables
		TFTPClient client = new TFTPClient();
		byte flipFlop = 0x01;
		
		//send directly to server and verbose ON
		client.testMode(false);
		client.verboseMode(true);
		
		//send full fille (includes wait for ACK)
		client.send("DatagramsOutForHarambe.txt", "octet", (byte)0x01);
		
		//receive server response
		
		
		
		
		
		
		/*
		while (reader.peek() != null)
		{
			//generate datagram RRW
			client.generateDatagram("DatagramsOutForHarambe.txt","octet", flipFlop);
		
			//send and echo outgoing datagram
			client.sendAndEcho();
		
			//idle until packet is received, echo and and save
			client.receiveAndEcho();
			
			//flip R/W byte
			if (flipFlop == 0x01)
			{
				flipFlop = 0x02;
			}
			else
			{
				flipFlop = 0x01;
			}
			
			System.out.println("----------------------------------------\n");
		}
		*/
		
		//close client
		client.close();
		
		/*
		//generate and send bad datagram
		client.generateDatagram("gArBaGe.trash","trascii", (byte)0x05);
		client.sendAndEcho();
		client.receiveAndEcho();
		*/
	}
}
