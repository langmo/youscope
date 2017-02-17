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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.microscope.Device;
import org.youscope.uielements.DescriptionPanel;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class ManageTabLoadedDevices extends ManageTabElement implements ActionListener
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
		
		// Initialize available device driver table
		loadedDevicesListModel = new LoadedDevicesListModel();
		loadedDevicesList = new JList<LoadedDevice>(loadedDevicesListModel);
		loadedDevicesList.setCellRenderer(new LoadedDevicesListRenderer());
		loadedDevicesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		// Buttons panel
		Icon addButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "add device");
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
					ManageAddDeviceFrame addFrame = new ManageAddDeviceFrame(childFrame);
					addFrame.addDevicesChangedListener(ManageTabLoadedDevices.this);
					childFrame.setVisible(true);
				}
			});
		
		Icon deleteButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "remove device");
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
					int row = loadedDevicesList.getSelectedIndex();//.getSelectedRow();
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
		
	}
	
	private final class LoadedDevice
	{
		public final String deviceName;
		public final String driverLibrary;
		public final String driverName;
		LoadedDevice(String deviceName, String driverLibrary, String driverName)
		{
			this.deviceName = deviceName;
			this.driverLibrary = driverLibrary;
			this.driverName = driverName;
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
	public void actionPerformed(ActionEvent e)
	{
		somethingChanged = true;
		initializeContent();
	}
	@Override
	public void initializeContent()
	{
		loadedDevices.clear();
		try
		{
			for(Device device : YouScopeClientImpl.getMicroscope().getDevices())
			{
				loadedDevices.addElement(new LoadedDevice(device.getDeviceID(), device.getLibraryID(), device.getDriverID()));
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
