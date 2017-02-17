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
package org.youscope.plugin.repeatjob;

import java.util.ArrayList;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.CompositeJobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A job consisting of other jobs, which are repeated for a certain amount of times.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("repeat-job")
public class RepeatJobConfiguration implements CompositeJobConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long						serialVersionUID	= 4791159938367096630L;

	/**
	 * The jobs which should be run several times when the repeat job executes.
	 */
	@XStreamAlias("jobs")
	private final ArrayList<JobConfiguration>	jobs				= new ArrayList<JobConfiguration>();

	@XStreamAlias("num-repeats")
	private int numRepeats = 1;
	
	@Override
	public String getDescription()
	{
		if(jobs == null || jobs.size() == 0)
			return "Empty Repeat Job";
		String description = "<p>for k=1:" + Integer.toString(numRepeats) + "</p>" +
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
		jobs.get(index);
	}

	@Override
	public void addJob(JobConfiguration job, int index)
	{
		jobs.add(index, job);
	}

	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.RepeatJob";

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	/**
	 * Returns the number of times the sub-jobs should be repeated each time the repeat-job is executed.
	 * @return number of times the job should be repeated.
	 */
	public int getNumRepeats()
	{
		return numRepeats;
	}

	/**
	 * Sets the number of times the sub-jobs should be repeated each time the repeat-job is executed.
	 * @param numRepeats number of times the job should be repeated. If smaller than 0, it is set to zero.
	 */
	public void setNumRepeats(int numRepeats)
	{
		this.numRepeats = numRepeats > 0 ? numRepeats : 0;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		for(JobConfiguration childJob : jobs)
		{
			childJob.checkConfiguration();
		}
		
	}
}
