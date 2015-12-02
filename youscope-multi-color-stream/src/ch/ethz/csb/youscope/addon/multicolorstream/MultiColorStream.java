/**
 * 
 */
package ch.ethz.csb.youscope.addon.multicolorstream;

import java.awt.Dimension;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.YouScopeFrameListener;
import ch.ethz.csb.youscope.client.addon.tool.ToolAddon;
import ch.ethz.csb.youscope.shared.YouScopeServer;

/**
 * @author Moritz Lang
 */
class MultiColorStream implements YouScopeFrameListener, ToolAddon
{
	private MultiStreamAndControlsPanel	mainPanel;

	private final YouScopeServer server;
	private final YouScopeClient client;
	
	
	
	MultiColorStream(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}

	@Override
	public void frameClosed()
	{
		mainPanel.stopMeasurement();
	}

	@Override
	public void frameOpened()
	{
		mainPanel.startMeasurement();
	}

	@Override
	public void createUI(YouScopeFrame frame)
	{
		frame.setTitle("Multi-Color Stream");
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
