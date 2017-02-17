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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.ShutterDevice;
import org.youscope.uielements.DescriptionPanel;
import org.youscope.uielements.DeviceSettingsPanel;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class ManageTabChannels extends ManageTabElement
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 1297549458836713214L;
	private final DeviceSettingsPanel channelOnSettingsPanel;
	private final DeviceSettingsPanel channelOffSettingsPanel;
	private final JList<Object> channelsField = new JList<Object>();
	private boolean actualizing = false;
	private boolean somethingChanged = false;
	private boolean contentChanged = false;
	
	private Channel[] channels = new Channel[0];
	private Channel currentChannel = null;
	
	private final JButton addChannelButton;
    private final JButton deleteChannelButton;
	
    private final YouScopeFrame frame;
    
    private final JCheckBox useAutoShutterField = new JCheckBox("Automatically trigger shutter.");
    private final JPanel shutterChoosePanel = new JPanel(new GridLayout(1, 2, 2, 2));
    private final JComboBox<String> autoShutterField = new JComboBox<String>();
    
    private final JCheckBox useDelayField = new JCheckBox("Use additional channel delay.");
    private final JPanel delayChoosePanel = new JPanel(new GridLayout(1, 2, 2, 2));
    private final JFormattedTextField delayField =  new JFormattedTextField(StandardFormats.getIntegerFormat());
    
	ManageTabChannels(YouScopeFrame frame)
	{
		this.frame = frame;
		
		channelsField.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				if(e.getValueIsAdjusting() || actualizing || channelsField.getSelectedIndex() < 0)
					return;
				showChannel(channels[channelsField.getSelectedIndex()]);
			}
		});
		channelsField.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		channelsField.setCellRenderer(new ChannelListRenderer());
		
		
		ActionListener settingsListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(actualizing || currentChannel == null)
					return;
				somethingChanged = true;
				contentChanged = true;
			}
		};
		channelOnSettingsPanel = new DeviceSettingsPanel(new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer(), true);
		channelOnSettingsPanel.setEditable(false);
		channelOnSettingsPanel.addActionListener(settingsListener);
		
		channelOffSettingsPanel = new DeviceSettingsPanel(new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer(), true);
		channelOffSettingsPanel.setEditable(false);
		channelOffSettingsPanel.addActionListener(settingsListener);
		
		// Buttons
		Icon addChannelIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add Channel");
		Icon deleteChannelIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Delete Channel");
        if (addChannelIcon == null)
            addChannelButton = new JButton("New");
        else
            addChannelButton = new JButton(addChannelIcon);
        addChannelButton.setOpaque(false);
        addChannelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	YouScopeFrame modalFrame = ManageTabChannels.this.frame.createModalChildFrame();
                	@SuppressWarnings("unused")
					ChannelNamingFrame channelNamingFrame = new ChannelNamingFrame(modalFrame);
                	modalFrame.setVisible(true);
                }
            });
        
        if (deleteChannelIcon == null)
            deleteChannelButton = new JButton("Delete");
        else
            deleteChannelButton = new JButton(deleteChannelIcon);
        deleteChannelButton.setOpaque(false);
        deleteChannelButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	int row = channelsField.getSelectedIndex();
					if(row < 0 || row >= channels.length)
						return;
					
					Channel channel = channels[row];
					String channelName;
					try
					{
						channelName = channel.getChannelGroupID() + "." + channel.getChannelID();
					}
					catch(RemoteException e2)
					{
						ClientSystem.err.println("Could not find selected channel.", e2);
						return;
					}
					
					int shouldDelete = JOptionPane.showConfirmDialog(null, "Should the channel " + channelName + " really be deleted?", "Delete Channel", JOptionPane. YES_NO_OPTION);
            		if(shouldDelete != JOptionPane.YES_OPTION)
            			return;
            		
					try
					{
						YouScopeClientImpl.getMicroscope().getChannelManager().removeChannel(channel.getChannelGroupID(), channel.getChannelID());
					}
					catch(Exception e1)
					{
						ClientSystem.err.println("Could not remove channel " + channel.toString() + ".", e1);
					}
					currentChannel = null;
					contentChanged = true;
					initializeContent();
                }
            });

        
        // Panel to choose auto focus device
        DynamicPanel topPanel = new DynamicPanel();
		topPanel.setOpaque(false);
		useAutoShutterField.setOpaque(false);
        useAutoShutterField.addActionListener(new ActionListener()
        {

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				shutterChoosePanel.setVisible(useAutoShutterField.isSelected());
				if(actualizing)
					return;
				somethingChanged = true;
				contentChanged = true;
			}
        	
        });
        topPanel.add(useAutoShutterField);
        shutterChoosePanel.setOpaque(false);
        shutterChoosePanel.add(new JLabel("Shutter Device:"));
        autoShutterField.addActionListener(new ActionListener()
        {

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(actualizing)
					return;
				somethingChanged = true;
				contentChanged = true;
			}
        	
        });
        shutterChoosePanel.add(autoShutterField);
        topPanel.add(shutterChoosePanel);
        
        // Panel to choose additional channel delay
        useDelayField.setOpaque(false);
        useDelayField.addActionListener(new ActionListener()
        {

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				delayChoosePanel.setVisible(useDelayField.isSelected());
				if(actualizing)
					return;
				somethingChanged = true;
				contentChanged = true;
			}
        	
        });
        topPanel.add(useDelayField);
        delayChoosePanel.setOpaque(false);
        delayChoosePanel.add(new JLabel("Channel Delay (ms):"));
        delayField.addActionListener(new ActionListener()
        {

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(actualizing)
					return;
				somethingChanged = true;
				contentChanged = true;
			}
        	
        });
        delayChoosePanel.add(delayField);
        topPanel.add(delayChoosePanel);
        topPanel.add(new JLabel("Settings activated before imaging:"));
        topPanel.addFill(channelOnSettingsPanel);
        topPanel.add(new JLabel("Settings activated after imaging:"));
        topPanel.addFill(channelOffSettingsPanel);
               
        // Buttons to add and delete channels
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 2, 2));
        buttonPanel.setOpaque(false);
        for(int i=0; i<3;i++)
        {
        	JPanel emptyPanel = new JPanel();
	        emptyPanel.setOpaque(false);
	        buttonPanel.add(emptyPanel);
        }
        buttonPanel.add(addChannelButton);
        buttonPanel.add(deleteChannelButton);
        
        
		JPanel mainPanel = new JPanel(new GridLayout(1,2,2,2));
		mainPanel.setOpaque(false);
		
		JPanel channelSelectionPanel = new JPanel(new BorderLayout());
		channelSelectionPanel.setOpaque(false);
		channelSelectionPanel.setBorder(new TitledBorder("Step 1: Select Channel"));
		channelSelectionPanel.add(new JScrollPane(channelsField), BorderLayout.CENTER);
		channelSelectionPanel.add(buttonPanel, BorderLayout.SOUTH);
		mainPanel.add(channelSelectionPanel);
		
		JPanel channelDefinitionPanel = new JPanel(new BorderLayout());
		channelDefinitionPanel.setOpaque(false);
		channelDefinitionPanel.setBorder(new TitledBorder("Step 2: Configure Channel"));
		channelDefinitionPanel.add(topPanel, BorderLayout.CENTER);
		mainPanel.add(channelDefinitionPanel);
		
		DescriptionPanel descriptionPanel = new DescriptionPanel("Description", "In YouScope, every image is taken in a specific channel. A channel is described by a set of device settings which are applied before taking an image, e.g. setting filters, and changing light sources.\n Furthermore, several device settings might be necessary to be applied after imaging in a channel, e.g. to turn of a light source to reduce bleaching when not using shutters.\nTypically, not additional device delay must be specified, since YouScope automatically synchronizes with all devices whose state is changed and the shutter before imaging. However, some devices might be slow, like turning on some lights, and then an additional delay might be specified which is applied right after applying all device settings, but before opening the shutter (if any) and imaging.\n"
				+ "Typically, a microscope has several specified channels, e.g. to image in bright-field, or to image green or red fluorescence proteins.");

		setOpaque(false);
		setLayout(new BorderLayout(5, 5));
		add(mainPanel, BorderLayout.CENTER);
		JScrollPane scrollPane = new JScrollPane(descriptionPanel);
		scrollPane.setPreferredSize(new Dimension(400, 150));
		add(scrollPane, BorderLayout.NORTH);
	}
	private class ChannelListRenderer extends JLabel implements ListCellRenderer<Object>
	{
	     /**
		 * Serial Version UID.
		 */
		 private static final long	serialVersionUID	= 911102337440330543L;
	     @Override
	     public Component getListCellRendererComponent(JList<? extends Object> list,
	    		 										Object value,
	                                                   int index,
	                                                   boolean isSelected,
	                                                   boolean cellHasFocus) 
	     {
	    	if(value instanceof Channel)
	    	{
	    		try
				{
					setText(((Channel)value).getChannelGroupID() + "." + ((Channel)value).getChannelID());
				}
				catch(RemoteException e)
				{
					ClientSystem.err.println("Could not get name of channel.", e);
					setText(value.toString());
				}
	    	}
	    	else
	    	{
	    		 setText(value.toString());
	    	}
	    	if(isSelected)
			{
				setBackground(channelsField.getSelectionBackground());
		    	setForeground(channelsField.getSelectionForeground());
			}
			else
			{
				setBackground(channelsField.getBackground());
	    		setForeground(channelsField.getForeground());
			}
	    	setOpaque(true);
			return this;
	     }
	}
	private class ChannelNamingFrame
	{
		private final JTextField channelGroupField = new JTextField("Channels");
		private final JTextField channelField = new JTextField("");
		private final YouScopeFrame frame;
		ChannelNamingFrame(YouScopeFrame frame)
		{
			this.frame = frame;
			frame.setTitle("New Channel");
			frame.setResizable(false);
			frame.setClosable(true);
			frame.setMaximizable(false);
			
			JButton addButton = new JButton("Add Channel");
			addButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						String configName = channelGroupField.getText();
						String channelName = channelField.getText();
						if(configName.length() < 1 || channelName.length()<1)
						{
							JOptionPane.showMessageDialog(null, "Channel as well as channel group name\nhave to be at least one character long.", "Invalid channel and config group name", JOptionPane.INFORMATION_MESSAGE);
							return;
						}
						try
						{
							YouScopeClientImpl.getMicroscope().getChannelManager().addChannel(configName, channelName);
						}
						catch(Exception e)
						{
							ClientSystem.err.println("Could not add channel.", e);
						}
						initializeContent();
						contentChanged = true;
						ChannelNamingFrame.this.frame.setVisible(false);
					}
				});
			
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						ChannelNamingFrame.this.frame.setVisible(false);
					}
				});
			
			JPanel elementsPanel = new JPanel(new GridLayout(2,2,2,2));
			elementsPanel.add(new JLabel("Channel Group Name:"));
			elementsPanel.add(channelGroupField);
			elementsPanel.add(new JLabel("Channel Name:"));
			elementsPanel.add(channelField);
			
			JPanel buttonsPanel = new JPanel(new GridLayout(1,2,2,2));
			buttonsPanel.add(cancelButton);
			buttonsPanel.add(addButton);
        	
			JPanel contentPane = new JPanel(new BorderLayout());
			contentPane.add(elementsPanel, BorderLayout.CENTER);
			contentPane.add(buttonsPanel, BorderLayout.SOUTH);
            frame.setContentPane(contentPane);
            frame.pack();
		}
	}
	private void showChannel(Channel channel)
	{
		actualizing = true;
		if(currentChannel != null && somethingChanged)
		{
			try
			{
				currentChannel.setChannelOnSettings(channelOnSettingsPanel.getSettings());
				currentChannel.setChannelOffSettings(channelOffSettingsPanel.getSettings());
				if(useAutoShutterField.isSelected() && autoShutterField.getSelectedItem() != null)
				{
					currentChannel.setShutter(autoShutterField.getSelectedItem().toString());
				}
				else
				{
					currentChannel.setShutter(null);
				}
				if(useDelayField.isSelected() && ((Number)delayField.getValue()).intValue() > 0)
				{
					currentChannel.setChannelTimeout(((Number)delayField.getValue()).intValue());
				}
				else
				{
					currentChannel.setChannelTimeout(0);
				}
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not save channel.", e);
			}
		}
		somethingChanged = false;
		currentChannel = channel;
		channelOnSettingsPanel.clear();
		channelOffSettingsPanel.clear();
		if(channel == null)
		{
			channelOnSettingsPanel.setEditable(false);
			channelOffSettingsPanel.setEditable(false);
			useAutoShutterField.setEnabled(false);
			autoShutterField.setEnabled(false);
			useDelayField.setEnabled(false);
			delayField.setEditable(false);
		}
		else
		{
			channelOnSettingsPanel.setEditable(true);
			channelOffSettingsPanel.setEditable(true);
			useAutoShutterField.setEnabled(true);
			autoShutterField.setEnabled(true);
			useDelayField.setEnabled(true);
			delayField.setEditable(true);
			try
			{
				channelOnSettingsPanel.setSettings(channel.getChannelOnSettings());
				channelOffSettingsPanel.setSettings(channel.getChannelOffSettings());
				String shutterDeviceID = channel.getShutter();
				if(shutterDeviceID == null)
				{
					useAutoShutterField.setSelected(false);
					shutterChoosePanel.setVisible(false);
				}
				else
				{
					useAutoShutterField.setSelected(true);
					shutterChoosePanel.setVisible(true);
					autoShutterField.setSelectedItem(shutterDeviceID);
				}
				int delay = channel.getChannelTimeout();
				if(delay > 0)
				{
					useDelayField.setSelected(true);
					delayField.setValue(delay);
					delayChoosePanel.setVisible(true);
				}
				else
				{
					useDelayField.setSelected(false);
					delayField.setValue(0);
					delayChoosePanel.setVisible(false);
				}
			}
			catch(Exception e)
			{
				ClientSystem.err.println("Could not get device settings of selected channel.", e);
			}					
		}
		actualizing = false;
	}
	
	
	@Override
	public void initializeContent()
	{
		actualizing = true;
		try
		{
			channels = YouScopeClientImpl.getMicroscope().getChannelManager().getChannels();
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not obtain channel information.", e);
			channels = new Channel[0];
		}
		channelsField.setListData(channels);
		
		autoShutterField.removeAllItems();		
		try
		{
			ShutterDevice[] shutterDevices = YouScopeClientImpl.getMicroscope().getShutterDevices();
			for(ShutterDevice device : shutterDevices)
			{
				autoShutterField.addItem(device.getDeviceID());
			}
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not obtain list of shutter devices.", e);
		}
		
		if(channels.length > 0)
		{
			channelsField.setSelectedIndex(0);
			showChannel(channels[0]);
		}
		else
		{
			showChannel(null);
		}
		
		actualizing = false;
	}
	@Override
	public boolean storeContent()
	{
		showChannel(null);
		return contentChanged;
	}
}
