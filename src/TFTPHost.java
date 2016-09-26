/**
*Class:             TFTPHost.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    17/09/2016                                              
*Version:           1.0.0                                                      
*                                                                                    
*Purpose:           Receives packet from Client, sends packet to Server and waits
					for Server response. Sends Server response back to Client. Repeats
					this process indefinitely. Designed to allow for the simulation of errors and lost packets in future.
* 
* 
*Update Log:        v1.0.0
*                       - null
*/


//imports
import java.io.*;
import java.net.*;


public class TFTPHost 
{
	
	//declaring local instance variables
	private DatagramPacket sentPacket;
	private DatagramPacket receivedPacket;
	private DatagramSocket inSocket;
	private DatagramSocket outSocket;
	private DatagramSocket generalSocket;
	private int clientPort;
	private boolean verbose;
		
	//declaring local class constants
	private static final int CLIENT_PORT = 23;
	private static final int SERVER_RECEIVE_PORT = 69;
	private static final int MAX_SIZE = 100;

	
	//generic constructor
	public TFTPHost()
	{
		//construct socket for incoming client communication, bind to IN_PORT
		try
		{
			inSocket = new DatagramSocket(CLIENT_PORT);
		}
		//enter if socket creation results in failure
		catch (SocketException se)
		{
			se.printStackTrace();
			System.exit(1);
		}
		//construct socket for general server communication
		try
		{
			generalSocket = new DatagramSocket();
		}
		catch(SocketException se)
		{
			se.printStackTrace();
			System.exit(1);
		}
		//initialize echo --> off
		verbose = false;
	}
	
	
	//basic accessors and mutators
	public DatagramSocket getInSocket()
	{
		return inSocket;
	}
	public DatagramSocket getOutSocket()
	{
		return outSocket;
	}
	public DatagramSocket getGeneralSocket()
	{
		return generalSocket;
	}
	public void setClientPort(int n)
	{
		clientPort = n;
	}
	public int getClientPort()
	{
		return clientPort;
	}
	public DatagramPacket getReceivedPacket()
	{
		return receivedPacket;
	}
	public void setOutSocket(DatagramSocket soc)
	{
		outSocket = soc;
	}
	public void setVerbose(boolean f)
	{
		verbose = f;
	}
	
	
	
	//receive packet on inPort
	public void receiveAndEcho(DatagramSocket inputSocket)
	{
		//construct an empty datagram packet for receiving purposes
		byte[] arrayholder = new byte[MAX_SIZE];
		receivedPacket = new DatagramPacket(arrayholder, arrayholder.length);
		
		//wait for incoming data
		System.out.println("Host:   Waiting for data...");
		try
		{
			inputSocket.receive(receivedPacket);
		}
		catch (IOException e)
		{
			System.out.print("Incoming socket timed out\n" + e);
			e.printStackTrace();
			System.exit(1);
		}
		
		//deconstruct packet and print contents
		byte[] data = receivedPacket.getData();
		int packetSize = receivedPacket.getLength();
		System.out.println("Host:   Packet received");
		System.out.println("        Source: " + receivedPacket.getAddress());
		System.out.println("        Port:   " + receivedPacket.getPort());
		System.out.println("        Bytes:  " + packetSize);
		System.out.println("        Cntn:  " + (new String(data,0,packetSize)));
		System.out.printf("%s", "        Cntn:  ");
		for(int i = 0; i < packetSize; i++)
		{
			System.out.printf("0x%02X", data[i]);
			System.out.printf("%-2c", ' ');
		}
	}
	
	
	//send packet to server and wait for server response
	/**
	 * 
	 */
	public void sendAndEcho(int outPort, DatagramSocket socket)
	{
		//prep packet to send
		System.out.println("\nHost:   Sending packet...");
		sentPacket = receivedPacket;
		sentPacket.setPort(outPort );
		
		//print contents
		byte[] data = sentPacket.getData();
		int packetSize = sentPacket.getLength();
		System.out.println("        Source: " + sentPacket.getAddress());
		System.out.println("        Port:   " + sentPacket.getPort());
		System.out.println("        Bytes:  " + packetSize);
		System.out.println("        Cntn:  " + (new String(data,0,packetSize)));
		System.out.printf("%s", "        Cntn:  ");
		for(int i = 0; i < packetSize; i++)
		{
			System.out.printf("0x%02X", data[i]);
			System.out.printf("%-2c", ' ');
		}
		
		//send packet
		try
		{
			socket.send(sentPacket);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("\nHost:   Packet successfully sent");
		
	}
	
	
	public static void main(String[] args) 
	{
		//declaring local variables
		TFTPHost host = new TFTPHost();
		
		while(true)
		{
			//wait for client's packet (save clients port)
			host.receiveAndEcho(host.getInSocket());
			host.setClientPort(host.getReceivedPacket().getPort());
			//System.out.println("\n>> " + host.getClientPort());
			
			//send packet to server and wait for response
			host.sendAndEcho(SERVER_RECEIVE_PORT, host.getGeneralSocket());
			host.receiveAndEcho(host.getGeneralSocket());
			
			//prep socket to use for sending datagram packet
			//create temp socket, random port
			try
			{
				host.setOutSocket(new DatagramSocket());
			}
			catch(SocketException se)
			{
				se.printStackTrace();
				System.exit(1);
			}
			
			//send packet to client
			host.sendAndEcho(host.getClientPort(), host.getOutSocket());
			System.out.println("----------------------------------------\n");
		}
	}

}
