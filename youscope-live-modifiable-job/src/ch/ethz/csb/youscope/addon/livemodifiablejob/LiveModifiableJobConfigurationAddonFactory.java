/**
 * 
 */
package ch.ethz.csb.youscope.addon.livemodifiablejob;

import javax.swing.ImageIcon;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonFactory;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

/**
 * @author langmo
 */
public class LiveModifiableJobConfigurationAddonFactory implements JobConfigurationAddonFactory
{
    @Override
    public JobConfigurationAddon createJobConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server)
    {
        if (LiveModifiableJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
            return new LiveModifiableJobConfigurationAddon(client, server);
		return null;
    }

    @Override
    public String[] getSupportedConfigurationIDs()
    {
        return new String[] { LiveModifiableJobConfigurationDTO.TYPE_IDENTIFIER };
    }

    @Override
    public boolean supportsConfigurationID(String ID)
    {
        for (String addonID : getSupportedConfigurationIDs())
        {
            if (addonID.compareToIgnoreCase(ID) == 0)
                return true;
        }
        return false;
    }

    @Override
    public String getJobName(String ID)
    {
        if (LiveModifiableJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
            return "Misc/Live Modifiable Job";
		return null;
    }

    @Override
    public Class<? extends JobConfiguration> getConfigurationClass(String ID)
    {
        if (LiveModifiableJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
            return LiveModifiableJobConfigurationDTO.class;
		return null;
    }

    @Override
    public ImageIcon getJobIcon(String ID)
    {
        // TODO Add icon
        return null;
    }
}
