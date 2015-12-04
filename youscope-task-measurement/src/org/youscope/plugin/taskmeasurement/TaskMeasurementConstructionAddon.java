/**
 * 
 */
package org.youscope.plugin.taskmeasurement;

import java.rmi.RemoteException;

import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.measurement.MeasurementConstructionAddon;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.configuration.RegularPeriod;
import org.youscope.common.configuration.TaskConfiguration;
import org.youscope.common.configuration.VaryingPeriodDTO;
import org.youscope.common.measurement.ComponentCreationException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.Job;
import org.youscope.common.measurement.job.JobCreationException;
import org.youscope.common.measurement.task.MeasurementTask;

/**
 * @author langmo
 * 
 */
public class TaskMeasurementConstructionAddon implements MeasurementConstructionAddon
{

	@Override
	public void initializeMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, ConstructionContext jobInitializer) throws ConfigurationException, RemoteException, JobCreationException
	{
		if(!(measurementConfiguration instanceof TaskMeasurementConfiguration))
		{
			throw new ConfigurationException("Measurement configuration is not a configurable measurement.");
		}
		TaskMeasurementConfiguration configuration = (TaskMeasurementConfiguration)measurementConfiguration;

		// Add jobs
		for(TaskConfiguration taskConfiguration : configuration.getTasks())
		{
			// Every job gets an associated own task
			MeasurementTask task;
			if(taskConfiguration.getPeriod() == null)
			{
				// Set period to AFAP
				RegularPeriod period = new RegularPeriod();
				period.setPeriod(0);
				period.setFixedTimes(false);
				period.setStartTime(0);
				taskConfiguration.setPeriod(period);
			}

			if(taskConfiguration.getPeriod() instanceof RegularPeriod)
			{
				RegularPeriod period = (RegularPeriod)taskConfiguration.getPeriod();
				try
				{
					task = measurement.addTask(period.getPeriod(), period.isFixedTimes(), period.getStartTime(), period.getNumExecutions());
				}
				catch(MeasurementRunningException e)
				{
					throw new JobCreationException("Could not create measurement since it is already running.", e);
				}
			}
			else if(taskConfiguration.getPeriod() instanceof VaryingPeriodDTO)
			{
				VaryingPeriodDTO period = (VaryingPeriodDTO)taskConfiguration.getPeriod();
				try
				{
					task = measurement.addMultiplePeriodTask(period.getPeriods(), period.getBreakTime(), period.getStartTime(), period.getNumExecutions());
				}
				catch(MeasurementRunningException e)
				{
					throw new JobCreationException("Could not create measurement since it is already running.", e);
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
						throw new JobCreationException("Could not create child job.", e);
					}
					task.addJob(job);
				}
			}
			catch(MeasurementRunningException e)
			{
				throw new JobCreationException("Could not create measurement since it is already running.", e);
			}
		}
	}

}
