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
package org.youscope.plugin.taskmeasurement;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.measurement.MeasurementInitializer;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.task.Task;
import org.youscope.common.task.RegularPeriodConfiguration;
import org.youscope.common.task.TaskConfiguration;
import org.youscope.common.task.TaskException;
import org.youscope.common.task.VaryingPeriodConfiguration;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author langmo
 * 
 */
public class TaskMeasurementInitializer implements MeasurementInitializer<TaskMeasurementConfiguration>
{

	@Override
	public void initializeMeasurement(Measurement measurement, TaskMeasurementConfiguration configuration, ConstructionContext jobInitializer) throws ConfigurationException, AddonException
	{
		// Add jobs
		for(TaskConfiguration taskConfiguration : configuration.getTasks())
		{
			// Every job gets an associated own task
			Task task;
			if(taskConfiguration.getPeriod() == null)
			{
				// Set period to AFAP
				RegularPeriodConfiguration period = new RegularPeriodConfiguration();
				period.setPeriod(0);
				period.setFixedTimes(false);
				period.setStartTime(0);
				taskConfiguration.setPeriod(period);
			}

			if(taskConfiguration.getPeriod() instanceof RegularPeriodConfiguration)
			{
				RegularPeriodConfiguration period = (RegularPeriodConfiguration)taskConfiguration.getPeriod();
				try
				{
					task = measurement.addTask(period.getPeriod(), period.isFixedTimes(), period.getStartTime(), period.getNumExecutions());
				}
				catch(ComponentRunningException e)
				{
					throw new AddonException("Could not create measurement since it is already running.", e);
				}
				catch(RemoteException e)
				{
					throw new AddonException("Could not create measurement due to remote exception.", e);
				}
			}
			else if(taskConfiguration.getPeriod() instanceof VaryingPeriodConfiguration)
			{
				VaryingPeriodConfiguration period = (VaryingPeriodConfiguration)taskConfiguration.getPeriod();
				try
				{
					task = measurement.addMultiplePeriodTask(period.getPeriods(), period.getStartTime(), period.getNumExecutions());
				}
				catch(ComponentRunningException e)
				{
					throw new AddonException("Could not create measurement since it is already running.", e);
				}
				catch(RemoteException e)
				{
					throw new AddonException("Could not create measurement due to remote exception.", e);
				}
			}
			else
				throw new ConfigurationException("Period type is not supported.");
			JobConfiguration[] jobConfigurations = taskConfiguration.getJobs();
			try
			{
				for(JobConfiguration jobConfiguration : jobConfigurations)
				{
					Job job;
					try {
						job = jobInitializer.getComponentProvider().createJob(new PositionInformation(), jobConfiguration);
					} catch (ComponentCreationException e) {
						throw new AddonException("Could not create child job.", e);
					}
					try {
						task.addJob(job);
					} catch (TaskException e) {
						throw new AddonException("Could not add job to task.", e);
					}
				}
			}
			catch(ComponentRunningException e)
			{
				throw new AddonException("Could not create measurement since it is already running.", e);
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create measurement due to remote exception.", e);
			}
		}
	}

}
