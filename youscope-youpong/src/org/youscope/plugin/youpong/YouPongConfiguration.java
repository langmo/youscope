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
package org.youscope.plugin.youpong;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.FloatProperty;
import org.youscope.common.microscope.Property;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class YouPongConfiguration extends JPanel
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -5644518678024967329L;
	
	private final YouScopeServer server;
	private final YouScopeClient client;
	
	private final JComboBox<String>								channelGroupField		= new JComboBox<String>();
	private final JComboBox<String>								channelField			= new JComboBox<String>();
	
	private final JComboBox<ControlProperty>								player1ControlField			= new JComboBox<ControlProperty>();
	private final JComboBox<ControlProperty>								player2ControlField			= new JComboBox<ControlProperty>();
	private final JFormattedTextField					player1MaxField			= new JFormattedTextField(StandardFormats.getDoubleFormat());
	private final JFormattedTextField					player1MinField			= new JFormattedTextField(StandardFormats.getDoubleFormat());
	private final JFormattedTextField					player2MaxField			= new JFormattedTextField(StandardFormats.getDoubleFormat());
	private final JFormattedTextField					player2MinField			= new JFormattedTextField(StandardFormats.getDoubleFormat());
	
	
	private final JFormattedTextField					exposureField			= new JFormattedTextField(StandardFormats.getDoubleFormat());
	private final JFormattedTextField					imagingPeriodField			= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private ArrayList<ActionListener> imagingConfigurationListeners = new ArrayList<ActionListener>();
	private ArrayList<ActionListener> controlConfigurationListeners = new ArrayList<ActionListener>();
	private ArrayList<ActionListener> configurationFinishedListeners = new ArrayList<ActionListener>();
	
	private volatile boolean channelChanging = false;
	private volatile boolean controlChanging = false;
		
	public YouPongConfiguration(YouScopeServer server, YouScopeClient client)
	{
		this.server = server;
		this.client = client;
		setOpaque(false);
		setBorder(new LineBorder(Color.BLACK, 1));
	}

	@Override
	protected void paintComponent(Graphics g)
	{
		//super.paintComponent(g);
		
		g.setColor(new Color(1f, 1f, 1f, 0.5f));
		g.fillRect(0, 0, getWidth(), getHeight());
	}
	
	private void loadChannelGroupNames()
	{
		String[] configGroupNames = null;
		try
		{
			configGroupNames = server.getMicroscope().getChannelManager().getChannelGroupIDs();
		}
		catch(Exception e)
		{
			client.sendError("Could not obtain config group names.", e);
		}

		if(configGroupNames == null || configGroupNames.length <= 0)
		{
			configGroupNames = new String[] {""};
		}

		channelGroupField.removeAllItems();
		for(String configGroupName : configGroupNames)
		{
			channelGroupField.addItem(configGroupName);
		}
	}

	private void loadChannels()
	{
		channelChanging = true;
		String[] channelNames = null;

		Object selectedGroup = channelGroupField.getSelectedItem();
		if(selectedGroup != null && selectedGroup.toString().length() > 0)
		{
			try
			{
				Channel[] channels = server.getMicroscope().getChannelManager().getChannels(selectedGroup.toString()); 
				channelNames = new String[channels.length];
				for(int i=0; i<channels.length; i++)
				{
					channelNames[i] = channels[i].getChannelID();
				}
			}
			catch(Exception e)
			{
				client.sendError("Could not obtain channel names of microscope.", e);
			}
		}

		if(channelNames == null || channelNames.length <= 0)
		{
			channelNames = new String[] {""};
		}

		channelField.removeAllItems();
		for(String channelName : channelNames)
		{
			channelField.addItem(channelName);
		}
		channelChanging = false;
		imagingSettingsChanged();
	}
	
	private class ControlProperty
	{
		public final String deviceID;
		public final String propertyID;
		public ControlProperty(String deviceID, String propertyID)
		{
			this.deviceID = deviceID;
			this.propertyID = propertyID;
		}
		
		@Override
		public String toString()
		{
			return deviceID + "." + propertyID;
		}
	}
	
	private void loadControls()
	{
		Vector<ControlProperty> controlProperties = new Vector<ControlProperty>();
		try
		{
			for(Device device : server.getMicroscope().getDevices())
			{
				String deviceName = device.getDeviceID();
				for(Property property : device.getProperties())
				{
					if(property instanceof FloatProperty)
					{
						controlProperties.addElement(new ControlProperty(deviceName, property.getPropertyID()));
					}
				}
			}
		}
		catch(RemoteException e)
		{
			client.sendError("Could not load devices properties which can be used as inputs for YouPong.", e);
		}
		if(controlProperties.size() <= 0)
		{
			controlProperties.add(new ControlProperty("", ""));
		}
		
		player1ControlField.removeAllItems();
		player2ControlField.removeAllItems();
		for(ControlProperty controlProperty : controlProperties)
		{
			player1ControlField.addItem(controlProperty);
			player2ControlField.addItem(controlProperty);
		}
	}
	private void loadControlMinMax(boolean firstPlayer)
	{
		ControlProperty controlProperty;
		if(firstPlayer)
		{
			controlProperty = (ControlProperty)player1ControlField.getSelectedItem();
		}
		else
		{
			controlProperty = (ControlProperty)player2ControlField.getSelectedItem();
		}
		if(controlProperty.deviceID.length() <= 0 || controlProperty.propertyID.length() <= 0)
			return;
		
		double currentVal;
		try
		{
			currentVal = ((FloatProperty)server.getMicroscope().getDevice(controlProperty.deviceID).getProperty(controlProperty.propertyID)).getFloatValue();
		}
		catch(Exception e)
		{
			client.sendError("Could not obtain current value of device property " + controlProperty.toString() + ".", e);
			return;
		}
		
		if(firstPlayer)
		{
			player1MinField.setValue(currentVal - 10.0);
			player1MaxField.setValue(currentVal + 10.0);
		}
		else
		{
			player2MinField.setValue(currentVal - 10.0);
			player2MaxField.setValue(currentVal + 10.0);
		}
	}
	
	public void createUI()
	{
		setLayout(new BorderLayout());
		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagConstraints bottomContstr = StandardFormats.getBottomContstraint();
		
		// First panel : imaging
		GridBagLayout imagingLayout = new GridBagLayout();
		JPanel imagingPanel = new JPanel(imagingLayout);
		imagingPanel.setOpaque(false);
		imagingPanel.setBorder(new TitledBorder("Imaging Settings"));
		StandardFormats.addGridBagElement(new JLabel("Channel Group:"), imagingLayout, newLineConstr, imagingPanel);
		StandardFormats.addGridBagElement(channelGroupField, imagingLayout, newLineConstr, imagingPanel);
		StandardFormats.addGridBagElement(new JLabel("Channel:"), imagingLayout, newLineConstr, imagingPanel);
		StandardFormats.addGridBagElement(channelField, imagingLayout, newLineConstr, imagingPanel);
		StandardFormats.addGridBagElement(new JLabel("Exposure (ms):"), imagingLayout, newLineConstr, imagingPanel);
		exposureField.setValue(20.0);
		exposureField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				imagingSettingsChanged();
			}
		});
		StandardFormats.addGridBagElement(exposureField, imagingLayout, newLineConstr, imagingPanel);
		StandardFormats.addGridBagElement(new JLabel("Imaging Period (ms):"), imagingLayout, newLineConstr, imagingPanel);
		imagingPeriodField.setValue(300.0);
		imagingPeriodField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				imagingSettingsChanged();
			}
		});
		StandardFormats.addGridBagElement(imagingPeriodField, imagingLayout, newLineConstr, imagingPanel);
		JPanel emptyPanel = new JPanel();
		emptyPanel.setOpaque(false);
		StandardFormats.addGridBagElement(emptyPanel, imagingLayout, bottomContstr, imagingPanel);
		
		// Second panel : player 1
		GridBagLayout player1Layout = new GridBagLayout();
		JPanel player1Panel = new JPanel(player1Layout);
		player1Panel.setOpaque(false);
		player1Panel.setBorder(new TitledBorder("Player 1"));
		StandardFormats.addGridBagElement(new JLabel("Device property to control paddle:"), player1Layout, newLineConstr, player1Panel);
		StandardFormats.addGridBagElement(player1ControlField, player1Layout, newLineConstr, player1Panel);
		StandardFormats.addGridBagElement(new JLabel("Minimal property value:"), player1Layout, newLineConstr, player1Panel);
		StandardFormats.addGridBagElement(player1MinField, player1Layout, newLineConstr, player1Panel);
		StandardFormats.addGridBagElement(new JLabel("Maximal property value:"), player1Layout, newLineConstr, player1Panel);
		StandardFormats.addGridBagElement(player1MaxField, player1Layout, newLineConstr, player1Panel);
		emptyPanel = new JPanel();
		emptyPanel.setOpaque(false);
		StandardFormats.addGridBagElement(emptyPanel, player1Layout, bottomContstr, player1Panel);
		
		
		// Third panel : player 2
		GridBagLayout player2Layout = new GridBagLayout();
		JPanel player2Panel = new JPanel(player2Layout);
		player2Panel.setOpaque(false);
		player2Panel.setBorder(new TitledBorder("Player 2"));
		StandardFormats.addGridBagElement(new JLabel("Device property to control paddle:"), player2Layout, newLineConstr, player2Panel);
		StandardFormats.addGridBagElement(player2ControlField, player2Layout, newLineConstr, player2Panel);
		StandardFormats.addGridBagElement(new JLabel("Minimal property value:"), player2Layout, newLineConstr, player2Panel);
		StandardFormats.addGridBagElement(player2MinField, player2Layout, newLineConstr, player2Panel);
		StandardFormats.addGridBagElement(new JLabel("Maximal property value:"), player2Layout, newLineConstr, player2Panel);
		StandardFormats.addGridBagElement(player2MaxField, player2Layout, newLineConstr, player2Panel);
		emptyPanel = new JPanel();
		emptyPanel.setOpaque(false);
		StandardFormats.addGridBagElement(emptyPanel, player2Layout, bottomContstr, player2Panel);
		
		
		JButton startButton = new JButton("Start YouPong");
		startButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				YouPongConfiguration.this.setVisible(false);
				for(ActionListener listener : configurationFinishedListeners)
				{
					listener.actionPerformed(new ActionEvent(this, 109, "Configuration finished"));
				}
			}
		});

		loadControls();
		loadControlMinMax(true);
		loadControlMinMax(false);
		loadChannelGroupNames();
		loadChannels();

		channelGroupField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				loadChannels();
			}
		});
		channelField.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent itemEvent)
			{
				if(!channelChanging)
					imagingSettingsChanged();
			}
		});
				
		player1ControlField.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent itemEvent)
			{
				controlChanging = true;
				loadControlMinMax(true);
				controlChanging = false;
				controlConfigurationChanged();
			}
		});
		player2ControlField.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent itemEvent)
			{
				controlChanging = true;
				loadControlMinMax(false);
				controlChanging = false;
				controlConfigurationChanged();
			}
		});
		
		ActionListener controlConfigurationChangedListener = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(controlChanging)
					return;
				controlConfigurationChanged();
			}
		};
		
		player1MaxField.addActionListener(controlConfigurationChangedListener);
		player1MinField.addActionListener(controlConfigurationChangedListener);
		player2MaxField.addActionListener(controlConfigurationChangedListener);
		player2MinField.addActionListener(controlConfigurationChangedListener);

		JPanel centralPanel = new JPanel(new GridLayout(1, 3, 5, 5));
		centralPanel.setOpaque(false);
		centralPanel.add(imagingPanel);
		centralPanel.add(player1Panel);
		centralPanel.add(player2Panel);

		add(new JLabel("<html><center><h1>Welcome to YouPong</h1><h3>The best way to abuse the microscope for having fun!</h3></center>", JLabel.CENTER), BorderLayout.NORTH);
		add(centralPanel, BorderLayout.CENTER);
		add(startButton, BorderLayout.SOUTH);
	}
	
	private void imagingSettingsChanged()
	{
		for(ActionListener listener : imagingConfigurationListeners)
		{
			listener.actionPerformed(new ActionEvent(this, 108, "Channel changed"));
		}
	}
	private void controlConfigurationChanged()
	{
		for(ActionListener listener : controlConfigurationListeners)
		{
			listener.actionPerformed(new ActionEvent(this, 110, "Control configuration changed"));
		}
	}
	
	String getChannelGroup()
	{
		return channelGroupField.getSelectedItem().toString();
	}
	
	String getChannel()
	{
		return channelField.getSelectedItem().toString();
	}
	
	double getExposure()
	{
		return ((Number)exposureField.getValue()).doubleValue();
	}
	
	int getImagingPeriod()
	{
		return ((Number)imagingPeriodField.getValue()).intValue();
	}
	
	String[] getPlayerControl(boolean firstPlayer)
	{
		ControlProperty controlProperty;
		if(firstPlayer)
		{
			controlProperty = (ControlProperty)player1ControlField.getSelectedItem();
		}
		else
		{
			controlProperty = (ControlProperty)player2ControlField.getSelectedItem();
		}
		if(controlProperty.deviceID.length() <= 0 || controlProperty.propertyID.length() <= 0)
			return null;
		return new String[]{controlProperty.deviceID, controlProperty.propertyID};
	}
	
	double getPlayerMin(boolean firstPlayer)
	{
		if(firstPlayer)
			return ((Number)player1MinField.getValue()).doubleValue();
		return ((Number)player2MinField.getValue()).doubleValue();
	}
	double getPlayerMax(boolean firstPlayer)
	{
		if(firstPlayer)
			return ((Number)player1MaxField.getValue()).doubleValue();
		return ((Number)player2MaxField.getValue()).doubleValue();
	}
	
	void addImagingConfigurationListener(ActionListener listener)
	{
		imagingConfigurationListeners.add(listener);
	}
	
	void removeImagingConfigurationListener(ActionListener listener)
	{
		imagingConfigurationListeners.remove(listener);
	}
	
	void addConfigurationFinishedListener(ActionListener listener)
	{
		configurationFinishedListeners.add(listener);
	}
	
	void removeConfigurationFinishedListener(ActionListener listener)
	{
		configurationFinishedListeners.remove(listener);
	}
	
	void addControlConfigurationListener(ActionListener listener)
	{
		controlConfigurationListeners.add(listener);
	}
	
	void removeControlConfigurationListener(ActionListener listener)
	{
		controlConfigurationListeners.remove(listener);
	}
}
