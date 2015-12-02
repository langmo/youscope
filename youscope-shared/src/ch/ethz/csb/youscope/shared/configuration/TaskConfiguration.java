/**
 * 
 */
package ch.ethz.csb.youscope.shared.configuration;

import java.util.Vector;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Moritz Lang
 */
@XStreamAlias("task")
public class TaskConfiguration implements JobContainerConfiguration, Configuration
{
	/**
	 * Serial Version UID.
	 */
	private static final long			serialVersionUID	= -4576475530737746377L;

	/**
	 * Period in which the jobs should be executed in microseconds. NULL is interpreted as as fast
	 * as possible.
	 */
	@XStreamAlias("period")
	private Period					period				= new RegularPeriod();

	/**
	 * A list of all the jobs which should be done during the measurement.
	 */
	@XStreamAlias("jobs")
	private Vector<JobConfiguration>	jobs				= new Vector<JobConfiguration>();

	@Override
	public JobConfiguration[] getJobs()
	{
		return jobs.toArray(new JobConfiguration[jobs.size()]);
	}

	@Override
	public void setJobs(JobConfiguration[] jobs)
	{
		this.jobs.clear();
		for(JobConfiguration job : jobs)
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
	public Object clone() throws CloneNotSupportedException
	{
		TaskConfiguration clone = (TaskConfiguration)super.clone();
		clone.jobs = new Vector<JobConfiguration>();
		for(int i = 0; i < jobs.size(); i++)
		{
			clone.jobs.add((JobConfiguration)jobs.elementAt(i).clone());
		}
		if(period != null)
			clone.period = (Period)period.clone();

		return clone;
	}

	/**
	 * Returns a description of this task
	 * 
	 * @return Description of task.
	 */
	public String getDescription()
	{
		boolean fixedTimes = !(getPeriod() instanceof RegularPeriod) || ((RegularPeriod)getPeriod()).isFixedTimes();

		String description;
		if(getPeriod().getNumExecutions() < 0)
			description = "<p>while true</p>";
		else
			description = "<p>for n = 1 : " + getPeriod().getNumExecutions() + "</p>";
		description += "<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
		if(fixedTimes)
			description += "<li>deltaT = startTimer()</li>";
		for(JobConfiguration job : jobs)
		{
			description += "<li>" + job.getDescription() + "</li>";
		}
		if(fixedTimes)
		{
			if(getPeriod() instanceof RegularPeriod)
			{
				description += "<li>wait(deltaT &lt; " + Integer.toString(((RegularPeriod)getPeriod()).getPeriod()) + "ms)</li>";
			}
			else if(getPeriod() instanceof VaryingPeriodDTO)
			{
				description += "<li>wait(deltaT &lt; periods[n])</li>";
			}
		}
		else
		{
			description += "<li>wait(" + Integer.toString(((RegularPeriod)getPeriod()).getPeriod()) + "ms)</li>";
		}

		description += "</ul><p>end</p>";
		return description;
	}

	/**
	 * @param period the period to set
	 */
	public void setPeriod(Period period)
	{
		this.period = period;
	}

	/**
	 * @return the period
	 */
	public Period getPeriod()
	{
		return period;
	}
	
	@Override
	public String getTypeIdentifier() 
	{
		return "CSB::RegularTask";
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(period == null)
			throw new ConfigurationException("Period is null");
		period.checkConfiguration();
		if(jobs == null)
			throw new ConfigurationException("Jobs are null");
		for(JobConfiguration job : jobs)
			job.checkConfiguration();
		
	}
}
