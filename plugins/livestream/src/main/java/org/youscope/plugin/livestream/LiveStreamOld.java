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

	public final static String TYPE_IDENTIFIER = "YouScope.YouScopeLiveStreamOld ";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Old LiveStream", new String[]{"misc"}, 
				"Old and deprecated implementation of the LiveStream. Displays the current camera image, with an option to change channel, exposure time and similar.","icons/film.png");
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
