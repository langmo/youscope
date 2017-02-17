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
package org.youscope.plugin.simplecompositejob;

import java.util.Vector;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.CompositeJobConfiguration;
import org.youscope.common.job.basicjobs.SimpleCompositeJob;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A job consisting of other jobs.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("composite-job")
public class SimpleCompositeJobConfiguration implements CompositeJobConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long						serialVersionUID	= 4794459938367096630L;

	/**
	 * The jobs which should be run when the composite job starts.
	 */
	@XStreamAlias("jobs")
	private Vector<JobConfiguration>	jobs				= new Vector<JobConfiguration>();

	@Override
	public String getDescription()
	{
		if(jobs == null || jobs.size() == 0)
			return "Empty Job Container";
		String description = "<p>begin</p>" +
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
		jobs.removeElementAt(index);
	}

	@Override
	public void addJob(JobConfiguration job, int index)
	{
		jobs.insertElementAt(job, index);
	}

	@Override
	public String getTypeIdentifier()
	{
		return SimpleCompositeJob.DEFAULT_TYPE_IDENTIFIER;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(jobs == null)
			throw new ConfigurationException("Jobs is null");
		for(JobConfiguration job : jobs)
			job.checkConfiguration();
	}
}
