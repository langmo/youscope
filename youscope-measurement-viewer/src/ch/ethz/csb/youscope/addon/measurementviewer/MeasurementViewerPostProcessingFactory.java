/**
 * 
 */
package ch.ethz.csb.youscope.addon.measurementviewer;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.postprocessing.MeasurementPostProcessorAddon;
import ch.ethz.csb.youscope.client.addon.postprocessing.MeasurementPostProcessorAddonFactory;
import ch.ethz.csb.youscope.shared.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public class MeasurementViewerPostProcessingFactory implements MeasurementPostProcessorAddonFactory
{

	@Override
	public MeasurementPostProcessorAddon createMeasurementConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server, String measurementFolder)
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
