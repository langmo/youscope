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
package org.youscope.plugin.devicesettingmanager;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
class DeviceSettingManager extends ToolAddonUIAdapter
{
	private final DevicesPanel devicesPanel;
	private final PropertiesPanel proertiesPanel;
	
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 * @throws AddonException 
	 */
	public DeviceSettingManager(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
		devicesPanel = new DevicesPanel(client, server);
		proertiesPanel = new PropertiesPanel(client, server);
		devicesPanel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				proertiesPanel.setSelectedDevice(devicesPanel.getSelectedDevice());
			}
		});
		
	}
	
	public final static String TYPE_IDENTIFIER = "YouScope.DeviceSettingManager";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Device Setting Manager", null, 
				"Allows to directly change the settings of the microscope hardware, e.g. to open or close shutters, change stage positions, and similar.",
				"icons/wrench-screwdriver.png");
	}
	
	@Override
	public java.awt.Component createUI()
	{
		setTitle("Device Setting Manager");
		setResizable(true);
		setMaximizable(true);
		setPreferredSize(new Dimension(600, 600));
		
		JButton refreshButton = new JButton("Refresh");
		refreshButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				//proertiesPanel.setSelectedDevice(null);
				devicesPanel.actualize();
			}
		});
		
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(proertiesPanel, BorderLayout.CENTER);
		contentPane.add(devicesPanel, BorderLayout.WEST);
		contentPane.add(refreshButton, BorderLayout.SOUTH);

		proertiesPanel.setSelectedDevice(null);
		devicesPanel.actualize();
		
		return contentPane;
	}
}
