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
package org.youscope.plugin.continousimaging;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.basicjobs.ContinuousImagingJob;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class ContinuousImagingJobFactory extends ComponentAddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public ContinuousImagingJobFactory()
	{
		addAddon(ContinuousImagingJob.DEFAULT_TYPE_IDENTIFIER, ContinuousImagingJobConfiguration.class, CREATOR_CONTINUOUS_IMAGING);
		addAddon(ShortContinuousImagingJobConfiguration.TYPE_IDENTIFIER, ShortContinuousImagingJobConfiguration.class, CREATOR_SHORT_CONTINUOUS_IMAGING);
	}
	
	private static final CustomAddonCreator<ContinuousImagingJobConfiguration, ContinuousImagingJob> CREATOR_CONTINUOUS_IMAGING = new CustomAddonCreator<ContinuousImagingJobConfiguration,ContinuousImagingJob>()
	{
		@Override
		public ContinuousImagingJob createCustom(PositionInformation positionInformation, ContinuousImagingJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			ContinuousImagingJobImpl job;
			try
			{
				job = new ContinuousImagingJobImpl(positionInformation);
				job.setCamera(configuration.getCamera() == null ? null : configuration.getCamera().getCameraDevice());
				if(configuration.getChannel() == null)
					job.setChannel(null, null);
				else
					job.setChannel(configuration.getChannel().getChannelGroup(), configuration.getChannel().getChannel());
				job.setExposure(configuration.getExposure());
				job.setBurstImaging(configuration.isBurstImaging());
				if(configuration.isSaveImages())
				{
					job.addImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()));
					job.setImageDescription(configuration.getImageSaveName() + " (" + job.getImageDescription() + ")");
				}
				
			}
			catch(ComponentRunningException e)
			{
				throw new AddonException("Could not create continuous imaging job since newly created job is already running.", e);
			} catch (RemoteException e) {
				throw new AddonException("Could not create continuous imaging job due to remote exception.", e);
			}
			return job;
		}

		@Override
		public Class<ContinuousImagingJob> getComponentInterface() {
			return ContinuousImagingJob.class;
		}
	};
	
	private static final CustomAddonCreator<ShortContinuousImagingJobConfiguration, ShortContinuousImagingJob> CREATOR_SHORT_CONTINUOUS_IMAGING = new CustomAddonCreator<ShortContinuousImagingJobConfiguration,ShortContinuousImagingJob>()
	{
		@Override
		public ShortContinuousImagingJob createCustom(PositionInformation positionInformation, ShortContinuousImagingJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			ShortContinuousImagingJobImpl job;
			try
			{
				job = new ShortContinuousImagingJobImpl(positionInformation);
				job.setCamera(configuration.getCamera() == null ? null : configuration.getCamera().getCameraDevice());
				if(configuration.getChannel() == null)
					job.setChannel(null, null);
				else
					job.setChannel(configuration.getChannel().getChannelGroup(), configuration.getChannel().getChannel());
				job.setExposure(configuration.getExposure());
				job.setImagingPeriod(configuration.getImagingPeriod());
				job.setNumImages(configuration.getNumImages());
				if(configuration.isSaveImages())
				{
					job.addImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()));
					job.setImageDescription(configuration.getImageSaveName() + " (" + job.getImageDescription() + ")");
				}
				
			}
			catch(ComponentRunningException e)
			{
				throw new AddonException("Could not create short continuous imaging job since newly created job is already running.", e);
			} catch (RemoteException e) {
				throw new AddonException("Could not create short continuous imaging job due to remote exception.", e);
			}
			return job;
		}

		@Override
		public Class<ShortContinuousImagingJob> getComponentInterface() {
			return ShortContinuousImagingJob.class;
		}
	};
}
