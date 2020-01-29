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
package org.youscope.plugin.lifecelldetection;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.celldetection.CellDetectionAddon;
import org.youscope.addon.celldetection.CellDetectionConfiguration;
import org.youscope.addon.celldetection.CellVisualizationAddon;
import org.youscope.addon.celldetection.CellVisualizationConfiguration;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageProducer;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class CellDetectionJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	private static final CustomAddonCreator<CellDetectionJobConfiguration, CellDetectionJob> CREATOR = new CustomAddonCreator<CellDetectionJobConfiguration, CellDetectionJob>()
	{

		@Override
		public CellDetectionJob createCustom(PositionInformation positionInformation,
				CellDetectionJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{			
				// Get detection & visualization algorithms.
				CellDetectionConfiguration detectionAlgorithmConfiguration = configuration.getDetectionAlgorithmConfiguration();
				CellVisualizationConfiguration visualizationAlgorithmConfiguration = configuration.getVisualizationAlgorithmConfiguration();
				if(detectionAlgorithmConfiguration == null)
				{
					throw new ConfigurationException("Configuration of cell detection algorithm is null.");
				}
				CellDetectionAddon cellDetectionAddon = constructionContext.getComponentProvider().createComponent(positionInformation, detectionAlgorithmConfiguration, CellDetectionAddon.class);
				CellVisualizationAddon cellVisualizationAddon;
				if(visualizationAlgorithmConfiguration != null)
				{
					cellVisualizationAddon = constructionContext.getComponentProvider().createComponent(positionInformation, visualizationAlgorithmConfiguration, CellVisualizationAddon.class);
					
				}
				else
					cellVisualizationAddon = null;
				 
				CellDetectionJobImpl cellDetectionJob = new CellDetectionJobImpl(positionInformation);
				cellDetectionJob.addMessageListener(constructionContext.getLogger());
				cellDetectionJob.setDetectionAlgorithm(cellDetectionAddon);
				cellDetectionJob.setVisualizationAlgorithm(cellVisualizationAddon);
				cellDetectionJob.setMinimalTimeMS(configuration.getMinimalTimeMS());
							
				// Detection Image producing job
				JobConfiguration detectionImageProducerConfig = configuration.getDetectionJob();
				if(detectionImageProducerConfig == null)
					throw new ConfigurationException("No image producing job defined to take the image used for detection.");
				Job detectionImageProducer = constructionContext.getComponentProvider().createJob(positionInformation, configuration.getDetectionJob());
				if(detectionImageProducer == null)
					throw new ConfigurationException("Type of job defined to take the image used for detection (" + detectionImageProducerConfig.getTypeIdentifier() + ") unknown.");
				if(!(detectionImageProducer instanceof ImageProducer))
						throw new ConfigurationException("Job defined to take the image used for detection (" + detectionImageProducerConfig.getTypeIdentifier() + ") does not implement interface ImageProducer.");
				
				// Add image save listeners
				if(configuration.getSegmentationImageSaveName() != null)
				{
					cellDetectionJob.addSegmentationImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getSegmentationImageSaveName()));
				} 
				if(configuration.getControlImageSaveName() != null)
				{
					cellDetectionJob.addControlImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getControlImageSaveName()));
				}
				
				// Change image name for display
				if(configuration.getControlImageSaveName() != null && configuration.getVisualizationAlgorithmConfiguration() != null)
				{
					cellDetectionJob.setImageDescription(configuration.getControlImageSaveName() + " (cell-detection)");
				}
				else if(configuration.getVisualizationAlgorithmConfiguration() != null)
				{
					cellDetectionJob.setImageDescription("cell-detection");
				}
				else if(configuration.getSegmentationImageSaveName() != null) 
				{
					cellDetectionJob.setImageDescription(configuration.getSegmentationImageSaveName() + " (cell-detection)");
				} 
				else
				{
					cellDetectionJob.setImageDescription("cell-detection");
				}
				
				if(configuration.getCellTableSaveName() != null)
				{
					cellDetectionJob.addTableListener(constructionContext.getMeasurementSaver().getSaveTableListener(configuration.getCellTableSaveName()));
				}
				cellDetectionJob.addJob(detectionImageProducer);
				
				// other image producing jobs
				for(JobConfiguration childJobConfig : configuration.getJobs())
				{
					Job childJob = constructionContext.getComponentProvider().createJob(positionInformation, childJobConfig);
					if(!(childJob instanceof ImageProducer))
						throw new ConfigurationException("Job defined to take the image used for quantification (" + childJobConfig.getTypeIdentifier() + ") does not implement interface ImageProducer.");
				
					cellDetectionJob.addJob(childJob);
				}

				return cellDetectionJob;
				
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create job due to remote exception.", e);
			} catch (ComponentCreationException e) {
				throw new AddonException("Could not create all components of job.", e);
			} catch (ComponentRunningException e) {
				throw new AddonException("Could not initialize newly created job since job is already running.", e);
			}
		}

		@Override
		public Class<CellDetectionJob> getComponentInterface() {
			return CellDetectionJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public CellDetectionJobAddonFactory()
	{
		super(CellDetectionJobAddonUI.class, CREATOR, CellDetectionJobAddonUI.getMetadata());
	}
}
