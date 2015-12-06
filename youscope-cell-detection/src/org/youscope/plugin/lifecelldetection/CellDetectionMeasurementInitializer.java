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
import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.measurement.CustomMeasurementInitializer;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.RegularPeriod;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.basicjobs.ContinuousImagingJob;
import org.youscope.common.measurement.task.MeasurementTask;

/**
 * @author langmo
 * 
 */
public class CellDetectionMeasurementInitializer implements CustomMeasurementInitializer<CellDetectionMeasurementConfiguration>
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
		
		RegularPeriod period = new RegularPeriod();
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
		MeasurementTask task;
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
			task.addJob(cellDetectionJob);
		}
		catch(RemoteException e)
		{
			throw new AddonException("Remote exception while creating job.", e);
		} catch (MeasurementRunningException e) {
			throw new AddonException("Could not initialize measurement since measurement is already running.", e);
		}
	}
}
