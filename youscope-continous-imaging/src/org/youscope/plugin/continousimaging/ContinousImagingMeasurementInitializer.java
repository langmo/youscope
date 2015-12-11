/**
 * 
 */
package org.youscope.plugin.continousimaging;

import org.youscope.addon.AddonException;
import org.youscope.addon.measurement.MeasurementInitializer;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.RegularPeriodConfiguration;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.basicjobs.ContinuousImagingJob;
import org.youscope.common.measurement.task.MeasurementTask;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author langmo
 * 
 */
public class ContinousImagingMeasurementInitializer implements MeasurementInitializer<ContinousImagingMeasurementConfiguration>
{

	@Override
	public void initializeMeasurement(Measurement measurement, ContinousImagingMeasurementConfiguration configuration, ConstructionContext jobInitializer) throws ConfigurationException, AddonException
	{
		RegularPeriodConfiguration period = new RegularPeriodConfiguration();
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
			ContinuousImagingJob job = new ContinuousImagingJobImpl(new PositionInformation());

			job.setChannel(configuration.getChannelGroup(), configuration.getChannel());
			job.setExposure(configuration.getExposure());
			job.setBurstImaging(taskPeriod <= 0);
			if(configuration.getSaveImages())
			{
				job.setImageDescription(configuration.getImageSaveName() + " (" + job.getImageDescription() + ")");
				job.addImageListener(jobInitializer.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()));
			}
			task.addJob(job);
		}
		catch(Exception e)
		{
			throw new AddonException("Could not create measurement.", e);
		}
	}
}
