/**
 * 
 */
package ch.ethz.csb.youscope.addon.repeatjob;

import javax.swing.ImageIcon;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonFactory;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

/**
 * Job factory to create an UI to configure repeat jobs.
 * @author Moritz Lang
 *
 */
public class RepeatJobConfigurationAddonFactory implements JobConfigurationAddonFactory
{
	@Override
	public JobConfigurationAddon createJobConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(RepeatJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return new RepeatJobConfigurationAddon(client, server);
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		return new String[]{RepeatJobConfigurationDTO.TYPE_IDENTIFIER};
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
		if(RepeatJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return "Containers/Repeat Job";
		return null;
	}

	@Override
	public Class<? extends JobConfiguration> getConfigurationClass(String ID)
	{
		if(RepeatJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return RepeatJobConfigurationDTO.class;
		return null;
	}

	@Override
	public ImageIcon getJobIcon(String ID)
	{
		//TODO Add icon
		return null;
	}
}
