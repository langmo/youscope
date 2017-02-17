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
package org.youscope.plugin.autofocus;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.Device;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImagePanel;
import org.youscope.uielements.IntegerTextField;
import org.youscope.uielements.JobsDefinitionPanel;

/**
 * @author Moritz Lang
 */
class AutoFocusJobConfigurationAddon extends ComponentAddonUIAdapter<AutoFocusJobConfiguration>
{
    private final JTextField									imageNameField				= new JTextField();

	private final JLabel										imageNameLabel				= new JLabel(
																							"Image name used for saving:");
	
	private final JCheckBox										focusTableSaveField			= new JCheckBox("Save focus scores", true);
	
	private final JCheckBox										rememberFocusField			= new JCheckBox("Center at last focal plane", true);
	private final JCheckBox										resetFocusField			= new JCheckBox("Reset focus after job execution", true);
	
	private final JTextField									focusTableNameField				= new JTextField();

	private final JLabel										focusTableNameLabel				= new JLabel(
																							"Focus-table save name (without extension):");

	private final JComboBox<String>								channelGroupField		= new JComboBox<String>();

	private final JComboBox<String>								channelField			= new JComboBox<String>();

	private final DoubleTextField					exposureField			= new DoubleTextField();
	
	private final JCheckBox								saveImagesField			= new JCheckBox("Save images", true);
	
	private final JComboBox<String> focusDevicesField = new JComboBox<String>();
	
	private JobsDefinitionPanel jobPanel;
	
	private final IntegerTextField adjustmentTimeField = new IntegerTextField();
		
