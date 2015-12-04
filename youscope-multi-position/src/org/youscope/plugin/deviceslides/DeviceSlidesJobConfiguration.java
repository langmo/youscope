/**
 * 
 */
package org.youscope.plugin.deviceslides;

import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.configuration.JobContainerConfiguration;
import org.youscope.common.microscope.DeviceSettingDTO;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author langmo
 */
@XStreamAlias("multi-position-job")
public class DeviceSlidesJobConfiguration extends JobConfiguration implements JobContainerConfiguration
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
	private DeviceSettingDTO[][]		multiPosDeviceSettings	= new DeviceSettingDTO[0][];

	/**
	 * Sets the list of device settings which should be applied to the positions. The number of
	 * values for each device setting must be the same.
	 * 
	 * @param multiPosDeviceSettings
	 */
	public void setMultiPosDeviceSettings(DeviceSettingDTO[][] multiPosDeviceSettings)
	{
		this.multiPosDeviceSettings = multiPosDeviceSettings;
	}

	/**
	 * Returns the multi-position device settings.
	 * 
	 * @return The settings.
	 */
	public DeviceSettingDTO[][] getMultiPosDeviceSettings()
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
	public Object clone() throws CloneNotSupportedException
	{
		DeviceSlidesJobConfiguration job = (DeviceSlidesJobConfiguration)super.clone();
		job.jobs = new JobConfiguration[jobs.length];
		for(int i = 0; i < jobs.length; i++)
		{
			job.jobs[i] = (JobConfiguration)jobs[i].clone();
		}

		job.multiPosDeviceSettings = new DeviceSettingDTO[multiPosDeviceSettings.length][];
		for(int i = 0; i < multiPosDeviceSettings.length; i++)
		{
			job.multiPosDeviceSettings[i] = new DeviceSettingDTO[multiPosDeviceSettings[i].length];
			for(int j=0; j < multiPosDeviceSettings[i].length; j++)
			{
				job.multiPosDeviceSettings[i][j] = multiPosDeviceSettings[i][j].clone();
			}
			
		}

		return job;
	}

	@Override
	public JobConfiguration[] getJobs()
	{
		return jobs;
	}

	@Override
	public void setJobs(JobConfiguration[] jobs)
	{
		this.jobs = jobs;
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
	public static final String	TYPE_IDENTIFIER	= "CSB::MultiPositionJob";

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}
}
