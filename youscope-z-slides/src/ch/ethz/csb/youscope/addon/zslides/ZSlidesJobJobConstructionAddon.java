/**
 * 
 */
package ch.ethz.csb.youscope.addon.zslides;

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
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.FocusingJob;

/**
 * @author langmo
 * 
 */
class ZSlidesJobJobConstructionAddon implements JobConstructionAddon
{

	@Override
	public Job createJob(JobConfiguration generalJobConfiguration, ConstructionContext initializer, PositionInformation positionInformation) throws RemoteException, ConfigurationException, JobCreationException
	{
		if(!(generalJobConfiguration instanceof ZSlidesJobConfiguration))
		{
			throw new ConfigurationException("Configuration is not supported by this addon.");
		}
		ZSlidesJobConfiguration jobConfiguration = (ZSlidesJobConfiguration)generalJobConfiguration;
		
		CompositeJob overallJobContainer;
		try {
			overallJobContainer = initializer.getComponentProvider().createJob(positionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
		} catch (ComponentCreationException e1) {
			throw new JobCreationException("Z-stack jobs need the composite job plugin.", e1);
		}
			
		try
		{		
			double[] positions = jobConfiguration.getSlideZPositions();
			double currentPosition = 0;
			for(int i = 0; i < positions.length; i++)
			{
				PositionInformation jobPositionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_ZSTACK, i);
				
				CompositeJob jobContainer;
				try {
					jobContainer = initializer.getComponentProvider().createJob(jobPositionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
				} catch (ComponentCreationException e) {
					throw new JobCreationException("Z-stack jobs need the composite job plugin.", e);
				}
				
				// Set focus
				FocusingJob focusJob;
				try {
					focusJob = initializer.getComponentProvider().createJob(jobPositionInformation, FocusingJob.DEFAULT_TYPE_IDENTIFIER, FocusingJob.class);
				} catch (ComponentCreationException e) {
					throw new JobCreationException("Z-stack jobs need the focussing job plugin.", e);
				}
				
				if(jobConfiguration.getFocusConfiguration() != null)
				{
					focusJob.setFocusDevice(jobConfiguration.getFocusConfiguration().getFocusDevice());
					focusJob.setFocusAdjustmentTime(jobConfiguration.getFocusConfiguration().getAdjustmentTime());
				}
				else
				{
					focusJob.setFocusDevice(null);
					focusJob.setFocusAdjustmentTime(0);
				}
				focusJob.setPosition(positions[i] - currentPosition, true);
				jobContainer.addJob(focusJob);
				
				// Add all child jobs
				for(JobConfiguration childJobConfig : jobConfiguration.getJobs())
				{
					Job childJob;
					try {
						childJob = initializer.getComponentProvider().createJob(jobPositionInformation, childJobConfig);
					} catch (ComponentCreationException e) {
						throw new JobCreationException("Could not create child job.", e);
					}
					jobContainer.addJob(childJob);
				}
				
				currentPosition = positions[i];
				overallJobContainer.addJob(jobContainer);
			}
			
			// Reset focus
			FocusingJob focusJob;
			try {
				focusJob = initializer.getComponentProvider().createJob(positionInformation, FocusingJob.DEFAULT_TYPE_IDENTIFIER, FocusingJob.class);
			} catch (ComponentCreationException e) {
				throw new JobCreationException("Z-stack jobs need the focussing job plugin.", e);
			}
				
			if(jobConfiguration.getFocusConfiguration() != null)
			{
				focusJob.setFocusDevice(jobConfiguration.getFocusConfiguration().getFocusDevice());
				focusJob.setFocusAdjustmentTime(jobConfiguration.getFocusConfiguration().getAdjustmentTime());
			}
			else
			{
				focusJob.setFocusDevice(null);
				focusJob.setFocusAdjustmentTime(0);
			}
			focusJob.setPosition(- currentPosition, true);
			overallJobContainer.addJob(focusJob);
		}
		catch(MeasurementRunningException e)
		{
			throw new ConfigurationException("Newly created job already running.", e);
		}
			
		return overallJobContainer;
	}
}
