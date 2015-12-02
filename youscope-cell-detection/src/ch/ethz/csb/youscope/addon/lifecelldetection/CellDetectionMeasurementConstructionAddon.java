/**
 * 
 */
package ch.ethz.csb.youscope.addon.lifecelldetection;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.measurement.MeasurementConstructionAddon;
import ch.ethz.csb.youscope.shared.addon.celldetection.CellDetectionAddon;
import ch.ethz.csb.youscope.shared.addon.celldetection.CellDetectionConfiguration;
import ch.ethz.csb.youscope.shared.addon.celldetection.CellVisualizationAddon;
import ch.ethz.csb.youscope.shared.addon.celldetection.CellVisualizationConfiguration;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.configuration.RegularPeriod;
import ch.ethz.csb.youscope.shared.measurement.ComponentCreationException;
import ch.ethz.csb.youscope.shared.measurement.Measurement;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.ContinuousImagingJob;
import ch.ethz.csb.youscope.shared.measurement.task.MeasurementTask;

/**
 * @author langmo
 * 
 */
public class CellDetectionMeasurementConstructionAddon implements MeasurementConstructionAddon
{

	@Override
	public void initializeTasksOfMeasurement(Measurement measurement, MeasurementConfiguration measurementConfiguration, ConstructionContext constructionContext) throws ConfigurationException, RemoteException, JobCreationException
	{
		if(!(measurementConfiguration instanceof CellDetectionMeasurementConfiguration))
		{
			throw new ConfigurationException("Measurement configuration is not a multi camera continous imaging measurement.");
		}
		CellDetectionMeasurementConfiguration configuration = (CellDetectionMeasurementConfiguration)measurementConfiguration;
	
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
		} catch (ComponentCreationException e1) {
			throw new JobCreationException("Could not create cell detection algorithm.", e1);
		}
		 
		CellVisualizationAddon cellVisualizationAddon;
		if(visualizationAlgorithmConfiguration != null)
		{
			try {
				cellVisualizationAddon= constructionContext.getComponentProvider().createComponent(positionInformation, visualizationAlgorithmConfiguration, CellVisualizationAddon.class);
			} catch (ComponentCreationException e) {
				throw new JobCreationException("Could not create cell visualization algorithm.", e);
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
			ContinuousImagingJob continuousJob = constructionContext.getComponentProvider().createJob(new PositionInformation(), ContinuousImagingJob.DEFAULT_TYPE_IDENTIFIER, ContinuousImagingJob.class);
			if(continuousJob == null)
				throw new JobCreationException("Continuous life cell detection measurement needs the continuous imaging plugin.");
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
		catch(Exception e)
		{
			throw new ConfigurationException("Error while creating job.", e);
		}
	}
}
