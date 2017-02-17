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
package org.youscope.plugin.slimjob;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.basicjobs.ImagingJob;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class SlimJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	private static final CustomAddonCreator<SlimJobConfiguration, SlimJob> CREATOR = new CustomAddonCreator<SlimJobConfiguration, SlimJob>()
	{

		@Override
		public SlimJob createCustom(PositionInformation positionInformation,
				SlimJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				SlimJobImpl job = new SlimJobImpl(positionInformation);
		
				job.setReflectorDevice(configuration.getReflectorDevice());
				job.setAttenuationFactor(configuration.getAttenuationFactor());
				job.setMaskX(configuration.getMaskX());
				job.setMaskY(configuration.getMaskY());
				job.setInnerRadius(configuration.getInnerRadius());
				job.setOuterRadius(configuration.getOuterRadius());
				job.setMaskFileName(configuration.getMaskFileName());
				job.setPhaseShiftOutside(configuration.getPhaseShiftOutside());
				for(int i=0; i<4; i++)
				{
					job.setPhaseShiftMask(i, configuration.getPhaseShiftMask(i));
				}
				job.setSlimDelayMs(configuration.getSlimDelayMS());
				if(configuration.isSaveImages())
				{
					job.addImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()));
					job.setImageDescription(configuration.getImageSaveName() + " (" + job.getImageDescription() + ")");
				}
				// add imaging sub-jobs
				for(int i=0;i<4;i++)
				{
					ImagingJob subJob;
					try {
						subJob = constructionContext.getComponentProvider().createJob(positionInformation, ImagingJob.DEFAULT_TYPE_IDENTIFIER, ImagingJob.class);
					} catch (ComponentCreationException e) {
						throw new AddonException("Could not create child job.", e);
					}
					subJob.setCamera(configuration.getCamera());
					subJob.setChannel(configuration.getChannelGroup(), configuration.getChannel());
					subJob.setExposure(configuration.getExposure());
					if(configuration.isSaveImages())
					{
						subJob.addImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()+"_mask"+Integer.toString(i+1)));
						subJob.setImageDescription(configuration.getImageSaveName()+"_mask"+Integer.toString(i+1) + " (" + subJob.getImageDescription() + ")");
					}
					job.addJob(subJob);
				}
				return job;
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create job due to remote exception.", e);
			} catch (ComponentRunningException e) {
				throw new AddonException("Could not initialize newly created job since job is already running.", e);
			}
		}

		@Override
		public Class<SlimJob> getComponentInterface() {
			return SlimJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public SlimJobAddonFactory()
	{
		super(SlimJobConfigurationAddon.class, CREATOR, SlimJobConfigurationAddon.getMetadata());
	}
}
