/**
 * 
 */
package ch.ethz.csb.youscope.addon.openmeasurementfolder;

import java.awt.Desktop;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.postprocessing.MeasurementPostProcessorAddon;
import ch.ethz.csb.youscope.client.addon.postprocessing.MeasurementPostProcessorAddonFactory;
import ch.ethz.csb.youscope.shared.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public class OpenMeasurementFolderFactory implements MeasurementPostProcessorAddonFactory
{

	@Override
	public MeasurementPostProcessorAddon createMeasurementConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server, String measurementFolder)
	{
		if(OpenMeasurementFolder.ADDON_ID.compareTo(ID) == 0)
		{
			return new OpenMeasurementFolder(client, server, measurementFolder);
		}
		return null;
	}

	@Override
	public String[] getSupportedPostProcessorIDs()
	{
		if(Desktop.isDesktopSupported())
			return new String[]{OpenMeasurementFolder.ADDON_ID};
		return new String[0];
	}

	@Override
	public boolean supportsPostProcessorID(String ID)
	{
		if(OpenMeasurementFolder.ADDON_ID.compareTo(ID) == 0)
			return true;
		return false;
	}

	@Override
	public String getPostProcessorName(String ID)
	{
		if(OpenMeasurementFolder.ADDON_ID.compareTo(ID) == 0)
			return "Open Folder";
		return null;
	}

}
