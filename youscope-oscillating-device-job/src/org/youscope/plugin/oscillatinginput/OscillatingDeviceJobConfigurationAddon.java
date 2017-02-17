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
package org.youscope.plugin.oscillatinginput;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.job.basicjobs.ScriptingJob;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.Property;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

/**
 * @author langmo
 */
class OscillatingDeviceJobConfigurationAddon extends ComponentAddonUIAdapter<OscillatingDeviceJobConfiguration>
{
    private JComboBox<String> deviceField;

    private JComboBox<String> propertyField;

    private JFormattedTextField minValueField = new JFormattedTextField(
            StandardFormats.getDoubleFormat());

    private JFormattedTextField maxValueField = new JFormattedTextField(
            StandardFormats.getDoubleFormat());

    private JFormattedTextField periodLengthField = new JFormattedTextField(
            StandardFormats.getDoubleFormat());

    private JFormattedTextField phaseField = new JFormattedTextField(
            StandardFormats.getDoubleFormat());

    private String[] devices;

    private String[] deviceProperties;
	
    /**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public OscillatingDeviceJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ComponentMetadataAdapter<OscillatingDeviceJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<OscillatingDeviceJobConfiguration>(OscillatingDeviceJobConfiguration.TYPE_IDENTIFIER, 
				OscillatingDeviceJobConfiguration.class, 
				ScriptingJob.class, 
				"Oscillating Input", 
				new String[]{"misc"});
	}
    
	@Override
	protected Component createUI(OscillatingDeviceJobConfiguration configuration) throws AddonException
	{
		setTitle("Oscillating Input");
		setResizable(false);
		setMaximizable(false);
		
		GridBagLayout elementsLayout = new GridBagLayout();
		JPanel elementsPanel = new JPanel(elementsLayout);
        GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();

        StandardFormats.addGridBagElement(new JLabel("Device:"), elementsLayout, newLineConstr, elementsPanel);
        deviceField = new JComboBox<String>();
        loadDevices();
        class DeviceFieldActionListener implements ActionListener
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                loadDevicePropertyNames();
            }

        }
        deviceField.addActionListener(new DeviceFieldActionListener());
        StandardFormats.addGridBagElement(deviceField, elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(new JLabel("Property:"), elementsLayout, newLineConstr, elementsPanel);
        propertyField = new JComboBox<String>();
        loadDevicePropertyNames();
        StandardFormats.addGridBagElement(propertyField, elementsLayout, newLineConstr, elementsPanel);

        // Minimal and maximal value of the device's property during oscillation.
        StandardFormats.addGridBagElement(new JLabel("Minimal value during oscillation:"), elementsLayout,
                newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(minValueField, elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(new JLabel("Maximal value during oscillation:"), elementsLayout,
                newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(maxValueField, elementsLayout, newLineConstr, elementsPanel);

        // Period length of the oscillation in s
        StandardFormats.addGridBagElement(new JLabel("Period length (ms):"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(periodLengthField, elementsLayout, newLineConstr, elementsPanel);

        // Initial phase at t=0s in rad (Signal is a sine, not a cosine).
        StandardFormats.addGridBagElement(new JLabel("Initial phase (rad):"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(phaseField, elementsLayout, newLineConstr, elementsPanel);
		        
        loadSettingsIntoLayout(configuration);
        
        return elementsPanel;
    }

    private void loadSettingsIntoLayout(OscillatingDeviceJobConfiguration configuration)
    {
        // Set state
    	String device = configuration.getDevice();
    	String property = configuration.getProperty();
        if (device != null)
        {
            for (int i = 0; i < devices.length; i++)
            {
                if (devices[i].compareTo(device) == 0)
                    deviceField.setSelectedIndex(i);
            }
            if (property != null)
            {
                for (int i = 0; i < deviceProperties.length; i++)
                {
                    if (deviceProperties[i].compareTo(property) == 0)
                        propertyField.setSelectedIndex(i);
                }
            }
        }
        minValueField.setValue(configuration.getMinValue());
        maxValueField.setValue(configuration.getMaxValue());
        periodLengthField.setValue(configuration.getPeriodLength());
        phaseField.setValue(configuration.getInitialPhase());
    }

    private void loadDevices()
    {
        deviceField.removeAllItems();

        try
        {
        	Device[] deviceH = getServer().getMicroscope().getDevices();
            devices = new String[deviceH.length];
            for(int i=0; i<deviceH.length; i++)
            {
            	devices[i] = deviceH[i].getDeviceID();
            }
        } 
        catch (Exception e)
        {
        	sendErrorMessage("Could not load device names.", e);
            devices = new String[0];
        } 
        if (devices == null)
        {
            devices = new String[0];
        }
        for (int i = 0; i < devices.length; i++)
        {
            deviceField.addItem(devices[i]);
        }
    }

    private void loadDevicePropertyNames()
    {
        String device = (String) deviceField.getSelectedItem();
        if (device == null)
            return;

        propertyField.removeAllItems();
        try
        {
        	Property[] properties = getServer().getMicroscope().getDevice(device).getEditableProperties();        	
            deviceProperties = new String[properties.length];
            for(int i=0; i<properties.length; i++)
            {
            	deviceProperties[i] = properties[i].getPropertyID();
            }
        } 
        catch (Exception e)
        {
        	sendErrorMessage("Could not load device property names for device "+device+".", e);
            deviceProperties = new String[0];
        } 
        if (deviceProperties == null)
        {
            deviceProperties = new String[0];
        }
        for (int i = 0; i < deviceProperties.length; i++)
        {
            propertyField.addItem(deviceProperties[i]);
        }
    }

	@Override
	protected void commitChanges(OscillatingDeviceJobConfiguration configuration) {
		// Get parameters
        String device = deviceField.getSelectedItem().toString();
        String property = propertyField.getSelectedItem().toString();
        double minValue = ((Number)minValueField.getValue()).doubleValue();
        double maxValue = ((Number)maxValueField.getValue()).doubleValue();
        int periodLength = ((Number)periodLengthField.getValue()).intValue();
        double initialPhase = ((Number)phaseField.getValue()).doubleValue();

        // Store parameters
        configuration.setDevice(device);
        configuration.setInitialPhase(initialPhase);
        configuration.setMaxValue(maxValue);
        configuration.setMinValue(minValue);
        configuration.setPeriodLength(periodLength);
        configuration.setProperty(property);
	}

	@Override
	protected void initializeDefaultConfiguration(OscillatingDeviceJobConfiguration configuration)
			throws AddonException {
		// do nothing.
	}
}
