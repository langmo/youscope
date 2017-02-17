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
package org.youscope.plugin.autofocus;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.addon.focusscore.FocusScoreResource;
import org.youscope.addon.focussearch.FocusSearchResource;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class AutoFocusJobFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public AutoFocusJobFactory()
	{
		super(AutoFocusJobConfigurationAddon.class, CREATOR, AutoFocusJobConfigurationAddon.getMetadata());
	}
	
	private static final CustomAddonCreator<AutoFocusJobConfiguration,AutoFocusJob> CREATOR = new CustomAddonCreator<AutoFocusJobConfiguration,AutoFocusJob>()
	{
		@Override
		public AutoFocusJob createCustom(PositionInformation positionInformation, AutoFocusJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			AutoFocusJob job;
			try
			{
				job = new AutoFocusJobImpl(positionInformation);
				// Configure focussing
				if(configuration.getFocusConfiguration() != null)
				{
					job.setFocusDevice(configuration.getFocusConfiguration().getFocusDevice());
					job.setFocusAdjustmentTime(configuration.getFocusConfiguration().getAdjustmentTime());
				}
				else
				{
					job.setFocusDevice(null);
					job.setFocusAdjustmentTime(0);
				}
				
				// configure imaging
				job.setChannel(configuration.getChannelGroup(), configuration.getChannel());
				job.setExposure(configuration.getExposure());
				if(configuration.getImageSaveName() != null)
				{
					job.addImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()));
					job.setImageDescription(configuration.getImageSaveName() + " (" + job.getImageDescription() + ")");
				}
				
				job.addMessageListener(constructionContext.getLogger());
				job.setRememberFocus(configuration.isRememberFocus());
				job.setResetFocusAfterSearch(configuration.isResetFocusAfterSearch());
				
				// add child jobs
				for(JobConfiguration childJobConfig : configuration.getJobs())
				{
					try
					{
						Job childJob = constructionContext.getComponentProvider().createJob(positionInformation, childJobConfig);
						job.addJob(childJob);
					}
					catch(Exception e)
					{
						throw new AddonException("Could not create child job.", e);
					}
				}
				
				// set focus search
				job.setResetFocusAfterSearch(configuration.isResetFocusAfterSearch());
				if(configuration.getFocusTableSaveName() != null)
				{
					job.addTableListener(constructionContext.getMeasurementSaver().getSaveTableListener(configuration.getFocusTableSaveName()));
				}
				
				if(configuration.getFocusScoreAlgorithm() == null)
					throw new ConfigurationException("No focus score algorithm defined.");
				FocusScoreResource focusScoreAlgorithm;
				try 
				{
					focusScoreAlgorithm = constructionContext.getComponentProvider().createComponent(positionInformation, configuration.getFocusScoreAlgorithm(), FocusScoreResource.class);
				} 
				catch (ComponentCreationException e) 
				{
					throw new AddonException("Could not create focus score algorithm.", e);
				}
				job.setFocusScoreAlgorithm(focusScoreAlgorithm);
				
				
				if(configuration.getFocusSearchAlgorithm() == null)
					throw new ConfigurationException("No focus search algorithm defined.");
				FocusSearchResource focusSearchAlgorithm;
				try {
					focusSearchAlgorithm = constructionContext.getComponentProvider().createComponent(positionInformation, configuration.getFocusSearchAlgorithm(), FocusSearchResource.class);
				} 
				catch (ComponentCreationException e) 
				{
					throw new AddonException("Could not create focus search algorithm.", e);
				}
				job.setFocusSearchAlgorithm(focusSearchAlgorithm);
				
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
		public Class<AutoFocusJob> getComponentInterface() {
			return AutoFocusJob.class;
		}
	};
}
