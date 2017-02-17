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
package org.youscope.plugin.imagingjob;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.job.basicjobs.ImagingJob;
import org.youscope.common.microscope.CameraDevice;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.CameraField;
import org.youscope.uielements.ChannelField;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImagePanel;

/**
 * UI to configure an imaging job.
 * @author Moritz Lang
 */
class ImagingJobConfigurationAddon extends ComponentAddonUIAdapter<ImagingJobConfiguration>
{
	private ChannelField channelField; 

	private final DoubleTextField					exposureField			= new DoubleTextField();

	private final JCheckBox								saveImagesField			= new JCheckBox("Save images", true);

	private final JTextField							nameField				= new JTextField();

	private final JLabel								nameLabel				= new JLabel("Image name:");
	
	private CameraField cameraField;

	ImagingJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	
	static ComponentMetadataAdapter<ImagingJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<ImagingJobConfiguration>(ImagingJob.DEFAULT_TYPE_IDENTIFIER, 
				ImagingJobConfiguration.class, 
				ImagingJob.class, 
				"Imaging", 
				new String[]{"Imaging"},
				"Takes an image in a given channel, using a given exposure, and saves the image to disk.",
				"icons/image.png");
	}
	
	@Override
	protected Component createUI(ImagingJobConfiguration configuration) throws AddonException
	{
		setTitle("Imaging Job");
		setResizable(false);
		setMaximizable(false);
		
		channelField = new ChannelField(configuration.getChannelConfiguration(), getClient(), getServer());
		cameraField = new CameraField(configuration.getCameraConfiguration(), getClient(), getServer());
		
		DynamicPanel contentPanel = new DynamicPanel();
		if(cameraField.isChoice())
		{
			contentPanel.add(new JLabel("Camera:"));
			contentPanel.add(cameraField);
		}
		
		contentPanel.add(new JLabel("Channel:"));
		contentPanel.add(channelField);
		
		contentPanel.add(new JLabel("Exposure (ms):"));
		contentPanel.add(exposureField);

		saveImagesField.setOpaque(false);
		contentPanel.add(saveImagesField);
		
		contentPanel.add(nameLabel);
		contentPanel.add(nameField);

		JButton snapImageButton = new JButton("Snap Image");
		snapImageButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String channel = channelField.getChannel();
				String channelGroup = channelField.getChannelGroup();
				double exposure = exposureField.getValue();
				String camera = cameraField.getCameraDevice();
				snapImage(camera, channelGroup, channel, exposure);
			}
		});
		contentPanel.add(snapImageButton);

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
					if(lastItem != null && lastItem.compareToIgnoreCase(nameField.getText()) == 0)
					{
						String newName = itemEvent.getItem().toString();
						if(newName.length() > 3)
							newName = newName.substring(0, 3);
						nameField.setText(newName);
					}
				}
			}
		});

		exposureField.setValue(configuration.getExposure());
		saveImagesField.setSelected(configuration.isSaveImages());
		if(!configuration.isSaveImages())
		{
			nameField.setVisible(false);
			nameLabel.setVisible(false);
		}
		saveImagesField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				boolean selected = saveImagesField.isSelected();
				nameField.setVisible(selected);
				nameLabel.setVisible(selected);
				notifyLayoutChanged();
			}
		});
		
		String name = configuration.getImageSaveName();
		if(name.length() < 1)
		{
			name = channelField.getChannel();
			if(name == null)
				name = "img";
			else if(name.length() > 3)
				name = name.substring(0, 3);
		}
		nameField.setText(name);

		contentPanel.addFillEmpty();
		return contentPanel;
	}

	private void snapImage(final String camera, final String channelGroup, final String channel, final double exposure)
	{
		// Create snap image window
		final ImagePanel imagePanel = new ImagePanel(getClient());
		final YouScopeFrame childFrame = imagePanel.toFrame();
		getContainingFrame().addModalChildFrame(childFrame);
		childFrame.setVisible(true);
		childFrame.startLoading();
		
		// Make image
		Runnable runner = new Runnable()
		{
			@Override
			public void run()
			{
				ImageEvent<?> imageEvent;
				try
				{
					CameraDevice cameraDevice;
					if(camera != null && camera.length() > 0)
						cameraDevice = getServer().getMicroscope().getCameraDevice(camera);
					else
						cameraDevice = getServer().getMicroscope().getCameraDevice();
					if(cameraDevice == null)
						throw new Exception("Camera was not found.");
					
					
					imageEvent = cameraDevice.makeImage(channelGroup, channel, exposure);
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
		};
		new Thread(runner).start();
	}

	@Override
	protected void commitChanges(ImagingJobConfiguration configuration) 
	{
		configuration.setChannelConfiguration(channelField.getChannelConfiguration());
		configuration.setExposure(exposureField.getValue());
		configuration.setSaveImages(saveImagesField.isSelected());
		configuration.setImageSaveName(nameField.getText());
		configuration.setCameraConfiguration(cameraField.getCameraConfiguration());
	}

	@Override
	protected void initializeDefaultConfiguration(ImagingJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