	private FocusSearchAlgorithmPanel focusSearchAlgorithmConfiguration = null;
	private FocusScoreAlgorithmPanel focusScoreAlgorithmConfiguration = null;
	
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public AutoFocusJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	
	static ComponentMetadataAdapter<AutoFocusJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<AutoFocusJobConfiguration>(AutoFocusJobConfiguration.TYPE_IDENTIFIER, 
				AutoFocusJobConfiguration.class, 
				AutoFocusJob.class, "Auto-Focus", new String[]{"Misc"}, "Uses a software autofocus algorithm to find and set the optimal focal position",
				"icons/arrow-stop-090.png");
	}
    
	@Override
	protected Component createUI(AutoFocusJobConfiguration configuration) throws AddonException
	{
		setTitle("Autofocus Job");
		setResizable(true);
		setMaximizable(false);
		
		DynamicPanel imagingPanel = new DynamicPanel();
		
		imagingPanel.add(new JLabel("Channel Group:"));
		imagingPanel.add(channelGroupField);

		imagingPanel.add(new JLabel("Channel:"));
		imagingPanel.add(channelField);

		imagingPanel.add(new JLabel("Exposure (ms):"));
		imagingPanel.add(exposureField);
		
		JButton snapImageButton = new JButton("Snap Image");
		snapImageButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String channel = (String)channelField.getSelectedItem();
				String channelGroup = (String)channelGroupField.getSelectedItem();
				double exposure = ((Number)exposureField.getValue()).doubleValue();
				snapImage(channelGroup, channel, exposure);
			}
		});
		imagingPanel.add(snapImageButton);
		saveImagesField.setOpaque(false);
		imagingPanel.add(saveImagesField);
		imagingPanel.add(imageNameLabel);
		imagingPanel.add(imageNameField);
		
		imagingPanel.setBorder(new TitledBorder("Imaging for Autofocus"));
		imagingPanel.setOpaque(false);
		
		DynamicPanel generalPanel = new DynamicPanel();
		rememberFocusField.setOpaque(false);
		generalPanel.add(rememberFocusField);
		resetFocusField.setOpaque(false);
		generalPanel.add(resetFocusField);
		generalPanel.setBorder(new TitledBorder("General Settings"));
		
		DynamicPanel leftPanel = new DynamicPanel();
		leftPanel.add(imagingPanel);
		leftPanel.add(generalPanel);
		leftPanel.addFillEmpty();
		
		DynamicPanel centerPanel = new DynamicPanel();
		centerPanel.add(new JLabel("Focus Device:"));
		centerPanel.add(focusDevicesField);
        centerPanel.add(new JLabel("Adjustment Time (ms):"));
        centerPanel.add(adjustmentTimeField);
        focusSearchAlgorithmConfiguration = new FocusSearchAlgorithmPanel(getClient(), getServer(), getContainingFrame(), configuration.getFocusSearchAlgorithm());
        centerPanel.add(focusSearchAlgorithmConfiguration);
        focusScoreAlgorithmConfiguration = new FocusScoreAlgorithmPanel(getClient(), getServer(), getContainingFrame(), configuration.getFocusScoreAlgorithm());
        centerPanel.add(focusScoreAlgorithmConfiguration);
        focusTableSaveField.setOpaque(false);
        centerPanel.add(focusTableSaveField);
		centerPanel.add(focusTableNameLabel);
		centerPanel.add(focusTableNameField);
		centerPanel.addFillEmpty();
		centerPanel.setBorder(new TitledBorder("Autofocus Configuration"));
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setOpaque(false);
		rightPanel.add(new JLabel("Executed jobs in focus:"), BorderLayout.NORTH);
        jobPanel = new JobsDefinitionPanel(getClient(), getServer(), getContainingFrame());
        jobPanel.setJobs(configuration.getJobs());
        rightPanel.add(jobPanel, BorderLayout.CENTER);
        rightPanel.setBorder(new TitledBorder("Imaging Protocol"));
		
		saveImagesField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = saveImagesField.isSelected();
				imageNameLabel.setVisible(selected);
				imageNameField.setVisible(selected);
				getContainingFrame().pack();
			}
		});
		focusTableSaveField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = focusTableSaveField.isSelected();
				focusTableNameLabel.setVisible(selected);
				focusTableNameField.setVisible(selected);
				getContainingFrame().pack();
			}
		});
		
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
					if(imageNameField.getText().length() == 0 || (lastItem != null && lastItem.compareToIgnoreCase(imageNameField.getText()) == 0))
					{
						String newName = itemEvent.getItem().toString();
						if(newName.length() > 3)
							newName = newName.substring(0, 3);
						imageNameField.setText(newName);
					}
				}
			}
		});
		
		loadSettingsIntoLayout(configuration);
		
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(leftPanel, BorderLayout.WEST);
		contentPane.add(centerPanel, BorderLayout.CENTER);
		contentPane.add(rightPanel, BorderLayout.EAST); 
		contentPane.setOpaque(false);
		return contentPane; 
    }

	private void snapImage(String channelGroup, String channel, double exposure)
	{
		// Create snap image window
		ImagePanel imagePanel = new ImagePanel(getClient());
		YouScopeFrame childFrame = imagePanel.toFrame();
		getContainingFrame().addModalChildFrame(childFrame);
		childFrame.setVisible(true);
		childFrame.startLoading();
		
		// Make image
		class ImageSnapper implements Runnable
		{
			private final YouScopeFrame childFrame;
			private final ImagePanel imagePanel;
			private final String channelGroup;
			private final String channel;
			private final double exposure;
			ImageSnapper(YouScopeFrame childFrame, ImagePanel imagePanel, String channelGroup, String channel, double exposure)
			{
				this.channel = channel;
				this.channelGroup = channelGroup;
				this.childFrame = childFrame;
				this.imagePanel = imagePanel;
				this.exposure = exposure;
			}

			@Override
			public void run()
			{
				ImageEvent<?> imageEvent;
				try
				{
					imageEvent = getServer().getMicroscope().getCameraDevice().makeImage(channelGroup, channel, exposure);
				}
				catch(Exception e1)
				{
					childFrame.setToErrorState("Error occured while taking image.", e1);
					return;
				}
				if(imageEvent == null)
				{
					childFrame.setToErrorState("No image was returned by the microscope.", null);
					return;
				}
				
				// Show image
				childFrame.endLoading();
				imagePanel.setImage(imageEvent);
			}
		}
		new Thread(new ImageSnapper(childFrame, imagePanel, channelGroup, channel, exposure)).start();
	}
	
	private void loadSettingsIntoLayout(AutoFocusJobConfiguration configuration)
	{
		rememberFocusField.setSelected(configuration.isRememberFocus());
		resetFocusField.setSelected(configuration.isResetFocusAfterSearch());
		
		loadFocusDevices();
        if(configuration.getFocusConfiguration() != null && configuration.getFocusConfiguration().getFocusDevice() != null)
        {
        	String focusDevice = configuration.getFocusConfiguration().getFocusDevice();
        	for (int i = 0; i < focusDevicesField.getItemCount(); i++)
            {
                if (focusDevice.compareTo(focusDevicesField.getItemAt(i).toString()) == 0)
                	focusDevicesField.setSelectedIndex(i);
            }
        }
        if(configuration.getFocusConfiguration() != null)
        	adjustmentTimeField.setValue(configuration.getFocusConfiguration().getAdjustmentTime());
        else
        	adjustmentTimeField.setValue(0);
		
		loadConfigGroupNames();
		String configGroup = configuration.getChannelGroup();
		if(configGroup == null || configGroup.length() < 1)
			configGroup = (String) getClient().getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_CHANNEL_GROUP);
		for(int i = 0; i < channelGroupField.getItemCount(); i++)
		{
			if(configGroup.compareTo(channelGroupField.getItemAt(i).toString()) == 0)
				channelGroupField.setSelectedIndex(i);
		}

		loadChannels();
		for(int i = 0; i < channelField.getItemCount(); i++)
		{
			if(configuration.getChannel().compareTo(channelField.getItemAt(i).toString()) == 0)
				channelField.setSelectedIndex(i);
		}
		
		String imageName = configuration.getImageSaveName();
		if (imageName == null || imageName.length() < 1)
		{
			imageName = AutoFocusJobConfiguration.IMAGE_SAVE_NAME_DEFAULT;
		}
		imageNameField.setText(imageName);
		
		String tableName = configuration.getFocusTableSaveName();
		if (tableName == null || tableName.length() < 1)
		{
			tableName = AutoFocusJobConfiguration.FOCUS_TABLE_DEFAULT_NAME;
		}
		focusTableNameField.setText(tableName);
		
		exposureField.setValue(configuration.getExposure());
		
		saveImagesField.setSelected(configuration.getImageSaveName() != null);
		boolean selected = saveImagesField.isSelected();
		imageNameLabel.setVisible(selected);
		imageNameField.setVisible(selected);
		focusTableSaveField.setSelected(configuration.getFocusTableSaveName() != null);
	}
	
    @Override
	protected void commitChanges(AutoFocusJobConfiguration configuration)
	{
		configuration.setRememberFocus(rememberFocusField.isSelected());
		configuration.setResetFocusAfterSearch(resetFocusField.isSelected());
    	    	
    	String focusDevice = focusDevicesField.getSelectedItem().toString();
        int adjustmentTime = ((Number)adjustmentTimeField.getValue()).intValue();
    	FocusConfiguration focusConfiguration = new FocusConfiguration();
        focusConfiguration.setAdjustmentTime(adjustmentTime);
        focusConfiguration.setFocusDevice(focusDevice);
        configuration.setFocusConfiguration(focusConfiguration);
        
        configuration.setFocusScoreAlgorithm(focusScoreAlgorithmConfiguration.getConfiguration());
		configuration.setFocusSearchAlgorithm(focusSearchAlgorithmConfiguration.getConfiguration());
		
		configuration.setChannel((String)channelGroupField.getSelectedItem(), (String)channelField.getSelectedItem());
    	configuration.setExposure(((Number)exposureField.getValue()).doubleValue());
    	configuration.setImageSaveName(saveImagesField.isSelected()? imageNameField.getText() : null);
    	configuration.setFocusTableSaveName(focusTableSaveField.isSelected()? focusTableNameField.getText() : null);
    	

    	configuration.setJobs(jobPanel.getJobs());
    	
		getClient().getPropertyProvider().setProperty(StandardProperty.PROPERTY_LAST_CHANNEL_GROUP, channelGroupField.getSelectedItem());
    }
    
    private void loadFocusDevices()
	{
    	String[] focusDevices;
    	try
		{
    		Device[] devices = getServer().getMicroscope().getFocusDevices();
    		focusDevices = new String[devices.length]; 
    		for(int i=0; i<devices.length; i++)
    		{
    			focusDevices[i] = devices[i].getDeviceID();
    		}
		}
		catch (Exception e)
		{
			getClient().sendError("Could not obtain focus device names.", e);
			focusDevices = new String[0];
		}
		
		focusDevicesField.removeAllItems();
		for(String focusDevice : focusDevices)
		{
			focusDevicesField.addItem(focusDevice);
		}
	}

    
    private void loadConfigGroupNames()
	{
		String[] configGroupNames = null;
		try
		{
			configGroupNames = getServer().getMicroscope().getChannelManager().getChannelGroupIDs();
		}
		catch(Exception e)
		{
			getClient().sendError("Could not obtain config group names.", e);
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
		String[] channelNames = null;

		Object selectedGroup = channelGroupField.getSelectedItem();
		if(selectedGroup != null && selectedGroup.toString().length() > 0)
		{
			try
			{
				Channel[] channels = getServer().getMicroscope().getChannelManager().getChannels(selectedGroup.toString()); 
				channelNames = new String[channels.length];
				for(int i=0; i<channels.length; i++)
				{
					channelNames[i] = channels[i].getChannelID();
				}
			}
			catch(Exception e)
			{
				getClient().sendError("Could not obtain channel names of microscope.", e);
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
	protected void initializeDefaultConfiguration(AutoFocusJobConfiguration configuration) throws AddonException {
		// do nothing.
	}

	
}
