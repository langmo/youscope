/**
 * 
 */
package org.youscope.plugin.customjob;

import java.util.Vector;

import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.JobContainerConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A job consisting of other jobs, which can be defined by the user.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("custom-job")
public class CustomJobConfiguration extends JobConfiguration implements JobContainerConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long						serialVersionUID	= 4794454448367096630L;

	/**
	 * The jobs which should be run when the composite job starts.
	 */
	@XStreamAlias("jobs")
	private Vector<JobConfiguration>	jobs				= new Vector<JobConfiguration>();

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
	public CustomJobConfiguration clone() throws CloneNotSupportedException
	{
		CustomJobConfiguration clone = (CustomJobConfiguration)super.clone();
		clone.jobs= new Vector<JobConfiguration>();
		for(int i = 0; i < jobs.size(); i++)
		{
			clone.jobs.add((JobConfiguration)jobs.elementAt(i).clone());
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
}
