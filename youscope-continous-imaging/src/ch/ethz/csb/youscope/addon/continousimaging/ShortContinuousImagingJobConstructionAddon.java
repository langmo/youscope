/**
 * 
 */
package ch.ethz.csb.youscope.addon.continousimaging;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;

/**
 * @author Moritz Lang
 * 
 */
public class ShortContinuousImagingJobConstructionAddon implements JobConstructionAddon
{

	@Override
	public Job createJob(JobConfiguration generalJobConfiguration, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, ConfigurationException, JobCreationException
	{
		if(!(generalJobConfiguration instanceof ShortContinuousImagingJobConfiguration))
		{
			throw new ConfigurationException("Configuration is not supported by this addon.");
		}
		ShortContinuousImagingJobConfiguration jobConfiguration = (ShortContinuousImagingJobConfiguration)generalJobConfiguration;
		
		
		ShortContinuousImagingJobImpl job = new ShortContinuousImagingJobImpl(positionInformation);
		try
		{
			job.setCamera(jobConfiguration.getCamera());
			job.setChannel(jobConfiguration.getChannelGroup(), jobConfiguration.getChannel());
			job.setExposure(jobConfiguration.getExposure());
			job.setImagingPeriod(jobConfiguration.getImagingPeriod());
			job.setNumImages(jobConfiguration.getNumImages());
			if(jobConfiguration.getSaveImages())
			{
				job.addImageListener(initializer.getMeasurementSaver().getSaveImageListener(jobConfiguration.getImageSaveName()));
				job.setImageDescription(jobConfiguration.getImageSaveName() + " (" + job.getImageDescription() + ")");
			}
			
		}
		catch(MeasurementRunningException e)
		{
			throw new ConfigurationException("Newly created job already running.", e);
		}
		return job;
	}
}
