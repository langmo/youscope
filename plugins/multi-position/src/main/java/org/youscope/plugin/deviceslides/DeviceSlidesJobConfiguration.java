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
package org.youscope.plugin.deviceslides;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.CompositeJobConfiguration;
import org.youscope.common.microscope.DeviceSetting;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author langmo
 */
@XStreamAlias("multi-position-job")
public class DeviceSlidesJobConfiguration implements CompositeJobConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long						serialVersionUID		= 8198191383459728511L;

	/**
	 * The jobs which should be run at every position.
	 */
	@XStreamAlias("jobs")
	private JobConfiguration[]	jobs					= new JobConfiguration[0];
	
	/**
	 * The list of device settings which should be applied to the positions. The number of values
	 * for each device setting must be the same.
	 */
	@XStreamAlias("settings")
	private DeviceSetting[][]		multiPosDeviceSettings	= new DeviceSetting[0][];

	/**
	 * Sets the list of device settings which should be applied to the positions. The number of
	 * values for each device setting must be the same.
	 * 
	 * @param multiPosDeviceSettings
	 */
	public void setMultiPosDeviceSettings(DeviceSetting[][] multiPosDeviceSettings)
	{
		this.multiPosDeviceSettings = multiPosDeviceSettings;
	}

	/**
	 * Returns the multi-position device settings.
	 * 
	 * @return The settings.
	 */
	public DeviceSetting[][] getMultiPosDeviceSettings()
	{
		return multiPosDeviceSettings;
	}

	@Override
	public String getDescription()
	{
		String description;
		if(multiPosDeviceSettings.length > 0)
		{
			description = "<p>" + "for i = 1 : " + Integer.toString(multiPosDeviceSettings.length) + "</p><ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
			for(int i = 0; i < multiPosDeviceSettings[0].length; i++)
			{
				description += "<li>" + multiPosDeviceSettings[0][i].getDevice() + "." + multiPosDeviceSettings[0][i].getProperty();
				if(multiPosDeviceSettings[0][i].isAbsoluteValue())
					description += " = ";
				else
					description += " += ";
				description += "settings[" + Integer.toString(i) + "][i]</li>";
			}
			for(JobConfiguration job : jobs)
			{
				description += "<li>" + job.getDescription() + "</li>";
			}
			description += "</ul><p>end</p>";
		}
		else
			description = "<p>Empty Multi-Position Job</p>";
		return description;
	}

	@Override
	public JobConfiguration[] getJobs()
	{
		return jobs;
	}

	@Override
	public void setJobs(JobConfiguration[] jobs)
	{
		if(jobs != null)
			this.jobs = jobs;
		else
			this.jobs = new JobConfiguration[0];
	}

	@Override
	public void addJob(JobConfiguration job)
	{
		JobConfiguration[] newJobs = new JobConfiguration[jobs.length +1];
		System.arraycopy(jobs, 0, newJobs, 0, jobs.length);
		newJobs[jobs.length] = job;
		jobs = newJobs;
	}

	@Override
	public void clearJobs()
	{
		jobs = new JobConfiguration[0];
	}

	@Override
	public void removeJobAt(int index)
	{
		JobConfiguration[] newJobs = new JobConfiguration[jobs.length - 1];
		for(int i=0; i < newJobs.length; i++)
		{
			newJobs[i] = jobs[i >= index? i+1 : i];
		}
		jobs = newJobs;
	}

	@Override
	public void addJob(JobConfiguration job, int index)
	{
		JobConfiguration[] newJobs = new JobConfiguration[jobs.length + 1];
		for(int i=0; i < newJobs.length; i++)
		{
			if(i < index)
				newJobs[i] = jobs[i];
			else if(i == index)
				newJobs[i] = job;
			else
				newJobs[i] = jobs[i-1];
		}
		jobs = newJobs;
	}

	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.MultiPositionJob";

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		for(JobConfiguration childJob : jobs)
		{
			childJob.checkConfiguration();
		}
	}
}
