/**
 * 
 */
package ch.ethz.csb.youscope.addon.deviceslides;

import javax.swing.ImageIcon;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonFactory;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

/**
 * @author langmo
 *
 */
public class DeviceSlidesJobConfigurationAddonFactory implements JobConfigurationAddonFactory
{
	@Override
	public JobConfigurationAddon createJobConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(DeviceSlidesJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return new DeviceSlidesJobConfigurationAddon(client, server);
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		return new String[]{DeviceSlidesJobConfigurationDTO.TYPE_IDENTIFIER};
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		for(String addonID : getSupportedConfigurationIDs())
		{
			if(addonID.compareToIgnoreCase(ID) == 0)
				return true;
		}
		return false;
	}

	@Override
	public String getJobName(String ID)
	{
		if(DeviceSlidesJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return "Containers/Device-Slides";
		return null;
	}
	
	@Override
	public Class<? extends JobConfiguration> getConfigurationClass(String ID)
	{
		if(DeviceSlidesJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return DeviceSlidesJobConfigurationDTO.class;
		return null;
	}

	@Override
	public ImageIcon getJobIcon(String ID)
	{
		// TODO Add icon
		return null;
	}

}
