/**
 * 
 */
package org.youscope.plugin.cellx;

import java.awt.Desktop;

import org.youscope.addon.postprocessing.PostProcessorAddon;
import org.youscope.addon.postprocessing.PostProcessorAddonFactory;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public class OpenCellXFactory implements PostProcessorAddonFactory
{

	@Override
	public PostProcessorAddon createMeasurementConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server, String measurementFolder)
	{
		if(OpenCellX.ADDON_ID.compareTo(ID) == 0)
		{
			return new OpenCellX(client, server, measurementFolder);
		}
		return null;
	}

	@Override
	public String[] getSupportedPostProcessorIDs()
	{
		if(Desktop.isDesktopSupported())
			return new String[]{OpenCellX.ADDON_ID};
		return new String[0];
	}

	@Override
	public boolean supportsPostProcessorID(String ID)
	{
		if(OpenCellX.ADDON_ID.compareTo(ID) == 0)
			return true;
		return false;
	}

	@Override
	public String getPostProcessorName(String ID)
	{
		if(OpenCellX.ADDON_ID.compareTo(ID) == 0)
			return "Detect Cells (CellX)";
		return null;
	}

}
