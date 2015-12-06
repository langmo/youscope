/**
 * 
 */
package org.youscope.plugin.livestream;

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
class LiveStreamOld extends ToolAddonUIAdapter implements YouScopeFrameListener
{
	private ContinousMeasurementAndControlsPanel	mainPanel;

	LiveStreamOld(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
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
	public java.awt.Component createUI() throws AddonException
	{
		setTitle("YouScope LiveStream");
		setResizable(true);
		setMaximizable(true);
		
		try
		{
			mainPanel = new ContinousMeasurementAndControlsPanel(getClient(), getServer());
		}
		catch(Exception e)
		{
			throw new AddonException("Could not establish continuous measurement.", e);
		}

		getContainingFrame().addFrameListener(this);
		setPreferredSize(new Dimension(800, 600));
		return mainPanel;
	}
}
