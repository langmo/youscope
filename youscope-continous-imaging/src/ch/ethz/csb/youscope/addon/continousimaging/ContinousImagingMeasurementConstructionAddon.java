/**
 * 
 */
package ch.ethz.csb.youscope.addon.continousimaging;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.measurement.MeasurementConstructionAddon;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.configuration.RegularPeriod;
import ch.ethz.csb.youscope.shared.measurement.Measurement;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.ContinuousImagingJob;
import ch.ethz.csb.youscope.shared.measurement.task.MeasurementTask;

/**
 * @author langmo
 * 
 */
public class ContinousImagingMeasurementConstructionAddon implements MeasurementConstructionAddon
{

	@Override
	public void initializeTasksOfMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, ConstructionContext jobInitializer) throws ConfigurationException, RemoteException, JobCreationException
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
