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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.youscope.client.ManageAddDeviceFrame.PeripheralDevicesPage;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.HubDevice;
import org.youscope.common.microscope.MicroscopeConfigurationListener;
import org.youscope.uielements.DescriptionPanel;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class ManageTabLoadedDevices extends ManageTabElement
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 5057698474208368445L;
	private final JList<LoadedDevice> loadedDevicesList;
	private final LoadedDevicesListModel loadedDevicesListModel;
	private final Vector<LoadedDevice> loadedDevices = new Vector<LoadedDevice>();
	private final YouScopeFrame frame;
	
	private final GridBagConstraints	newLineConstr = StandardFormats.getNewLineConstraint();
	private final GridBagConstraints	bottomConstr = StandardFormats.getBottomContstraint();
	
	private boolean somethingChanged = false;
	
	ManageTabLoadedDevices(YouScopeFrame frame)
	{
		this.frame = frame;
		setOpaque(false);
		
		final MicroscopeConfigurationListener configurationListener = new MicroscopeConfigurationListener() {
			
			@Override
			public void microscopeUninitialized() 
			{
				somethingChanged = true;
				initializeContent();
			}
			
			@Override
			public void labelChanged(DeviceSetting oldLabel, DeviceSetting newLabel)
			{
				somethingChanged = true;
				initializeContent();
			}
			
			@Override
			public void deviceRemoved(String deviceID)
			{
				somethingChanged = true;
				initializeContent();
			}

			@Override
			public void deviceAdded(String deviceID)
			{
				somethingChanged = true;
				initializeContent();
			}
		};
		frame.addFrameListener(new YouScopeFrameListener() {
			
			@Override
			public void frameOpened() {
				try {
					YouScopeClientImpl.getMicroscope().addConfigurationListener(configurationListener);
				} catch (RemoteException e) {
					ClientSystem.err.println("Could not add listener which gets notified if microscope configuration changes.", e);
				}
			}
			
			@Override
			public void frameClosed() {
				try {
					YouScopeClientImpl.getMicroscope().removeConfigurationListener(configurationListener);
				} catch (RemoteException e) {
					ClientSystem.err.println("Could not remove listener which gets notified if microscope configuration changes.", e);
				}
			}
		});
		
		final Icon addButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "add device");
		final Icon deleteButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "remove device");
		
		// Initialize available device driver table
		loadedDevicesListModel = new LoadedDevicesListModel();
		loadedDevicesList = new JList<LoadedDevice>(loadedDevicesListModel);
		loadedDevicesList.setCellRenderer(new LoadedDevicesListRenderer());
		loadedDevicesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		loadedDevicesList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent mouseEvent)
			{
				showContextMenu(mouseEvent);				
			}
			@Override
	        public void mousePressed(MouseEvent mouseEvent) 
	        {
				showContextMenu(mouseEvent);
	        }
			private void showContextMenu(MouseEvent mouseEvent)
			{
	        	if (!mouseEvent.isPopupTrigger())
	            	return;
	            int index = loadedDevicesList.locationToIndex(mouseEvent.getPoint());
	            if (index < 0 || index >= loadedDevices.size()) 
	            	return;
	            loadedDevicesList.setSelectedIndex(index);
	            final LoadedDevice device = loadedDevices.get(index);
	            
                JPopupMenu menu = new JPopupMenu();
                if(device.deviceType == DeviceType.HubDevice)
                {
                	JMenuItem peripheralsItem;
                    if(addButtonIcon == null)
                    	peripheralsItem = new JMenuItem("Add Peripherals");
                    else
                    	peripheralsItem = new JMenuItem("Add Peripherals", addButtonIcon);
                    peripheralsItem.addActionListener(new ActionListener() 
                    {
                        @Override
        				public void actionPerformed(ActionEvent arg0)
        				{
                        	Device hubTemp;
							try 
							{
								hubTemp = YouScopeClientImpl.getMicroscope().getDevice(device.deviceName);
							} 
							catch (RemoteException|DeviceException e) 
							{
								ClientSystem.err.println("Could not get hub with ID " + device.deviceName, e);
								return;
							}
                        	if(!(hubTemp instanceof HubDevice))
                        	{
                        		ClientSystem.err.println("Device " + device.deviceName + " is not a hub device, but identifies as such.");
                        		return;
                        	}
                        	final HubDevice hub = (HubDevice)hubTemp;
                        	
                        	
                			final YouScopeFrame childFrame = ManageTabLoadedDevices.this.frame.createModalChildFrame();
                			
                			// Initialize frame
                			childFrame.setTitle("Add Peripheral Devices");
                			childFrame.setResizable(true);
                			childFrame.setClosable(true);
                			childFrame.setMaximizable(false);
                			// Initialize content.
                			childFrame.startInitializing();
                			new Thread(new Runnable()
                				{
                					@Override
                					public void run()
                					{
                						PeripheralDevicesPage peripheralsPage = new PeripheralDevicesPage(hub, childFrame);
                						childFrame.setContentPane(peripheralsPage);
                						childFrame.pack();
                						childFrame.setSize(new Dimension(540, 500));
                						childFrame.endLoading();
                					}
                				}).start();
                			childFrame.setVisible(true);
        				}
                    });
                    menu.add(peripheralsItem);
                }
                JMenuItem deleteItem;
                if(deleteButtonIcon == null)
                	deleteItem = new JMenuItem("Remove Device");
                else
                	deleteItem = new JMenuItem("Remove Device", deleteButtonIcon);
                deleteItem.addActionListener(new ActionListener() 
                {
                    @Override
    				public void actionPerformed(ActionEvent arg0)
    				{
    					int shouldDelete = JOptionPane.showConfirmDialog(null, "Should the device " + device.deviceName + " really be deleted?", "Delete Device", JOptionPane. YES_NO_OPTION);
                		if(shouldDelete != JOptionPane.YES_OPTION)
                			return;
                		
    					try
    					{
    						YouScopeClientImpl.getMicroscope().getDeviceLoader().removeDevice(device.deviceName);
    					}
    					catch(Exception e)
    					{
    						ClientSystem.err.println("Could not remove device " + device.deviceName + ".", e);
    					}
    					somethingChanged = true;
    					initializeContent();
    				}
                });
                menu.add(deleteItem);
                Point relativeMousePosition = loadedDevicesList.getMousePosition();
                if(relativeMousePosition == null)
                	return;
                menu.show(loadedDevicesList, relativeMousePosition.x, relativeMousePosition.y);
	        }
	    });
		
		// Buttons panel
		JButton addButton;
		if(addButtonIcon == null)
			addButton= new JButton("Add Device");
		else
			addButton = new JButton("Add Device", addButtonIcon);
		addButton.setHorizontalAlignment(SwingConstants.LEFT);
		addButton.setOpaque(false);
		addButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					YouScopeFrame childFrame = ManageTabLoadedDevices.this.frame.createModalChildFrame();
					ManageAddDeviceFrame addFrame = new ManageAddDeviceFrame();
					addFrame.initializeFrame(childFrame);
					childFrame.setVisible(true);
				}
			});
		
		// Buttons panel
		final JButton peripheralButton;
		if(addButtonIcon == null)
			peripheralButton= new JButton("Add Peripherals");
		else
			peripheralButton = new JButton("Add Peripherals", addButtonIcon);
		peripheralButton.setEnabled(false);
		peripheralButton.setHorizontalAlignment(SwingConstants.LEFT);
		peripheralButton.setOpaque(false);
		peripheralButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					int row = loadedDevicesList.getSelectedIndex();
					if(row < 0 || row >= loadedDevices.size())
						return;
					
					LoadedDevice device = loadedDevices.elementAt(row);
					
					Device hubTemp;
					try 
					{
						hubTemp = YouScopeClientImpl.getMicroscope().getDevice(device.deviceName);
					} 
					catch (RemoteException|DeviceException e) 
					{
						ClientSystem.err.println("Could not get hub with ID " + device.deviceName, e);
						return;
					}
                	if(!(hubTemp instanceof HubDevice))
                	{
                		ClientSystem.err.println("Device " + device.deviceName + " is not a hub device, but identifies as such.");
                		return;
                	}
                	final HubDevice hub = (HubDevice)hubTemp;
                	
                	
        			final YouScopeFrame childFrame = ManageTabLoadedDevices.this.frame.createModalChildFrame();
        			
        			// Initialize frame
        			childFrame.setTitle("Add Peripheral Devices");
        			childFrame.setResizable(true);
        			childFrame.setClosable(true);
        			childFrame.setMaximizable(false);
        			// Initialize content.
        			childFrame.startInitializing();
        			new Thread(new Runnable()
        				{
        					@Override
        					public void run()
        					{
        						PeripheralDevicesPage peripheralsPage = new PeripheralDevicesPage(hub, childFrame);
        						childFrame.setContentPane(peripheralsPage);
        						childFrame.pack();
        						childFrame.setSize(new Dimension(540, 500));
        						childFrame.endLoading();
        					}
        				}).start();
        			childFrame.setVisible(true);
				}
			});
		
		JButton deleteButton;
		if(deleteButtonIcon == null)
			deleteButton= new JButton("Remove Device");
		else
			deleteButton = new JButton("Remove Device", deleteButtonIcon);
		deleteButton.setHorizontalAlignment(SwingConstants.LEFT);
		deleteButton.setOpaque(false);
		deleteButton.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					int row = loadedDevicesList.getSelectedIndex();
					if(row < 0 || row >= loadedDevices.size())
						return;
					
					LoadedDevice device = loadedDevices.elementAt(row);
					int shouldDelete = JOptionPane.showConfirmDialog(null, "Should the device " + device.deviceName + " really be deleted?", "Delete Device", JOptionPane. YES_NO_OPTION);
            		if(shouldDelete != JOptionPane.YES_OPTION)
            			return;
            		
					try
					{
						YouScopeClientImpl.getMicroscope().getDeviceLoader().removeDevice(device.deviceName);
					}
					catch(Exception e)
					{
						ClientSystem.err.println("Could not remove device " + device.deviceName + ".", e);
					}
					somethingChanged = true;
					initializeContent();
				}
			});
		GridBagLayout buttonLayout = new GridBagLayout();
        JPanel buttonPanel = new JPanel(buttonLayout);
        buttonPanel.setOpaque(false);
        StandardFormats.addGridBagElement(addButton, buttonLayout, newLineConstr, buttonPanel);
        StandardFormats.addGridBagElement(peripheralButton, buttonLayout, newLineConstr, buttonPanel);
        StandardFormats.addGridBagElement(deleteButton, buttonLayout, newLineConstr, buttonPanel);
        JPanel emptyPanel = new JPanel();
        emptyPanel.setOpaque(false);
        StandardFormats.addGridBagElement(emptyPanel, buttonLayout, bottomConstr, buttonPanel);
		
        DescriptionPanel descriptionPanel = new DescriptionPanel("Description", "YouScope communicates with the microscope by using so called device drivers. Thereby, a driver represents a small part of the microscope, like the stage, a shutter, a light source, and similar.\n"
        		+ "The drivers of all devices which should be controlled by YouScope have to be loaded/added and separately configured. Thereby, you can assign a unique human readable name to every device with which it can later-on be identified.\n"
        		+ "Some device drivers might depend on others, and, thus, have to be loaded after the drivers they depend on. This includes drivers of devices connected to a serial port, for which the serial port driver has to be installed a priori, or devices communicating over a hub.\n"
        		+ "To get information how a specific driver or drivers of a device have to be installed, visit the respective site at http://www.micro-manager.org (YouScope uses the device drivers from the excellent open-source project microManager).");
        
		// Setup layout
		setLayout(new BorderLayout());
		DynamicPanel mainPanel = new DynamicPanel();
		mainPanel.setOpaque(false);
		JScrollPane scrollPane = new JScrollPane(descriptionPanel);
		scrollPane.setPreferredSize(new Dimension(400, 150));
		mainPanel.add(scrollPane);
		mainPanel.add(new JLabel("Loaded Devices:"));
		JPanel loadedDevicesPanel = new JPanel(new BorderLayout(5, 5));
		loadedDevicesPanel.setOpaque(false);
		loadedDevicesPanel.add(new JScrollPane(loadedDevicesList), BorderLayout.CENTER);
		loadedDevicesPanel.add(buttonPanel, BorderLayout.EAST);
		mainPanel.addFill(loadedDevicesPanel);
		add(mainPanel, BorderLayout.CENTER);
		
		loadedDevicesList.addListSelectionListener(new ListSelectionListener() 
		{
			@Override
			public void valueChanged(ListSelectionEvent e) 
			{
				if(e.getValueIsAdjusting())
					return;
				int row = loadedDevicesList.getSelectedIndex();
				if(row < 0 || row >= loadedDevices.size())
					peripheralButton.setEnabled(false);
				
				LoadedDevice device = loadedDevices.elementAt(row);
				if(device.deviceType == DeviceType.HubDevice)
					peripheralButton.setEnabled(true);
				else
					peripheralButton.setEnabled(false);
			}
		});
	}
	
	private final class LoadedDevice
	{
		public final String deviceName;
		public final String driverLibrary;
		public final String driverName;
		public final DeviceType deviceType;
		LoadedDevice(String deviceName, String driverLibrary, String driverName, DeviceType deviceType)
		{
			this.deviceName = deviceName;
			this.driverLibrary = driverLibrary;
			this.driverName = driverName;
			this.deviceType = deviceType;
		}
	}
	
	private class LoadedDevicesListRenderer extends JPanel implements ListCellRenderer<LoadedDevice>
	{
	     /**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 911402337440330543L;

		private JLabel libraryLabel = new JLabel();
		private JLabel deviceLabel = new JLabel();
		private JLabel nameLabel = new JLabel("");
		public LoadedDevicesListRenderer() 
	    {
			 setOpaque(true);
	         setLayout(new BorderLayout());
			 JPanel topPanel = new JPanel(new GridLayout(1, 3, 0, 0));
			 topPanel.setOpaque(false);
			 topPanel.add(nameLabel);
			 topPanel.add(libraryLabel);
			 topPanel.add(deviceLabel);
			 add(topPanel, BorderLayout.CENTER);
			 Icon icon = ImageLoadingTools.getResourceIcon("bonus/icons-24/wooden-box-label.png", "device");
			 if(icon != null)
				 add(new JLabel(icon), BorderLayout.WEST);
			 setBorder(new MatteBorder(0, 0, 1, 0, Color.DARK_GRAY));
	    }

	     @Override
	     public Component getListCellRendererComponent(JList<? extends LoadedDevice> list,
	    		 										LoadedDevice device,
	                                                   int index,
	                                                   boolean isSelected,
	                                                   boolean cellHasFocus) 
	     {
	    	 isSelected = loadedDevicesList.isSelectedIndex(index);
	    	 
	    	// Load data
	    	
			libraryLabel.setText("<html>Library:<br /><i>" + device.driverLibrary + "</i></html>");
			deviceLabel.setText("<html>Driver:<br /><i>" + device.driverName + "</i></html>");
			nameLabel.setText("<html>Device:<br /><i>" + device.deviceName + "</i></html>");
			if(isSelected)
			{
				setBackground(loadedDevicesList.getSelectionBackground());
		    	setForeground(loadedDevicesList.getSelectionForeground());
			}
			else
			{
				setBackground(loadedDevicesList.getBackground());
	    		setForeground(loadedDevicesList.getForeground());
			}
			return this;
	     }
	}
	private class LoadedDevicesListModel extends DefaultListModel<LoadedDevice>
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -933873118426200597L;
		@Override
		public LoadedDevice getElementAt(int index)
		{
			if(index < 0 || index >= loadedDevices.size())
				return null;
			return loadedDevices.elementAt(index);
		}

		@Override
		public int getSize()
		{
			return loadedDevices.size();
		}
		public void fireContentsChanged()
		{
			super.fireContentsChanged(this, 0, getSize()-1);
		}
	}
	
	@Override
	public void initializeContent()
	{
		loadedDevices.clear();
		try
		{
			for(Device device : YouScopeClientImpl.getMicroscope().getDevices())
			{
				loadedDevices.addElement(new LoadedDevice(device.getDeviceID(), device.getLibraryID(), device.getDriverID(), device.getType()));
			}
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not update loaded devices.", e);
		}
		loadedDevicesListModel.fireContentsChanged();
	}
	@Override
	public boolean storeContent()
	{
		return somethingChanged;
	}
}
