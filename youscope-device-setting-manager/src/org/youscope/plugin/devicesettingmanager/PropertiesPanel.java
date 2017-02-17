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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.microscope.FloatProperty;
import org.youscope.common.microscope.IntegerProperty;
import org.youscope.common.microscope.Property;
import org.youscope.common.microscope.ReadOnlyProperty;
import org.youscope.common.microscope.SelectableProperty;
import org.youscope.common.microscope.StringProperty;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.IntegerTextField;

/**
 * @author Moritz Lang
 *
 */
class PropertiesPanel extends JPanel
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 957877788928884617L;
	
	private final YouScopeServer server;
	private final YouScopeClient client;
	private final WaitPane waitPane = new WaitPane();
	
	private volatile String nextSelectedDevice = null;
	
	PropertiesPanel(YouScopeClient client, YouScopeServer server)
	{
		super(new BorderLayout());
		this.server = server;
		this.client = client;
		setBorder(new TitledBorder("Device Properties"));
		
		setContent(null);
	}
	
	private void setContent(JComponent content)
	{
		class ContentSetter implements Runnable
		{
			public final JComponent content;
			ContentSetter(JComponent content)
			{
				this.content = content;
			}
			@Override
			public void run()
			{
				if(content == null)
				{
					PropertiesPanel.this.removeAll();
					PropertiesPanel.this.add(waitPane, BorderLayout.CENTER);
					waitPane.activate(true);
				}
				else
				{
					waitPane.activate(false);
					PropertiesPanel.this.removeAll();
					PropertiesPanel.this.add(new JScrollPane(content), BorderLayout.CENTER);
					
				}
				revalidate();
			}
		}
		if(SwingUtilities.isEventDispatchThread())
			new ContentSetter(content).run();
		else
		{
			try
			{
				SwingUtilities.invokeAndWait(new ContentSetter(content));
			}
			catch(Exception e)
			{
				client.sendError("Could not actualize device properties menu.", e);
				return;
			}
		}
	}
	
	public void setSelectedDevice(String device)
	{
		class DeviceSettingsLoader implements Runnable
		{
			private final String device;
			DeviceSettingsLoader(String device)
			{
				this.device = device;
			}
			@Override
			public void run()
			{
				synchronized(PropertiesPanel.this)
				{
					// Prevent old threads from actualizing new data, as well as loading the same device settings more than once.
					if(nextSelectedDevice == device)
						return;
					nextSelectedDevice = device;
				}
				setContent(null);
				
				JComponent contentPanel;
				if(device == null)
					contentPanel = new JLabel("Select Device!", SwingConstants.CENTER);
				else
					contentPanel = createPanelForDevice(device);
				synchronized(PropertiesPanel.this)
				{
					if(device != nextSelectedDevice)
						return;
					nextSelectedDevice = null;
				}
				setContent(contentPanel);
			}
		}
		new Thread(new DeviceSettingsLoader(device)).start();
	}
	
	private JPanel createPanelForDevice(String deviceName)
	{
		class DeviceSettingChangedListener implements ActionListener
		{
			private Property	property;

			DeviceSettingChangedListener(Property property)
			{
				this.property = property;
			}

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				String value;
				Object source = arg0.getSource();
				if (source instanceof JComboBox)
				{
					value = ((JComboBox<?>) source).getSelectedItem().toString();
				}
				else if (source instanceof JFormattedTextField)
				{
					value = ((JFormattedTextField) source).getValue().toString();
				}
				else if (source instanceof JTextField)
				{
					value = ((JTextField) source).getText();
				}
				else
					return;
				
				class PropertyChanger implements Runnable
				{
					private final Property property;
					private final String value;
					PropertyChanger(Property property, String value)
					{
						this.property = property;
						this.value = value;
					}
					@Override
					public void run()
					{
						try
						{
							property.setValue(value);
						}
						catch(Exception e)
						{
							client.sendError("Could not set property to " + value + ".", e);
						}
					}
				}
				new Thread(new PropertyChanger(property, value)).start();
			}
		}
		
		try
		{
			Property[] properties = server.getMicroscope().getDevice(deviceName).getProperties();
			JPanel elementsPanel = new JPanel(new GridLayout(properties.length, 2, 5, 5));
	
			for (int i = 0; i < properties.length; i++)
			{
				if (properties[i] instanceof SelectableProperty)
				{
					elementsPanel.add(new JLabel(properties[i].getPropertyID() + ":"));
					
					JComboBox<String> comboBox = new JComboBox<String>(((SelectableProperty)properties[i]).getAllowedPropertyValues());
					comboBox.setSelectedItem(properties[i].getValue());
					comboBox.addActionListener(new DeviceSettingChangedListener(properties[i]));
					elementsPanel.add(comboBox);
				}
				else if(properties[i] instanceof StringProperty)
				{
					elementsPanel.add(new JLabel(properties[i].getPropertyID() + ":"));
					JTextField textField = new JTextField(properties[i].getValue());
					elementsPanel.add(textField);
					textField.addActionListener(new DeviceSettingChangedListener(properties[i]));
				}
				else if(properties[i] instanceof IntegerProperty)
				{
					String label = properties[i].getPropertyID();
					
					int minimalValue = ((IntegerProperty)properties[i]).getLowerLimit();
					int maximalValue = ((IntegerProperty)properties[i]).getUpperLimit();
					IntegerTextField textField = new IntegerTextField(((IntegerProperty)properties[i]).getIntegerValue());
					if(minimalValue > Integer.MIN_VALUE && maximalValue < Integer.MAX_VALUE)
					{
						textField.setMinimalValue(minimalValue);
						textField.setMaximalValue(maximalValue);
						
						label += " [" + Integer.toString(minimalValue) + ", " + Integer.toString(maximalValue) + "]";
					}
					elementsPanel.add(new JLabel(label + ":"));
					elementsPanel.add(textField);
					textField.addActionListener(new DeviceSettingChangedListener(properties[i]));
				}
				else if(properties[i] instanceof FloatProperty)
				{
					String label = properties[i].getPropertyID();
					
					float minimalValue = ((FloatProperty)properties[i]).getLowerLimit();
					float maximalValue = ((FloatProperty)properties[i]).getUpperLimit();
					DoubleTextField textField = new DoubleTextField(((FloatProperty)properties[i]).getFloatValue());
					if(minimalValue > Float.MIN_VALUE && maximalValue < Float.MAX_VALUE)
					{
						textField.setMinimalValue(minimalValue);
						textField.setMaximalValue(maximalValue);
						
						label += " [" + Float.toString(minimalValue) + ", " + Float.toString(maximalValue) + "]";
					}
					elementsPanel.add(new JLabel(label + ":"));
					elementsPanel.add(textField);
					textField.addActionListener(new DeviceSettingChangedListener(properties[i]));
				}
				else if(properties[i] instanceof ReadOnlyProperty)
				{
					elementsPanel.add(new JLabel(properties[i].getPropertyID() + ":"));
					JTextField textField = new JTextField(properties[i].getValue());
					elementsPanel.add(textField);
					textField.setEditable(false);
				}
			}
			JPanel propertiesPanel = new JPanel(new BorderLayout());
			propertiesPanel.add(elementsPanel, BorderLayout.NORTH);
			propertiesPanel.add(new JPanel(), BorderLayout.CENTER);
			propertiesPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			return propertiesPanel;
		}
		catch(Exception e)
		{
			client.sendError("Error loading properties of device " + deviceName + ".", e);
			return new JPanel();
		}
	}
	
	private class WaitPane extends JPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 1113023010617741626L;

		private final JLabel		messageLabel;
		private final JProgressBar		waitbar;
		private final int			messageWidth;
		private final int			messageHeight;

		WaitPane()
		{
			super(null);
			setOpaque(true);
			setBackground(Color.WHITE);
			messageLabel = new JLabel("Loading Properties", SwingConstants.CENTER);
			messageWidth = messageLabel.getPreferredSize().width;
			messageHeight = messageLabel.getPreferredSize().height;
			messageLabel.setSize(messageWidth, messageHeight);
			add(messageLabel);
			waitbar = new JProgressBar();
			waitbar.setSize(messageWidth, 20);
			add(waitbar);
		}

		public void activate(boolean activated)
		{
			if(activated)
			{
				waitbar.setIndeterminate(true);
			}
			else
			{
				waitbar.setIndeterminate(false);
			}
		}

		@Override
		public void paintComponent(Graphics grp)
		{
			grp.setColor(new Color(1.0F, 1.0F, 1.0F));
			grp.fillRect(0, 0, getWidth(), getHeight());

			int width = getWidth();
			int height = getHeight();

			messageLabel.setLocation((width - messageWidth) / 2, height / 2 - messageHeight - 2);
			waitbar.setLocation((width - messageWidth) / 2, height / 2 + 2);
			
			super.paintComponent(grp);
		}
	}

}
