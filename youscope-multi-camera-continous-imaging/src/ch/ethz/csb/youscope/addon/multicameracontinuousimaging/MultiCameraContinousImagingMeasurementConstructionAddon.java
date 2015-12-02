/**
 * 
 */
package ch.ethz.csb.youscope.addon.multicameracontinuousimaging;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.measurement.MeasurementConstructionAddon;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.configuration.RegularPeriod;
import ch.ethz.csb.youscope.shared.measurement.ComponentCreationException;
import ch.ethz.csb.youscope.shared.measurement.Measurement;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.ContinuousImagingJob;
import ch.ethz.csb.youscope.shared.measurement.task.MeasurementTask;

/**
 * @author langmo
 * 
 */
public class MultiCameraContinousImagingMeasurementConstructionAddon implements MeasurementConstructionAddon
{

	@Override
	public void initializeTasksOfMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, ConstructionContext jobInitializer) throws ConfigurationException, RemoteException, JobCreationException
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
