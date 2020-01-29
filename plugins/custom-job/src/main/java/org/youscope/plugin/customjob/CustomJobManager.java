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
		customJobIdentifiers = new String[xmlFiles.length];
		for(int i=0; i<xmlFiles.length; i++)
		{
			customJobIdentifiers[i] = xmlFiles[i].getName(); 
			customJobIdentifiers[i] = getCustomJobTypeIdentifier(customJobIdentifiers[i].substring(0, customJobIdentifiers[i].length()-CUSTOM_JOBS_FILE_ENDING.length()));
		}
		return customJobIdentifiers;
	}
	static synchronized boolean deleteCustomJob(String typeIdentifier)
	{
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
		boolean success =  file.delete();
		customJobIdentifiers = null; // enforce reloading.
		return success;
	}
	
	static boolean deleteCustomJob(CustomJobConfiguration customJob)
	{
		return deleteCustomJob(customJob.getTypeIdentifier());
	}
	static synchronized boolean saveCustomJob(CustomJobConfiguration customJob) throws CustomJobException
	{
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
		customJobIdentifiers = null; // enforce reloading.
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
