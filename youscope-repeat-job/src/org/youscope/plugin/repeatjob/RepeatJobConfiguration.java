/**
 * 
 */
package org.youscope.plugin.repeatjob;

import java.util.Vector;

import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.configuration.JobContainerConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A job consisting of other jobs, which are repeated for a certain amount of times.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("repeat-job")
public class RepeatJobConfiguration extends JobConfiguration implements JobContainerConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long						serialVersionUID	= 4791159938367096630L;

	/**
	 * The jobs which should be run several times when the repeat job executes.
	 */
	@XStreamAlias("jobs")
	private Vector<JobConfiguration>	jobs				= new Vector<JobConfiguration>();

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
	public Object clone() throws CloneNotSupportedException
	{
		RepeatJobConfiguration clone = (RepeatJobConfiguration)super.clone();
		clone.jobs= new Vector<JobConfiguration>();
		for(int i = 0; i < jobs.size(); i++)
		{
			clone.jobs.add((JobConfiguration)jobs.get(i).clone());
		}
		return clone;
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
		jobs.insertElementAt(job, index);
	}

	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "CSB::RepeatJob";

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
}
