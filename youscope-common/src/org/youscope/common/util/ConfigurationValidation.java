/**
 * 
 */
package org.youscope.common.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import org.youscope.common.image.ImageProducerConfiguration;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.JobContainerConfiguration;

/**
 * Provides methods to check a configuration for validity.
 * @author Moritz Lang
 * 
 */
public class ConfigurationValidation
{
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
		if(jobConfiguration instanceof JobContainerConfiguration)
		{
			JobConfiguration[] subJobs = ((JobContainerConfiguration)jobConfiguration).getJobs();
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
