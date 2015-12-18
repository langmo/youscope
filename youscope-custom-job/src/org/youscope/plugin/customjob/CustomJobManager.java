/**
 * 
 */
package org.youscope.plugin.customjob;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import org.youscope.addon.ConfigurationManagement;

/**
 * @author Moritz Lang
 *
 */
class CustomJobManager
{
	private static final String CUSTOM_JOBS_FOLDER_NAME = "configuration/custom_jobs";
	private static final String CUSTOM_JOBS_TYPE_IDENTIFIER_PREFIX = "YouScope.CustomJob.";
	private static final String CUSTOM_JOBS_FILE_ENDING = ".csb";
	private static String[] customJobIdentifiers = null;
	
	static String getCustomJobTypeIdentifier(String customJobName)
	{
		return CUSTOM_JOBS_TYPE_IDENTIFIER_PREFIX + customJobName;
	}
	static String getCustomJobName(String typeIdentifier)
	{
		return typeIdentifier.substring(CUSTOM_JOBS_TYPE_IDENTIFIER_PREFIX.length());
	}
	static String getCustomJobFileName(String typeIdentifier)
	{
		return typeIdentifier.substring(CUSTOM_JOBS_TYPE_IDENTIFIER_PREFIX.length())+CUSTOM_JOBS_FILE_ENDING;
	}
	
	static synchronized String[] getCustomJobTypeIdentifiers()
	{
		if(customJobIdentifiers != null)
			return customJobIdentifiers;
		File folder = new File(CUSTOM_JOBS_FOLDER_NAME);
		if(!folder.exists() || !folder.isDirectory())
		{
			return new String[0];
		}
		File[] xmlFiles = folder.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name) {
		        return (name.endsWith(CUSTOM_JOBS_FILE_ENDING));
		    }

		});
		String[] returnVal = new String[xmlFiles.length];
		for(int i=0; i<xmlFiles.length; i++)
		{
			returnVal[i] = xmlFiles[i].getName();
			returnVal[i] = getCustomJobTypeIdentifier(returnVal[i].substring(0, returnVal[i].length()-CUSTOM_JOBS_FILE_ENDING.length()));
		}
		return returnVal;
	}
	static synchronized boolean deleteCustomJob(String typeIdentifier)
	{
		customJobIdentifiers = null; // enforce reloading.
		
		File folder = new File(CUSTOM_JOBS_FOLDER_NAME);
		if(!folder.exists() || !folder.isDirectory())
		{
			return true;
		}
		File file = new File(folder, getCustomJobFileName(typeIdentifier));
		if(!file.exists())
		{
			return true;
		}		
		return file.delete();
	}
	
	static boolean deleteCustomJob(CustomJobConfiguration customJob)
	{
		return deleteCustomJob(customJob.getTypeIdentifier());
	}
	static synchronized boolean saveCustomJob(CustomJobConfiguration customJob) throws CustomJobException
	{
		customJobIdentifiers = null; // enforce reloading.
		
		File folder = new File(CUSTOM_JOBS_FOLDER_NAME);
		if(!folder.exists() || !folder.isDirectory())
		{
			boolean result = folder.mkdirs();
			if(!result)
			{
				throw new CustomJobException("Custom job folder could not be created. Check if YouScope has sufficients rights to create sub-folders in the YouScope directory.");
			}
		}
		
		try {
			ConfigurationManagement.saveConfiguration(new File(folder, getCustomJobFileName(customJob.getTypeIdentifier())).toString(), customJob);
		} catch(IOException e)
		{
			throw new CustomJobException("Could not save custom job file.", e);
		}
		
		return true;
	}
	
	static synchronized CustomJobConfiguration getCustomJob(String typeIdentifier) throws CustomJobException
	{
		File folder = new File(CUSTOM_JOBS_FOLDER_NAME);
		if(!folder.exists() || !folder.isDirectory())
		{
			throw new CustomJobException("Custom job folder does not exist, thus, custom job could not be localized.");
		}
		File xmlFile = new File(folder, getCustomJobFileName(typeIdentifier));
		try
		{
			return (CustomJobConfiguration) ConfigurationManagement.loadConfiguration(xmlFile.toString());
		}
		catch(Throwable e)
		{
			throw new CustomJobException("Could not load custom job.", e);
		}
	}
}
