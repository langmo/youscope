/**
 * 
 */
package ch.ethz.csb.youscope.addon.simplemeasurement;

import java.io.Serializable;
import java.util.Vector;

import ch.ethz.csb.youscope.shared.configuration.JobContainerConfiguration;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.configuration.Period;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class represents the configuration of simple measurement, e.g. one repeated task with several jobs.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("simple-measurement")
public class SimpleMeasurementConfigurationDTO extends MeasurementConfiguration implements Cloneable, Serializable, JobContainerConfiguration
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
	private String statisticsFileName = null;
	
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
	 * The identifier for this measurement type.
	 */
	public static final String								TYPE_IDENTIFIER		= "CSB::SimpleMeasurement";

	/**
	 * Period in which the jobs should be repeated. NULL is
	 * interpreted as as fast as possible.
	 */
	@XStreamAlias("period")
	private Period										period				= null;

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		SimpleMeasurementConfigurationDTO clone = (SimpleMeasurementConfigurationDTO)super.clone();
		clone.jobs = new Vector<JobConfiguration>();
		for(int i = 0; i < jobs.size(); i++)
		{
			clone.jobs.add((JobConfiguration)jobs.elementAt(i).clone());
		}
		if(period != null)
		{
			clone.period = (Period)period.clone();
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
	public void setPeriod(Period period)
	{
		try
		{
			this.period = (Period)period.clone();
		}
		catch(CloneNotSupportedException e)
		{
			throw new IllegalArgumentException("Period can not be cloned.", e);
		}
	}

	/**
	 * @return the period
	 */
	public Period getPeriod()
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
