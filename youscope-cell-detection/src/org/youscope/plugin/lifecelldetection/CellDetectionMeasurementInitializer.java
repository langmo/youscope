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
package org.youscope.plugin.lifecelldetection;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.celldetection.CellDetectionAddon;
import org.youscope.addon.celldetection.CellDetectionConfiguration;
import org.youscope.addon.celldetection.CellVisualizationAddon;
import org.youscope.addon.celldetection.CellVisualizationConfiguration;
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
public class CellDetectionMeasurementInitializer implements MeasurementInitializer<CellDetectionMeasurementConfiguration>
{

	@Override
	public void initializeMeasurement(Measurement measurement, CellDetectionMeasurementConfiguration configuration, ConstructionContext constructionContext) throws ConfigurationException, AddonException
	{
		PositionInformation positionInformation = new PositionInformation(null);
		
		// Get detection & visualization algorithms.
		CellDetectionConfiguration detectionAlgorithmConfiguration = configuration.getDetectionAlgorithmConfiguration();
		CellVisualizationConfiguration visualizationAlgorithmConfiguration = configuration.getVisualizationAlgorithmConfiguration();
		if(detectionAlgorithmConfiguration == null)
		{
			throw new ConfigurationException("Configuration of cell detection algorithm is null.");
		}
		
		CellDetectionAddon cellDetectionAddon;
		try {
			cellDetectionAddon = constructionContext.getComponentProvider().createComponent(positionInformation, detectionAlgorithmConfiguration, CellDetectionAddon.class);
		} catch (Exception e1) {
			throw new AddonException("Could not create cell detection algorithm.", e1);
		}
		 
		CellVisualizationAddon cellVisualizationAddon;
		if(visualizationAlgorithmConfiguration != null)
		{
			try {
				cellVisualizationAddon= constructionContext.getComponentProvider().createComponent(positionInformation, visualizationAlgorithmConfiguration, CellVisualizationAddon.class);
			} catch (Exception e) {
				throw new AddonException("Could not create cell visualization algorithm.", e);
			}
		}
		else
			cellVisualizationAddon = null;
		
		RegularPeriodConfiguration period = new RegularPeriodConfiguration();
		int taskPeriod = configuration.getImagingPeriod();
		if(taskPeriod <= 0)
		{
			// burst mode
			period.setPeriod(0);
			period.setFixedTimes(false);
		}
		else
		{
			// fixed mode
			period.setPeriod(taskPeriod);
			period.setFixedTimes(true);
		}		
		
		period.setStartTime(0);
		Task task;
		try
		{
			task = measurement.addTask(period.getPeriod(), period.isFixedTimes(), period.getStartTime(), period.getNumExecutions());
			
			CellDetectionJobImpl cellDetectionJob = new CellDetectionJobImpl(new PositionInformation(null));
			cellDetectionJob.setDetectionAlgorithm(cellDetectionAddon);
			cellDetectionJob.setVisualizationAlgorithm(cellVisualizationAddon);			
			
			// continuous imaging job
			ContinuousImagingJob continuousJob;
			try {
				continuousJob = constructionContext.getComponentProvider().createJob(new PositionInformation(), ContinuousImagingJob.DEFAULT_TYPE_IDENTIFIER, ContinuousImagingJob.class);
			} catch (ComponentCreationException e) {
				throw new AddonException("Continuous life cell detection measurement needs the continuous imaging plugin.", e);
			}
			continuousJob.setChannel(configuration.getChannelGroup(), configuration.getChannel());
			continuousJob.setExposure(configuration.getExposure());
			int imagingPeriod = configuration.getImagingPeriod();
			continuousJob.setBurstImaging(imagingPeriod <= 0);
			if(configuration.isSaveImages())
				continuousJob.addImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()));
			if(visualizationAlgorithmConfiguration != null && configuration.isSaveImages())
				cellDetectionJob.addImageListener(constructionContext.getMeasurementSaver().getSaveImageListener("Detect"));
						
			cellDetectionJob.addJob(continuousJob);
			try {
				task.addJob(cellDetectionJob);
			} catch (TaskException e) {
				throw new AddonException("Could not add job to task.", e);
			}
		}
		catch(RemoteException e)
		{
			throw new AddonException("Remote exception while creating job.", e);
		} catch (ComponentRunningException e) {
			throw new AddonException("Could not initialize measurement since measurement is already running.", e);
		}
	}
}
