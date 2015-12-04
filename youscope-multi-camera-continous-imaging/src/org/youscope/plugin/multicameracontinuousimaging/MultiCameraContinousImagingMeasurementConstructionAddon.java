/**
 * 
 */
package org.youscope.plugin.multicameracontinuousimaging;

import java.rmi.RemoteException;

import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.measurement.MeasurementConstructionAddon;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.configuration.RegularPeriod;
import org.youscope.common.measurement.ComponentCreationException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.JobCreationException;
import org.youscope.common.measurement.job.basicjobs.ContinuousImagingJob;
import org.youscope.common.measurement.task.MeasurementTask;

/**
 * @author langmo
 * 
 */
public class MultiCameraContinousImagingMeasurementConstructionAddon implements MeasurementConstructionAddon
{

	@Override
	public void initializeMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, ConstructionContext jobInitializer) throws ConfigurationException, RemoteException, JobCreationException
	{
		if(!(measurementConfiguration instanceof MultiCameraContinousImagingConfigurationDTO))
		{
			throw new ConfigurationException("Measurement configuration is not a multi camera continous imaging measurement.");
		}
		MultiCameraContinousImagingConfigurationDTO configuration = (MultiCameraContinousImagingConfigurationDTO)measurementConfiguration;

		
		RegularPeriod period = new RegularPeriod();
		int taskPeriod = configuration.getImagingPeriod();
		if(taskPeriod <= 0)
		{
			// burst mode, images are send directly to listeners, thus, we do not have to evaluate the job too often at all.
			period.setPeriod(10000);
			period.setFixedTimes(false);
		}
		else
		{
			// fixed mode
			period.setPeriod(taskPeriod);
			period.setFixedTimes(true);
		}		
		// wait a little bit until first images have arrived.
		period.setStartTime(taskPeriod);
		MeasurementTask task;
		try
		{
			task = measurement.addTask(period.getPeriod(), period.isFixedTimes(), period.getStartTime(), period.getNumExecutions());
			ContinuousImagingJob job;
			try {
				job = jobInitializer.getComponentProvider().createJob(new PositionInformation(),ContinuousImagingJob.DEFAULT_TYPE_IDENTIFIER,  ContinuousImagingJob.class);
			} catch (ComponentCreationException e) {
				throw new JobCreationException("Multi camera continuous imaging measurement needs the continuous imaging plugin.", e);
			}
			job.setChannel(configuration.getChannelGroup(), configuration.getChannel());
			job.setCameras(configuration.getCameras());
			job.setExposures(configuration.getExposures());
			job.setBurstImaging(taskPeriod <= 0);
			if(configuration.isSaveImages())
				job.addImageListener(jobInitializer.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()));
			task.addJob(job);
		}
		catch(MeasurementRunningException e)
		{
			throw new ConfigurationException("Could not create measurement since it is already running.", e);
		}
	}
}
