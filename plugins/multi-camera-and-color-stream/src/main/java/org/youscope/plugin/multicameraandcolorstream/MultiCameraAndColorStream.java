/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.plugin.multicameraandcolorstream;

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
class MultiCameraAndColorStream extends ToolAddonUIAdapter implements YouScopeFrameListener
{
	private MultiStreamAndControlsPanel	mainPanel;	
	
	MultiCameraAndColorStream(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	
	public final static String TYPE_IDENTIFIER = "YouScope.MultiCameraAndColorStream";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Multi-Camera & -Color Stream", new String[]{"multi-cam"}, 
				"Displays the current images of multiple cameras as overlayed images of different colors.",
				"icons/film-cast.png");
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
		setTitle("Multi-Camera and -Color Stream");
		setResizable(true);
		setMaximizable(true);
		getContainingFrame().addFrameListener(this);
		setPreferredSize(new Dimension(800, 600));
		
		try
		{
			mainPanel = new MultiStreamAndControlsPanel(getClient(), getServer());
		}
		catch(Exception e)
		{
			throw new AddonException("Could not establish continuous measurement.", e);
		}
		
		return mainPanel;
	}	
}
