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
package org.youscope.plugin.zslides;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.JobException;
import org.youscope.common.job.basicjobs.SimpleCompositeJob;
import org.youscope.common.job.basicjobs.FocusingJob;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class ZSlidesJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	private static final CustomAddonCreator<ZSlidesJobConfiguration,SimpleCompositeJob> CREATOR = new CustomAddonCreator<ZSlidesJobConfiguration, SimpleCompositeJob>()
	{

		@Override
		public SimpleCompositeJob createCustom(PositionInformation positionInformation,
				ZSlidesJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				SimpleCompositeJob overallJobContainer;
				try {
					overallJobContainer = constructionContext.getComponentProvider().createJob(positionInformation, SimpleCompositeJob.DEFAULT_TYPE_IDENTIFIER, SimpleCompositeJob.class);
				} catch (ComponentCreationException e1) {
					throw new AddonException("Z-stack jobs need the composite job plugin.", e1);
				}
							
				double[] positions = configuration.getSlideZPositions();
				double currentPosition = 0;
				for(int i = 0; i < positions.length; i++)
				{
					PositionInformation jobPositionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_ZSTACK, i);
					
					SimpleCompositeJob jobContainer;
					try {
						jobContainer = constructionContext.getComponentProvider().createJob(jobPositionInformation, SimpleCompositeJob.DEFAULT_TYPE_IDENTIFIER, SimpleCompositeJob.class);
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
					try {
						jobContainer.addJob(focusJob);
					} catch (JobException e1) {
						throw new AddonException("Could not add child job to job.", e1);
					}
					
					// Add all child jobs
					for(JobConfiguration childJobConfig : configuration.getJobs())
					{
						Job childJob;
						try {
							childJob = constructionContext.getComponentProvider().createJob(jobPositionInformation, childJobConfig);
						} catch (ComponentCreationException e) {
							throw new AddonException("Could not create child job.", e);
						}
						try {
							jobContainer.addJob(childJob);
						} catch (JobException e) {
							throw new AddonException("Could not add child job to job.", e);
						}
					}
					
					currentPosition = positions[i];
					try {
						overallJobContainer.addJob(jobContainer);
					} catch (JobException e) {
						throw new AddonException("Could not add child job to job.", e);
					}
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
				try {
					overallJobContainer.addJob(focusJob);
				} catch (JobException e) {
					throw new AddonException("Could not add child job to job.", e);
				}
				
					
				return overallJobContainer;
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create job due to remote exception.", e);
			} catch (ComponentRunningException e) {
				throw new AddonException("Could not initialize newly created job since job is already running.", e);
			}
		}

		@Override
		public Class<SimpleCompositeJob> getComponentInterface() {
			return SimpleCompositeJob.class;
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
