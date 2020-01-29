/**
 * 
 */
package org.youscope.plugin.matlabscripting;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLClassLoader;
import java.util.HashSet;

import org.youscope.plugin.matlabscripting.util.LongFileNameToShort;

/**
 * @author Moritz Lang
 */
class ScriptingTools
{
	private static String startupScript = null;
	static synchronized String createStartupScript(String bindValue) throws MatlabConnectionException
	{
		if(startupScript != null)
			return startupScript;
		
		File initFile;
		try 
		{
			initFile = File.createTempFile("mscriptInit", ".m");
		} 
		catch (IOException e1) 
		{
			throw new MatlabConnectionException("Could not create temporary matlab initialization script.", e1);
		}
        initFile.deleteOnExit();
        try(PrintStream printStream = new PrintStream(initFile);)
        {
            // Write header
            printStream.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            printStream.println("% Initialization file for using Matlab as a ScriptEngine in Java.                                        %");
            printStream.println("%                                                                                                        %");
            printStream.println("% This file should be deleted automatically, as soon as the respective Java application stops executing. %");
            printStream.println("%                                                                                                        %");
            printStream.println("% Created by Moritz Lang                                                                                 %");
            printStream.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
            printStream.println();
            printStream.println("% Disable warnings");
            printStream.println("warning('off', 'MATLAB:javaclasspath:jarAlreadySpecified');");
            printStream.println();
            
            // Add all java classpath which were set in java.class.path system property.
            HashSet<String> classpathURLs = new HashSet<String>();
        	String javaclasspath = System.getProperties().getProperty("java.class.path", null);
            String[] javaPaths = javaclasspath.split(";");
            for(int i=0; i<javaPaths.length; i++)
            {
            	if(javaPaths[i].length() <= 0)
            		continue;
				try
				{
					classpathURLs.add(ScriptingTools.toMatlabPath(new File(javaPaths[i]).toURI().toURL().getPath()));
				}
				catch (@SuppressWarnings("unused") MalformedURLException e)
				{
					// do nothing, just don't add it.
				}
            }
            printStream.println("% Add java classpaths");    
            for (String path : classpathURLs)
            {
            	printStream.println("javaaddpath('" + path + "');");
            }
            printStream.println("javaaddpath('" + ScriptingTools.toMatlabPath(RemoteMatlabProxyFactory.class.getProtectionDomain().getCodeSource().getLocation().getPath()) + "');");
            
            // Change to Java application path
            printStream.println();
            printStream.println("% Go to Java application path");
            printStream.println("cd('" + new File("").getAbsolutePath() + "');");
    		
    		// Connect to application
    		printStream.println();
            printStream.println("% Establish connection to Java application");
    		printStream.println(MatlabConnector.class.getName() + ".connectFromMatlab('" + bindValue + "');");
    		printStream.println();
            
    		return initFile.getAbsolutePath();
        } 
        catch (Exception e)
        {
            throw new MatlabConnectionException("Could not create temporary matlab initialization script.", e);
        }
	}
	
	
	
    static String toMatlabPath(String path) throws MatlabConnectionException
    {
        // Get Operating system
        String osName = System.getProperty("os.name");

        path = path.replace("%20", " ");

        // Workaround for error in debugging in Windows using Eclipse (and probably at other
        // occasions, too).
        if (osName.indexOf("Windows") >= 0)
        {
            if (path.charAt(0) == '/')
                path = path.substring(1);

            if (path.indexOf(" ") >= 0)
            {
                // Workaround for "Space-Error"...
                try {
					path = LongFileNameToShort.convertToShortFileName(path);
				} catch (Exception e) {
					throw new MatlabConnectionException("Could not establish Matlab connection since path to YouScope contains spaces, and workaround for the Matlab bug to allow RMI communication when path contains spaces failed.", e);
				}
                if (path.indexOf(" ") >= 0)
                {
                	throw new MatlabConnectionException("YouScope is located in a path containing spaces. The Matlab implementation of the Java VM contains a bug preventing the correct handling of paths with spaces. YouScope contains a workaround converting filenames with spaces to their 8.3 equivalence, but 8.3 filenames seems to be deactivated on your system. Either reactivate them or copy YouScope into a folder not containing spaces.");
                    /*// Did not work, do manually...
                    boolean isJar;
                    if (path.charAt(path.length() - 1) == '/'
                            || path.charAt(path.length() - 1) == '\\')
                        isJar = false;
                    else
                        isJar = true;

                    String[] parts = path.split("[/\\\\]");
                    path = "";
                    for (int i = 0; i < parts.length; i++)
                    {
                        if (parts[i].indexOf(" ") >= 0)
                        {
                            parts[i] = parts[i].replace(" ", "");
                            parts[i] = parts[i].replace(".", "");
                            parts[i] = parts[i].replace(",", "_");
                            parts[i] = parts[i].replace("[", "_");
                            parts[i] = parts[i].replace("]", "_");

                            if (parts[i].length() > 8)
                                parts[i] = parts[i].substring(0, 6) + "~1";
                        }
                        path += parts[i] + "\\";
                    }
                    if (isJar)
                    {
                        path = path.substring(0, path.length() - 1);
                    }*/
                }
            }
        }
        return path;
    }
}
