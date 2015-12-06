/**
 * 
 */
package org.youscope.plugin.multicamerastream;

import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author langmo
 *
 */
class MultiStream implements ToolAddonUI
{
	protected YouScopeServer server;
	protected YouScopeClient client;
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 */
	public MultiStream(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
	
	public final static String TYPE_IDENTIFIER = "CSB::YouScopeMultiStream";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Multi-Camera Stream", new String[]{"multi-cam"}, "icons/films.png");
	}
	
	@Override
	public void createUI(YouScopeFrame frame)
	{
		// Initialize frames.
		@SuppressWarnings("unused")
		CameraSelectionDialog cameraSelectionDialog = new CameraSelectionDialog(frame, server, client);
	}
}
