/**
 * 
 */
package ch.ethz.csb.youscope.addon.measurementviewer;

import javax.swing.ImageIcon;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.tool.ToolAddon;
import ch.ethz.csb.youscope.client.addon.tool.ToolAddonFactory;
import ch.ethz.csb.youscope.client.uielements.ImageLoadingTools;
import ch.ethz.csb.youscope.shared.YouScopeServer;

/**
 * @author langmo
 *
 */
public class MeasurementViewerFactory implements ToolAddonFactory
{

	/**
	 * The ID used by this addon to identify itself.
	 */
	public final static String ADDON_ID = "CSB::YouScopeMeasurementViewer::1.0";
	@Override
	public ToolAddon createToolAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		return new MeasurementViewer(client, server);
	}

	@Override
	public String[] getSupportedToolIDs()
	{
		return new String[] {ADDON_ID};
	}

	@Override
	public boolean supportsToolID(String ID)
	{
		if(ADDON_ID.compareToIgnoreCase(ID) == 0)
			return true;
		return false;
	}

	@Override
	public String getToolName(String ID)
	{
		if(ADDON_ID.compareToIgnoreCase(ID) == 0)
			return "Measurement Viewer";
		return null;
	}
	
	@Override
	public ImageIcon getToolIcon(String toolID)
	{
		return ImageLoadingTools.getResourceIcon("icons/eye.png", "MeasurementViewer");
	}
}
