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
package org.youscope.plugin.multicamerajob;

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
public class ParallelImagingJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	private static final CustomAddonCreator<ParallelImagingJobConfiguration, ImagingJob> CREATOR = new CustomAddonCreator<ParallelImagingJobConfiguration, ImagingJob>()
	{

		@Override
		public ImagingJob createCustom(PositionInformation positionInformation,
				ParallelImagingJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				ImagingJob job;
				try {
					job = constructionContext.getComponentProvider().createJob(positionInformation, ImagingJob.DEFAULT_TYPE_IDENTIFIER, ImagingJob.class);
				} catch (ComponentCreationException e1) {
					throw new AddonException("Parallel imaging jobs need the imaging job plugin.", e1);
				}
				
				job.setChannel(configuration.getChannelGroup(), configuration.getChannel());
				job.setCameras(configuration.getCameras());
				job.setExposures(configuration.getExposures());
				if(configuration.isSaveImages())
					job.addImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()));
				
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
		public Class<ImagingJob> getComponentInterface() {
			return ImagingJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public ParallelImagingJobAddonFactory()
	{
		super(ParallelImagingJobConfigurationAddon.class, CREATOR, ParallelImagingJobConfigurationAddon.getMetadata());
	}
}
