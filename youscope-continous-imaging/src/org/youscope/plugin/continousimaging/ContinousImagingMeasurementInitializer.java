/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.plugin.continousimaging;

import org.youscope.addon.AddonException;
import org.youscope.addon.measurement.MeasurementInitializer;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.basicjobs.ContinuousImagingJob;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.task.Task;
import org.youscope.common.task.RegularPeriodConfiguration;
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
		Task task;
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
