/**
 * 
 */
package org.youscope.plugin.multicamerastream;

import org.youscope.addon.tool.ToolAddon;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author langmo
 *
 */
class MultiStream implements ToolAddon
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
	@Override
	public void createUI(YouScopeFrame frame)
	{
		// Initialize frames.
		@SuppressWarnings("unused")
		CameraSelectionDialog cameraSelectionDialog = new CameraSelectionDialog(frame, server, client);
	}
}
