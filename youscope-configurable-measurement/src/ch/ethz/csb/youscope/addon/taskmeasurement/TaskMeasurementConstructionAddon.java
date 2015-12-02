/**
 * 
 */
package ch.ethz.csb.youscope.addon.taskmeasurement;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.measurement.MeasurementConstructionAddon;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.configuration.RegularPeriod;
import ch.ethz.csb.youscope.shared.configuration.TaskConfiguration;
import ch.ethz.csb.youscope.shared.configuration.VaryingPeriodDTO;
import ch.ethz.csb.youscope.shared.measurement.ComponentCreationException;
import ch.ethz.csb.youscope.shared.measurement.Measurement;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.measurement.task.MeasurementTask;

/**
 * @author langmo
 * 
 */
public class TaskMeasurementConstructionAddon implements MeasurementConstructionAddon
{

	@Override
	public void initializeTasksOfMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, ConstructionContext jobInitializer) throws ConfigurationException, RemoteException, JobCreationException
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
