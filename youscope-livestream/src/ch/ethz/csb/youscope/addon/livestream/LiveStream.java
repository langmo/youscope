/**
 * 
 */
package ch.ethz.csb.youscope.addon.livestream;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.tool.ToolAddon;
import ch.ethz.csb.youscope.client.uielements.LiveStreamPanel;
import ch.ethz.csb.youscope.shared.YouScopeServer;

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
