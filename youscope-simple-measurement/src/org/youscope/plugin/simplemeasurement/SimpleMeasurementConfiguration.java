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
package org.youscope.plugin.simplemeasurement;

import java.util.Vector;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.task.PeriodConfiguration;
import org.youscope.common.util.ConfigurationTools;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class represents the configuration of simple measurement, e.g. one repeated task with several jobs.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("simple-measurement")
public class SimpleMeasurementConfiguration extends MeasurementConfiguration
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
	
	/**
	 * Returns the jobs executed each time the measurement executes.
	 * 
	 * @return Jobs.
	 */
	public JobConfiguration[] getJobs()
	{
		return jobs.toArray(new JobConfiguration[jobs.size()]);
	}

	/**
	 * Set the jobs executed each time the measurement executes.
	 * @param jobs Jobs to be executed.
	 */
	public void setJobs(JobConfiguration[] jobs)
	{
		this.jobs.clear();
		for(JobConfiguration job:jobs)
		{
			this.jobs.add(job);
		}
	}

	/**
	 * Adds a job to be executed each time the measurement executes.
	 * @param job Job to add.
	 */
	public void addJob(JobConfiguration job)
	{
		jobs.add(job);
	}

	/**
	 * Removes all jobs executed each time the measurement executes.
	 */
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

	/**
	 * Removes a job executed each time the measurement executes with the given index.
	 * @param index Index of job to be removed.
	 */
	public void removeJob(int index)
	{
		jobs.removeElementAt(index);
	}

	/**
	 * Inserts a job executed each time the measurement executes at the given index. 
	 * @param job job to add.
	 * @param index index where to add job.
	 */
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
			this.period = ConfigurationTools.deepCopy(period, PeriodConfiguration.class);
		}
		catch(ConfigurationException e)
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
