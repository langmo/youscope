/**
 * 
 */
package ch.ethz.csb.youscope.addon.positioncontrol;

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
public class PositionControlFactory implements ToolAddonFactory
{

	/**
	 * The ID used by this addon to identify itself.
	 */
	public final static String addonID = "CSB::YouScopePositionControl";
	@Override
	public ToolAddon createToolAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		return new PositionControl(client, server);
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
			return "Stage and Focus Position";
		return null;
	}
	
	@Override
	public ImageIcon getToolIcon(String toolID)
	{
		return ImageLoadingTools.getResourceIcon("icons/map.png", "Stage and Focus Position");
	}
}
