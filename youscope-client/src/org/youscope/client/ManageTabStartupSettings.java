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
class ManageTabStartupSettings extends ManageTabElement
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 1730251748743397942L;
	private DeviceSettingsPanel deviceSettingPanel = null;
	private boolean somethingChanged = false;
	ManageTabStartupSettings()
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
		
		DescriptionPanel descriptionPanel = new DescriptionPanel("Description", "Startup settings are applied by YouScope whenever loading a given microscope configuration, typically during startup of YouScope.\nYouScope can then automatically apply a set of device settings, e.g. turn on lights or change the binning of a camera.");
		
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
			deviceSettingPanel.setSettings(YouScopeClientImpl.getMicroscope().getMicroscopeConfiguration().getSystemStartupSettings());
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
			DeviceSetting[] startupSettings = deviceSettingPanel.getSettings();
			try
			{
				YouScopeClientImpl.getMicroscope().getMicroscopeConfiguration().setSystemStartupSettings(startupSettings);
			}
			catch(Exception e1)
			{
				ClientSystem.err.println("Could not set startup settings.", e1);
			}
		}
		return somethingChanged;
	}
}
