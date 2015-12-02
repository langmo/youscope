/**
 * 
 */
package ch.ethz.csb.youscope.addon.compositejob;

import java.util.Vector;

import ch.ethz.csb.youscope.shared.configuration.JobContainerConfiguration;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.CompositeJob;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A job consisting of other jobs.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("composite-job")
public class CompositeJobConfiguration extends JobConfiguration implements JobContainerConfiguration
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
	public Object clone() throws CloneNotSupportedException
	{
		CompositeJobConfiguration clone = (CompositeJobConfiguration)super.clone();
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
		return CompositeJob.DEFAULT_TYPE_IDENTIFIER;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(jobs == null)
			throw new ConfigurationException("Jobs is null");
		for(JobConfiguration job : jobs)
			job.checkConfiguration();
	}
}
