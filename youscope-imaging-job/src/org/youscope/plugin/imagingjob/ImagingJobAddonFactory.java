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
package org.youscope.plugin.imagingjob;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.basicjobs.ImagingJob;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class ImagingJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public ImagingJobAddonFactory()
	{
		super(ImagingJobConfigurationAddon.class, CREATOR, ImagingJobConfigurationAddon.getMetadata());
	}
	
	private static final CustomAddonCreator<ImagingJobConfiguration, ImagingJob> CREATOR = new CustomAddonCreator<ImagingJobConfiguration,ImagingJob>()
	{
		@Override
		public ImagingJob createCustom(PositionInformation positionInformation, ImagingJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			ImagingJob job;
			try
			{
				job = new ImagingJobImpl(positionInformation);
				job.setChannel(configuration.getChannelGroup(), configuration.getChannel());
				job.setExposure(configuration.getExposure());
				job.setCamera(configuration.getCamera());
				if(configuration.isSaveImages())
				{
					job.addImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()));
					job.setImageDescription(configuration.getImageSaveName() + " (" + job.getImageDescription() + ")");
				}
			}
			catch(ComponentRunningException e)
			{
				throw new AddonException("Could not create imaging job since newly created job is already running.", e);
			} catch (RemoteException e) {
				throw new AddonException("Could not create imaging job due to remote exception.", e);
			}
			return job;
		}

		@Override
		public Class<ImagingJob> getComponentInterface() {
			return ImagingJob.class;
		}
	};
}
