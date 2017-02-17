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
package org.youscope.uielements;

import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.microscope.FocusDevice;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * UI element to select a focus device and focus adjustment delay.
 * @author Moritz Lang
 *
 */
public class FocusField  extends DynamicPanel
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -3255470611690866823L;

	private final JComboBox<String>								focusDeviceField		= new JComboBox<String>();
	private final IntegerTextField adjustmentTimeField = new IntegerTextField();

    private final YouScopeClient client;
    private final YouScopeServer server;
    
    /**
     * Constructor.
     * @param client Interface to the client.
     * @param server Interface to the server.
     */
    public FocusField(YouScopeClient client, YouScopeServer server)
    {
    	this(null, 0, client, server);
    }
    
    /**
     * Constructor.
     * @param focusDevice Selected focus device.
     * @param client Interface to the client.
     * @param server Interface to the server.
     */
    public FocusField(String focusDevice, YouScopeClient client, YouScopeServer server)
    {
    	this(focusDevice, 0, client, server);
    }
    
    /**
     * Constructor.
     * @param focusConfiguration Selected focus configuration, or null.
     * @param client Interface to the client.
     * @param server Interface to the server.
     */
    public FocusField(FocusConfiguration focusConfiguration, YouScopeClient client, YouScopeServer server)
    {
    	this(focusConfiguration==null?null : focusConfiguration.getFocusDevice(), focusConfiguration==null? 0 : focusConfiguration.getAdjustmentTime(), client, server);
    }
    
    /**
     * Constructor.
     * @param focusDevice Selected focus device.
     * @param adjustmentTimeMS focus adjustment time in ms.
     * @param client Interface to the client.
     * @param server Interface to the server.
     */
    public FocusField(String focusDevice, int adjustmentTimeMS, YouScopeClient client, YouScopeServer server)
    {
    	this.client = client;
    	this.server = server;
    
    	loadFocusDevices();
    	if(focusDevice == null)
    		focusDevice = getDefaultFocusDevice();
    	if(focusDevice != null)
    		focusDeviceField.setSelectedItem(focusDevice);
    	adjustmentTimeField.setMinimalValue(0);
    	adjustmentTimeField.setValue(adjustmentTimeMS < 0 ? 0 : adjustmentTimeMS);
    	add(focusDeviceField);
    	JLabel label = new JLabel("Adjustment time (ms):");
    	label.setOpaque(false);
    	add(label);
    	add(adjustmentTimeField);
    	setOpaque(false);
    }
    
    /**
     * Adds an action listener which gets notified when the focus device or focus adjustment time selection changed.
     * @param listener Listener to add.
     */
    public void addActionListener(ActionListener listener)
    {
    	adjustmentTimeField.addActionListener(listener);
    	focusDeviceField.addActionListener(listener);
    }
    
    /**
     * Removes a previously added Action listener.
     * @param listener Listener to remove.
     */
    public void removeActionListener(ActionListener listener)
    {
    	adjustmentTimeField.removeActionListener(listener);
    	focusDeviceField.removeActionListener(listener);
    }
    
    /**
     * Returns the currently selected focus device configuration.
     * @return Focus configuration.
     */
    public FocusConfiguration getFocusConfiguration()
    {
    	FocusConfiguration configuration = new FocusConfiguration();
    	Object focusDevice = focusDeviceField.getSelectedItem();
    	configuration.setFocusDevice(focusDevice == null ? null : focusDevice.toString());
    	configuration.setAdjustmentTime(adjustmentTimeField.getValue());
    	return configuration;
    }
    
    /**
     * Returns the currently selected focus device, or null if no focus device can be selected since there are no focus device.
     * @return Focus device name or null.
     */
    public String getFocusDevice()
    {
    	Object facus = focusDeviceField.getSelectedItem();
    	if(facus == null)
    		return null;
    	return facus.toString();
    }
    
    /**
     * Sets the currently chosen focus device.
     * @param focusDevice The focus device name.
     */
    public void setFocusDevice(String focusDevice)
    {
    	if(focusDevice != null)
    	{
    		focusDeviceField.setSelectedItem(focusDevice);
    	}
    }
    
    /**
     * Sets the currently selected focus configuration.
     * @param focusConfiguration Focus configuration.
     */
    public void setCamera(FocusConfiguration focusConfiguration)
    {
    	if(focusConfiguration == null)
    		return;
    	setFocusDevice(focusConfiguration.getFocusDevice());
    	setAdjustmentTime(focusConfiguration.getAdjustmentTime());
    }
    /**
     * Sets the currently selected focus adjustment time in ms.
     * @param adjustmentTimeMS focus adjustment time in ms.
     */
    public void setAdjustmentTime(int adjustmentTimeMS)
    {
    	adjustmentTimeField.setValue(adjustmentTimeMS < 0?0:adjustmentTimeMS);
    }
    
    /**
     * Returns the currently selected focus adjustment time in ms.
     * @return focus adjustment time in ms.
     */
    public int getAdjustmentTime()
    {
    	return adjustmentTimeField.getValue();
    }
    
    private String getDefaultFocusDevice()
    {
    	try {
			FocusDevice defaultFocus = server.getMicroscope().getFocusDevice();
			if(defaultFocus == null)
				return null;
			return defaultFocus.getDeviceID();
		} catch (Exception e) {
			client.sendError("Could not get default focus device ID.", e);
			return null;
		}
    }
    
    private void loadFocusDevices()
	{
    	String[] focusDevices;
    	try
		{
    		FocusDevice[] devices = server.getMicroscope().getFocusDevices();
    		focusDevices = new String[devices.length]; 
    		for(int i=0; i<devices.length; i++)
    		{
    			focusDevices[i] = devices[i].getDeviceID();
    		}
		}
		catch (Exception e)
		{
			client.sendError("Could not obtain focus device names.", e);
			focusDevices = new String[0];
		}
		
		focusDeviceField.removeAllItems();
		for(String focusDevice : focusDevices)
		{
			focusDeviceField.addItem(focusDevice);
		}
	}
}
