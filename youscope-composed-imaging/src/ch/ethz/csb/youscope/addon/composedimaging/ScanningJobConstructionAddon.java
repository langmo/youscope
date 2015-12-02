/**
 * 
 */
package ch.ethz.csb.youscope.addon.composedimaging;

import java.awt.Dimension;
import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.measurement.ComponentCreationException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.CompositeJob;

/**
 * @author Moritz Lang
 * 
 */
public class ScanningJobConstructionAddon implements JobConstructionAddon
{

	@Override
	public Job createJob(JobConfiguration generalJobConfiguration, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, ConfigurationException, JobCreationException
	{
		if(generalJobConfiguration instanceof ComposedImagingJobConfiguration)
		{
			return createComposedImagingJob((ComposedImagingJobConfiguration)generalJobConfiguration, initializer, positionInformation);
		}
		else if(generalJobConfiguration instanceof PlateScanningJobConfiguration)
		{
			return createPlateScanningJob((PlateScanningJobConfiguration)generalJobConfiguration, initializer, positionInformation);
		}
		else if(generalJobConfiguration instanceof StaggeringJobConfiguration)
		{
			return createStaggeringJob((StaggeringJobConfiguration)generalJobConfiguration, initializer, positionInformation);
		}
		else
		{
			throw new ConfigurationException("Configuration is not supported by this addon.");
		}
		
	}
	private Job createPlateScanningJob(PlateScanningJobConfiguration jobConfiguration, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, ConfigurationException, JobCreationException
	{
		PlateScanningJob job = new PlateScanningJobImpl(positionInformation);
		try
		{
			job.setDeltaX(jobConfiguration.getDeltaX());
			job.setDeltaY(jobConfiguration.getDeltaY());
			job.setNumTiles(new Dimension(jobConfiguration.getNumTilesX(), jobConfiguration.getNumTilesY()));
			
			for(int x = 0; x < jobConfiguration.getNumTilesX(); x++)
			{
				PositionInformation xPositionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_XTILE, x);
				for(int y = 0; y < jobConfiguration.getNumTilesY(); y++)
				{
					PositionInformation yPositionInformation = new PositionInformation(xPositionInformation, PositionInformation.POSITION_TYPE_YTILE, y);
					
					CompositeJob jobContainer;
					try {
						jobContainer = initializer.getComponentProvider().createJob(yPositionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
					} catch (ComponentCreationException e) {
						throw new JobCreationException("Plate scanning jobs need the composite job plugin.", e);
					}
			
					
					// Add all child jobs
					for(JobConfiguration childJobConfig : jobConfiguration.getJobs())
					{
						Job childJob;
						try {
							childJob = initializer.getComponentProvider().createJob(yPositionInformation, childJobConfig);
						} catch (ComponentCreationException e) {
							throw new JobCreationException("Could not create child job.", e);
						}
						jobContainer.addJob(childJob);
					}
					
					job.addJob(jobContainer);
				}
			}
			
		}
		catch(MeasurementRunningException e)
		{
			throw new JobCreationException("Newly created job already running.", e);
		}
		return job;
	}
	
	private Job createStaggeringJob(StaggeringJobConfiguration jobConfiguration, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, ConfigurationException, JobCreationException
	{
		StaggeringJob job = new StaggeringJobImpl(positionInformation);
		try
		{
			job.setDeltaX(jobConfiguration.getDeltaX());
			job.setDeltaY(jobConfiguration.getDeltaY());
			job.setNumTiles(new Dimension(jobConfiguration.getNumTilesX(), jobConfiguration.getNumTilesY()));
			job.setNumIterationsBreak(jobConfiguration.getNumIterationsBreak());
			job.setNumTilesPerIteration(jobConfiguration.getNumTilesPerIteration());
			
			for(int x = 0; x < jobConfiguration.getNumTilesX(); x++)
			{
				PositionInformation xPositionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_XTILE, x);
				for(int y = 0; y < jobConfiguration.getNumTilesY(); y++)
				{
					PositionInformation yPositionInformation = new PositionInformation(xPositionInformation, PositionInformation.POSITION_TYPE_YTILE, y);
					
					CompositeJob jobContainer;
					try {
						jobContainer = initializer.getComponentProvider().createJob(yPositionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
					} catch (ComponentCreationException e) {
						throw new JobCreationException("Plate scanning jobs need the composite job plugin.", e);
					}
						
					
					// Add all child jobs
					for(JobConfiguration childJobConfig : jobConfiguration.getJobs())
					{
						Job childJob;
						try {
							childJob = initializer.getComponentProvider().createJob(yPositionInformation, childJobConfig);
						} catch (ComponentCreationException e) {
							throw new JobCreationException("Could not create child jobs.", e);
						}
						jobContainer.addJob(childJob);
					}
					
					job.addJob(jobContainer);
				}
			}
			
		}
		catch(MeasurementRunningException e)
		{
			throw new ConfigurationException("Newly created job already running.", e);
		}
		return job;
	}
	private Job createComposedImagingJob(ComposedImagingJobConfiguration jobConfiguration, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, ConfigurationException
	{
		double dx = jobConfiguration.getPixelSize() * jobConfiguration.getNumPixels().width * (1 - jobConfiguration.getOverlap());
		double dy = jobConfiguration.getPixelSize() * jobConfiguration.getNumPixels().height * (1 - jobConfiguration.getOverlap());
		ComposedImagingJob job = new ComposedImagingJobImpl(positionInformation); 
		try
		{
			job.setChannel(jobConfiguration.getChannelGroup(), jobConfiguration.getChannel());
			job.setExposure(jobConfiguration.getExposure());
			job.setDeltaX(dx);
			job.setDeltaY(dy);
			job.setSubImageNumber(new Dimension(jobConfiguration.getNx(), jobConfiguration.getNy()));
				
			if(jobConfiguration.isSaveImages())
				job.addImageListener(initializer.getMeasurementSaver().getSaveImageListener(jobConfiguration.getImageSaveName()));
		}
		catch(MeasurementRunningException e)
		{
			throw new ConfigurationException("Newly created job already running.", e);
		}
		return job;
	}

	
}
