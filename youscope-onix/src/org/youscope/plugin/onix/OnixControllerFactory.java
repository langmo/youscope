/**
 * 
 */
package org.youscope.plugin.onix;

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
public class OnixControllerFactory implements ToolAddonFactory
{

	/**
	 * The ID used by this addon to identify itself.
	 */
	public final static String addonID = "CSB::OnixController";
	@Override
	public ToolAddon createToolAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(addonID.compareToIgnoreCase(ID) == 0)
			return new OnixController(client, server);
		return null;
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
			return "Onix Controller";
		return null;
	}

	@Override
	public ImageIcon getToolIcon(String toolID)
	{
		return ImageLoadingTools.getResourceIcon("icons/beaker.png", "Onix Controller");
	}
}
