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
package org.youscope.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JScrollPane;

import org.youscope.common.microscope.DeviceSetting;
import org.youscope.uielements.DescriptionPanel;
import org.youscope.uielements.DeviceSettingsPanel;

/**
 * @author Moritz Lang
 *
 */
class ManageTabShutdownSettings extends ManageTabElement
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 1130251748743397942L;
	private DeviceSettingsPanel deviceSettingPanel = null;
	private boolean somethingChanged = false;
	ManageTabShutdownSettings()
	{
		setOpaque(false);
		
		deviceSettingPanel= new DeviceSettingsPanel(new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer(), true);
		deviceSettingPanel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				somethingChanged = true;
			}
		});
		
		DescriptionPanel descriptionPanel = new DescriptionPanel("Description", "Similar to startup settings, shutdown settings are applied by YouScope whenever unloading a given microscope configuration, typically during shutdown of YouScope.\nYouScope can then automatically apply a set of device settings, e.g. turn off lights or change the image path.");
		
		setLayout(new BorderLayout(5, 5));
		JScrollPane scrollPane = new JScrollPane(descriptionPanel);
		scrollPane.setPreferredSize(new Dimension(400, 150));
		add(scrollPane, BorderLayout.NORTH);
		add(deviceSettingPanel, BorderLayout.CENTER);
	}
	
	@Override
	public void initializeContent()
	{
		if(deviceSettingPanel == null)
			return;
		try
		{
			deviceSettingPanel.setSettings(YouScopeClientImpl.getMicroscope().getMicroscopeConfiguration().getSystemShutdownSettings());
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not obtain current startup settings.", e);
		}
	}

	@Override
	public boolean storeContent()
	{
		if(somethingChanged)
		{
			// Send settings to server.
			DeviceSetting[] settings = deviceSettingPanel.getSettings();
			try
			{
				YouScopeClientImpl.getMicroscope().getMicroscopeConfiguration().setSystemShutdownSettings(settings);
			}
			catch(Exception e1)
			{
				ClientSystem.err.println("Could not set shutdown settings.", e1);
			}
		}
		return somethingChanged;
	}
}
