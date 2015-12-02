/**
 * 
 */
package ch.ethz.csb.youscope.addon.livestream;

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
public class LiveStreamFactory implements ToolAddonFactory
{

	/**
	 * The ID used by this addon to identify itself.
	 */
	public final static String addonID = "CSB::YouScopeLiveStream";
	/**
	 * The old live stream.
	 */
	public final static String addonIDOld = "CSB::YouScopeLiveStreamOld";
	@Override
	public ToolAddon createToolAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(addonID.equals(ID))
			return new LiveStream(client, server);
		else if(addonIDOld.equals(ID))
			return new LiveStreamOld(client, server);
		return null;
	}

	@Override
	public String[] getSupportedToolIDs()
	{
		return new String[] {addonID,addonIDOld};
	}

	@Override
	public boolean supportsToolID(String ID)
	{
		if(addonID.equals(ID) || addonIDOld.equals(ID))
			return true;
		return false;
	}

	@Override
	public String getToolName(String ID)
	{
		if(addonID.equals(ID))
			return "YouScope LiveStream";
		else if(addonIDOld.equals(ID))
			return "YouScope LiveStream (old)";
		return null;
	}

	@Override
	public ImageIcon getToolIcon(String toolID)
	{
		return ImageLoadingTools.getResourceIcon("icons/film.png", "LiveStream");
	}
}
