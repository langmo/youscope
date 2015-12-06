/**
 * 
 */
package org.youscope.plugin.measurementviewer;

import org.youscope.addon.AddonException;
import org.youscope.addon.postprocessing.PostProcessorAddonFactory;
import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public class MeasurementViewerPostProcessingFactory implements PostProcessorAddonFactory
{

	@Override
	public ToolAddonUI createPostProcessorUI(String ID, YouScopeClient client, YouScopeServer server, String measurementFolder) throws AddonException
	{
		if(MeasurementViewer.TYPE_IDENTIFIER.equals(ID))
		{
			return new MeasurementViewer(client, server, measurementFolder);
		}
		return null;
	}

	@Override
	public String[] getSupportedPostProcessorIDs()
	{
		return new String[]{MeasurementViewer.TYPE_IDENTIFIER};		
	}

	@Override
	public boolean supportsPostProcessorID(String ID)
	{
		if(MeasurementViewer.TYPE_IDENTIFIER.equals(ID))
			return true;
		return false;
	}

	@Override
	public String getPostProcessorName(String ID)
	{
		if(MeasurementViewer.TYPE_IDENTIFIER.equals(ID))
			return "Open in Measurement Viewer";
		return null;
	}

}
