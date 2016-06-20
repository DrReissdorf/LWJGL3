package util;
/* 
 * Cologne University of Applied Sciences
 * Institute for Media and Imaging Technologies - Computer Graphics Group
 *
 * Copyright (c) 2012 Cologne University of Applied Sciences. All rights reserved.
 *
 * This source code is property of the Cologne University of Applied Sciences. Any redistribution
 * and use in source and binary forms, with or without modification, requires explicit permission. 
 */


import java.io.BufferedReader;
import java.io.FileReader;



public class FileIO 
{
	public static String readTXT( String filename )
	{
		String text = "";
		String line;
		
		try
		{
			BufferedReader reader = new BufferedReader( new FileReader(filename) );
			while( (line = reader.readLine()) != null )
			{
				text += line + "\n";
			}
			reader.close();
		}
		catch ( Exception e )
		{
			System.err.println( "*ERROR* IOUtils: Unable to read file: " + filename );
			return "";
		}
		
		return text;
	}
	
	
	public static String pathOf( String filename )
	{
		return "resources/" + filename;
	}
}
