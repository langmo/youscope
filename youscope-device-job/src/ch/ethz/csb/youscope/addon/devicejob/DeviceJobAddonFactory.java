/**
 * 
 */
package ch.ethz.csb.youscope.addon.devicejob;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;
import ch.ethz.csb.youscope.addon.adapters.CustomAddonCreator;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.DeviceSettingJob;

/**
 * @author Moritz Lang
 */
public class DeviceJobAddonFactory extends AddonFactoryAdapter 
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
