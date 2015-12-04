/**
 * 
 */
package org.youscope.plugin.devicejob;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.basicjobs.DeviceSettingJob;

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
			} catch (MeasurementRunningException e) {
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
