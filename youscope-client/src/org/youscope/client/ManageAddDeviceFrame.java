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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.microscope.AvailableDeviceDriver;
import org.youscope.common.microscope.DeviceLoader;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.PreInitDeviceProperty;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.StandardFormats;

import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * @author langmo
 *
 */
class ManageAddDeviceFrame
{
	private final GridBagConstraints	newLineConstr = StandardFormats.getNewLineConstraint();
	private final GridBagConstraints	bottomConstr = StandardFormats.getBottomContstraint();

	private final YouScopeFrame			frame;
	
	private final JList<Object>					deviceTypesField			= new JList<Object>();
	
	private AvailableDeviceDriver[] deviceDrivers = null;
	
	private final Vector<AvailableDeviceDriver> currentlyShownDevices = new Vector<AvailableDeviceDriver>();
	private final CurrentlyShownDevicesListModel currentlyShownDevicesListModel;
	private final JList<Object> currentlyShownDevicesList;
	
	private final JComboBox<String> sortTypeField = new JComboBox<String>(new String[]{"Device Type", "Library"});
	
	private final SelectedDevicePanel selectedDevicePanel = new SelectedDevicePanel();
	
	private Vector<ActionListener> devicesChangedListener = new Vector<ActionListener>();
	
	private final JPanel deviceTypeElement;
	private final JPanel deviceDriverElement;
	private final JPanel deviceSettingsElement;
	
	private JLabel selectDriverLabel;
	
