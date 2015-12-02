/**
 * 
 */
package ch.ethz.csb.youscope.addon.composedimaging;

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
public class ScanningJobConfigurationAddonFactory implements JobConfigurationAddonFactory
{
	@Override
	public JobConfigurationAddon createJobConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(ComposedImagingJobConfiguration.TYPE_IDENTIFIER.equals(ID))
			return new ComposedImagingJobConfigurationAddon(client, server);
		else if(PlateScanningJobConfiguration.TYPE_IDENTIFIER.equals(ID))
			return new PlateScanningJobConfigurationAddon(client, server);
		else if(StaggeringJobConfiguration.TYPE_IDENTIFIER.equals(ID))
			return new StaggeringJobConfigurationAddon(client, server);
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		return new String[]{ComposedImagingJobConfiguration.TYPE_IDENTIFIER, PlateScanningJobConfiguration.TYPE_IDENTIFIER, StaggeringJobConfiguration.TYPE_IDENTIFIER};
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		for(String addonID : getSupportedConfigurationIDs())
		{
			if(addonID.equals(ID))
				return true;
		}
		return false;
	}

	@Override
	public String getJobName(String ID)
	{
		if(ComposedImagingJobConfiguration.TYPE_IDENTIFIER.equals(ID))
			return "Imaging/Composed Imaging";
		else if(PlateScanningJobConfiguration.TYPE_IDENTIFIER.equals(ID))
			return "Containers/Plate Scanning";
		else if(StaggeringJobConfiguration.TYPE_IDENTIFIER.equals(ID))
			return "Containers/Staggering";
		else
			return null;
	}
	
	@Override
	public Class<? extends JobConfiguration> getConfigurationClass(String ID)
	{
		if(ComposedImagingJobConfiguration.TYPE_IDENTIFIER.compareToIgnoreCase(ID) == 0)
			return ComposedImagingJobConfiguration.class;
		else if(PlateScanningJobConfiguration.TYPE_IDENTIFIER.equals(ID))
			return PlateScanningJobConfiguration.class;
		else if(StaggeringJobConfiguration.TYPE_IDENTIFIER.equals(ID))
			return StaggeringJobConfiguration.class;
		else
			return null;
	}

	@Override
	public ImageIcon getJobIcon(String ID)
	{
		// TODO Add icon
		return null;
	}

}
