/**
 * 
 */
package org.youscope.plugin.zslides;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.basicjobs.CompositeJob;
import org.youscope.common.job.basicjobs.FocusingJob;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class ZSlidesJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	private static final CustomAddonCreator<ZSlidesJobConfiguration,CompositeJob> CREATOR = new CustomAddonCreator<ZSlidesJobConfiguration, CompositeJob>()
	{

		@Override
		public CompositeJob createCustom(PositionInformation positionInformation,
				ZSlidesJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				CompositeJob overallJobContainer;
				try {
					overallJobContainer = constructionContext.getComponentProvider().createJob(positionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
				} catch (ComponentCreationException e1) {
					throw new AddonException("Z-stack jobs need the composite job plugin.", e1);
				}
							
				double[] positions = configuration.getSlideZPositions();
				double currentPosition = 0;
				for(int i = 0; i < positions.length; i++)
				{
					PositionInformation jobPositionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_ZSTACK, i);
					
					CompositeJob jobContainer;
					try {
						jobContainer = constructionContext.getComponentProvider().createJob(jobPositionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
					} catch (ComponentCreationException e) {
						throw new AddonException("Z-stack jobs need the composite job plugin.", e);
					}
					
					// Set focus
					FocusingJob focusJob;
					try {
						focusJob = constructionContext.getComponentProvider().createJob(jobPositionInformation, FocusingJob.DEFAULT_TYPE_IDENTIFIER, FocusingJob.class);
					} catch (ComponentCreationException e) {
						throw new AddonException("Z-stack jobs need the focussing job plugin.", e);
					}
					
					if(configuration.getFocusConfiguration() != null)
					{
						focusJob.setFocusDevice(configuration.getFocusConfiguration().getFocusDevice());
						focusJob.setFocusAdjustmentTime(configuration.getFocusConfiguration().getAdjustmentTime());
					}
					else
					{
						focusJob.setFocusDevice(null);
						focusJob.setFocusAdjustmentTime(0);
					}
					focusJob.setPosition(positions[i] - currentPosition, true);
					jobContainer.addJob(focusJob);
					
					// Add all child jobs
					for(JobConfiguration childJobConfig : configuration.getJobs())
					{
						Job childJob;
						try {
							childJob = constructionContext.getComponentProvider().createJob(jobPositionInformation, childJobConfig);
						} catch (ComponentCreationException e) {
							throw new AddonException("Could not create child job.", e);
						}
						jobContainer.addJob(childJob);
					}
					
					currentPosition = positions[i];
					overallJobContainer.addJob(jobContainer);
				}
				
				// Reset focus
				FocusingJob focusJob;
				try {
					focusJob = constructionContext.getComponentProvider().createJob(positionInformation, FocusingJob.DEFAULT_TYPE_IDENTIFIER, FocusingJob.class);
				} catch (ComponentCreationException e) {
					throw new AddonException("Z-stack jobs need the focussing job plugin.", e);
				}
					
				if(configuration.getFocusConfiguration() != null)
				{
					focusJob.setFocusDevice(configuration.getFocusConfiguration().getFocusDevice());
					focusJob.setFocusAdjustmentTime(configuration.getFocusConfiguration().getAdjustmentTime());
				}
				else
				{
					focusJob.setFocusDevice(null);
					focusJob.setFocusAdjustmentTime(0);
				}
				focusJob.setPosition(- currentPosition, true);
				overallJobContainer.addJob(focusJob);
				
					
				return overallJobContainer;
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create job due to remote exception.", e);
			} catch (MeasurementRunningException e) {
				throw new AddonException("Could not initialize newly created job since job is already running.", e);
			}
		}

		@Override
		public Class<CompositeJob> getComponentInterface() {
			return CompositeJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public ZSlidesJobAddonFactory()
	{
		super(ZSlidesJobConfigurationAddon.class, CREATOR, ZSlidesJobConfigurationAddon.getMetadata());
	}
}
