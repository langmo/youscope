/**
 * 
 */
package ch.ethz.csb.youscope.addon.microplatejob;

import javax.swing.ImageIcon;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonFactory;
import ch.ethz.csb.youscope.client.uielements.ImageLoadingTools;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

/**
 * The factory to create the UI for the configuration of microplate jobs.
 * @author Moritz Lang
 */
public class MicroplateJobConfigurationAddonFactory implements JobConfigurationAddonFactory
{
    @Override
	public JobConfigurationAddon createJobConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(MicroplateJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return new MicroplateJobConfigurationAddon(client, server);
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		return new String[]{MicroplateJobConfigurationDTO.TYPE_IDENTIFIER};
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		if(MicroplateJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return true;
		return false;
	}

	@Override
	public String getJobName(String ID)
	{
		if(MicroplateJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return "Containers/Microplate";
		return null;
	}
	
	@Override
	public Class<? extends JobConfiguration> getConfigurationClass(String ID)
	{
		if(MicroplateJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return MicroplateJobConfigurationDTO.class;
		return null;
	}

	@Override
	public ImageIcon getJobIcon(String ID)
	{
		if(supportsConfigurationID(ID))
			return ImageLoadingTools.getResourceIcon("icons/map.png", "Microplate Job");
		return null;
	}
}
