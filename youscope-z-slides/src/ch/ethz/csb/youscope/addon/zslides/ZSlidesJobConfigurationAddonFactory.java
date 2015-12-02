/**
 * 
 */
package ch.ethz.csb.youscope.addon.zslides;

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
public class ZSlidesJobConfigurationAddonFactory implements JobConfigurationAddonFactory
{
	@Override
	public JobConfigurationAddon createJobConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(ZSlidesJobConfiguration.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return new ZSlidesJobConfigurationAddon(client, server);
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		return new String[]{ZSlidesJobConfiguration.TYPE_IDENTIFIER};
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
		if(ZSlidesJobConfiguration.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return "Containers/Z-Stack";
		return null;
	}

	@Override
	public Class<? extends JobConfiguration> getConfigurationClass(String ID)
	{
		if(ZSlidesJobConfiguration.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return ZSlidesJobConfiguration.class;
		return null;
	}

	@Override
	public ImageIcon getJobIcon(String ID)
	{
		// TODO Add icon
		return null;
	}
}
