/**
 * 
 */
package ch.ethz.csb.youscope.addon.openbis;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.postprocessing.MeasurementPostProcessorAddon;
import ch.ethz.csb.youscope.client.addon.postprocessing.MeasurementPostProcessorAddonFactory;
import ch.ethz.csb.youscope.shared.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public class OpenBISUploaderFactory implements MeasurementPostProcessorAddonFactory
{

	@Override
	public MeasurementPostProcessorAddon createMeasurementConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server, String measurementFolder)
	{
		if(OpenBISUploader.ADDON_ID.compareTo(ID) == 0)
		{
			return new OpenBISUploader(client, server, measurementFolder);
		}
		return null;
	}

	@Override
	public String[] getSupportedPostProcessorIDs()
	{
		return new String[]{OpenBISUploader.ADDON_ID};
	}

	@Override
	public boolean supportsPostProcessorID(String ID)
	{
		if(OpenBISUploader.ADDON_ID.compareTo(ID) == 0)
			return true;
		return false;
	}

	@Override
	public String getPostProcessorName(String ID)
	{
		if(OpenBISUploader.ADDON_ID.compareTo(ID) == 0)
			return "Upload to OpenBIS";
		return null;
	}

}
