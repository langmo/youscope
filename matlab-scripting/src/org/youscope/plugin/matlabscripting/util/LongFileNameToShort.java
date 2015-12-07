/**
 * 
 */
package org.youscope.plugin.matlabscripting.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author langmo
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
     * conversion didn't succeed, the original file name is returned.
     * 
     * @param longFileName The long file name to convert.
     * @return The short file name (8.3).
     */
    public static synchronized String convertToShortFileName(String longFileName)
    {
        if (!libLoaded)
        {
        	// find out if x32 or x64
        	String dataModel = System.getProperty("sun.arch.data.model", "32");
        	boolean x64 = dataModel.compareTo("64") == 0;
        	String folder = x64 ? LIBRARY_LOCATION_64 : LIBRARY_LOCATION_32;
        	
            // Copy library from jar achieve to the file system
            InputStream inputStream = null;
            File libraryFile;
            try
            {
            	inputStream = LongFileNameToShort.class.getClassLoader().getResourceAsStream(folder + LIBRARY_BASE_NAME + ".dll");
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
            	System.err.println(e.getMessage());
            	e.printStackTrace();
                return longFileName;
            }
            finally
            {
            	try {
					inputStream.close();
				} catch (@SuppressWarnings("unused") IOException e) {
					// do nothing.
				}
            }

            // Load the library
            // System.loadLibrary(libraryName);
            try
            {
            	System.load(libraryFile.getAbsolutePath());
            }
            catch(UnsatisfiedLinkError e)
            {
            	throw new UnsatisfiedLinkError("Could not load temporary native library file \"" + libraryFile.getAbsolutePath() +"\".\nError message: " + e.getMessage());
            }
            libLoaded = true;
        }
        return (new LongFileNameToShort()).toShortFileName(longFileName);
    }
}
