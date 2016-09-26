/**
*Class:             TFTPClient.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    25/09/2016                                              
*Version:           1.1.0                                                      
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
*Update Log:        v1.1.0
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


public class TFTPClient 
{
	//declaring local instance variables
	private DatagramPacket sentPacket;
	private DatagramPacket recievedPacket;
	private DatagramSocket generalSocket;
	private boolean verbose;
	private int outPort;
	
	//declaring local class constants
	private static final int IN_PORT_ERRORSIM = 23;
	private static final int IN_PORT_SERVER = 69;
	private static final int MAX_SIZE = 100;
	
	
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
	public void setVerbose(boolean f)
	{
		verbose = f;
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
	public void generateDatagram(String fileName, String mode, byte RWval)
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
			
		
		//generate and return datagram packet
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
	
	
	//send and echo the datagram
	public void sendAndEcho()
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
			System.out.println("        Cntn:  " + (new String(data,0,packetSize)));
			System.out.printf("%s", "        Cntn:  ");
			
			for(int i = 0; i < packetSize; i++)
			{
				System.out.printf("0x%02X", data[i]);
				System.out.printf("%-2c", ' ');
			}
			System.out.println("");
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
		
		//send directly to server and non-verbose
		client.testMode(false);
		client.verboseMode(false);
		
		for(int i=0; i<10; i++)
		{
			//generate datagram
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
