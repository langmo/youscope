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
package org.youscope.plugin.onoffdevicejob;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.basicjobs.SimpleCompositeJob;
import org.youscope.common.job.basicjobs.DeviceSettingJob;
import org.youscope.common.job.basicjobs.WaitJob;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class OnOffDeviceJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public OnOffDeviceJobAddonFactory()
	{
		super(OnOffDeviceJobConfigurationAddon.class, CREATOR, OnOffDeviceJobConfigurationAddon.getMetadata());
	}
	
	private static final CustomAddonCreator<OnOffDeviceJobConfiguration, SimpleCompositeJob> CREATOR = new CustomAddonCreator<OnOffDeviceJobConfiguration,SimpleCompositeJob>()
	{
		@Override
		public SimpleCompositeJob createCustom(PositionInformation positionInformation, OnOffDeviceJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			SimpleCompositeJob compositeJob;
			try
			{
				compositeJob = constructionContext.getComponentProvider().createJob(positionInformation, SimpleCompositeJob.DEFAULT_TYPE_IDENTIFIER, SimpleCompositeJob.class);
			
				if(configuration.getDeviceSettingsOn() != null && configuration.getDeviceSettingsOn().length > 0)
				{
					DeviceSettingJob job = constructionContext.getComponentProvider().createJob(positionInformation,DeviceSettingJob.DEFAULT_TYPE_IDENTIFIER, DeviceSettingJob.class);
					job.setDeviceSettings(configuration.getDeviceSettingsOn());
					compositeJob.addJob(job);
				}
				if(configuration.getExposure() > 0.0)
				{
					WaitJob job = constructionContext.getComponentProvider().createJob(positionInformation, WaitJob.DEFAULT_TYPE_IDENTIFIER, WaitJob.class);
					job.setWaitTime((long)configuration.getExposure());
					compositeJob.addJob(job);
				}
				if(configuration.getDeviceSettingsOff() != null && configuration.getDeviceSettingsOff().length > 0)
				{
					DeviceSettingJob job = constructionContext.getComponentProvider().createJob(positionInformation, DeviceSettingJob.DEFAULT_TYPE_IDENTIFIER, DeviceSettingJob.class);
					job.setDeviceSettings(configuration.getDeviceSettingsOff());
					compositeJob.addJob(job);
				}
			}
			catch(Exception e)
			{
				throw new AddonException("Could not create job.", e);
			}
			return compositeJob;
		}

		@Override
		public Class<SimpleCompositeJob> getComponentInterface() {
			return SimpleCompositeJob.class;
		}
	};
}
