/**
 * 
 */
package ch.ethz.csb.youscope.addon.multicamerajob;

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
 * @author langmo
 * 
 */
class ParallelImagingJobConstructionAddon implements JobConstructionAddon
{

	@Override
	public Job createJob(JobConfiguration generalJobConfiguration, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, ConfigurationException, JobCreationException
	{
		if(generalJobConfiguration instanceof ParallelImagingJobConfigurationDTO)
		{
			ParallelImagingJobConfigurationDTO jobConfiguration = (ParallelImagingJobConfigurationDTO)generalJobConfiguration;
			ImagingJob job;
			try {
				job = initializer.getComponentProvider().createJob(positionInformation, ImagingJob.DEFAULT_TYPE_IDENTIFIER, ImagingJob.class);
			} catch (ComponentCreationException e1) {
				throw new JobCreationException("Parallel imaging jobs need the imaging job plugin.", e1);
			}
			
			try
			{
				job.setChannel(jobConfiguration.getChannelGroup(), jobConfiguration.getChannel());
				job.setCameras(jobConfiguration.getCameras());
				job.setExposures(jobConfiguration.getExposures());
				if(jobConfiguration.isSaveImages())
					job.addImageListener(initializer.getMeasurementSaver().getSaveImageListener(jobConfiguration.getImageSaveName()));
			}
			catch(MeasurementRunningException e)
			{
				throw new ConfigurationException("Newly created job already running.", e);
			}
			return job;
		}
		throw new ConfigurationException("Configuration is not supported by this addon.");
	}
}
