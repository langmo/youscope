/**
 * 
 */
package org.youscope.plugin.multicolorstream;

import java.awt.Dimension;

import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 */
class MultiColorStream extends ToolAddonUIAdapter implements YouScopeFrameListener
{
	private MultiStreamAndControlsPanel	mainPanel;
	
	public final static String TYPE_IDENTIFIER = "YouScope.MultiColorLiveStream";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Multi-Color Stream", new String[]{"misc"}, "icons/film-cast.png");
	}
	
	MultiColorStream(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
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
	public java.awt.Component createUI() throws AddonException
	{
		setTitle("Multi-Color Stream");
		setResizable(true);
		setMaximizable(true);
		
		setPreferredSize(new Dimension(800, 600));
		try
		{
			mainPanel = new MultiStreamAndControlsPanel(getClient(), getServer());
		}
		catch(Exception e)
		{
			throw new AddonException("Could not establish continuous measurement.", e);
		}
		getContainingFrame().addFrameListener(this);
		
		return (mainPanel);
	}	
}
