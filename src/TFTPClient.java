/**
*Class:             TFTPReader.java
*Project:           TFTP Project - Group 4
*Author:            Jason Van Kerkhoven                                             
*Date of Update:    26/09/2016                                              
*Version:           1.0.0                                                      
*                                                                                    
*Purpose:           Read a file, parse into 512 byte sections
*					and return a list of byte arrays
*					(Note currently file must be in the directory
*					1 above src)
* 
* 
*Update Log:    	v1.0.0
*                       - null
*/


//imports
import java.io.*;
import java.util.*;


public class TFTPReader 
{
	//declaring local instance variables
	LinkedList<byte[]> dataChain;
	
	//declaring local class constants
	private static final int MAX_SIZE = 512;
	
	
	//constructor
	public TFTPReader()
	{
		dataChain = new LinkedList<byte[]>();
	}

	
	/* reads file passed to it, returns a linked list of byte[]
	 * splits file into arrays (0B to MAX_Size in length) 
	 * (last array will ALWAYS be less than MAX_SIZE in length)
	 */	
	public void readAndSplit(String file) throws FileNotFoundException, IOException 
	{
		//declaring local variables
		byte[] arr = new byte[MAX_SIZE];
		int b = 0;
		int i = 0;
		
		//clear dataChain
		dataChain.clear();
		
		//load buffer with data
		BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));
	
		//run until buffer is empty
		while( (b=input.read()) != -1)
		{
			arr[i] = (byte)b;
			i++;
			
			//check if current arr is full
			if (i >= MAX_SIZE)
			{
				//reset 512B max arr construction, add full
				//arr data to linked list to be returned
				i=0;
				dataChain.add(arr.clone());
			}
		}
		
		//more than 0 bytes left in arr
		if (i > 0)
		{
			byte[] tempArr = new byte[i];
			for(int a=0; a<i; a++)
			{
				tempArr[a] = arr[a];
			}
			dataChain.add(tempArr);
		}
		
		//close buffer
		input.close();
	}
	
	
	//passes the next [512max] array of bytes
	public byte[] pop()
	{
		if (dataChain.peek() != null)
		{
			return dataChain.pop();
		}
		else
		{
			return null;
		}
	}
	
	
	//looks at next array of bytes
	public byte[] peek()
	{
		return dataChain.peek();
	}
	
	
	//returns true if empty
	public boolean isEmpty()
	{
		return dataChain.isEmpty();
	}
	
	
	//returns how many byte arrays have been read
	public int arrNum()
	{
		return dataChain.size();
	}
	
	
	//test function please ignore
	public static void main(String args[])
	{
		TFTPReader reader = new TFTPReader();
		int n = 0 ;
		byte[] arr ;
		
		try
		{
			reader.readAndSplit("DatagramsOutForHarambe.txt");
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Packets: " + reader.arrNum());
		while ( !(reader.isEmpty()) )
		{
			n++;
			arr = reader.pop();
			System.out.println("Packet #" + n + ": " + arr.length + " Bytes");
			for(int i=0; i<arr.length; i++)
			{
				System.out.println("  " + (char)arr[i] + " | " + arr[i]);
			}
		}
	}
	
}
