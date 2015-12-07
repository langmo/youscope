/**
 * 
 */
package org.youscope.plugin.customjob;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;

import org.youscope.addon.ConfigurationManagement;

/**
 * @author Moritz Lang
 *
 */
class CustomJobManager
{
	private static final String CUSTOM_JOBS_FOLDER_NAME = "configuration/custom_jobs";
	
	private static CustomJobConfiguration[] customJobs = null;
	
	static CustomJobConfiguration[] loadCustomJobs() throws CustomJobException
	{
		if(customJobs != null)
			return customJobs;
		File folder = new File(CUSTOM_JOBS_FOLDER_NAME);
		if(!folder.exists() || !folder.isDirectory())
		{
			return new CustomJobConfiguration[0];
		}
		File[] xmlFiles = folder.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name) {
		        return (name.endsWith(".csb"));
		    }

		});
		ArrayList<CustomJobConfiguration> customJobList = new ArrayList<CustomJobConfiguration>();
		for(File xmlFile :xmlFiles)
		{
			CustomJobConfiguration customJob = getCustomJob(xmlFile);
			if(customJob != null)
				customJobList.add(customJob);
		}
		customJobs = customJobList.toArray(new CustomJobConfiguration[customJobList.size()]); 
		return customJobs;
	}
	static boolean deleteCustomJob(CustomJobConfiguration customJob) throws CustomJobException
	{
		if(customJob == null || customJob.getCustomJobName() == null)
			return false;
		customJobs = null; // enforce reloading.
		
		File folder = new File(CUSTOM_JOBS_FOLDER_NAME);
		if(!folder.exists() || !folder.isDirectory())
		{
			throw new CustomJobException("Custom job folder could not be found.");
		}
		File file = new File(folder, customJob.getCustomJobName()+".csb");
		if(!file.exists())
		{
			throw new CustomJobException("Custom job definition \"" + customJob.getCustomJobName() + ".csb\" does not exist on file system and could thus not be deleted.");
		}		
		return file.delete();
	}
	static boolean saveCustomJob(CustomJobConfiguration customJob) throws CustomJobException
	{
		customJobs = null; // enforce reloading.
		
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
			ConfigurationManagement.saveConfiguration(new File(folder, customJob.getCustomJobName() + ".csb").toString(), customJob);
		} catch(IOException e)
		{
			throw new CustomJobException("Could not save custom job file.", e);
		}
		
		return true;
	}
	
	private static CustomJobConfiguration getCustomJob(File xmlFile) throws CustomJobException
	{
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
