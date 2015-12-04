/**
 * 
 */
package org.youscope.plugin.livestream;

import org.youscope.addon.tool.ToolAddon;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.LiveStreamPanel;

/**
 * @author Moritz Lang
 */
class LiveStream implements ToolAddon
{
	private LiveStreamPanel liveStreamPanel = null;
	private final YouScopeServer server;
	private final YouScopeClient client;
	
	LiveStream(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
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
