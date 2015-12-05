/**
 * 
 */
package org.youscope.plugin.microplatemeasurement;

import java.io.Serializable;
import java.util.Vector;

import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.configuration.JobContainerConfiguration;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.configuration.Period;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class represents the configuration of a user configurable microtiter
 * plate measurement.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("microplate-measurement")
public class MicroplateMeasurementConfiguration extends MeasurementConfiguration implements Cloneable, Serializable, JobContainerConfiguration
{
	/**
	 * Serial version UID.
	 */
	private static final long						serialVersionUID	= 3413810800791783902L;

	/**
	 * A list of all the jobs which should be done during the measurement.
	 */
	private Vector<JobConfiguration>	jobs				= new Vector<JobConfiguration>();

	@XStreamAlias("statistics-file")
	private String statisticsFileName = "statistics";
	
	@XStreamAlias("path-optimizer")
	private String pathOptimizerID = null;
	
	@XStreamAlias("stage")
	private String stageDevice = null;
	
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
	public static final String								TYPE_IDENTIFIER		= "CSB::MicroPlateMeasurement";

	/**
	 * Time maximal needed per well in microseconds. Set to "-1" for
	 * "as fast as possible".
	 */
	@XStreamAlias("time-per-well")
	private int												timePerWell			= -1;

	/**
	 * Period in which every well should be visited in microseconds. NULL is
	 * interpreted as as fast as possible.
	 */
	@XStreamAlias("period")
	private Period										period				= null;

	/**
	 * Type of plate.
	 */
	@XStreamAlias("microplate")
	private MicroplatePositionConfiguration	microplatePositions	= new MicroplatePositionConfiguration();

	/**
	 * Configuration of the focus device used for focussing the wells. Set to
	 * null to not set focus in wells.
	 */
	@XStreamAlias("focus")
	private FocusConfiguration		 			focusConfiguration	= null;

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		MicroplateMeasurementConfiguration clone = (MicroplateMeasurementConfiguration)super.clone();
		clone.jobs = new Vector<JobConfiguration>();
		for(int i = 0; i < jobs.size(); i++)
		{
			clone.jobs.add((JobConfiguration)jobs.elementAt(i).clone());
		}
		if(period != null)
		{
			clone.period = (Period)period.clone();
		}
		if(focusConfiguration != null)
			clone.focusConfiguration = focusConfiguration.clone();
		
		clone.microplatePositions = (MicroplatePositionConfiguration)microplatePositions.clone();
		
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
	 * @param timePerWell
	 *            the timePerWell to set
	 */
	public void setTimePerWell(int timePerWell)
	{
		this.timePerWell = timePerWell;
	}

	/**
	 * @return the timePerWell
	 */
	public int getTimePerWell()
	{
		return timePerWell;
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
	 * @param focusConfiguration
	 *            The configuration of the focus used when entering a well. Set
	 *            to NULL to not set focus.
	 */
	public void setFocusConfiguration(FocusConfiguration focusConfiguration)
	{
		this.focusConfiguration = focusConfiguration;
	}

	/**
	 * @return The configuration of the focus used when entering a well. NULL if
	 *         focus is not set.
	 */
	public FocusConfiguration getFocusConfiguration()
	{
		return focusConfiguration;
	}

	/**
	 * Sets the used microplate and the measured positions therein.
	 * @param microplatePositions the configuration of the used microplate and the measured positions therein.
	 */
	public void setMicroplatePositions(MicroplatePositionConfiguration microplatePositions)
	{
		this.microplatePositions = microplatePositions;
	}

	/**
	 * Returns the used microplate and the measured positions therein.
	 * @return the configuration of the used microplate and the measured positions therein.
	 */
	public MicroplatePositionConfiguration getMicroplatePositions()
	{
		return microplatePositions;
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

	/**
	 * Sets the ID of the optimizer which should be used to minimize the distances between two wells/positions measured.
	 * Set to null to not use any optimized path.
	 * @param pathOptimizerID The ID of the path optimizer used, or null
	 */
	public void setPathOptimizerID(String pathOptimizerID)
	{
		this.pathOptimizerID = pathOptimizerID;
	}

	/**
	 * Returns the ID of the optimizer which should be used to minimize the distances between two wells/positions measured.
	 * Returns null if not uses any optimized path.
	 * @return The ID of the path optimizer used, or null
	 */
	public String getPathOptimizerID()
	{
		return pathOptimizerID;
	}

	/**
	 * Sets the ID of the stage device which should be used to change between wells and positions. Set to NULL to use default stage device.
	 * @param stageDevice The ID of the stage device, or NULL.
	 */
	public void setStageDevice(String stageDevice)
	{
		this.stageDevice = stageDevice;
	}

	/**
	 * Returns the ID of the stage device which should be used to change between wells and positions. Returns NULL to use default stage device.
	 * @return The ID of the stage device, or NULL.
	 */
	public String getStageDevice()
	{
		return stageDevice;
	}
}
