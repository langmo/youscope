/**
 * 
 */
package ch.ethz.csb.youscope.addon.slimjob;

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
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.ImagingJob;

/**
 * @author Moritz Lang
 * 
 */
public class SlimJobConstructionAddon implements JobConstructionAddon
{

	@Override
	public Job createJob(JobConfiguration generalJobConfiguration, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, ConfigurationException, JobCreationException
	{
		if(!(generalJobConfiguration instanceof SlimJobConfigurationDTO))
		{
			throw new ConfigurationException("Configuration is not supported by this addon.");
		}
		SlimJobConfigurationDTO jobConfiguration = (SlimJobConfigurationDTO)generalJobConfiguration;
		if(jobConfiguration.getReflectorDevice() == null)
			throw new ConfigurationException("The reflector device must be set.");
		
		SlimJobImpl job = new SlimJobImpl(positionInformation);
		try
		{
			job.setReflectorDevice(jobConfiguration.getReflectorDevice());
			job.setMaskX(jobConfiguration.getMaskX());
			job.setMaskY(jobConfiguration.getMaskY());
			job.setInnerRadius(jobConfiguration.getInnerRadius());
			job.setOuterRadius(jobConfiguration.getOuterRadius());
			job.setMaskFileName(jobConfiguration.getMaskFileName());
			job.setPhaseShiftOutside(jobConfiguration.getPhaseShiftOutside());
			for(int i=0; i<4; i++)
			{
				job.setPhaseShiftMask(i, jobConfiguration.getPhaseShiftMask(i));
			}
			job.setSlimDelayMs(jobConfiguration.getSlimDelayMS());
			if(jobConfiguration.isSaveImages())
			{
				job.addImageListener(initializer.getMeasurementSaver().getSaveImageListener(jobConfiguration.getImageSaveName()));
				job.setImageDescription(jobConfiguration.getImageSaveName() + " (" + job.getImageDescription() + ")");
			}
			// add imaging sub-jobs
			for(int i=0;i<4;i++)
			{
				ImagingJob subJob;
				try {
					subJob = initializer.getComponentProvider().createJob(positionInformation, ImagingJob.DEFAULT_TYPE_IDENTIFIER, ImagingJob.class);
				} catch (ComponentCreationException e) {
					throw new JobCreationException("Could not create child job.", e);
				}
				subJob.setCamera(jobConfiguration.getCamera());
				subJob.setChannel(jobConfiguration.getChannelGroup(), jobConfiguration.getChannel());
				subJob.setExposure(jobConfiguration.getExposure());
				if(jobConfiguration.isSaveImages())
				{
					subJob.addImageListener(initializer.getMeasurementSaver().getSaveImageListener(jobConfiguration.getImageSaveName()+"_mask"+Integer.toString(i+1)));
					subJob.setImageDescription(jobConfiguration.getImageSaveName()+"_mask"+Integer.toString(i+1) + " (" + subJob.getImageDescription() + ")");
				}
				job.addJob(subJob);
			}
		}
		catch(MeasurementRunningException e)
		{
			throw new JobCreationException("Newly created job already running.", e);
		}
		return job;
	}
}
