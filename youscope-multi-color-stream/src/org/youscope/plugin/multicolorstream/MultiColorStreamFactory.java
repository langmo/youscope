/**
 * 
 */

package org.youscope.plugin.multicolorstream;
import javax.swing.ImageIcon;

import org.youscope.addon.tool.ToolAddon;
import org.youscope.addon.tool.ToolAddonFactory;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;

/**
 * @author langmo
 *
 */
public class MultiColorStreamFactory implements ToolAddonFactory
{

	/**
	 * The ID used by this addon to identify itself.
	 */
	public final static String addonID = "CSB::MultiColorLiveStream";
	@Override
	public ToolAddon createToolAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		return new MultiColorStream(client, server);
	}

	@Override
	public String[] getSupportedToolIDs()
	{
		return new String[] {addonID};
	}

	@Override
	public boolean supportsToolID(String ID)
	{
		if(addonID.compareToIgnoreCase(ID) == 0)
			return true;
		return false;
	}

	@Override
	public String getToolName(String ID)
	{
		if(addonID.compareToIgnoreCase(ID) == 0)
			return "Multi-Color Stream";
		return null;
	}

	@Override
	public ImageIcon getToolIcon(String toolID)
	{
		return ImageLoadingTools.getResourceIcon("icons/film-cast.png", "Multi-Color Stream");
	}
}
