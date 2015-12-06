/**
 * 
 */
package org.youscope.plugin.multicameraandcolorstream;

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
class MultiCameraAndColorStream implements YouScopeFrameListener, ToolAddonUI
{
	private MultiStreamAndControlsPanel	mainPanel;

	private final YouScopeServer server;
	private final YouScopeClient client;
	
	
	
	MultiCameraAndColorStream(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
	
	public final static String TYPE_IDENTIFIER = "CSB::MultiCameraAndColorStream";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Multi-Camera & -Color Stream", new String[]{"multi-cam"}, "icons/film-cast.png");
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
