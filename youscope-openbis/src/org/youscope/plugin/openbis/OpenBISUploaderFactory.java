/**
 * 
 */
package org.youscope.plugin.openbis;

import org.youscope.addon.postprocessing.PostProcessorAddon;
import org.youscope.addon.postprocessing.PostProcessorAddonFactory;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public class OpenBISUploaderFactory implements PostProcessorAddonFactory
{

	@Override
	public PostProcessorAddon createMeasurementConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server, String measurementFolder)
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
