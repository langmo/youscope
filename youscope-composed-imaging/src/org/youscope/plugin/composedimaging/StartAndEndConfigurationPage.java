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
package org.youscope.plugin.composedimaging;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.microscope.CameraDevice;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.Property;
import org.youscope.common.task.RegularPeriodConfiguration;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DeviceSettingsPanel;

/**
 * @author langmo
 *
 */
class StartAndEndConfigurationPage extends MeasurementAddonUIPage<ComposedImagingMeasurementConfiguration>
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -8833466993051293347L;

	private DeviceSettingsPanel							deviceSettingsOn;
	private DeviceSettingsPanel							deviceSettingsOff;
	
	private final YouScopeClient	client;
	private final YouScopeServer			server;
	
	StartAndEndConfigurationPage(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
	@Override
	public void createUI(YouScopeFrame frame)
	{
		setLayout(new GridLayout(2, 1));
		deviceSettingsOn = new DeviceSettingsPanel(client, server, true);
		deviceSettingsOff = new DeviceSettingsPanel(client, server, true);
		
		JPanel onPanel = new JPanel(new BorderLayout(2, 2));
		onPanel.add(new JLabel("Device Settings when measurement starts:"),
				BorderLayout.NORTH);
		onPanel.add(deviceSettingsOn, BorderLayout.CENTER);
		add(onPanel);
		JPanel offPanel = new JPanel(new BorderLayout(2, 2));
		offPanel.add(new JLabel("Device Settings when measurement ends:"),
				BorderLayout.NORTH);
		offPanel.add(deviceSettingsOff, BorderLayout.CENTER);
		add(offPanel);

		setBorder(new TitledBorder("Measurement Start and End Settings"));
	}
	
	@Override
	public void loadData(ComposedImagingMeasurementConfiguration configuration)
	{
		deviceSettingsOn.setSettings(configuration.getDeviseSettingsOn());
		deviceSettingsOff.setSettings(configuration.getDeviseSettingsOff());
	}

	@Override
	public boolean saveData(ComposedImagingMeasurementConfiguration configuration)
	{
		configuration.setDeviseSettingsOn(deviceSettingsOn.getSettings());
		configuration.setDeviseSettingsOff(deviceSettingsOff.getSettings());
		return true;
	}

	@Override
	public void setToDefault(ComposedImagingMeasurementConfiguration configuration)
	{
		if ((Boolean) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_PREINITIALIZE_CAMERA_SETTINGS))
		{
			try
			{
				Microscope microscope = server.getMicroscope();
				CameraDevice camera = microscope.getCameraDevice();
				String deviceName = camera.getDeviceID();
				Property[] properties = camera.getEditableProperties();
				DeviceSetting[] cameraDeviceSettings = new DeviceSetting[properties.length];
				for(int i=0; i< properties.length; i++)
				{
					cameraDeviceSettings[i] = new DeviceSetting();
					cameraDeviceSettings[i].setAbsoluteValue(true);
					cameraDeviceSettings[i].setDeviceProperty(deviceName, properties[i].getPropertyID());
					cameraDeviceSettings[i].setValue(properties[i].getValue());
				}
				configuration.setDeviseSettingsOn(cameraDeviceSettings);
			}
			catch (Exception e)
			{
				client.sendError("Could not pre-initialize measurement startup settings. Letting these settings empty and continuing.", e);
				configuration.setDeviseSettingsOn(new DeviceSetting[0]);
			}
		}
		
		if(configuration.getPeriod() == null)
		{
			// Set to AFAP
			RegularPeriodConfiguration period = new RegularPeriodConfiguration();
			period.setFixedTimes(false);
			period.setStartTime(0);
			period.setPeriod(0);
			configuration.setPeriod(period);
		}
	}

	@Override
	public String getPageName()
	{
		return "Start and End Settings";
	}

}
