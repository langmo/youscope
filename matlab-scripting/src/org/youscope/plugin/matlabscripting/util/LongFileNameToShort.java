/**
 * 
 */
package org.youscope.plugin.matlabscripting.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Helper class to convert normal filenames to their (old style) 8.3 representation.
 * Matlab, at least in some versions, has problems with Java classpath elements containing spaces. This is a workaround therefore...
 * @author Moritz Lang
 */
public class LongFileNameToShort
{
    /**
     *  Native method to get 8.3 file name of a normal file name.
     * @param longFileName The normal file name.
     * @return Its 8.3 representation.
     */
    private native String toShortFileName(String longFileName);

    /**
     *  Stores if library is already loaded.
     */
    private static boolean libLoaded = false;

    private static final String LIBRARY_LOCATION_32 = "lib/win_x32/";
    
    private static final String LIBRARY_LOCATION_64 = "lib/win_x64/";
    
    /**
     *  Base name of native library (the DLL).
     */
    private static final String LIBRARY_BASE_NAME = "EightDotThreeFileNames";

    /**
     * Converts a Windows long file name to its short file name (8.3 DOS-like file names). If
     * conversion didn't succeed, an exception is thrown.
     * 
     * @param longFileName The long file name to convert.
     * @return The short file name (8.3).
     * @throws Exception Thrown if conversion did not work
     */
    public static synchronized String convertToShortFileName(String longFileName) throws Exception
    {
        if (!libLoaded)
        {
        	// find out if x32 or x64
        	String dataModel = System.getProperty("sun.arch.data.model", "32");
        	boolean x64 = dataModel.compareTo("64") == 0;
        	String folder = x64 ? LIBRARY_LOCATION_64 : LIBRARY_LOCATION_32;
        	
            // Copy library from jar achieve to the file system
            InputStream inputStream = LongFileNameToShort.class.getClassLoader().getResourceAsStream(folder + LIBRARY_BASE_NAME + ".dll");
            if(inputStream == null)
            {
            	throw new Exception("Could not find temporary native library file to convert file names to old-style 8.3 file names. Expected location was \""+folder + LIBRARY_BASE_NAME + ".dll"+"\".");
            }
            File libraryFile;
            try
            {
                libraryFile = File.createTempFile(LIBRARY_BASE_NAME, ".dll");
                libraryFile.deleteOnExit();
                FileOutputStream fileOutputStream = new FileOutputStream(libraryFile);
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) > 0)
                {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }
                fileOutputStream.close();
            } 
            catch (Exception e)
            {
            	throw new Exception("Could not extract temporary native library file to convert file names to old-style 8.3 file names.", e);
            }
            finally
            {
            	if(inputStream != null)
            	{
	            	try {
						inputStream.close();
					} catch (@SuppressWarnings("unused") IOException e) {
						// do nothing.
					}
            	}
            }

            try
            {
            	System.load(libraryFile.getAbsolutePath());
            }
            catch(Throwable e)
            {
            	throw new Exception("Could not load extracted temporary native library file \"" + libraryFile.getAbsolutePath() +"\"  to convert file names to old-style 8.3 file names.", e);
            }
            libLoaded = true;
        }
        return (new LongFileNameToShort()).toShortFileName(longFileName);
    }
}
