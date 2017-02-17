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
package org.youscope.plugin.multicameracontinuousimaging;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.measurement.MeasurementInitializer;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.basicjobs.ContinuousImagingJob;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.task.Task;
import org.youscope.common.task.RegularPeriodConfiguration;
import org.youscope.common.task.TaskException;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author langmo
 * 
 */
public class MultiCameraContinousImagingInitializer implements MeasurementInitializer<MultiCameraContinousImagingConfiguration>{

	@Override
	public void initializeMeasurement(Measurement measurement, MultiCameraContinousImagingConfiguration configuration, ConstructionContext jobInitializer) throws ConfigurationException, AddonException
	{		
		RegularPeriodConfiguration period = new RegularPeriodConfiguration();
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
		Task task;
		try
		{
			task = measurement.addTask(period.getPeriod(), period.isFixedTimes(), period.getStartTime(), period.getNumExecutions());
			ContinuousImagingJob job;
			try {
				job = jobInitializer.getComponentProvider().createJob(new PositionInformation(),ContinuousImagingJob.DEFAULT_TYPE_IDENTIFIER,  ContinuousImagingJob.class);
			} catch (ComponentCreationException e) {
				throw new AddonException("Multi camera continuous imaging measurement needs the continuous imaging plugin.", e);
			}
			job.setChannel(configuration.getChannelGroup(), configuration.getChannel());
			job.setCameras(configuration.getCameras());
			job.setExposures(configuration.getExposures());
			job.setBurstImaging(taskPeriod <= 0);
			if(configuration.isSaveImages())
				job.addImageListener(jobInitializer.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()));
			try {
				task.addJob(job);
			} catch (TaskException e) {
				throw new AddonException("Could not add job to task.", e);
			}
		}
		catch(ComponentRunningException e)
		{
			throw new AddonException("Could not create measurement since it is already running.", e);
		}
		catch (RemoteException e1)
		{
			throw new AddonException("Could not create measurement due to remote exception.", e1);
		}
	}
}
