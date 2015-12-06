/**
 * 
 */
package org.youscope.plugin.livestream;

import java.awt.Dimension;

import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 */
class LiveStreamOld implements YouScopeFrameListener, ToolAddonUI
{
	private ContinousMeasurementAndControlsPanel	mainPanel;

	private final YouScopeServer server;
	private final YouScopeClient client;
	
	
	LiveStreamOld(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}

	public final static String TYPE_IDENTIFIER = "CSB::YouScopeLiveStreamOld ";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Old LiveStream", new String[]{"misc"}, "icons/film.png");
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
