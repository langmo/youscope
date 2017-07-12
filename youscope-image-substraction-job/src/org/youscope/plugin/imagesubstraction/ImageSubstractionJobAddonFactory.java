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
package org.youscope.plugin.imagesubstraction;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class ImageSubstractionJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	private static final CustomAddonCreator<ImageSubstractionJobConfiguration, ImageSubstractionJob> CREATOR = new CustomAddonCreator<ImageSubstractionJobConfiguration, ImageSubstractionJob>()
	{

		@Override
		public ImageSubstractionJob createCustom(PositionInformation positionInformation,
				ImageSubstractionJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				ImageSubstractionJobImpl job = new ImageSubstractionJobImpl(positionInformation);
				
				if(configuration.getFocusConfiguration()==null)
				{
					job.setFocusDevice(null);
					job.setFocusAdjustmentTime(0);
				}
				else
				{
					job.setFocusDevice(configuration.getFocusConfiguration().getFocusDevice());
					job.setFocusAdjustmentTime(configuration.getFocusConfiguration().getAdjustmentTime());
				}
				job.setOffset1(configuration.getPosition1());
				job.setOffset2(configuration.getPosition2());
				job.setCamera(configuration.getCamera());
				
				job.setChannel(configuration.getChannelGroup(), configuration.getChannel());
				job.setExposure(configuration.getExposure());
				if(configuration.isSaveImages())
				{ 
					job.addImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()));
					job.setImageDescription(configuration.getImageSaveName() + " (" + job.getImageDescription() + ")");
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
		public Class<ImageSubstractionJob> getComponentInterface() {
			return ImageSubstractionJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public ImageSubstractionJobAddonFactory()
	{
		super(ImageSubstractionJobConfigurationAddon.class, CREATOR, ImageSubstractionJobConfigurationAddon.getMetadata());
	}
}
