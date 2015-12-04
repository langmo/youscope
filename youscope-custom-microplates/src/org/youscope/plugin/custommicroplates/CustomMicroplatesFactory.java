/**
 * 
 */
package org.youscope.plugin.custommicroplates;

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
public class CustomMicroplatesFactory implements ToolAddonFactory
{

	/**
	 * The ID used by this addon to identify itself.
	 */
	public final static String addonID = "CSB::YouScopeCustomMicroplates";
	@Override
	public ToolAddon createToolAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(addonID.compareToIgnoreCase(ID) == 0)
			return new CustomMicroplates(client, server);
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
			return "Custom Microplates Configuration";
		return null;
	}

	@Override
	public ImageIcon getToolIcon(String toolID)
	{
		return ImageLoadingTools.getResourceIcon("icons/table.png", "Custom Microplate");
	}
}
