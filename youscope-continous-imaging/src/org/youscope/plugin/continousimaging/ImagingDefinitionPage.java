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
package org.youscope.plugin.continousimaging;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.youscope.addon.measurement.MeasurementAddonUIPage;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.microscope.Channel;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

class ImagingDefinitionPage extends MeasurementAddonUIPage<ContinousImagingMeasurementConfiguration>
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 2394611369656492466L;
	private JComboBox<String>								configGroupField		= new JComboBox<String>();

	private JComboBox<String>								channelField			= new JComboBox<String>();

	private JFormattedTextField					exposureField			= new JFormattedTextField(StandardFormats.getDoubleFormat());
	private JFormattedTextField					imagingPeriodField			= new JFormattedTextField(StandardFormats.getIntegerFormat());
	private JLabel imagingPeriodLabel = new JLabel("Imaging Period (ms):");

	private JCheckBox								saveImagesField			= new JCheckBox("Save images", true);

	private JTextField									imageNameField				= new JTextField();

	private JLabel										imageNameLabel				= new JLabel(
																							"Image name used for saving:");
	
	private JComboBox<String>									imagingSpeedField = new JComboBox<String>(new String[]{"Burst", "Given Period"});

	private final YouScopeClient client; 
	private final YouScopeServer server; 
	ImagingDefinitionPage(YouScopeClient client, YouScopeServer server)
	{
		this.client = client;
		this.server = server;
	}
	
	@Override
	public void loadData(ContinousImagingMeasurementConfiguration configuration)
	{		
		String configGroup = configuration.getChannelGroup();
		if(configGroup == null || configGroup.length() < 1)
			configGroup = (String) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_CHANNEL_GROUP);
		for(int i = 0; i < configGroupField.getItemCount(); i++)
		{
			if(configGroup.compareTo(configGroupField.getItemAt(i).toString()) == 0)
				configGroupField.setSelectedIndex(i);
		}

		for(int i = 0; i < channelField.getItemCount(); i++)
		{
			if(configuration.getChannel().compareTo(channelField.getItemAt(i).toString()) == 0)
				channelField.setSelectedIndex(i);
		}
		
		String imageName = configuration.getImageSaveName();
		if (imageName.length() < 1)
		{
			imageName = channelField.getSelectedItem().toString();
			if (imageName.length() > 3)
				imageName = imageName.substring(0, 3);
		}
		imageNameField.setText(imageName);
		
		exposureField.setValue(configuration.getExposure());
		if(configuration.getImagingPeriod() <= 0)
		{
			imagingPeriodField.setValue(1000);
			imagingPeriodField.setVisible(false);
			imagingPeriodLabel.setVisible(false);
			imagingSpeedField.setSelectedIndex(0);
		}
		else
		{
			imagingPeriodField.setValue(configuration.getImagingPeriod());
			imagingSpeedField.setSelectedIndex(1);
		}

		saveImagesField.setSelected(configuration.getSaveImages());

	}

	@Override
	public boolean saveData(ContinousImagingMeasurementConfiguration configuration)
	{
		configuration.setChannel((String)configGroupField.getSelectedItem(), (String)channelField.getSelectedItem());
		configuration.setExposure(((Number)exposureField.getValue()).doubleValue());
		configuration.setImagingPeriod(((Number)exposureField.getValue()).intValue());
		configuration.setSaveImages(saveImagesField.isSelected());
		if(imageNameField.getText().length() > 3)
			configuration.setImageSaveName(imageNameField.getText().substring(0, 3));
		else
			configuration.setImageSaveName(imageNameField.getText());
		if(imagingSpeedField.getSelectedIndex() == 0)
		{
			configuration.setImagingPeriod(0);
		}
		else
		{
			configuration.setImagingPeriod(((Number)imagingPeriodField.getValue()).intValue());
		}
		return true;
	}

	@Override
	public void setToDefault(ContinousImagingMeasurementConfiguration configuration)
	{
		// Do nothing.
	}

	@Override
	public String getPageName()
	{
		return "Imaging";
	}
	
	private void loadConfigGroupNames()
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

		configGroupField.removeAllItems();
		for(String configGroupName : configGroupNames)
		{
			configGroupField.addItem(configGroupName);
		}
	}

	private void loadChannels()
	{
		String[] channelNames = null;

		Object selectedGroup = configGroupField.getSelectedItem();
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
	}

	@Override
	public void createUI(final YouScopeFrame frame)
	{
		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		
		GridBagLayout elementsLayout = new GridBagLayout();
		setLayout(elementsLayout);
		
		StandardFormats.addGridBagElement(new JLabel("Channel Group:"), elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(configGroupField, elementsLayout, newLineConstr, this);

		StandardFormats.addGridBagElement(new JLabel("Channel:"), elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(channelField, elementsLayout, newLineConstr, this);

		StandardFormats.addGridBagElement(new JLabel("Exposure (ms):"), elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(exposureField, elementsLayout, newLineConstr, this);
		
		StandardFormats.addGridBagElement(new JLabel("Imaging Type:"), elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(imagingSpeedField, elementsLayout, newLineConstr, this);
		
		StandardFormats.addGridBagElement(imagingPeriodLabel, elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(imagingPeriodField, elementsLayout, newLineConstr, this);

		StandardFormats.addGridBagElement(saveImagesField, elementsLayout, newLineConstr, this);
		StandardFormats.addGridBagElement(imageNameLabel, elementsLayout, newLineConstr,
				this);
		StandardFormats.addGridBagElement(imageNameField, elementsLayout, newLineConstr,
				this);
					
		StandardFormats.addGridBagElement(new JPanel(), elementsLayout, StandardFormats.getBottomContstraint(), this);

		setBorder(new TitledBorder("Imaging Properties"));
		
		loadConfigGroupNames();
		loadChannels();
		
		saveImagesField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = saveImagesField.isSelected();
				imageNameLabel.setVisible(selected);
				imageNameField.setVisible(selected);

				frame.pack();
			}
		});
		
		configGroupField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				loadChannels();
			}
		});
		channelField.addItemListener(new ItemListener()
		{
			private String	lastItem	= null;

			@Override
			public void itemStateChanged(ItemEvent itemEvent)
			{
				if(itemEvent.getStateChange() == ItemEvent.DESELECTED)
				{
					lastItem = itemEvent.getItem().toString();
					if(lastItem.length() > 3)
						lastItem = lastItem.substring(0, 3);
				}
				else
				{
					if(lastItem != null && lastItem.compareToIgnoreCase(imageNameField.getText()) == 0)
					{
						String newName = itemEvent.getItem().toString();
						if(newName.length() > 3)
							newName = newName.substring(0, 3);
						imageNameField.setText(newName);
					}
				}
			}
		});
		imagingSpeedField.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent itemEvent)
			{
				if(imagingSpeedField.getSelectedIndex() == 0)
				{
					imagingPeriodLabel.setVisible(false);
					imagingPeriodField.setVisible(false);
				}
				else
				{
					imagingPeriodLabel.setVisible(true);
					imagingPeriodField.setVisible(true);
				}
			}
		});
	}

}
