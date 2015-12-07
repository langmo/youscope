/**
 * 
 */
package org.youscope.plugin.livestream;

import java.awt.Component;

import org.youscope.addon.AddonException;
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
	private final YouScopeClient client;
	private final YouScopeServer server;
	private LiveStreamPanel liveStreamPanel = null;
	LiveStream(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		this.client = client;
		this.server = server;
	}

	public final static String TYPE_IDENTIFIER = "CSB::YouScopeLiveStream";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "YouScope LiveStream", new String[0], "icons/film.png");
	}

	@Override
	public YouScopeFrame toFrame() throws AddonException
	{
		liveStreamPanel = new LiveStreamPanel(client, server);
		liveStreamPanel.setAutoStartStream(true);
		liveStreamPanel.setUserChoosesFullScreen(true);
		return liveStreamPanel.toFrame();
	}

	@Override
	public Component toPanel(YouScopeFrame containingFrame) throws AddonException
	{
		liveStreamPanel = new LiveStreamPanel(client, server);
		liveStreamPanel.setAutoStartStream(true);
		liveStreamPanel.setUserChoosesFullScreen(false);
		containingFrame.addFrameListener(liveStreamPanel.getFrameListener());
		return liveStreamPanel;
	}

	@Override
	public ToolMetadata getAddonMetadata()
	{
		return getMetadata();
	}
}
