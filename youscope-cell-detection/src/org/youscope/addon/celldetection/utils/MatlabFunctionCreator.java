/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
package org.youscope.addon.celldetection.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.youscope.common.resource.ResourceException;

/**
 * Function converting a Matlab script file stored as a resource inside a jar file into a function, and stores this function
 * as a temporary file such that it can be called normally. 
 * @author Moritz Lang
 *
 */
public class MatlabFunctionCreator 
{
	/**
	 * Base name of all temporary files created.
	 */
	public static final String FILE_BASE_NAME = "youscope";
	/**
	 * File extension of all temporary files created.
	 */
	public static final String FILE_EXTENSION = ".m";
	private final String resourcePath;
	private final String[] arguments;
	private String functionName = null;
	private String path = null;
	private File matlabFile = null;
	/**
	 * Constructor.
	 * @param resourcePath The resource path to the Matlab script in a jar file.
	 * @param arguments The arguments the Matlab function takes. Typically, the same as the names of the variables provided to Matlab.
	 */
	public MatlabFunctionCreator(String resourcePath, String... arguments)
	{
		this.resourcePath = resourcePath;
		this.arguments = arguments;
	}

	/**
	 * Returns the name of the function created. Same as the file name, without extension.
	 * @return Matlab function name.
	 */
	public String getFunctionName()
	{
		return functionName;
	}
	/**
	 * Returns the path to the temporary directory where the created Matlab function is stored.
	 * @return Path to temporary directory.
	 */
	public String getFunctionPath()
	{
		return path;
	}
	/**
	 * Returns a string with which the Matlab function can be called. Variables with the same name as the function's argument
	 * must have already be passed to Matlab. Furthermore, Matlab has to be at the temporary directory.
	 * @return Function invocation string.
	 */
	public String getFunctionCallString()
	{
		String functionCall = getFunctionName()+"(";
		for(int i=0; i<arguments.length; i++)
		{
			if(i>0)
				functionCall+=", ";
			functionCall+=arguments[i];
		}
		functionCall+=");";
		return functionCall;
	}
	/**
	 * Returns the command for Matlab to switch its current directory to the temporary directory.
	 * @return Directory change string.
	 */
	public String getCDString()
	{
		return "cd('"+getFunctionPath().replace('\\', '/')+"');";
	}
	/**
	 * Returns a string with which the Matlab function can be called. Variables with the same name as the function's argument
	 * must have already be passed to Matlab. The directory is automatically set to the temporary directory.
	 * Combination of {@link #getCDString()} and {@link #getFunctionCallString()}.
	 * @return Full Matlab function invocation string.
	 */
	public String getFullInvokeString()
	{
		return getCDString()+"\n"+getFunctionCallString();
	}
	/**
	 * Creates the Matlab temporary function file.
	 * @throws ResourceException Thrown if file could not be created.
	 */
	public void initialize() throws ResourceException
	{
		InputStream inputStream = null;
	    try
	    {
	    	inputStream = MatlabFunctionCreator.class.getClassLoader().getResourceAsStream(resourcePath);
	        matlabFile = File.createTempFile(FILE_BASE_NAME, FILE_EXTENSION);
	        matlabFile.deleteOnExit();
	        FileOutputStream fileOutputStream = new FileOutputStream(matlabFile);
	        
	        // get info about created file
	        functionName = matlabFile.getName();
	        // delete .m ending
	        functionName = functionName.substring(0, functionName.length()-FILE_EXTENSION.length());
	        path = matlabFile.getParentFile().getAbsolutePath();
	        
	        String header = "function "+functionName+"(";
	        for(int i=0; i<arguments.length; i++)
	        {
	        	if(i>0)
	        		header+=", ";
	        	header +=arguments[i];
	        }
	        header+=")\n\r";
	        byte[] headerBytes = header.getBytes(Charset.defaultCharset());
	        fileOutputStream.write(headerBytes, 0, headerBytes.length);
	        
	        byte[] buffer = new byte[8192];
	        int bytesRead;
	        while ((bytesRead = inputStream.read(buffer)) > 0)
	        {
	            fileOutputStream.write(buffer, 0, bytesRead);
	        }
	        String footer = "\n\rend";
	        byte[] footerBytes = footer.getBytes(Charset.defaultCharset());
	        fileOutputStream.write(footerBytes, 0, footerBytes.length);
	        
	        fileOutputStream.close();
	        
	    } 
	    catch (Exception e)
	    {
	    	throw new ResourceException("Could not create temporary matlab file.", e);
	    }
	    finally
	    {
	    	try {
				inputStream.close();
			} catch (@SuppressWarnings("unused") IOException e) {
				// do nothing.
			}
	    }
	}
	/**
	 * Deletes the temporary Matlab string, if necessary.
	 * @throws ResourceException Thrown if file could not be deleted.
	 */
	public void uninitialize() throws ResourceException
	{
		if(matlabFile != null)
		{
			try
			{
				matlabFile.delete();
			}
			catch(@SuppressWarnings("unused") Throwable error)
			{
				// do nothing.
			}
		}
	}
}
