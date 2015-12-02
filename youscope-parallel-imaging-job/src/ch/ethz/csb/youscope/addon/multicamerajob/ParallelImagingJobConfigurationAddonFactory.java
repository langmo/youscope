/**
 * 
 */
package ch.ethz.csb.youscope.addon.multicamerajob;

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
public class ParallelImagingJobConfigurationAddonFactory implements JobConfigurationAddonFactory
{
	@Override
	public JobConfigurationAddon createJobConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(ParallelImagingJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return new ParallelImagingJobConfigurationAddon(client, server);
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		return new String[]{ParallelImagingJobConfigurationDTO.TYPE_IDENTIFIER};
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
		if(ParallelImagingJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return "Multi-Camera/Multi-Camera Imaging";
		return null;
	}
	
	@Override
	public Class<? extends JobConfiguration> getConfigurationClass(String ID)
	{
		if(ParallelImagingJobConfigurationDTO.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return ParallelImagingJobConfigurationDTO.class;
		return null;
	}

	@Override
	public ImageIcon getJobIcon(String ID)
	{
		// TODO Add icon
		return null;
	}
}
