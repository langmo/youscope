/**
 * 
 */
package org.youscope.plugin.simplemeasurement;

import java.rmi.RemoteException;

import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.measurement.MeasurementConstructionAddon;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.configuration.RegularPeriod;
import org.youscope.common.configuration.VaryingPeriodDTO;
import org.youscope.common.measurement.ComponentCreationException;
import org.youscope.common.measurement.ComponentException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.EditableJobContainer;
import org.youscope.common.measurement.job.Job;
import org.youscope.common.measurement.job.JobCreationException;
import org.youscope.common.measurement.job.basicjobs.CompositeJob;
import org.youscope.common.measurement.job.basicjobs.StatisticsJob;
import org.youscope.common.measurement.task.MeasurementTask;

/**
 * @author langmo
 * 
 */
class SimpleMeasurementConstructionAddon implements MeasurementConstructionAddon
{

	@Override
	public void initializeMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, ConstructionContext jobInitializer) throws ConfigurationException, RemoteException, JobCreationException
	{
		if(!(measurementConfiguration instanceof SimpleMeasurementConfigurationDTO))
		{
			throw new ConfigurationException("Measurement configuration is not a simple measurement.");
		}
		SimpleMeasurementConfigurationDTO configuration = (SimpleMeasurementConfigurationDTO)measurementConfiguration;

				// If iteration through wells should be AFAP, put everything in one task
		MeasurementTask mainTask;
		if(configuration.getPeriod() instanceof RegularPeriod)
		{
			RegularPeriod period = (RegularPeriod)configuration.getPeriod();
			try
			{
				mainTask = measurement.addTask(period.getPeriod(), period.isFixedTimes(), period.getStartTime(), period.getNumExecutions());
			}
			catch(MeasurementRunningException e)
			{
				throw new ConfigurationException("Could not create measurement since it is already running.", e);
			}
		}
		else if(configuration.getPeriod() instanceof VaryingPeriodDTO)
		{
			VaryingPeriodDTO period = (VaryingPeriodDTO)configuration.getPeriod();
			try
			{
				mainTask = measurement.addMultiplePeriodTask(period.getPeriods(), period.getBreakTime(), period.getStartTime(), period.getNumExecutions());
			}
			catch(MeasurementRunningException e)
			{
				throw new ConfigurationException("Could not create measurement since it is already running.", e);
			}
		}
		else
		{
			throw new ConfigurationException("Period type is not supported.");
		}

		PositionInformation positionInformation = new PositionInformation(null);
		
		try
		{
			// Create a job container in which all jobs are put into.
			EditableJobContainer jobContainer;
			if(configuration.getStatisticsFileName() == null)
			{
				CompositeJob job;
				try {
					job = jobInitializer.getComponentProvider().createJob(positionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
				} catch (ComponentCreationException e) {
					throw new JobCreationException("Microplate measurements need the composite job plugin.", e);
				}
					
				job.setName("Job container for simple measurement imaging protocol");
				mainTask.addJob(job);
				jobContainer = job;
			}
			else
			{
				StatisticsJob job;
				try {
					job = jobInitializer.getComponentProvider().createJob(positionInformation, StatisticsJob.DEFAULT_TYPE_IDENTIFIER, StatisticsJob.class);
				} catch (ComponentCreationException e) {
					throw new JobCreationException("Microplate measurements need the statistic job plugin.", e);
				}
					
				job.setName("Job container/analyzer for simple measurement imaging protocol");
				job.addTableListener(jobInitializer.getMeasurementSaver().getSaveTableDataListener(configuration.getStatisticsFileName()));
				job.addMessageListener(jobInitializer.getLogger());
				mainTask.addJob(job);
				jobContainer = job;
			}
			
			// Add all jobs
			JobConfiguration[] jobConfigurations = configuration.getJobs();
			for(JobConfiguration jobConfiguration : jobConfigurations)
			{
				Job job;
				try {
					job = jobInitializer.getComponentProvider().createJob(positionInformation, jobConfiguration);
				} catch (ComponentCreationException e) {
					throw new JobCreationException("Could not create child job.", e);
				}
				jobContainer.addJob(job);
			}
		}
		catch(ComponentException e)
		{
			throw new ConfigurationException("Could not create measurement since it is already running.", e);
		}
			
	}
}
