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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.microscope.Device;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
class DevicesPanel extends JPanel
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -1269843524379415504L;
	
	private final JList<String>					devicesField			= new JList<String>();

	private volatile String selectedDevice = null;
	
	private volatile boolean actualizing = false;
	
	private final YouScopeServer server;
	private final YouScopeClient client;
	
	private Vector<ActionListener> actionListeners = new Vector<ActionListener>();
	DevicesPanel(YouScopeClient client, YouScopeServer server)
	{
		super(new BorderLayout());
		this.server = server;
		this.client = client;
		
		devicesField.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		devicesField.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if(actualizing)
					return;
				if(devicesField.getSelectedValue() == null)
					return;
				setSelectedDevice(devicesField.getSelectedValue().toString());
			}
		});
		devicesField.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		add(new JScrollPane(devicesField), BorderLayout.CENTER);
		setBorder(new TitledBorder("Devices"));
	}
	
	public void actualize()
	{
		if(actualizing)
			return;
		
		class DeviceNameActualizer implements Runnable
		{
			private volatile String[] deviceNames;
			private volatile String newSelectedValue = null;
			@Override
			public void run()
			{
				actualizing = true;
				// Load device names.
				// Depending on microscope, this may take significant time
				deviceNames = loadDeviceNames();
				// UI actualization must be in swing thread...
				try
				{
					SwingUtilities.invokeAndWait(new Runnable()
					{
						@Override
						public void run()
						{
							int idx = devicesField.getSelectedIndex();
							devicesField.setListData(deviceNames);
							if(idx >= 0 && idx < deviceNames.length)
								devicesField.setSelectedIndex(idx);
							else if(deviceNames.length > 0)
								devicesField.setSelectedIndex(0);
							if(devicesField.getSelectedValue() != null)
								newSelectedValue = devicesField.getSelectedValue().toString();
						}
					});
				}
				catch(Exception e)
				{
					client.sendError("Could not actualize devices menu.", e);
					return;
				}
				setSelectedDevice(newSelectedValue);
				actualizing = false;
			}
		}
		new Thread(new DeviceNameActualizer()).start();	
	}
	
	private String[] loadDeviceNames()
	{
		String[] deviceNames;
		try
		{
			Device[] devices = server.getMicroscope().getDevices();
			deviceNames = new String[devices.length];
			for(int i=0; i< devices.length; i++)
			{
				deviceNames[i] = devices[i].getDeviceID();
			}
		}
		catch(Exception e)
		{
			client.sendError("Could not load device names.", e);
			deviceNames = new String[0];
		}
		return deviceNames;
	}
	
	private void setSelectedDevice(String device)
	{
		selectedDevice = device;
		synchronized(actionListeners)
		{
			for(ActionListener listener : actionListeners)
			{
				listener.actionPerformed(new ActionEvent(this, 965, "selectedDeviceChanged"));
			}
		}
	}
	
	public String getSelectedDevice()
	{
		return selectedDevice;
	}
	
	public void addActionListener(ActionListener listener)
	{
		synchronized(actionListeners)
		{
			actionListeners.add(listener);
		}
	}
	
	public void removeActionListener(ActionListener listener)
	{
		synchronized(actionListeners)
		{
			actionListeners.remove(listener);
		}
	}
}
