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
package org.youscope.plugin.waitjob;

import java.util.ArrayList;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.CompositeJobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A job container executing its child jobs. If the execution of the child jobs takes less than a given time, the rest of this time is waited.
 * Thus, it is guaranteed that the execution of the child jobs take at least a given period of time.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("execute-and-wait-job")
public class ExecuteAndWaitJobConfiguration implements CompositeJobConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long						serialVersionUID	= 4794459938367096630L;

	/**
	 * The jobs which should be run when the job starts.
	 */
	@XStreamAlias("jobs")
	private final ArrayList<JobConfiguration>	jobs				= new ArrayList<JobConfiguration>();

	/**
	 * The wait time in milliseconds
	 */
	@XStreamAlias("wait-ms")
	private long				waitTime			= 0;
	
	@Override
	public String getDescription()
	{
		String description = "<p>begin</p>" +
			"<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">" +
			"<li>deltaT = startTimer()</li>";
	
		
		for(JobConfiguration job : jobs)
		{
			description += "<li>" + job.getDescription() + "</li>";
		}
		description += "<li>wait(deltaT &lt; " + Long.toString(waitTime) + "ms)</li>" +
			"</ul><p>end</p>";
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

	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.ExecuteAndWaitJob";

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}
	
	/**
	 * Returns the wait time in ms.
	 * @return Wait time in ms.
	 */
	public long getWaitTime()
	{
		return waitTime;
	}
	
	/**
	 * Sets the wait time in ms. Must be larger or equal 0.
	 * @param waitTime Wait time in ms.
	 */
	public void setWaitTime(long waitTime)
	{
		this.waitTime = waitTime > 0 ? waitTime : 0;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(waitTime < 0)
			throw new ConfigurationException("Wait time must be bigger or equal to zero.");
		for(JobConfiguration job : jobs)
			job.checkConfiguration();
	}
}
