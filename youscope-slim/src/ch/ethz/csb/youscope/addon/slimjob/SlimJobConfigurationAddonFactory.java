/**
 * 
 */
package ch.ethz.csb.youscope.addon.slimjob;

import javax.swing.ImageIcon;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonFactory;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

/**
 * @author Moritz Lang
 */
public class SlimJobConfigurationAddonFactory implements JobConfigurationAddonFactory
{
    @Override
	public JobConfigurationAddon createJobConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(SlimJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return new SlimJobConfigurationAddon(client, server);
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		return new String[]{SlimJobConfigurationDTO.TYPE_IDENTIFIER};
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		if(SlimJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return true;
		return false;
	}

	@Override
	public String getJobName(String ID)
	{
		if(SlimJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return "Imaging/SLIM";
		return null;
	}
	
	@Override
	public Class<? extends JobConfiguration> getConfigurationClass(String ID)
	{
		if(SlimJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return SlimJobConfigurationDTO.class;
		return null;
	}

	@Override
	public ImageIcon getJobIcon(String ID)
	{
		// TODO Add icon
		return null;
	}
}
