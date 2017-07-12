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
package org.youscope.plugin.imagesubstraction;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

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
import org.youscope.common.microscope.CameraDevice;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.CameraField;
import org.youscope.uielements.ChannelField;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.FocusField;
import org.youscope.uielements.ImagePanel;

/**
 * @author Moritz Lang
 */
class ImageSubstractionJobConfigurationAddon  extends ComponentAddonUIAdapter<ImageSubstractionJobConfiguration>
{
    // UI Elements
	private CameraField cameraField;
	private ChannelField channelField; 
	private FocusField focusField;
    
    private DoubleTextField exposureField = new DoubleTextField(5);
    
    private final JTextField							nameField				= new JTextField();

	private final JLabel								nameLabel				= new JLabel("Image name:");

    private JCheckBox saveImagesField = new JCheckBox("Save images", true);

    private DoubleTextField offset1Field = new DoubleTextField();
    private DoubleTextField offset2Field = new DoubleTextField();

    /**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public ImageSubstractionJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ComponentMetadataAdapter<ImageSubstractionJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<ImageSubstractionJobConfiguration>(ImageSubstractionJobConfiguration.TYPE_IDENTIFIER, 
				ImageSubstractionJobConfiguration.class, 
				ImageSubstractionJob.class, 
				"Image Substraction", 
				new String[]{"Imaging"}, 
				"Takes two images at different focus offsets and substracts their logarithms. Typically used for cell segmentation algorithms, since image substraction enhances cell-cell and cell-medium contrasts.",
				"icons/image-blur.png");
	}
	private void goFocus(Microscope microscope, String focusDevice, double offset, long adjustmentTime) throws InterruptedException, RemoteException, MicroscopeLockedException, MicroscopeException, DeviceException
	{
		if(focusDevice == null)
		{
			microscope.getFocusDevice().setRelativeFocusPosition(offset);
		}
		else
		{
			microscope.getFocusDevice(focusDevice).setRelativeFocusPosition(offset);
		}
		if(Thread.interrupted())
			throw new InterruptedException();
		Thread.sleep(adjustmentTime);
	}
	
	private void snapImage()
	{
		// Create snap image window
		final ImagePanel imagePanel = new ImagePanel(getClient());
		final YouScopeFrame childFrame = imagePanel.toFrame();
		getContainingFrame().addModalChildFrame(childFrame);
		childFrame.setVisible(true);
		childFrame.startLoading();
		
		final String channel = channelField.getChannel();
		final String channelGroup = channelField.getChannelGroup();
		final double exposure = exposureField.getValue();
		final String camera = cameraField.getCameraDevice();
		final String focusDevice = focusField.getFocusDevice();
		final int adjustmentTime = focusField.getAdjustmentTime();
		final double offset1 = offset1Field.getValue();
		final double offset2 = offset2Field.getValue();
		
		// Make image
		Runnable runner = new Runnable()
		{
			@Override
			public void run()
			{
				Microscope microscope;
				try {
					microscope = getServer().getMicroscope();
				} catch (RemoteException e1) {
					childFrame.setToErrorState("Could not access microscope.", e1);
					return;
				}
				CameraDevice cameraDevice;
				if(camera == null || camera.length() < 1)
				{
					try
					{
						cameraDevice = microscope.getCameraDevice();
					}
					catch(Exception e1)
					{
						childFrame.setToErrorState("Could not get default camera", e1);
						return;
					}
				}
				else
				{
					try
					{
						cameraDevice = microscope.getCameraDevice(camera);
					}
					catch(Exception e1)
					{
						childFrame.setToErrorState("Could not find camera with device ID \"" + camera + "\".", e1);
						return;
					}
				}
				
				ImageEvent<?> image1;
				ImageEvent<?> image2;
				try
				{
					goFocus(microscope, focusDevice, offset1, adjustmentTime);
					image1 = cameraDevice.makeImage(channelGroup, channel, exposure);					
					
					goFocus(microscope, focusDevice, -offset1+offset2, adjustmentTime);
					image2 = cameraDevice.makeImage(channelGroup, channel, exposure);
					
					goFocus(microscope, focusDevice, -offset2, adjustmentTime);
				}
				catch(Exception e)
				{
					childFrame.setToErrorState("Could not take images in focus offsets.", e);
					return;
				}
				ImageEvent<?> subImage;
				try {
					subImage = ImageSubstractionTools.divideImages(image1, image2);
				} catch (Exception e) {
					childFrame.setToErrorState("Could not substract offset images.", e);
					return;
				}
								
				// Show image
				childFrame.endLoading();
				imagePanel.setImage(subImage);
			}
		};
		new Thread(runner).start();
	}
	
	
	@Override
	protected Component createUI(ImageSubstractionJobConfiguration configuration) throws AddonException
	{
		setTitle("Image Substraction");
		setResizable(false);
		setMaximizable(false);
		
		channelField = new ChannelField(configuration.getChannelConfiguration(), getClient(), getServer());
		cameraField = new CameraField(configuration.getCameraConfiguration(), getClient(), getServer());
		focusField = new FocusField(configuration.getFocusConfiguration(), getClient(), getServer());
		
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
		
		contentPanel.add(focusField);
		contentPanel.add(new JLabel("Focus offset first image:"));
		contentPanel.add(offset1Field);
		contentPanel.add(new JLabel("Focus offset second image:"));
		contentPanel.add(offset2Field);

		JButton snapImageButton = new JButton("Snap Image");
		snapImageButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				snapImage();
			}
		});
		contentPanel.add(snapImageButton);

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
			name = "sub";
		}
		nameField.setText(name);
		
		offset1Field.setValue(configuration.getPosition1());
		offset2Field.setValue(configuration.getPosition2());

		contentPanel.addFillEmpty();
		return contentPanel;
    }

	@Override
	protected void commitChanges(ImageSubstractionJobConfiguration configuration) 
	{
        // Store parameters
        configuration.setCameraConfiguration(cameraField.getCameraConfiguration());
        configuration.setChannelConfiguration(channelField.getChannelConfiguration());
        configuration.setFocusConfiguration(focusField.getFocusConfiguration());
        configuration.setExposure(exposureField.getValue());
        configuration.setImageSaveName(nameField.getText());
        configuration.setPosition1(offset1Field.getValue());
        configuration.setPosition2(offset2Field.getValue());
        configuration.setSaveImages(saveImagesField.isSelected());
	}

	@Override
	protected void initializeDefaultConfiguration(ImageSubstractionJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
