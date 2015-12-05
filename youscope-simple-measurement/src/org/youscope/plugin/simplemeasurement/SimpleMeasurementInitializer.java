/**
 * 
 */
package org.youscope.plugin.simplemeasurement;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.measurement.CustomMeasurementInitializer;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.configuration.RegularPeriod;
import org.youscope.common.configuration.VaryingPeriodDTO;
import org.youscope.common.measurement.ComponentCreationException;
import org.youscope.common.measurement.ComponentException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.EditableJobContainer;
import org.youscope.common.measurement.job.Job;
import org.youscope.common.measurement.job.basicjobs.CompositeJob;
import org.youscope.common.measurement.job.basicjobs.StatisticsJob;
import org.youscope.common.measurement.task.MeasurementTask;

/**
 * @author langmo
 * 
 */
class SimpleMeasurementInitializer implements CustomMeasurementInitializer<SimpleMeasurementConfiguration>
{

	@Override
	public void initializeMeasurement(Measurement measurement, SimpleMeasurementConfiguration configuration, ConstructionContext jobInitializer) throws ConfigurationException, AddonException
	{
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
				throw new AddonException("Could not create measurement since it is already running.", e);
			}
			catch (RemoteException e)
			{
				throw new AddonException("Could not create measurement due to remote exception.", e);
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
				throw new AddonException("Could not create measurement since it is already running.", e);
			}
			catch (RemoteException e)
			{
				throw new AddonException("Could not create measurement due to remote exception.", e);
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
				try 
				{
					job = jobInitializer.getComponentProvider().createJob(positionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
					job.setName("Job container for simple measurement imaging protocol");
					mainTask.addJob(job);	
				} 
				catch (ComponentCreationException e) {
					throw new AddonException("Microplate measurements need the composite job plugin.", e);
				}
				catch (RemoteException e)
				{
					throw new AddonException("Could not create measurement due to remote exception.", e);
				}
					
				jobContainer = job;
			}
			else
			{
				StatisticsJob job;
				try {
					job = jobInitializer.getComponentProvider().createJob(positionInformation, StatisticsJob.DEFAULT_TYPE_IDENTIFIER, StatisticsJob.class);
					job.setName("Job container/analyzer for simple measurement imaging protocol");
					job.addTableListener(jobInitializer.getMeasurementSaver().getSaveTableDataListener(configuration.getStatisticsFileName()));
					job.addMessageListener(jobInitializer.getLogger());
					mainTask.addJob(job);
				} 
				catch (ComponentCreationException e) 
				{
					throw new AddonException("Microplate measurements need the statistic job plugin.", e);
				}
				catch (RemoteException e)
				{
					throw new AddonException("Could not create measurement due to remote exception.", e);
				}
					
				jobContainer = job;
			}
			
			// Add all jobs
			JobConfiguration[] jobConfigurations = configuration.getJobs();
			for(JobConfiguration jobConfiguration : jobConfigurations)
			{
				Job job;
				try {
					job = jobInitializer.getComponentProvider().createJob(positionInformation, jobConfiguration);
					jobContainer.addJob(job);
				} catch (ComponentCreationException e) {
					throw new AddonException("Could not create child job.", e);
				}
				catch (RemoteException e)
				{
					throw new AddonException("Could not create measurement due to remote exception.", e);
				}
			}
		}
		catch(ComponentException e)
		{
			throw new AddonException("Could not create measurement since it is already running.", e);
		}
			
	}
}
