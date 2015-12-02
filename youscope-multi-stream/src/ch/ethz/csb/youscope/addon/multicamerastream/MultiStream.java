/**
 * 
 */
package ch.ethz.csb.youscope.addon.multicamerastream;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.tool.ToolAddon;

import ch.ethz.csb.youscope.shared.YouScopeServer;

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
