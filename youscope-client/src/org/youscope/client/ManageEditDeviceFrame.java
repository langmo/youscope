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

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.microscope.AvailableDeviceDriver;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceLoader;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.PreInitDeviceProperty;
import org.youscope.uielements.StandardFormats;

/**
 * @author langmo
 *
 */
class ManageEditDeviceFrame implements ActionListener
{
	private GridBagConstraints	newLineConstr;
	private GridBagConstraints	bottomConstr;

	private YouScopeFrame			frame;
	
	private JPanel devicePanel = new JPanel();
	
	private Vector<ActionListener> devicesChangedListener = new Vector<ActionListener>();
	
	private final Device device;
	private AvailableDeviceDriver deviceDriver = null;
	private PreInitDeviceProperty[] preInitProperties = new PreInitDeviceProperty[0];
	
	private JComponent[] preInitPropertiesFields = new JComponent[0];
	private JTextField deviceNameField = new JTextField();
	
	ManageEditDeviceFrame(YouScopeFrame frame, Device device)
	{
		this.frame = frame;
		this.device = device;
		frame.setTitle("Edit Device");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(true);
		newLineConstr = StandardFormats.getNewLineConstraint();
		bottomConstr = StandardFormats.getBottomContstraint();
		
		// Initialize content.
		frame.startInitializing();
		new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					initializeFrame();
					ManageEditDeviceFrame.this.frame.endLoading();
					ManageEditDeviceFrame.this.frame.pack();
				}
			}).start();
	}
	@Override
	public synchronized void actionPerformed(ActionEvent arg0)
	{
		if(preInitProperties.length != preInitPropertiesFields.length)
		{
			ClientSystem.err.println("Internal error: number of pre-initialization fields is " + Integer.toString(preInitPropertiesFields.length)
					+", however, should be " + Integer.toString(preInitProperties.length));
			return;
		}
		// Add device.
		String deviceName = deviceNameField.getText();
		@SuppressWarnings("unused")
		String library;
		@SuppressWarnings("unused")
		String identifier;
		try
		{
			
			library = deviceDriver.getLibraryID();
			identifier = deviceDriver.getDriverID();
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not determine library and driver of device.", e);
			return;
		}
		DeviceSetting[] deviceSettings = new DeviceSetting[preInitProperties.length];
		for(int i=0; i<deviceSettings.length; i++)
		{
			
			String propertyName;
			try
			{
				propertyName = preInitProperties[i].getPropertyID();
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not determine ID of pre-initialization property.", e);
				return;
			}
			deviceSettings[i] = new DeviceSetting();
			deviceSettings[i].setAbsoluteValue(true);
			deviceSettings[i].setDeviceProperty(deviceName, propertyName);
			String value;
			if(preInitPropertiesFields[i] == null)
			{
				ClientSystem.err.println("Pre-initialization property field for property " + deviceName + "." + propertyName +" is null.");
				return;
			}
			else if(preInitPropertiesFields[i] instanceof JComboBox)
			{
				value = ((JComboBox<?>)preInitPropertiesFields[i]).getSelectedItem().toString();
			}
			else if(preInitPropertiesFields[i] instanceof JFormattedTextField)
			{
				value = ((JFormattedTextField)preInitPropertiesFields[i]).getValue().toString();
			}
			else if(preInitPropertiesFields[i] instanceof JTextField)
			{
				value = ((JTextField)preInitPropertiesFields[i]).getText();
			}
			else
			{
				ClientSystem.err.println("Cannot handle detect type of pre-initialization property field for property " + deviceName + "." + propertyName +". Type is " + preInitPropertiesFields[i].getClass().getSimpleName() + ".");
				return;
			}
			deviceSettings[i].setValue(value);
		}
		DeviceLoader deviceDriverManager;
		try
		{
			deviceDriverManager = YouScopeClientImpl.getMicroscope().getDeviceLoader();
		}
		catch(UnsupportedOperationException e)
		{
			ClientSystem.err.println("Maniplulating loaded devices is not supported by installed microscope conection type. Choose an other conncetion type.", e);
			return;
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not get device driver manager.", e);
			return;
		}
		// First unload old device definition
		try
		{
			deviceDriverManager.removeDevice(device.getDeviceID());
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not unload device prior to changes.", e);
			return;
		}
		
		// Now load new definition
		try
		{
			//TODO: CHANGE.
			//deviceDriverManager.addDevice(deviceName, library, identifier, deviceSettings);
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not load device with changes. Old device however already unloaded. Sorry!", e);
			return;
		}			
		
		for(ActionListener listener : devicesChangedListener)
		{
			listener.actionPerformed(new ActionEvent(frame, 1234, "New device added."));
		}
		// Hide frame.
		frame.setVisible(false);
	}
	
	private String getErrorText(String message, Exception e)
	{
		String errorMessage = "<p style=\"color:#EE2222\">" + message + "</p>";
		if (e != null)
        {
        	errorMessage += "<p>Cause: ";
        	for (Throwable throwable = e; throwable != null; throwable = throwable.getCause())
            {
        		if (throwable.getMessage() != null)
                	errorMessage += throwable.getClass().getSimpleName() +": " + throwable.getMessage().replace("\n", "<br />") + "<br />";
                else
                	errorMessage += throwable.getClass().getSimpleName() +": No further information.<br />";
            }
        	errorMessage += "</p>";
        }
		return errorMessage;
	}
	
	public void addDevicesChangedListener(ActionListener listener)
	{
		devicesChangedListener.add(listener);
	}
	
	public void removeDevicesChangedListener(ActionListener listener)
	{
		devicesChangedListener.remove(listener);
	}
	
	private void initializeFrame()
	{
		boolean canCreate = true;
		Vector<String> errorMessages = new Vector<String>();
		
		try
		{
			DeviceLoader deviceDriverManager = YouScopeClientImpl.getMicroscope().getDeviceLoader();
			AvailableDeviceDriver[] allDeviceDrivers = deviceDriverManager.getAvailableDeviceDrivers();
			String library = device.getLibraryID();
			String identifier = device.getDriverID();
			for(AvailableDeviceDriver deviceDriver : allDeviceDrivers)
			{
				if(deviceDriver.getLibraryID().equals(library) && deviceDriver.getDriverID().equals(identifier))
				{
					this.deviceDriver = deviceDriver;
					break;
				}
			}
		}
		catch(Exception e)
		{
			errorMessages.addElement(getErrorText("Could not get driver of device.", e));
			canCreate = false;
		}
		if(deviceDriver == null)
		{
			errorMessages.addElement(getErrorText("Could not find driver of device.", null));
			canCreate = false;
		}
		// Load data
		String identifier = "";
		String library = "";
		String type = "";
		String description = "";
		
		// Device description
		if(deviceDriver != null)
		{
			try
			{
				identifier = deviceDriver.getDriverID();
				library = deviceDriver.getLibraryID();
				type = deviceDriver.getType().getDescription();
				description = deviceDriver.getDescription();
			}
			catch(Exception e)
			{
				errorMessages.addElement(getErrorText("Could not get device description.", e));
			}
		}
		
		// Load pre-init properties
		if(deviceDriver != null)
		{
			try
			{
				//TODO: CHANGE.
				//preInitProperties = deviceDriver.getPreInitDeviceProperties();
				
			}
			catch(Exception e)
			{
				errorMessages.addElement(getErrorText("Could not get list of pre-initialization properties.", e));
				canCreate = false;
			}
		}
		if(preInitProperties == null)
			preInitProperties = new PreInitDeviceProperty[0];
		
		// Check if driver needs serial port but no serial port is available.
		boolean serialPortMising = false;
		if(preInitProperties.length > 0)
		{
			try
			{
				if(deviceDriver.isSerialPortDriver() && YouScopeClientImpl.getMicroscope().getDevices(DeviceType.SerialDevice).length <= 0)
				{
					serialPortMising = true;
					canCreate = false;
				}
			}
			catch(Exception e)
			{
				errorMessages.addElement(getErrorText("Could not detect if device needs a serial port.", e));
			}
		}
		
		// Setup properties
		JPanel preInitDevicePanel = null;
        if(preInitProperties.length > 0)
		{
			try
			{
				preInitPropertiesFields = new JComponent[preInitProperties.length];
				preInitDevicePanel = new JPanel(new GridLayout(preInitProperties.length, 2, 2, 2));
				
				for(int i=0; i < preInitProperties.length; i++)
				{
					// Add name of property.
					PreInitDeviceProperty preInitProperty = preInitProperties[i];
					preInitDevicePanel.add(new JLabel(preInitProperty.getPropertyID() + ":"));
					
					// Get current property value
					String propertyValue = null;
					try
					{
						propertyValue = device.getProperty(preInitProperty.getPropertyID()).getValue();
					}
					catch(@SuppressWarnings("unused") Exception e)
					{
						// do nothing, default value is OK.
					}
					if(propertyValue != null && propertyValue.length() < 1)
						propertyValue = null;
					
					// Add value chooser field
					String[] allowedPropertyValues = preInitProperty.getAllowedPropertyValues();
					if (allowedPropertyValues != null)
					{
						JComboBox<String> comboBox = new JComboBox<String>(allowedPropertyValues);
						if(propertyValue != null)
							comboBox.setSelectedItem(propertyValue);
						preInitDevicePanel.add(comboBox);
						preInitPropertiesFields[i] = comboBox;
					}
					else
					{
						switch (preInitProperty.getType())
						{
						case PROPERTY_STRING:
							JTextField textField = new JTextField();
							if(propertyValue != null)
								textField.setText(propertyValue);
							preInitDevicePanel.add(textField);
							preInitPropertiesFields[i] = textField;
							break;
						case PROPERTY_INTEGER:
							JFormattedTextField integerField = new JFormattedTextField(StandardFormats.getIntegerFormat());
							if(propertyValue != null)
							{
								try
								{
									integerField.setValue(Integer.parseInt(propertyValue));
								}
								catch(@SuppressWarnings("unused") NumberFormatException e)
								{
									// Do nothing.
								}
							}
							preInitDevicePanel.add(integerField);
							preInitPropertiesFields[i] = integerField;
							break;
						case PROPERTY_FLOAT:
							JFormattedTextField floatField = new JFormattedTextField(StandardFormats.getDoubleFormat());
							if(propertyValue != null)
							{
								try
								{
									floatField.setValue(Double.parseDouble(propertyValue));
								}
								catch(@SuppressWarnings("unused") NumberFormatException e)
								{
									// Do nothing.
								}
							}
							preInitDevicePanel.add(floatField);
							preInitPropertiesFields[i] = floatField;
							break;
						default:
							JTextField defaultField = new JTextField();
							if(propertyValue != null)
								defaultField.setText(propertyValue);
							preInitDevicePanel.add(defaultField);
							preInitPropertiesFields[i] = defaultField;
						}
						
					}
				}
			}
			catch(Exception e)
			{
				preInitDevicePanel = null;
				preInitProperties = new PreInitDeviceProperty[0];
				preInitPropertiesFields = new JComponent[0];
				canCreate = false;
				errorMessages.addElement(getErrorText("Could not get information about device pre-initialization properties.", e));
			}
		}
		
        // Setup layout
		GridBagLayout deviceLayout = new GridBagLayout();
		devicePanel.setLayout(deviceLayout);
        
        // Setup description
        JLabel deviceDescriptionLabel = new JLabel("<html><p>"
				+"Library: <i>" + library + "</i><br />"
				+"Driver: <i>" + identifier + "</i><br />"
				+"Type: <i>" + type + "</i><br />"
				+"Description: <i>" + description + "</i>"
				+"</p></html>");
		try
		{
			deviceNameField.setText(device.getDeviceID());
		}
		catch(Exception e)
		{
			errorMessages.addElement(getErrorText("Could not get device name.", e));
		}
		deviceDescriptionLabel.setOpaque(true);
		deviceDescriptionLabel.setBackground(Color.WHITE);
		deviceDescriptionLabel.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 1), new EmptyBorder(4,4,4,4)));
		StandardFormats.addGridBagElement(deviceDescriptionLabel, deviceLayout, newLineConstr, devicePanel);
		
		if(serialPortMising)
		{
			StandardFormats.addGridBagElement(new JLabel("<html>The seleted device communicates over a serial port.<br />Add a serial port first before editing this device.</html>"), deviceLayout, newLineConstr, devicePanel);
		}
		if(canCreate)
		{
			StandardFormats.addGridBagElement(new JLabel("Name for the device:"), deviceLayout, newLineConstr, devicePanel);
	        StandardFormats.addGridBagElement(deviceNameField, deviceLayout, newLineConstr, devicePanel);
		
	        if(preInitDevicePanel != null)
	        {
	        	StandardFormats.addGridBagElement(new JLabel("Device Settings which have to be set:"), deviceLayout, newLineConstr, devicePanel);
	        	StandardFormats.addGridBagElement(preInitDevicePanel, deviceLayout, newLineConstr, devicePanel);
	        }
		}

    	if(errorMessages.size() > 0)
    	{
    		String errorMessage = "<html>";
    		for(String error : errorMessages)
    		{
    			errorMessage += error;
    		}
    		errorMessage += "</html>";
    		JEditorPane editorPane = new JEditorPane("text/html", errorMessage);
			editorPane.setEditable(false);
			StandardFormats.addGridBagElement(new JScrollPane(editorPane), deviceLayout, bottomConstr, devicePanel);
    	}
    	else
    	{
    		StandardFormats.addGridBagElement(new JPanel(), deviceLayout, bottomConstr, devicePanel);
    	}
        
        if(canCreate)
        {
        	JButton addDeviceButton = new JButton("Edit device");
        	addDeviceButton.addActionListener(this);
        	StandardFormats.addGridBagElement(addDeviceButton, deviceLayout, newLineConstr, devicePanel);
        }	
        frame.setContentPane(devicePanel);
	}
	
}
