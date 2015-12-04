/**
 * 
 */
package org.youscope.plugin.multicameraandcolorstream;

import java.awt.Dimension;

import org.youscope.addon.tool.ToolAddon;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 */
class MultiCameraAndColorStream implements YouScopeFrameListener, ToolAddon
{
	private MultiStreamAndControlsPanel	mainPanel;

	private final YouScopeServer server;
	private final YouScopeClient client;
	
	
	
	MultiCameraAndColorStream(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}

	@Override
	public void frameClosed()
	{
		mainPanel.stopMeasurement(false);
	}

	@Override
	public void frameOpened()
	{
		mainPanel.startMeasurement();
	}

	@Override
	public void createUI(YouScopeFrame frame)
	{
		frame.setTitle("Multi-Camera and -Color Stream");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(true);
		
		try
		{
			mainPanel = new MultiStreamAndControlsPanel(client, server);
		}
		catch(Exception e)
		{
			frame.setToErrorState("Could not establish continuous measurement.", e);
			return;
		}
		
		frame.setContentPane(mainPanel);
		frame.addFrameListener(this);
		frame.setSize(new Dimension(800, 600));
		frame.setMaximum(true);
	}	
}
