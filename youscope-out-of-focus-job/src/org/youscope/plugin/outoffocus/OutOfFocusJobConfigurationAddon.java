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
package org.youscope.plugin.outoffocus;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceType;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImagePanel;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 */
class OutOfFocusJobConfigurationAddon  extends ComponentAddonUIAdapter<OutOfFocusJobConfiguration>
{
    // UI Elements
    private JComboBox<String> configGroupField = new JComboBox<String>();
    private JComboBox<String> channelField  = new JComboBox<String>();
    private JComboBox<String> focusDevicesField = new JComboBox<String>();

    private JFormattedTextField exposureField = new JFormattedTextField(
            StandardFormats.getDoubleFormat());
    
    private JFormattedTextField adjustmentTimeField = new JFormattedTextField(
            StandardFormats.getIntegerFormat());

    private JTextField imageNameField = new JTextField("OOF");

    private JCheckBox saveImagesField = new JCheckBox("Save images", true);

    private JFormattedTextField offsetField = new JFormattedTextField(
            StandardFormats.getDoubleFormat());

    /**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public OutOfFocusJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ComponentMetadataAdapter<OutOfFocusJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<OutOfFocusJobConfiguration>(OutOfFocusJobConfiguration.TYPE_IDENTIFIER, 
				OutOfFocusJobConfiguration.class, 
				OutOfFocusJob.class, 
				"Out-of-focus", 
				new String[]{"Imaging"}, 
				"Changes the focus position for a given offset, takes an image and changes the focus position back.",
				"icons/image-blur.png");
	}
    
	@Override
	protected Component createUI(OutOfFocusJobConfiguration configuration) throws AddonException
	{
		setTitle("Out-of-Focus");
		setResizable(false);
		setMaximizable(false);

		GridBagLayout elementsLayout = new GridBagLayout();
		JPanel elementsPanel = new JPanel(elementsLayout);
        GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
        
        StandardFormats.addGridBagElement(new JLabel("Channel Group:"), elementsLayout, newLineConstr,
        		elementsPanel);
        StandardFormats.addGridBagElement(configGroupField, elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(new JLabel("Channel:"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(channelField, elementsLayout, newLineConstr, elementsPanel);

        configGroupField.addActionListener(new ActionListener()
    	{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				loadChannels();
			}
    	});
        
        StandardFormats.addGridBagElement(new JLabel("Exposure (ms):"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(exposureField, elementsLayout, newLineConstr, elementsPanel);

        StandardFormats.addGridBagElement(new JLabel("Focus Device:"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(focusDevicesField, elementsLayout, newLineConstr, elementsPanel);
        
        StandardFormats.addGridBagElement(new JLabel("Offset:"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(offsetField, elementsLayout, newLineConstr, elementsPanel);
        
        StandardFormats.addGridBagElement(new JLabel("Adjustment Time:"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(adjustmentTimeField, elementsLayout, newLineConstr, elementsPanel);

        StandardFormats.addGridBagElement(new JLabel("Image Name:"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(imageNameField, elementsLayout, newLineConstr, elementsPanel);
        
        StandardFormats.addGridBagElement(saveImagesField, elementsLayout, newLineConstr, elementsPanel);
        
        StandardFormats.addGridBagElement(new JPanel(), elementsLayout, StandardFormats.getBottomContstraint(), elementsPanel);
        
        JButton snapImageButton = new JButton("Snap Image");
		snapImageButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String channel = (String)channelField.getSelectedItem();
				String channelGroup = (String)configGroupField.getSelectedItem();
				double exposure = ((Number)exposureField.getValue()).doubleValue();
				String focusDevice = focusDevicesField.getSelectedItem().toString();
				double offset = ((Number) offsetField.getValue()).doubleValue();
				snapImage(channelGroup, channel, exposure, focusDevice, offset);
			}
		});
		StandardFormats.addGridBagElement(snapImageButton, elementsLayout, newLineConstr, elementsPanel);
        
		loadSettingsIntoLayout(configuration);
		
		return elementsPanel;
    }

	private void snapImage(String channelGroup, String channel, double exposure, String focusDevice, double offset)
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
			private final String focusDevice;
			private final double offset;
			ImageSnapper(YouScopeFrame childFrame, ImagePanel imagePanel, String channelGroup, String channel, double exposure, String focusDevice, double offset)
			{
				this.channel = channel;
				this.channelGroup = channelGroup;
				this.childFrame = childFrame;
				this.imagePanel = imagePanel;
				this.exposure = exposure;
				this.focusDevice = focusDevice;
				this.offset = offset;
			}
			@Override
			public void run()
			{
				ImageEvent<?> imageEvent;
				try
				{
					getServer().getMicroscope().getFocusDevice(focusDevice).setRelativeFocusPosition(offset);
					imageEvent = getServer().getMicroscope().getCameraDevice().makeImage(channelGroup, channel, exposure);
					getServer().getMicroscope().getFocusDevice(focusDevice).setRelativeFocusPosition(-offset);
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
		new Thread(new ImageSnapper(childFrame, imagePanel, channelGroup, channel, exposure, focusDevice, offset)).start();
	}
	
    private void loadConfigGroupNames()
    {
    	String[] configGroupNames = null;
    	try
		{
    		configGroupNames = getServer().getMicroscope().getChannelManager().getChannelGroupIDs();
		}
		catch (Exception e)
		{
			sendErrorMessage("Could not obtain config group names.", e);
		}
		
		if (configGroupNames == null || configGroupNames.length <= 0)
        {
			configGroupNames = new String[]{""};
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
	        	Channel[] channels = getServer().getMicroscope().getChannelManager().getChannels(selectedGroup.toString()); 
				channelNames = new String[channels.length];
				for(int i=0; i<channels.length; i++)
				{
					channelNames[i] = channels[i].getChannelID();
				}
	        } 
	        catch (Exception e)
	        {
	        	sendErrorMessage("Could not obtain channel names of microscope.", e);
	        } 
    	}
    	
        if (channelNames == null || channelNames.length <= 0)
        {
            channelNames  = new String[]{""};
        }
        
        channelField.removeAllItems();
		for(String channelName : channelNames)
		{
			channelField.addItem(channelName);
		}
    }
    private void loadSettingsIntoLayout(OutOfFocusJobConfiguration configuration)
    {
        // Set state
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
        
        loadConfigGroupNames();
        if(configuration.getChannelGroup() != null)
        {
        	for (int i = 0; i < configGroupField.getItemCount(); i++)
            {
                if (configuration.getChannelGroup().compareTo(configGroupField.getItemAt(i).toString()) == 0)
                	configGroupField.setSelectedIndex(i);
            }
        }
        else
        {
        	loadChannels();
        }
        if (configuration.getChannel() != null)
        {
            for (int i = 0; i < channelField.getItemCount(); i++)
            {
                if (configuration.getChannel().compareTo(channelField.getItemAt(i).toString()) == 0)
                    channelField.setSelectedIndex(i);
            }
        }
        exposureField.setValue(configuration.getExposure());
        imageNameField.setText(configuration.getImageSaveName());
        if(configuration.getFocusConfiguration() != null)
        	adjustmentTimeField.setValue(configuration.getFocusConfiguration().getAdjustmentTime());
        else
        	adjustmentTimeField.setValue(0);

        saveImagesField.setSelected(configuration.isSaveImages());
        offsetField.setValue(configuration.getPosition());
    }
    
    private void loadFocusDevices()
	{
    	String[] focusDevices;
    	try
		{
    		Device[] devices = getServer().getMicroscope().getDevices(DeviceType.StageDevice);
    		focusDevices = new String[devices.length]; 
    		for(int i=0; i<devices.length; i++)
    		{
    			focusDevices[i] = devices[i].getDeviceID();
    		}
		}
		catch (Exception e)
		{
			sendErrorMessage("Could not obtain focus device names.", e);
			focusDevices = new String[0];
		}
		
		focusDevicesField.removeAllItems();
		for(String focusDevice : focusDevices)
		{
			focusDevicesField.addItem(focusDevice);
		}
	}

	@Override
	protected void commitChanges(OutOfFocusJobConfiguration configuration) 
	{
		// Get parameters
        String channel = channelField.getSelectedItem().toString();
        String configGroup = configGroupField.getSelectedItem().toString();
        double exposure = ((Number) exposureField.getValue()).doubleValue();
        boolean saveImages = saveImagesField.isSelected();
        String imageName = imageNameField.getText();
        double offset = ((Number) offsetField.getValue()).doubleValue();
        String focusDevice = focusDevicesField.getSelectedItem().toString();
        int adjustmentTime = ((Number)adjustmentTimeField.getValue()).intValue();

        // Store parameters
        configuration.setChannel(configGroup, channel);
        configuration.setExposure(exposure);
        FocusConfiguration focusConfiguration = new FocusConfiguration();
        focusConfiguration.setAdjustmentTime(adjustmentTime);
        focusConfiguration.setFocusDevice(focusDevice);
        configuration.setFocusConfiguration(focusConfiguration);
        configuration.setImageSaveName(imageName);
        configuration.setPosition(offset);
        configuration.setSaveImages(saveImages);
	}

	@Override
	protected void initializeDefaultConfiguration(OutOfFocusJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
