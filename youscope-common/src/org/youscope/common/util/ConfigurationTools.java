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
package org.youscope.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageProducerConfiguration;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.CompositeJobConfiguration;

/**
 * Provides methods to check a configuration for validity.
 * @author Moritz Lang
 * 
 */
public class ConfigurationTools
{
	/**
	 * Creates a deep copy of the configuration by serializing and unserializing it.
	 * @param configuration The configuration which should be copied.
	 * @param configurationClass The class of the configuration.
	 * @return A deep copy of the configuration.
	 * @throws ConfigurationException Thrown if making the deep copy failed. Typically indicates that the configuration does not properly implement {@link Serializable}.
	 */
	public static <T extends Configuration> T deepCopy(T configuration, Class<T> configurationClass) throws ConfigurationException
	{
		ByteArrayOutputStream outStream = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		ByteArrayInputStream inStream = null;
		try
    	{ 
			outStream = new ByteArrayOutputStream();
        	out = new ObjectOutputStream(outStream);
            out.writeObject(configuration);
            inStream = new ByteArrayInputStream(outStream.toByteArray());
            in = new ObjectInputStream(inStream);
            configuration.getClass();
            return configurationClass.cast(in.readObject());
    	} 
		catch (IOException | ClassNotFoundException e) {
			throw new ConfigurationException("Could not deep clone configuration. Probably the configuration or one of its sub-configurations does not properly implement the interface Serializable.", e);
		}
		finally
		{
			if(outStream != null)
			{
				try {
					outStream.close();
				} catch (@SuppressWarnings("unused") IOException e) {
					// ignore exception
				}
			}
			if(out != null)
			{
				try {
					out.close();
				} catch (@SuppressWarnings("unused") IOException e) {
					// ignore exception
				}
			}
			if(in != null)
			{
				try {
					in.close();
				} catch (@SuppressWarnings("unused") IOException e) {
					// ignore exception
				}
			}
			if(inStream != null)
			{
				try {
					inStream.close();
				} catch (@SuppressWarnings("unused") IOException e) {
					// ignore exception
				}
			}
		}
	}
	
	/**
	 * Recursively checks all job configurations in the provided array and all sub-jobs of these for colliding image save names.
	 * If two configurations of image producing jobs have the same image save name, the respective name is included in the output array.
	 * If no image save names are colliding, this function returns an empty array.
	 * @param jobConfigurations The configurations to check for image collisions.
	 * @return Array of colliding image save names, or empty array.
	 */
	public static String[] checkImageSaveNameCollision(JobConfiguration[] jobConfigurations)
	{
		Vector<String> collidingNames = new Vector<String>();
		HashSet<String> imageNames = new HashSet<String>();
		for(JobConfiguration job : jobConfigurations)
		{
			for(String imageName : getImageSaveNamesRecursive(job))
			{
				if(imageNames.contains(imageName))
					collidingNames.addElement(imageName);
				else
					imageNames.add(imageName); 
			}
		}

		return collidingNames.toArray(new String[collidingNames.size()]);
	}

	/**
	 * Returns a collection of all image save names defined by the given job and all of its subJobs.
	 * The returned collection may contain a given name more than once, if this image save name is used by more than one job.
	 * @param jobConfiguration The job configuration to detect the image names of.
	 * @return Collection of all image save names. Can be empty.
	 */
	public static Collection<String> getImageSaveNamesRecursive(JobConfiguration jobConfiguration)
	{
		Vector<String> imageNames = new Vector<String>();
		// Add self
		if(jobConfiguration instanceof ImageProducerConfiguration)
		{
			String[] localNames = ((ImageProducerConfiguration)jobConfiguration).getImageSaveNames();
			if(localNames != null)
			{
				for(String localName : localNames)
				{
					if(localName == null || localName.length() <= 0)
						continue;
					imageNames.addElement(localName);
				}
			}
		}

		// Add sub-jobs
		if(jobConfiguration instanceof CompositeJobConfiguration)
		{
			JobConfiguration[] subJobs = ((CompositeJobConfiguration)jobConfiguration).getJobs();
			if(subJobs != null)
			{
				for(JobConfiguration subJob : subJobs)
				{
					if(subJob == null)
						continue;
					imageNames.addAll(getImageSaveNamesRecursive(subJob));
				}
			}
		}
		return imageNames;
	}
}
