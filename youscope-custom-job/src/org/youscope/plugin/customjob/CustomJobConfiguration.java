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

import java.util.ArrayList;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.CompositeJobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A job consisting of other jobs, which can be defined by the user.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("custom-job")
public class CustomJobConfiguration implements CompositeJobConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long						serialVersionUID	= 4794454448367096630L;

	/**
	 * The jobs which should be run when the composite job starts.
	 */
	@XStreamAlias("jobs")
	private ArrayList<JobConfiguration>	jobs				= new ArrayList<JobConfiguration>();

	@XStreamAlias("custom-job-name")
	private String customJobName = "unnamed";
	
	@Override
	public String getDescription()
	{
		if(jobs == null || jobs.size() == 0)
			return "Empty " + customJobName;
		String description = "<p>"+customJobName+"</p>" +
			"<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
		for(JobConfiguration job : jobs)
		{
			description += "<li>" + job.getDescription() + "</li>";
		}
		description += "</ul><p>end</p>";
		return description;
	}

	@Override
	public JobConfiguration[] getJobs()
	{
		return jobs.toArray(new JobConfiguration[jobs.size()]);
	}

	@Override
	public void setJobs(JobConfiguration[] jobs)
	{
		this.jobs.clear();
		for(JobConfiguration job:jobs)
		{
			this.jobs.add(job);
		}
	}

	@Override
	public void addJob(JobConfiguration job)
	{
		jobs.add(job);
	}

	@Override
	public void clearJobs()
	{
		jobs.clear();
	}

	@Override
	public void removeJobAt(int index)
	{
		jobs.remove(index);
	}

	@Override
	public void addJob(JobConfiguration job, int index)
	{
		jobs.add(index, job);
	}

	@Override
	public String getTypeIdentifier()
	{
		return CustomJobManager.getCustomJobTypeIdentifier(getCustomJobName());
	}

	/**
	 * Sets the name or ID of the custom job, with which it is identified. Since used for saving, the ID must be a valid file name, but without file name extension.
	 * @param customJobName The name of the job template.
	 */
	void setCustomJobName(String customJobName)
	{
		this.customJobName = customJobName;
	}

	/**
	 * Returns the name or ID of the custom job, with which it is identified. The ID is also used as the basename for saving the job.
	 * @return Name of the job template.
	 */
	public String getCustomJobName()
	{
		return customJobName;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		for(JobConfiguration childJob : jobs)
		{
			childJob.checkConfiguration();
		}
		
	}
}
