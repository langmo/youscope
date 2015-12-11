/**
 * 
 */
package org.youscope.plugin.taskmeasurement;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.measurement.MeasurementInitializer;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.configuration.RegularPeriodConfiguration;
import org.youscope.common.configuration.TaskConfiguration;
import org.youscope.common.configuration.VaryingPeriodConfiguration;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.Job;
import org.youscope.common.measurement.task.MeasurementTask;
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
			MeasurementTask task;
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
				catch(MeasurementRunningException e)
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
					task = measurement.addMultiplePeriodTask(period.getPeriods(), period.getBreakTime(), period.getStartTime(), period.getNumExecutions());
				}
				catch(MeasurementRunningException e)
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
					task.addJob(job);
				}
			}
			catch(MeasurementRunningException e)
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
