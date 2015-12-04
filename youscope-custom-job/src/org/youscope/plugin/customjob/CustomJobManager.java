/**
 * 
 */
package org.youscope.plugin.customjob;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.ServiceLoader;

import org.youscope.addon.XMLConfigurationProvider;
import org.youscope.common.Microplate;
import org.youscope.common.Well;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.configuration.ImageFolderStructure;
import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.configuration.Period;
import org.youscope.common.configuration.RegularPeriod;
import org.youscope.common.configuration.TaskConfiguration;
import org.youscope.common.configuration.VaryingPeriodDTO;
import org.youscope.common.measurement.MeasurementSaveSettings;
import org.youscope.common.microscope.DeviceSettingDTO;
import org.youscope.common.tools.ConfigurationManagement;

import com.thoughtworks.xstream.XStream;

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
		
		XStream xstream = getSerializerInstance();
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(new File(folder, customJob.getCustomJobName() + ".csb"));
			OutputStreamWriter writer = new OutputStreamWriter(fos, "UTF-8");
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
			xstream.toXML(customJob, writer);
			writer.close();
		}
		catch(FileNotFoundException e)
		{
			throw new CustomJobException("Could not create custom job file.", e);
		}
		catch(IOException e)
		{
			throw new CustomJobException("Could not save custom job file.", e);
		}
		finally
		{
			if(fos != null)
			{
				try
				{
					fos.close();
				}
				catch(IOException e)
				{
					throw new CustomJobException("Could not close custom job file.", e);
				}
			}
		}
		return true;
	}
	
	private static XStream getSerializerInstance() throws CustomJobException
	{
		XStream xstream = new XStream();
		xstream.aliasSystemAttribute("type", "class");

		// First, process the annotations of the classes in this package.
		xstream.processAnnotations(new Class<?>[] {MeasurementSaveSettings.class, Well.class, Microplate.class, FocusConfiguration.class, FocusConfiguration.class, ImageFolderStructure.class, JobConfiguration.class, MeasurementConfiguration.class, Period.class, RegularPeriod.class, TaskConfiguration.class, VaryingPeriodDTO.class, DeviceSettingDTO.class});

		// Now, process all classes provided by the service providers
		ServiceLoader<XMLConfigurationProvider> xmlConfigurationProviders = ServiceLoader.load(XMLConfigurationProvider.class, ConfigurationManagement.class.getClassLoader());

		for(XMLConfigurationProvider provider : xmlConfigurationProviders)
		{
			try
			{
				xstream.processAnnotations(provider.getConfigurationClasses().toArray(new Class<?>[0]));
			}
			catch(Throwable e)
			{
				throw new CustomJobException("Cannot process addon provider " + provider.getClass().getName() + ". Repair or delete respective plug-in.", e);
			}
		}

		return xstream;
	}
	
	private static CustomJobConfiguration getCustomJob(File xmlFile) throws CustomJobException
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(xmlFile);
			XStream xstream = getSerializerInstance();
			CustomJobConfiguration job = (CustomJobConfiguration)xstream.fromXML(fis);
			// Change name to file name
			String name = xmlFile.getName(); 
			int idx = name.lastIndexOf(".csb");
			if(idx > 0)
			{
				name = name.substring(0, idx);
			}
			job.setCustomJobName(name);
			return job;
		}
		catch(FileNotFoundException e)
		{
			throw new CustomJobException("Could not locate custom job.", e);
		}
		finally
		{
			if(fis != null)
			{
				try
				{
					fis.close();
				}
				catch(IOException e)
				{
					throw new CustomJobException("Could not close custom job file.", e);
				}
			}
		}
	}
}
