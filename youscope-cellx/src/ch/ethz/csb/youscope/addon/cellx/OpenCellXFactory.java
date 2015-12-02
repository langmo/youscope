/**
 * 
 */
package ch.ethz.csb.youscope.addon.cellx;

import java.awt.Desktop;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.postprocessing.MeasurementPostProcessorAddon;
import ch.ethz.csb.youscope.client.addon.postprocessing.MeasurementPostProcessorAddonFactory;
import ch.ethz.csb.youscope.shared.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public class OpenCellXFactory implements MeasurementPostProcessorAddonFactory
{

	@Override
	public MeasurementPostProcessorAddon createMeasurementConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server, String measurementFolder)
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