	ManageAddDeviceFrame(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setTitle("Add Device");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(true);
		
		// Initialize device type chooser
		deviceTypesField.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		deviceTypesField.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				Object selectedValue = deviceTypesField.getSelectedValue();
				if(selectedValue == null)
					return;
				else if(sortTypeField.getSelectedIndex() == 0 &&  selectedValue instanceof DeviceType)
					showDevicesOfType((DeviceType)selectedValue);
				else if(sortTypeField.getSelectedIndex() == 1 && selectedValue instanceof String)
					showDevicesOfLibrary((String)selectedValue);
			}
		});
		
		// Initialize available device driver list
		currentlyShownDevicesListModel = new CurrentlyShownDevicesListModel();
		currentlyShownDevicesList = new JList<Object>(currentlyShownDevicesListModel);
		currentlyShownDevicesList.setCellRenderer(new CurrentlyShownDevicesListRenderer());
		currentlyShownDevicesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		currentlyShownDevicesList.getSelectionModel().addListSelectionListener(new ListSelectionListener()
			{
				@Override
				public void valueChanged(ListSelectionEvent arg0)
				{
					currentlyShownDevicesList.setPrototypeCellValue(null);

					if(arg0.getValueIsAdjusting())
						return;
					selectedDevicePanel.setSelectedDevice(currentlyShownDevicesList.getSelectedIndex());
				}
			});
		
		// Set layout
		deviceTypeElement = new JPanel(new BorderLayout(5, 5));
		JPanel sorterPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		sorterPanel.add(new JLabel("Sort by:"));
		sorterPanel.add(sortTypeField);
		deviceTypeElement.add(sorterPanel, BorderLayout.NORTH);
		deviceTypeElement.add(new JScrollPane(deviceTypesField), BorderLayout.CENTER);
		deviceTypeElement.setBorder(new TitledBorder("Step 1: Select Device Type"));
		
		deviceDriverElement = new JPanel(new BorderLayout());
		deviceDriverElement.setBorder(new TitledBorder("Step 2: Select Device Driver"));
		
		deviceSettingsElement = new JPanel(new BorderLayout());
		deviceSettingsElement.setBorder(new TitledBorder("Step 3: Configure Device"));
		
		// Initialize content.
		frame.startInitializing();
		new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					initializeFrame();
					ManageAddDeviceFrame.this.frame.endLoading();
					ManageAddDeviceFrame.this.frame.setSize(new Dimension(790, 500));
				}
			}).start();
	}
	
	private class QRImageField extends JComponent
    {
        /**
         * Serial Version UID.
         */
        private static final long serialVersionUID = 3997578873912009511L;

        private BufferedImage image = null;
        
        private static final int WIDTH = 150;
        private static final int HEIGHT = 150;
        
        private static final String QR_MESSAGE_PREFIX = "http://valelab.ucsf.edu/~MM/MMwiki/index.php/";
        
        private void setDriver(String libraryID)
    	{
    		if(libraryID == null)
    		{
    			this.image = null;
    			repaint();
    			return;
    		}
    		BitMatrix matrix;
    		try
    		{
    			matrix = new QRCodeWriter().encode(QR_MESSAGE_PREFIX + libraryID, com.google.zxing.BarcodeFormat.QR_CODE, WIDTH, HEIGHT);
    		}
    		catch(@SuppressWarnings("unused") WriterException e)
    		{
    			this.image = null;
    			repaint();
    			return;
    		}
    		
    		this.image = MatrixToImageWriter.toBufferedImage(matrix);
    		repaint();
    		return;
    	}
        
        @Override
        public Dimension getPreferredSize()
        {
        	return new Dimension(WIDTH+1, HEIGHT+1);
        }
        
        @Override
        public Dimension getMinimumSize()
        {
        	return new Dimension(WIDTH+1, HEIGHT+1);
        }
        
        @Override
        public synchronized void paintComponent(Graphics grp)
        {
            Graphics2D g2D = (Graphics2D) grp;

            if (image == null)
            {
                return;
            }

            double imageWidth = image.getWidth(this);
            double imageHeight = image.getHeight(this);
            if (WIDTH / imageWidth > HEIGHT / imageHeight)
            {
                imageWidth = imageWidth * HEIGHT / imageHeight;
                imageHeight = HEIGHT;
            } 
            else
            {
                imageHeight = imageHeight * WIDTH / imageWidth;
                imageWidth = WIDTH;
            }

            // draw the image
            g2D.drawImage(image, (int) (getWidth() - imageWidth) / 2,
                    (int) (getHeight() - imageHeight) / 2, (int) imageWidth, (int) imageHeight,
                    this);
            g2D.setColor(Color.BLACK);
            g2D.drawRect((int) (getWidth() - imageWidth) / 2,
                    (int) (getHeight() - imageHeight) / 2, (int) imageWidth, (int) imageHeight);
        }
    }
	
	private class SelectedDevicePanel extends JPanel implements ActionListener
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -9001873347874072884L;
		private AvailableDeviceDriver selectedDevice = null;
		private JTextField deviceNameField = new JTextField();
		private final QRImageField qrImage = new QRImageField();
		public synchronized void setSelectedDevice(int row)
		{
			// Remove all settings from previous driver.
			deviceNameField.setText("");
			selectedDevice = null;
			removeAll();
			if(row < 0 || row >= currentlyShownDevices.size())
			{
				validate();
				repaint();
				return;
			}
			
			// Get device
			selectedDevice = currentlyShownDevices.elementAt(row);
			
			// Load data
			String identifier = "";
			String library = "";
			String type = "";
			String description = "";
			boolean canCreate = true;
			Vector<String> errorMessages = new Vector<String>();
			// Device description
			try
			{
				identifier = selectedDevice.getDriverID();
				library = selectedDevice.getLibraryID();
				type = selectedDevice.getType().getDescription();
				description = selectedDevice.getDescription();
			}
			catch(Exception e)
			{
				errorMessages.addElement(getErrorText("Could not get device description.", e));
			}
			
	        // Setup layout
			GridBagLayout deviceLayout = new GridBagLayout();
			setLayout(deviceLayout);
	        
	        // Setup description
	        JLabel deviceDescriptionLabel = new JLabel("<html><p>"
					+"Library: <i>" + library + "</i><br />"
					+"Driver: <i>" + identifier + "</i><br />"
					+"Device Type: <i>" + type + "</i><br />"
					+"Description: <i>" + description + "</i>"
					+"</p></html>");
			deviceNameField.setText(identifier);
			deviceDescriptionLabel.setOpaque(true);
			deviceDescriptionLabel.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 1), new EmptyBorder(4,4,4,4)));
			StandardFormats.addGridBagElement(deviceDescriptionLabel, deviceLayout, newLineConstr, this);
			
			if(canCreate)
			{
				StandardFormats.addGridBagElement(new JLabel("Name for the device:"), deviceLayout, newLineConstr, this);
		        StandardFormats.addGridBagElement(deviceNameField, deviceLayout, newLineConstr, this);
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
    			StandardFormats.addGridBagElement(new JScrollPane(editorPane), deviceLayout, bottomConstr, this);
        	}
        	else
        	{
        		JPanel emptyPanel = new JPanel();
        		StandardFormats.addGridBagElement(emptyPanel, deviceLayout, bottomConstr, this);
        	}
        	
        	// Add QR image
        	qrImage.setDriver(library);
        	StandardFormats.addGridBagElement(qrImage, deviceLayout, newLineConstr, this);
        	StandardFormats.addGridBagElement(new JLabel("Scan for driver description!", SwingConstants.CENTER), deviceLayout, newLineConstr, this);
	        
	        if(canCreate)
	        {
	        	JButton addDeviceButton = new JButton("Add device");
	        	addDeviceButton.addActionListener(this);
	        	StandardFormats.addGridBagElement(addDeviceButton, deviceLayout, newLineConstr, this);
	        }
	        
	        deviceSettingsElement.removeAll();
			deviceSettingsElement.add(this, BorderLayout.CENTER);
			deviceSettingsElement.revalidate();
			deviceSettingsElement.repaint();
		}
		
		@Override
		public synchronized void actionPerformed(ActionEvent arg0)
		{
			String deviceID = deviceNameField.getText();
			PreInitDeviceProperty[] preInitProperties;
			try
			{
				preInitProperties = selectedDevice.loadDevice(deviceID);
			}
			catch(Exception e1)
			{
				ClientSystem.err.println("Could not load device driver.", e1);
				return;
			}
			
			if(preInitProperties != null && preInitProperties.length > 0)
			{
				YouScopeFrame childFrame = frame.createModalChildFrame();
				@SuppressWarnings("unused")
				DevicePropertiesFrame devicePropertiesFrame = new DevicePropertiesFrame(selectedDevice, preInitProperties, deviceID, childFrame, frame);
				childFrame.setVisible(true);
				return;
			}
			try
			{
				selectedDevice.initializeDevice(null);
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not initialize device driver.", e);
				return;
			}
			
			for(ActionListener listener : devicesChangedListener)
			{
				listener.actionPerformed(new ActionEvent(ManageAddDeviceFrame.this, 1234, "New device added."));
			}
			// Hide frame.
			frame.setVisible(false);
			for(ActionListener listener : devicesChangedListener)
			{
				listener.actionPerformed(new ActionEvent(ManageAddDeviceFrame.this, 1234, "New device added."));
			}
			// Hide frame.
			frame.setVisible(false);
		}
		
		
		
	}
	private class DevicePropertiesFrame extends JPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -3684188740751590716L;
		private final AvailableDeviceDriver selectedDevice;
		private final PreInitDeviceProperty[] preInitProperties;
		private JComponent[] preInitPropertiesFields;
		private final YouScopeFrame frame;
		private JPanel preInitDevicePanel;
		private final String deviceID;
		private final YouScopeFrame parentFrame;
		DevicePropertiesFrame(AvailableDeviceDriver selectedDevice, PreInitDeviceProperty[] preInitProperties, String deviceID, YouScopeFrame frame, YouScopeFrame parentFrame)
		{
			this.selectedDevice = selectedDevice;
			this.preInitProperties = preInitProperties;
			this.frame = frame;
			this.deviceID = deviceID;
			this.parentFrame = parentFrame;
			
			// Load meta-data
			String identifier = "";
			String library = "";
			String type = "";
			String description = "";
			boolean canCreate = true;
			Vector<String> errorMessages = new Vector<String>();
			// Device description
			try
			{
				identifier = selectedDevice.getDriverID();
				library = selectedDevice.getLibraryID();
				type = selectedDevice.getType().getDescription();
				description = selectedDevice.getDescription();
			}
			catch(Exception e)
			{
				errorMessages.addElement(getErrorText("Could not get device description.", e));
			}
			
			// Check if driver needs serial port but no serial port is available.
			boolean serialPortMising = false;
			if(preInitProperties.length > 0)
			{
				try
				{
					if(selectedDevice.isSerialPortDriver() && YouScopeClientImpl.getMicroscope().getDevices(DeviceType.SerialDevice).length <= 0)
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
			try
			{
				preInitPropertiesFields = new JComponent[preInitProperties.length];
				preInitDevicePanel = new JPanel(new GridLayout(preInitProperties.length, 2, 2, 2));
				
				for(int i=0; i < preInitProperties.length; i++)
				{
					// Add name of property.
					PreInitDeviceProperty preInitProperty = preInitProperties[i];
					preInitDevicePanel.add(new JLabel(preInitProperty.getPropertyID() + ":"));
					
					// Add value chooser field
					String defaultValue = preInitProperty.getDefaultValue();
					if(defaultValue != null && defaultValue.length() < 1)
						defaultValue = null;
					String[] allowedPropertyValues = preInitProperty.getAllowedPropertyValues();
					if (allowedPropertyValues != null)
					{
						JComboBox<String> comboBox = new JComboBox<String>(allowedPropertyValues);
						if(defaultValue != null)
							comboBox.setSelectedItem(defaultValue);
						preInitDevicePanel.add(comboBox);
						preInitPropertiesFields[i] = comboBox;
					}
					else
					{
						switch (preInitProperty.getType())
						{
						case PROPERTY_STRING:
							JTextField textField = new JTextField();
							if(defaultValue != null)
								textField.setText(defaultValue);
							preInitDevicePanel.add(textField);
							preInitPropertiesFields[i] = textField;
							break;
						case PROPERTY_INTEGER:
							JFormattedTextField integerField = new JFormattedTextField(StandardFormats.getIntegerFormat());
							if(defaultValue != null)
							{
								try
								{
									integerField.setValue(Integer.parseInt(defaultValue));
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
							if(defaultValue != null)
							{
								try
								{
									floatField.setValue(Double.parseDouble(defaultValue));
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
							if(defaultValue != null)
								defaultField.setText(defaultValue);
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
			
			GridBagLayout deviceLayout = new GridBagLayout();
			setLayout(deviceLayout);
	        
	        // Setup description
	        JLabel deviceDescriptionLabel = new JLabel("<html><p>"
					+"Device ID: <i>" + deviceID + "</i><br />"
	        		+"Library: <i>" + library + "</i><br />"
					+"Driver: <i>" + identifier + "</i><br />"
					+"Device Type: <i>" + type + "</i><br />"
					+"Description: <i>" + description + "</i>"
					+"</p></html>");
			deviceDescriptionLabel.setOpaque(true);
			deviceDescriptionLabel.setBorder(new CompoundBorder(new LineBorder(Color.BLACK, 1), new EmptyBorder(4,4,4,4)));
			StandardFormats.addGridBagElement(deviceDescriptionLabel, deviceLayout, newLineConstr, this);
			
			if(serialPortMising)
			{
				StandardFormats.addGridBagElement(new JLabel("<html>The seleted device communicates over a serial port.<br />Add a serial port first before adding this device.</html>"), deviceLayout, newLineConstr, this);
			}
			if(canCreate)
			{
				StandardFormats.addGridBagElement(new JLabel("Device Settings which have to be set:"), deviceLayout, newLineConstr, this);
		        StandardFormats.addGridBagElement(preInitDevicePanel, deviceLayout, newLineConstr, this);
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
    			StandardFormats.addGridBagElement(new JScrollPane(editorPane), deviceLayout, bottomConstr, this);
        	}
        	else
        	{
        		StandardFormats.addGridBagElement(new JPanel(), deviceLayout, bottomConstr, this);
        	}
	        
	        if(canCreate)
	        {
	        	JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 2, 2));
	        	buttonsPanel.setOpaque(false);
	        	JButton addDeviceButton = new JButton("Add device");
	        	addDeviceButton.addActionListener(new ActionListener()
	        	{
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						initializeDevice();
					}
	        	});
	        	buttonsPanel.add(addDeviceButton);
	        	JButton cancelButton = new JButton("Cancel");
	        	cancelButton.addActionListener(new ActionListener()
	        	{
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						unloadDevice();
					}
	        	});
	        	buttonsPanel.add(cancelButton);
	        	StandardFormats.addGridBagElement(buttonsPanel, deviceLayout, newLineConstr, this);
	        }
	        else
	        {
	        	JButton cancelButton = new JButton("Close");
	        	cancelButton.addActionListener(new ActionListener()
	        	{
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						unloadDevice();
					}
	        	});
	        	StandardFormats.addGridBagElement(cancelButton, deviceLayout, newLineConstr, this);
	        }
	        
	        // Initialize frame
	        frame.setTitle("Add Device");
			frame.setResizable(false);
			frame.setClosable(false);
			frame.setMaximizable(false);
			frame.setContentPane(this);
			frame.pack();
		}
		private void initializeDevice()
		{
			if(preInitProperties.length != preInitPropertiesFields.length)
			{
				ClientSystem.err.println("Internal error: number of pre-initialization fields is " + Integer.toString(preInitPropertiesFields.length)
						+", however, should be " + Integer.toString(preInitProperties.length));
				return;
			}
			// Add device.
			try
			{
				DeviceSetting[] deviceSettings = new DeviceSetting[preInitProperties.length];
				for(int i=0; i<deviceSettings.length; i++)
				{
					String propertyName = preInitProperties[i].getPropertyID();
					deviceSettings[i] = new DeviceSetting();
					deviceSettings[i].setAbsoluteValue(true);
					deviceSettings[i].setDeviceProperty(deviceID, propertyName);
					String value;
					if(preInitPropertiesFields[i] == null)
					{
						ClientSystem.err.println("Pre-initialization property field for property " + deviceID + "." + propertyName +" is null.");
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
						ClientSystem.err.println("Cannot handle detect type of pre-initialization property field for property " + deviceID + "." + propertyName +". Type is " + preInitPropertiesFields[i].getClass().getSimpleName() + ".");
						return;
					}
					deviceSettings[i].setValue(value);
				}
				selectedDevice.initializeDevice(deviceSettings);
				
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not add device driver.", e);
				return;
			}
			for(ActionListener listener : devicesChangedListener)
			{
				listener.actionPerformed(new ActionEvent(ManageAddDeviceFrame.this, 1234, "New device added."));
			}
			// Hide frame.
			frame.setVisible(false);
			// Hide parent frame.
			parentFrame.setVisible(false);
		} 
		private void unloadDevice()
		{
			try
			{
				selectedDevice.unloadDevice();
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not unload pre-initialized device.\nWe recommend reloading the configuration.", e);
			}
			frame.setVisible(false);
		}
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
		try
		{
			DeviceLoader deviceDriverManager = YouScopeClientImpl.getMicroscope().getDeviceLoader();
			deviceDrivers = deviceDriverManager.getAvailableDeviceDrivers();
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not initialize available driver list.", e);
			return;
		}
		
		sortTypeField.setSelectedIndex(1);
		updateDeviceTypes();
		sortTypeField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				updateDeviceTypes();
			}
		});
		
		Icon previousFirst = ImageLoadingTools.getResourceIcon("org/youscope/client/images/arrowLeft.png", "select previous tab first");
		
		JLabel selectTypeLabel = new JLabel("<html><p style=\"font-size:16pt\">Select device type!</p></html>", SwingConstants.CENTER);
		selectTypeLabel.setOpaque(false);
		selectTypeLabel.setVerticalAlignment(SwingConstants.CENTER);
		selectTypeLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		selectTypeLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		if(previousFirst != null)
		{
			selectTypeLabel.setIcon(previousFirst);
		}
		deviceDriverElement.add(selectTypeLabel, BorderLayout.CENTER);
		
		selectDriverLabel = new JLabel("<html><p style=\"font-size:16pt\">Select device driver!</p></html>", SwingConstants.CENTER);
		selectDriverLabel.setOpaque(false);
		selectDriverLabel.setVerticalAlignment(SwingConstants.CENTER);
		selectDriverLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
		selectDriverLabel.setHorizontalTextPosition(SwingConstants.CENTER);
		if(previousFirst != null)
		{
			selectDriverLabel.setIcon(previousFirst);
		}
		deviceSettingsElement.add(selectDriverLabel, BorderLayout.CENTER);
		
		JPanel contentPane = new JPanel(new GridLayout(1, 3, 2, 2));
		contentPane.add(deviceTypeElement);
		contentPane.add(deviceDriverElement);
		contentPane.add(deviceSettingsElement);
		frame.setContentPane(contentPane);
		frame.pack();
		
	}
	
	private void updateDeviceTypes()
	{
		if(sortTypeField.getSelectedIndex() == 0)
		{
			Vector<DeviceType> mentionedDeviceTypes = new Vector<DeviceType>();
			// Get all device types.
			for(AvailableDeviceDriver deviceDriver : deviceDrivers)
			{
				DeviceType type;
				try
				{
					type = deviceDriver.getType();
				}
				catch(Exception e)
				{
					ClientSystem.err.println("Could not get type of a driver.", e);
					continue;
				}
				if(!mentionedDeviceTypes.contains(type))
				{
					mentionedDeviceTypes.add(type);
				}
			}
			deviceTypesField.setListData(mentionedDeviceTypes);
		}
		else
		{
			Vector<String> mentionedDeviceTypes = new Vector<String>();
			// Get all libraries
			for(AvailableDeviceDriver deviceDriver : deviceDrivers)
			{
				String library;
				try
				{
					library = deviceDriver.getLibraryID();
				}
				catch(Exception e)
				{
					ClientSystem.err.println("Could not get library of a driver.", e);
					continue;
				}
				if(!mentionedDeviceTypes.contains(library))
				{
					mentionedDeviceTypes.add(library);
				}
			}
			Collections.sort(mentionedDeviceTypes);
			deviceTypesField.setListData(mentionedDeviceTypes);
		}
	}
	
	private void showDevicesOfType(DeviceType type)
	{
		currentlyShownDevicesList.clearSelection();
		currentlyShownDevices.clear();
		for(AvailableDeviceDriver deviceDriver : deviceDrivers)
		{
			try
			{
				if(deviceDriver.getType().equals(type))
				{
					currentlyShownDevices.addElement(deviceDriver);
				}
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not obtain type of one device.", e);
			}
		}
		
		currentlyShownDevicesListModel.fireContentsChanged();
		deviceDriverElement.removeAll();
		deviceDriverElement.add(new JScrollPane(currentlyShownDevicesList), BorderLayout.CENTER);
		deviceDriverElement.revalidate();
		deviceSettingsElement.removeAll();
		deviceSettingsElement.repaint();
		deviceSettingsElement.add(selectDriverLabel, BorderLayout.CENTER);
		deviceSettingsElement.revalidate();
		deviceSettingsElement.repaint();
	}
	private void showDevicesOfLibrary(String libraryID)
	{
		currentlyShownDevicesList.clearSelection();
		currentlyShownDevices.clear();
		for(AvailableDeviceDriver deviceDriver : deviceDrivers)
		{
			try
			{
				if(deviceDriver.getLibraryID().equals(libraryID))
				{
					currentlyShownDevices.addElement(deviceDriver);
				}
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not obtain type of one device.", e);
			}
		}
		
		currentlyShownDevicesListModel.fireContentsChanged();
		deviceDriverElement.removeAll();
		deviceDriverElement.add(new JScrollPane(currentlyShownDevicesList), BorderLayout.CENTER);
		deviceDriverElement.revalidate();
		deviceSettingsElement.removeAll();
		deviceSettingsElement.repaint();
		deviceSettingsElement.add(selectDriverLabel, BorderLayout.CENTER);
		deviceSettingsElement.revalidate();
		deviceSettingsElement.repaint();
	}
	
	private class CurrentlyShownDevicesListRenderer extends JPanel implements ListCellRenderer<Object>
	{
	     /**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 917402337440330543L;

		private JLabel libraryLabel = new JLabel();
		private JLabel deviceLabel = new JLabel();
		private JLabel descriptionLabel = new JLabel("");
		public CurrentlyShownDevicesListRenderer() 
	    {
			 setOpaque(true);
	         setLayout(new BorderLayout());
			 JPanel topPanel = new JPanel(new GridLayout(1, 2, 0, 0));
			 topPanel.setOpaque(false);
			 topPanel.add(libraryLabel);
			 topPanel.add(deviceLabel);
			 add(topPanel, BorderLayout.CENTER);
			 add(descriptionLabel, BorderLayout.SOUTH);
			 Icon newIcon = ImageLoadingTools.getResourceIcon("bonus/icons-24/wooden-box.png", "add driver");
			 if(newIcon != null)
				 add(new JLabel(newIcon), BorderLayout.WEST);
			 setBorder(new MatteBorder(0, 0, 1, 0, Color.DARK_GRAY));
	    }

	     @Override
	     public Component getListCellRendererComponent(JList<? extends Object> list,
	                                                   Object value,
	                                                   int index,
	                                                   boolean isSelected,
	                                                   boolean cellHasFocus) 
	     {
	    	 if(!(value instanceof AvailableDeviceDriver))
	    	 {
	    		 return new JLabel("<html>" + getErrorText("Value is not a device!", null) + "</html>");
	    	 }
	    	 isSelected = currentlyShownDevicesList.isSelectedIndex(index);
	    	 AvailableDeviceDriver selectedDevice = (AvailableDeviceDriver)value;
	    	 
	    	// Load data
			String identifier = "";
			String library = "";
			String description = "";
			try
			{
				identifier = selectedDevice.getDriverID();
				library = selectedDevice.getLibraryID();
				description = selectedDevice.getDescription();
			}
			catch(Exception e)
			{
				return new JLabel("<html>" + getErrorText("Could not get driver information!", e) + "</html>");
			}
	    	
			libraryLabel.setText("<html>Library:<br /><i>" + library + "</i></html>");
			deviceLabel.setText("<html>Driver:<br /><i>" + identifier + "</i></html>");
			if(isSelected)
			{
				descriptionLabel.setVisible(true);
				descriptionLabel.setText("<html>Description:<br /><i>" + description + "</i></html>");
				setBackground(currentlyShownDevicesList.getSelectionBackground());
		    	setForeground(currentlyShownDevicesList.getSelectionForeground());
			}
			else
			{
				descriptionLabel.setVisible(false);
				setBackground(currentlyShownDevicesList.getBackground());
	    		setForeground(currentlyShownDevicesList.getForeground());
			}
			return this;
	     }
	 }

	
	private class CurrentlyShownDevicesListModel extends DefaultListModel<Object>
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -933873118426200597L;
		@Override
		public Object getElementAt(int index)
		{
			if(index < 0 || index >= currentlyShownDevices.size())
				return null;
			return currentlyShownDevices.elementAt(index);
		}

		@Override
		public int getSize()
		{
			return currentlyShownDevices.size();
		}
		public void fireContentsChanged()
		{
			super.fireContentsChanged(this, 0, getSize()-1);
		}
	}
}
