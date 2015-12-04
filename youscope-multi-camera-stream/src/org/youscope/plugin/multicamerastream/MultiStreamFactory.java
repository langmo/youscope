/**
 * 
 */
package org.youscope.plugin.multicamerastream;

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
public class MultiStreamFactory implements ToolAddonFactory
{
	/**
	 * The ID used by this addon to identify itself.
	 */
	public final static String addonID = "CSB::YouScopeMultiStream";
	@Override
	public ToolAddon createToolAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		return new MultiStream(client, server);
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
			return "Multi-Camera/Multi-Camera Stream";
		return null;
	}
	
	@Override
	public ImageIcon getToolIcon(String toolID)
	{
		return ImageLoadingTools.getResourceIcon("icons/films.png", "Multi-Camera Stream");
	}
}
