/**
 * 
 */
package ch.ethz.csb.youscope.addon.livestream;

import java.awt.Dimension;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.YouScopeFrameListener;
import ch.ethz.csb.youscope.client.addon.tool.ToolAddon;
import ch.ethz.csb.youscope.client.uielements.ContinousMeasurementAndControlsPanel;
import ch.ethz.csb.youscope.shared.YouScopeServer;

/**
 * @author Moritz Lang
 */
class LiveStreamOld implements YouScopeFrameListener, ToolAddon
{
	private ContinousMeasurementAndControlsPanel	mainPanel;

	private final YouScopeServer server;
	private final YouScopeClient client;
	
	
	LiveStreamOld(YouScopeClient client, YouScopeServer server)
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
		frame.setTitle("YouScope LiveStream");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(true);
		
		try
		{
			mainPanel = new ContinousMeasurementAndControlsPanel(client, server);
		}
		catch(Exception e)
		{
			frame.setToErrorState("Could not establish continuous measurement.", e);
			return;
		}

		frame.addFrameListener(this);
		frame.setContentPane(mainPanel);
		frame.setSize(new Dimension(800, 600));
		frame.setMaximum(true);
	}
}
