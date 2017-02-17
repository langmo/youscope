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
package org.youscope.plugin.devicejob;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.basicjobs.DeviceSettingJob;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class DeviceJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public DeviceJobAddonFactory()
	{
		super(DeviceJobConfigurationAddon.class, CREATOR, DeviceJobConfigurationAddon.getMetadata());
	}
	
	private static final CustomAddonCreator<DeviceJobConfiguration, DeviceSettingJob> CREATOR = new CustomAddonCreator<DeviceJobConfiguration,DeviceSettingJob>()
	{
		@Override
		public DeviceSettingJob createCustom(PositionInformation positionInformation, DeviceJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			DeviceSettingJob job;
			try {
				job = new DeviceJobImpl(positionInformation);
				job.setDeviceSettings(configuration.getDeviceSettings());
			}
			catch (RemoteException e1) 
			{
				throw new AddonException("Could not create device job due to remote error.", e1);
			} catch (ComponentRunningException e) {
				throw new AddonException("Could not create device job since newly created job is already running.", e);
			}
			return job;
		}

		@Override
		public Class<DeviceSettingJob> getComponentInterface() {
			return DeviceSettingJob.class;
		}
	};
}
