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
package org.youscope.plugin.composedimaging;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.measurement.MeasurementInitializer;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.Job;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.task.Task;
import org.youscope.common.task.RegularPeriodConfiguration;
import org.youscope.common.task.VaryingPeriodConfiguration;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author langmo
 * 
 */
public class ComposedImagingMeasurementInitializer implements MeasurementInitializer<ComposedImagingMeasurementConfiguration>
{

	@Override
	public void initializeMeasurement(Measurement measurement, ComposedImagingMeasurementConfiguration configuration, ConstructionContext jobInitializer) throws ConfigurationException, AddonException
	{
		Task task;
		if(configuration.getPeriod() instanceof RegularPeriodConfiguration)
		{
			RegularPeriodConfiguration period = (RegularPeriodConfiguration)configuration.getPeriod();
			try
			{
				task = measurement.addTask(period.getPeriod(), period.isFixedTimes(), period.getStartTime(), period.getNumExecutions());
			}
			catch(ComponentRunningException e)
			{
				throw new AddonException("Could not create measurement since it is already running.", e);
			}
			catch (RemoteException e)
			{
				throw new AddonException("Could not create measurement due to remote exception.", e);
			}
		}
		else if(configuration.getPeriod() instanceof VaryingPeriodConfiguration)
		{
			VaryingPeriodConfiguration period = (VaryingPeriodConfiguration)configuration.getPeriod();
			try
			{
				task = measurement.addMultiplePeriodTask(period.getPeriods(), period.getStartTime(), period.getNumExecutions());
			}
			catch(ComponentRunningException e)
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
		
		ComposedImagingJobConfiguration jobConfiguration = new ComposedImagingJobConfiguration();
		jobConfiguration.setChannel(configuration.getChannelGroup(), configuration.getChannel());
		jobConfiguration.setExposure(configuration.getExposure());
		jobConfiguration.setImageSaveName(configuration.getImageSaveName());
		jobConfiguration.setNumPixels(configuration.getNumPixels());
		jobConfiguration.setNx(configuration.getNx());
		jobConfiguration.setNy(configuration.getNy());
		jobConfiguration.setOverlap(configuration.getOverlap());
		jobConfiguration.setPixelSize(configuration.getPixelSize());
		jobConfiguration.setSaveImages(configuration.isSaveImages());
		
		try
		{
			Job job = jobInitializer.getComponentProvider().createJob(new PositionInformation(), jobConfiguration);
			task.addJob(job);
		}
		catch(Exception e)
		{
			throw new AddonException("Could not create measurement.", e);
		}
	}
}
