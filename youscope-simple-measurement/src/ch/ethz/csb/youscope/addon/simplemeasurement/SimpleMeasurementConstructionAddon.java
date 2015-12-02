/**
 * 
 */
package ch.ethz.csb.youscope.addon.simplemeasurement;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.measurement.MeasurementConstructionAddon;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.configuration.RegularPeriod;
import ch.ethz.csb.youscope.shared.configuration.VaryingPeriodDTO;
import ch.ethz.csb.youscope.shared.measurement.ComponentCreationException;
import ch.ethz.csb.youscope.shared.measurement.ComponentException;
import ch.ethz.csb.youscope.shared.measurement.Measurement;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.EditableJobContainer;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.CompositeJob;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.StatisticsJob;
import ch.ethz.csb.youscope.shared.measurement.task.MeasurementTask;

/**
 * @author langmo
 * 
 */
class SimpleMeasurementConstructionAddon implements MeasurementConstructionAddon
{

	@Override
	public void initializeTasksOfMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, ConstructionContext jobInitializer) throws ConfigurationException, RemoteException, JobCreationException
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
