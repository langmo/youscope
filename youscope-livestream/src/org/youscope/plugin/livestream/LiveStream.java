/**
 * 
 */
package org.youscope.plugin.livestream;

import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.LiveStreamPanel;

/**
 * @author Moritz Lang
 */
class LiveStream implements ToolAddonUI
{
	private LiveStreamPanel liveStreamPanel = null;
	private final YouScopeServer server;
	private final YouScopeClient client;
	
	LiveStream(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}

	public final static String TYPE_IDENTIFIER = "CSB::YouScopeLiveStream";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "YouScope LiveStream", new String[0], "icons/film.png");
	}
	
	@Override
	public void createUI(final YouScopeFrame frame)
	{
		liveStreamPanel = new LiveStreamPanel(client, server);
		liveStreamPanel.setAutoStartStream(true);
		liveStreamPanel.setUserChoosesFullScreen(true);
		frame.relocateFrameTo(liveStreamPanel.toFrame());
	}
}
