/**
 * 
 */
package ch.ethz.csb.youscope.addon.continousimaging;

import javax.swing.ImageIcon;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonFactory;
import ch.ethz.csb.youscope.client.uielements.ImageLoadingTools;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

/**
 * @author Moritz Lang
 */
public class ShortContinuousImagingJobConfigurationAddonFactory implements JobConfigurationAddonFactory
{
    @Override
	public JobConfigurationAddon createJobConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(ShortContinuousImagingJobConfiguration.TYPE_IDENTIFIER.equals(ID))
			return new ShortContinuousImagingJobConfigurationAddon(client, server);
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		return new String[]{ShortContinuousImagingJobConfiguration.TYPE_IDENTIFIER};
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		if(ShortContinuousImagingJobConfiguration.TYPE_IDENTIFIER.equals(ID))
			return true;
		return false;
	}

	@Override
	public String getJobName(String ID)
	{
		if(ShortContinuousImagingJobConfiguration.TYPE_IDENTIFIER.equals(ID))
			return "Imaging/Short Continuous";
		return null;
	}
	
	@Override
	public Class<? extends JobConfiguration> getConfigurationClass(String ID)
	{
		if(ShortContinuousImagingJobConfiguration.TYPE_IDENTIFIER.equals(ID))
			return ShortContinuousImagingJobConfiguration.class;
		return null;
	}

	@Override
	public ImageIcon getJobIcon(String ID)
	{
		return ImageLoadingTools.getResourceIcon("icons/camcorder.png", "Continuous Imaging Measurement");
	}
}
