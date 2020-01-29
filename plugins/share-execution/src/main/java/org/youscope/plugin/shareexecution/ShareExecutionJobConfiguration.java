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
package org.youscope.plugin.shareexecution;

import java.util.ArrayList;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.CompositeJobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A job which executes its child job only for a certain share of all positions per execution
 * 
 * @author Moritz Lang
 */
@XStreamAlias("share-execution-job")
public class ShareExecutionJobConfiguration implements CompositeJobConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long						serialVersionUID	= 4791159938364446630L;

	/**
	 * The jobs which should be run several times when the repeat job executes.
	 */
	@XStreamAlias("jobs")
	private final ArrayList<JobConfiguration>	jobs				= new ArrayList<JobConfiguration>();

	@XStreamAlias("num-share")
	private int numShare = 1;
	
	@XStreamAlias("share-id")
	private int shareID = 1;
	
	@XStreamAlias("separate-for-each-well")
	private boolean separateForEachWell = false;
	
	/**
	 * Returns true if for each well the share of the jobs which get executed is determined separately.
	 * @return True if different counting for each well.
	 */
	public boolean isSeparateForEachWell() {
		return separateForEachWell;
	}

	/**
	 * If set to true, for each well the share of the jobs which get executed is determined separately.
	 * @param separateForEachWell True if different counting for each well.
	 */
	public void setSeparateForEachWell(boolean separateForEachWell) {
		this.separateForEachWell = separateForEachWell;
	}

	@Override
	public String getDescription()
	{
		if(jobs == null || jobs.size() == 0)
			return "Empty Share Execution Job";
		String description = "<p>if id&gt;=iteration*" + Integer.toString(numShare) + "%numIds && id&lt;(iteration+1)*" + Integer.toString(numShare) + "%numIds</p>" +
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
	public static final String	TYPE_IDENTIFIER	= "YouScope.ShareExecutionJob";

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	/**
	 * Returns the number of times this job gets totally executed per iteration.
	 * @return share of executions per iteration.
	 */
	public int getNumShare()
	{
		return numShare;
	}

	/**
	 * Sets the number of times this job gets totally executed per iteration.
	 * @param numShare share of executions per iteration.
	 */
	public void setNumShare(int numShare)
	{
		this.numShare = numShare > 0 ? numShare : 0;
	}

	/**
	 * Returns the ID for this share job. Share jobs with different IDs act independently from one another.
	 * @return share ID of this job.
	 */
	public int getShareID() {
		return shareID;
	}

	/**
	 * Sets the ID for this share job. Share jobs with different IDs act independently from one another.
	 * @param shareID share ID of this job.
	 */
	public void setShareID(int shareID) {
		this.shareID = shareID;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		for(JobConfiguration childJob : jobs)
		{
			childJob.checkConfiguration();
		}
		
	}
}
