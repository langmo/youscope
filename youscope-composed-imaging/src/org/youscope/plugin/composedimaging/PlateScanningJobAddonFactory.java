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
package org.youscope.plugin.composedimaging;

import java.awt.Dimension;
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
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class PlateScanningJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public PlateScanningJobAddonFactory()
	{
		super(PlateScanningJobConfigurationAddon.class, CREATOR, PlateScanningJobConfigurationAddon.getMetadata());
	}
	
	private static final CustomAddonCreator<PlateScanningJobConfiguration, PlateScanningJob> CREATOR = new CustomAddonCreator<PlateScanningJobConfiguration,PlateScanningJob>()
	{
		@Override
		public PlateScanningJob createCustom(PositionInformation positionInformation, PlateScanningJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			PlateScanningJobImpl job;
			try
			{
				job = new PlateScanningJobImpl(positionInformation);
				job.setDeltaX(configuration.getDeltaX());
				job.setDeltaY(configuration.getDeltaY());
				job.setNumTiles(new Dimension(configuration.getNumTilesX(), configuration.getNumTilesY()));
				
				for(int x = 0; x < configuration.getNumTilesX(); x++)
				{
					PositionInformation xPositionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_XTILE, x);
					for(int y = 0; y < configuration.getNumTilesY(); y++)
					{
						PositionInformation yPositionInformation = new PositionInformation(xPositionInformation, PositionInformation.POSITION_TYPE_YTILE, y);
						
						SimpleCompositeJob jobContainer;
						try {
							jobContainer = constructionContext.getComponentProvider().createJob(yPositionInformation, SimpleCompositeJob.DEFAULT_TYPE_IDENTIFIER, SimpleCompositeJob.class);
						} catch (ComponentCreationException e) {
							throw new AddonException("Plate scanning jobs need the composite job plugin.", e);
						}
				
						
						// Add all child jobs
						for(JobConfiguration childJobConfig : configuration.getJobs())
						{
							Job childJob;
							try {
								childJob = constructionContext.getComponentProvider().createJob(yPositionInformation, childJobConfig);
							} catch (ComponentCreationException e) {
								throw new AddonException("Could not create child job.", e);
							}
							try {
								jobContainer.addJob(childJob);
							} catch (JobException e) {
								throw new AddonException("Could not add child job to job.", e);
							}
						}
						
						job.addJob(jobContainer);
					}
				}
				
				
			}
			catch(ComponentRunningException e)
			{
				throw new AddonException("Could not create job, since newly created job is already running.", e);
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create job due to remote error.", e);
			}
			return job;
		}

		@Override
		public Class<PlateScanningJob> getComponentInterface() {
			return PlateScanningJob.class;
		}
	};
}
