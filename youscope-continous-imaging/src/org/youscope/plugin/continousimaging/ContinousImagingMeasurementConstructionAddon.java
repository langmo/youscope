/**
 * 
 */
package org.youscope.plugin.continousimaging;

import java.rmi.RemoteException;

import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.measurement.MeasurementConstructionAddon;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.configuration.RegularPeriod;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.JobCreationException;
import org.youscope.common.measurement.job.basicjobs.ContinuousImagingJob;
import org.youscope.common.measurement.task.MeasurementTask;

/**
 * @author langmo
 * 
 */
public class ContinousImagingMeasurementConstructionAddon implements MeasurementConstructionAddon
{

	@Override
	public void initializeMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, ConstructionContext jobInitializer) throws ConfigurationException, RemoteException, JobCreationException
	{
		if(!(measurementConfiguration instanceof ContinousImagingMeasurementConfiguration))
		{
			throw new ConfigurationException("Measurement configuration is not a configurable measurement.");
		}
		ContinousImagingMeasurementConfiguration configuration = (ContinousImagingMeasurementConfiguration)measurementConfiguration;

		RegularPeriod period = new RegularPeriod();
		int taskPeriod = configuration.getImagingPeriod();
		if(taskPeriod <= 0)
		{
			// burst mode, job does not have to be evaluated, since images are transmitted automatically
			period.setPeriod(10000);
			period.setFixedTimes(false);
		}
		else
		{
			// fixed mode
			period.setPeriod(taskPeriod);
			period.setFixedTimes(true);
		}		
		// start first query delayed, such that image can have arrived.
		period.setStartTime(taskPeriod);
		MeasurementTask task;
		try
		{
			task = measurement.addTask(period.getPeriod(), period.isFixedTimes(), period.getStartTime(), period.getNumExecutions());
			ContinuousImagingJob job = jobInitializer.getComponentProvider().createJob(new PositionInformation(), ContinuousImagingJob.DEFAULT_TYPE_IDENTIFIER, ContinuousImagingJob.class);
			if(job == null)
				throw new JobCreationException("Continuous measurement could not create continuous imaging job.");
			job.setChannel(configuration.getChannelGroup(), configuration.getChannel());
			job.setExposure(configuration.getExposure());
			job.setBurstImaging(taskPeriod <= 0);
			if(configuration.getSaveImages())
				job.addImageListener(jobInitializer.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()));
			task.addJob(job);
		}
		catch(Exception e)
		{
			throw new ConfigurationException("Could not create measurement.", e);
		}
	}
}
