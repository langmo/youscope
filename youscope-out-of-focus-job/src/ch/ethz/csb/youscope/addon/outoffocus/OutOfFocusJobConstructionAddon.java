/**
 * 
 */
package ch.ethz.csb.youscope.addon.outoffocus;

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
 * @author langmo
 * 
 */
class OutOfFocusJobConstructionAddon implements JobConstructionAddon
{

	@Override
	public Job createJob(JobConfiguration generalJobConfiguration, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, ConfigurationException, JobCreationException
	{
		if(!(generalJobConfiguration instanceof OutOfFocusJobConfigurationDTO))
		{
			throw new ConfigurationException("Configuration is not supported by this addon.");
		}
		OutOfFocusJobConfigurationDTO jobConfiguration = (OutOfFocusJobConfigurationDTO)generalJobConfiguration;
		
		OutOfFocusJobImpl job = new OutOfFocusJobImpl(positionInformation);
		try
		{
			if(jobConfiguration.getFocusConfiguration()==null)
			{
				job.setFocusDevice(null);
				job.setFocusAdjustmentTime(0);
			}
			else
			{
				job.setFocusDevice(jobConfiguration.getFocusConfiguration().getFocusDevice());
				job.setFocusAdjustmentTime(jobConfiguration.getFocusConfiguration().getAdjustmentTime());
			}
			job.setOffset(jobConfiguration.getPosition());
			
			job.setChannel(jobConfiguration.getChannelGroup(), jobConfiguration.getChannel());
			job.setExposure(jobConfiguration.getExposure());
			if(jobConfiguration.isSaveImages())
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
