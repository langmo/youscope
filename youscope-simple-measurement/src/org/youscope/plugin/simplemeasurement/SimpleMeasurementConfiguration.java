/**
 * 
 */
package org.youscope.plugin.simplemeasurement;

import java.io.Serializable;
import java.util.Vector;

import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.JobContainerConfiguration;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.task.PeriodConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class represents the configuration of simple measurement, e.g. one repeated task with several jobs.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("simple-measurement")
public class SimpleMeasurementConfiguration extends MeasurementConfiguration implements Cloneable, Serializable, JobContainerConfiguration
{
	/**
	 * Serial version UID.
	 */
	private static final long						serialVersionUID	= 3333810800791783902L;

	/**
	 * A list of all the jobs which should be done during the measurement.
	 */
	@XStreamAlias("jobs")
	private Vector<JobConfiguration>	jobs				= new Vector<JobConfiguration>();

	@XStreamAlias("statistics-file")
	private String statisticsFileName =  "statistics";
	
	@XStreamAlias("allow-edits-while-running")
	private boolean allowEditsWhileRunning = false;
	
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
	
	/**
	 * Returns true if measurement configuration can be edited while it is running.
	 * @return True if measurement can be edited.
	 */
	public boolean isAllowEditsWhileRunning() {
		return allowEditsWhileRunning;
	}

	/**
	 * Set to true to allow the measurement to be edited while running.
	 * @param allowEditsWhileRunning True if measurement should be changeable while running.
	 */
	public void setAllowEditsWhileRunning(boolean allowEditsWhileRunning) {
		this.allowEditsWhileRunning = allowEditsWhileRunning;
	}

	/**
	 * The identifier for this measurement type.
	 */
	public static final String								TYPE_IDENTIFIER		= "YouScope.SimpleMeasurement";

	/**
	 * Period in which the jobs should be repeated. NULL is
	 * interpreted as as fast as possible.
	 */
	@XStreamAlias("period")
	private PeriodConfiguration										period				= null;

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		SimpleMeasurementConfiguration clone = (SimpleMeasurementConfiguration)super.clone();
		clone.jobs = new Vector<JobConfiguration>();
		for(int i = 0; i < jobs.size(); i++)
		{
			clone.jobs.add((JobConfiguration)jobs.elementAt(i).clone());
		}
		if(period != null)
		{
			clone.period = (PeriodConfiguration)period.clone();
		}
		
		return clone;
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

	/**
	 * @param period
	 *            the period to set
	 */
	public void setPeriod(PeriodConfiguration period)
	{
		try
		{
			this.period = (PeriodConfiguration)period.clone();
		}
		catch(CloneNotSupportedException e)
		{
			throw new IllegalArgumentException("Period can not be cloned.", e);
		}
	}

	/**
	 * @return the period
	 */
	public PeriodConfiguration getPeriod()
	{
		return period;
	}
	
	/**
	 * Sets the name (without extension) of the file in which statistics of the measurement should be saved to.
	 * Set to null to not generate statistics.
	 * @param statisticsFileName name for the file (without extension) in which statistics should be saved, or null.
	 */
	public void setStatisticsFileName(String statisticsFileName)
	{
		this.statisticsFileName = statisticsFileName;
	}

	/**
	 * Returns the name (without extension) of the file in which statistics of the measurement should be saved to.
	 * Returns null if no statistics are generated.
	 * @return name for the file (without extension) in which statistics should be saved, or null.
	 */
	public String getStatisticsFileName()
	{
		return statisticsFileName;
	}
}
