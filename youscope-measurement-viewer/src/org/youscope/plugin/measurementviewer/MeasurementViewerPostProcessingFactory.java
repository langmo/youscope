/**
 * 
 */
package org.youscope.plugin.measurementviewer;

import org.youscope.addon.postprocessing.PostProcessorAddon;
import org.youscope.addon.postprocessing.PostProcessorAddonFactory;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public class MeasurementViewerPostProcessingFactory implements PostProcessorAddonFactory
{

	@Override
	public PostProcessorAddon createMeasurementConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server, String measurementFolder)
	{
		if(MeasurementViewerFactory.ADDON_ID.compareTo(ID) == 0)
		{
			return new MeasurementViewer(client, server, measurementFolder);
		}
		return null;
	}

	@Override
	public String[] getSupportedPostProcessorIDs()
	{
		return new String[]{MeasurementViewerFactory.ADDON_ID};		
	}

	@Override
	public boolean supportsPostProcessorID(String ID)
	{
		if(MeasurementViewerFactory.ADDON_ID.compareTo(ID) == 0)
			return true;
		return false;
	}

	@Override
	public String getPostProcessorName(String ID)
	{
		if(MeasurementViewerFactory.ADDON_ID.compareTo(ID) == 0)
			return "Open in Measurement Viewer";
		return null;
	}

}
