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
package org.youscope.plugin.slimjob;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.microscope.CameraDevice;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceException;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImagePanel;
import org.youscope.uielements.IntegerTextField;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 */
class SlimJobConfigurationAddon extends ComponentAddonUIAdapter<SlimJobConfiguration>
{

	private final JComboBox<String>								configGroupField		= new JComboBox<String>();

	private final JComboBox<String>								channelField			= new JComboBox<String>();

	private final JFormattedTextField					exposureField			= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private final JCheckBox								saveImagesField			= new JCheckBox("Save images", true);

	private final JTextField							nameField				= new JTextField();

	private final JLabel								nameLabel				= new JLabel("Image name:");
	
	private final JLabel 					cameraLabel = new JLabel("Camera:");
	private final JComboBox<String>					cameraField		= new JComboBox<String>();
	
	private final JLabel 					reflectorLabel = new JLabel("Reflector:");
	private final JComboBox<String>					reflectorField		= new JComboBox<String>();
	
	private final JComboBox<String> modeField = new JComboBox<String>();
	private final JTextField maskFileField = new JTextField();
	private final JPanel maskFilePanel = new JPanel(new BorderLayout());
	private final JLabel maskFileLabel = new JLabel("Mask File:");
	
	private final JLabel centerXLabel = new JLabel("X-position of Mask Center (px):");
	private final JLabel centerYLabel = new JLabel("Y-position of Mask Center (px):");
	private final JLabel innerRadiusLabel = new JLabel("Inner Radius (px):");
	private final JLabel outerRadiusLabel = new JLabel("Outer Radius (px):");
	
	
	private final IntegerTextField centerXField = new IntegerTextField();
	private final IntegerTextField centerYField = new IntegerTextField();
	private final IntegerTextField innerRadiusField = new IntegerTextField();
	private final IntegerTextField outerRadiusField = new IntegerTextField();
	private final IntegerTextField phaseShiftBackgroundField = new IntegerTextField();
	private final IntegerTextField[] phaseShiftMaskFields = new IntegerTextField[SlimJobConfiguration.NUM_PHASE_SHIFT_MASK];
	private final IntegerTextField reflectorDelayField = new IntegerTextField();
	private final DoubleTextField attenuationFactorField = new DoubleTextField();
	
	private final static String LAST_SLIM_PATH_PROPERTY = "YouScope.SLIM.lastPath";

	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public SlimJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ComponentMetadataAdapter<SlimJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<SlimJobConfiguration>(SlimJobConfiguration.TYPE_IDENTIFIER, 
				SlimJobConfiguration.class, 
				SlimJob.class, 
				"SLIM", 
				new String[]{"Imaging"},
				"Automatically sets SLIM settings and takes four images at different phase shifts needed to generate a SLIM image. At least one \"Reflector\" device and one camera must be installed. After taking four images, the mask and phase shifts are set to zero."
				,"icons/camera-lens.png");
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
			sendErrorMessage("Could not obtain config group names.", e);
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
				Channel[] channels = getServer().getMicroscope().getChannelManager().getChannels(selectedGroup.toString()); 
				channelNames = new String[channels.length];
				for(int i=0; i<channels.length; i++)
				{
					channelNames[i] = channels[i].getChannelID();
				}
			}
			catch(Exception e)
			{
				sendErrorMessage("Could not obtain channel names of microscope.", e);
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
	
	private String getDefaultCameraName()
	{
		try
		{
			CameraDevice defaultCamera = getServer().getMicroscope().getCameraDevice();
			if(defaultCamera == null)
				return null;
			return defaultCamera.getDeviceID();
		}
		catch(Exception e)
		{
			sendErrorMessage("Could not get name of default camera.", e);
			return null;
		}
	}
	
	private int loadReflectors()
	{
		ArrayList<String> reflectorNames = new ArrayList<String>();
		try
		{
			Device[] devices = getServer().getMicroscope().getDevices();
			for(Device device : devices)
			{
				if(device.getDriverID().equals("Reflector"))
					reflectorNames.add(device.getDeviceID());
			}
		}
		catch(RemoteException e)
		{
			sendErrorMessage("Could not obtain names of reflector devices.", e);
		}
		
		reflectorField.removeAllItems();
		for(String reflectorName : reflectorNames)
		{
			reflectorField.addItem(reflectorName);
		}
		return reflectorNames.size();
	}
	
	private int loadCameras()
	{
		String[] cameraNames = null;
		try
		{
			CameraDevice[] cameras = getServer().getMicroscope().getCameraDevices();
			cameraNames = new String[cameras.length];
			for(int i=0; i< cameras.length; i++)
			{
				cameraNames[i] = cameras[i].getDeviceID();
			}
		}
		catch(RemoteException e)
		{
			sendErrorMessage("Could not obtain names of cameras.", e);
			cameraNames = new String[0];
		}
				
		cameraField.removeAllItems();
		for(String cameraName : cameraNames)
		{
			cameraField.addItem(cameraName);
		}
		return cameraNames.length;
	}
	
	@Override
	protected Component createUI(SlimJobConfiguration configuration) throws AddonException
	{
		setTitle("SLIM");
		setResizable(true);
		setMaximizable(false);
		
		DynamicPanel elementsPanel = new DynamicPanel();
		elementsPanel.add(cameraLabel);
		elementsPanel.add(cameraField);
		elementsPanel.add(new JLabel("Channel Group:"));
		elementsPanel.add(configGroupField);
		elementsPanel.add(new JLabel("Channel:"));
		elementsPanel.add(channelField);
		elementsPanel.add(new JLabel("Exposure (ms):"));
		elementsPanel.add(exposureField);
		elementsPanel.add(nameLabel);
		elementsPanel.add(nameField);
		elementsPanel.add(saveImagesField);
		
		JButton snapImageButton = new JButton("Snap Image");
		snapImageButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String channel = (String)channelField.getSelectedItem();
				String channelGroup = (String)configGroupField.getSelectedItem();
				double exposure = ((Number)exposureField.getValue()).doubleValue();
				String camera = (String)cameraField.getSelectedItem();
				snapImage(camera, channelGroup, channel, exposure);
			}
		});
		elementsPanel.add(snapImageButton);
		elementsPanel.addFillEmpty();
		
		JButton snapSlimButton = new JButton("Snap SLIM image");
		snapSlimButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				snapSlim();
			}
		});		
		
		DynamicPanel reflectorSettings = new DynamicPanel();
		reflectorSettings.add(reflectorLabel);
		reflectorSettings.add(reflectorField);
		
		reflectorSettings.add(new JLabel("Mode:"));
		modeField.addItem("Donut");
		modeField.addItem("Mask File");
		modeField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				boolean donut = modeField.getSelectedIndex()==0;
				maskFileLabel.setVisible(!donut);
				maskFilePanel.setVisible(!donut);
				
				centerXLabel.setVisible(donut);
				centerXField.setVisible(donut);
				centerYLabel.setVisible(donut);
				centerYField.setVisible(donut);
				innerRadiusLabel.setVisible(donut);
				innerRadiusField.setVisible(donut);
				outerRadiusLabel.setVisible(donut);
				outerRadiusField.setVisible(donut);
			}
		});
		reflectorSettings.add(modeField);
		reflectorSettings.add(maskFileLabel);
		maskFilePanel.add(maskFileField, BorderLayout.CENTER);
		JButton maskFileButton = new JButton("Select");
		maskFileButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				JFileChooser fileChooser =
                    new JFileChooser(getClient().getPropertyProvider().getProperty(LAST_SLIM_PATH_PROPERTY, "/"));
	            File file;
	            while(true)
	            {
	            	int returnVal = fileChooser.showDialog(null, "Select");
	            	if (returnVal != JFileChooser.APPROVE_OPTION)
	            	{
	            		return;
	            	}
	            	file = fileChooser.getSelectedFile().getAbsoluteFile();
	            	if(!file.exists())
	            	{
	            		JOptionPane.showMessageDialog(null, "File " + file.toString() + " does not exist.", "File does not exist", JOptionPane. INFORMATION_MESSAGE);
	            		return;
	            	}
					break;
	            }
            
	            getClient().getPropertyProvider().setProperty(LAST_SLIM_PATH_PROPERTY, fileChooser
	            		.getCurrentDirectory().getAbsolutePath());
	            maskFileField.setText(fileChooser.getSelectedFile().getAbsolutePath());
			}
		});
		maskFilePanel.add(maskFileButton, BorderLayout.EAST);
		reflectorSettings.add(maskFilePanel);
		
		reflectorSettings.add(centerXLabel);
		reflectorSettings.add(centerXField);
		reflectorSettings.add(centerYLabel);
		reflectorSettings.add(centerYField);
		reflectorSettings.add(innerRadiusLabel);
		reflectorSettings.add(innerRadiusField);
		reflectorSettings.add(outerRadiusLabel);
		reflectorSettings.add(outerRadiusField);
		reflectorSettings.addFillEmpty();
		
		DynamicPanel phasePanel = new DynamicPanel();
		phasePanel.add(new JLabel("Phase Shift of Background (0-255):"));
		phasePanel.add(phaseShiftBackgroundField);
		phasePanel.add(new JLabel("Phase Shifts of Masks (0-255):"));
		for(int i=0; i<phaseShiftMaskFields.length; i++)
		{
			phaseShiftMaskFields[i] = new IntegerTextField();
			phasePanel.add(phaseShiftMaskFields[i]);
		}
		phasePanel.add(new JLabel("Slim Delay (ms):"));
		phasePanel.add(reflectorDelayField);
		phasePanel.add(new JLabel("Attenuation Factor:"));
		phasePanel.add(attenuationFactorField);
		JButton loadButton = new JButton("Load");
		loadButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                JFileChooser fileChooser =
                        new JFileChooser(getClient().getPropertyProvider().getProperty(LAST_SLIM_PATH_PROPERTY, "/"));
                File file;
                while(true)
                {
                	int returnVal = fileChooser.showDialog(null, "Load");
                	if (returnVal != JFileChooser.APPROVE_OPTION)
                	{
                		return;
                	}
                	file = fileChooser.getSelectedFile().getAbsoluteFile();
                	if(!file.exists())
                	{
                		JOptionPane.showMessageDialog(null, "File " + file.toString() + " does not exist.", "File does not exist", JOptionPane. INFORMATION_MESSAGE);
                		return;
                	}
					break;
                }
                
                getClient().getPropertyProvider().setProperty(LAST_SLIM_PATH_PROPERTY, fileChooser
                        .getCurrentDirectory().getAbsolutePath());
                
                BufferedReader reader = null;
                int[] lines = new int[10];
                try
				{
					reader = new BufferedReader(new FileReader(file));
					for(int i=0; i<lines.length; i++)
					{
						String line = reader.readLine();
						if(line == null)
						{
							if(i==9)
							{
								lines[i] = 0;
								break;
							}
							throw new Exception("SLIM configuration must contain at least nine lines.");
						}
						try
						{
							lines[i]= Integer.parseInt(line);
						}
						catch(NumberFormatException ee)
						{
							throw new Exception("Line " + Integer.toString(i+1) + " is not an integer.", ee);
						}
					}
				}
				catch(Exception e1)
				{
					sendErrorMessage("Could not load SLIM configuration protocol " + file.toString()+ ".", e1);
					return;
				}
				finally
				{
					if(reader != null)
					{
						try
						{
							reader.close();
						}
						catch(Exception e1)
						{
							sendErrorMessage("Could not SLIM configuration file " + file.toString()+ ".", e1);
						}
					}						
				}
				// set values 
				centerXField.setValue(lines[0]);
				centerYField.setValue(lines[1]);
				innerRadiusField.setValue(lines[2]);
				outerRadiusField.setValue(lines[3]);
				phaseShiftBackgroundField.setValue(lines[4]);
				for(int i=0; i<4; i++)
				{
					phaseShiftMaskFields[i].setValue(lines[5+i]);
				}
				reflectorDelayField.setValue(lines[9]);
            }
        });
		phasePanel.add(loadButton);
		phasePanel.addFillEmpty();
		
		// Load state
		int numCams = loadCameras();
		if(numCams <= 0)
		{
			throw new AddonException("No camera device defined.\nAdd camera before proceeding.", new Exception("No camera device defined.\nAdd camera before proceeding."));
		}
		else if(numCams>1)
		{
			cameraField.setVisible(true);
			cameraLabel.setVisible(true);
			
			String camera = configuration.getCamera();
			if(camera == null || camera.length() < 1)
			{
				// get default camera
				camera = getDefaultCameraName();
			}
			if(camera != null)
			{
				cameraField.setSelectedItem(camera);
			}
		}
		else
		{
			cameraField.setVisible(false);
			cameraLabel.setVisible(false);
		}
		loadConfigGroupNames();
		String configGroup = configuration.getChannelGroup();
		if(configGroup == null || configGroup.length() < 1)
			configGroup = getClient().getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_CHANNEL_GROUP).toString();
		for(int i = 0; i < configGroupField.getItemCount(); i++)
		{
			if(configGroup.compareTo(configGroupField.getItemAt(i).toString()) == 0)
				configGroupField.setSelectedIndex(i);
		}
		loadChannels();
		for(int i = 0; i < channelField.getItemCount(); i++)
		{
			if(configuration.getChannel().compareTo(channelField.getItemAt(i).toString()) == 0)
				channelField.setSelectedIndex(i);
		}

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
		
		String name = configuration.getImageSaveName();
		if(name.length() < 1)
		{
			name = channelField.getSelectedItem().toString();
			if(name.length() > 3)
				name = name.substring(0, 3);
		}
		nameField.setText(name);
		
		int numReflectors = loadReflectors();
		if(numReflectors<=0)
		{
			throw new AddonException("No reflector device defined.\nAdd reflector before proceeding.");
		}
		reflectorField.setVisible(numReflectors>1);
		reflectorLabel.setVisible(numReflectors>1);
		if(configuration.getReflectorDevice() != null)
			reflectorField.setSelectedItem(configuration.getReflectorDevice());
		
		centerXField.setMinimalValue(0);
		centerXField.setValue(configuration.getMaskX());
		
		centerYField.setMinimalValue(0);
		centerYField.setValue(configuration.getMaskY());
		
		innerRadiusField.setMinimalValue(0);
		innerRadiusField.setValue(configuration.getInnerRadius());
		
		outerRadiusField.setMinimalValue(0);
		outerRadiusField.setValue(configuration.getOuterRadius());
		
		String maskFile = configuration.getMaskFileName();
		if(maskFile != null)
		{
			maskFileField.setText(maskFile);
			modeField.setSelectedIndex(1);
		}
		else
			modeField.setSelectedIndex(0);
		
		reflectorDelayField.setMinimalValue(0);
		reflectorDelayField.setValue(configuration.getSlimDelayMS());
		
		phaseShiftBackgroundField.setMinimalValue(SlimJobConfiguration.MIN_PHASE_SHIFT);
		phaseShiftBackgroundField.setMaximalValue(SlimJobConfiguration.MIN_PHASE_SHIFT);
		phaseShiftBackgroundField.setValue(configuration.getPhaseShiftOutside());
		
		for(int i=0; i<phaseShiftMaskFields.length; i++)
		{
			phaseShiftMaskFields[i].setMinimalValue(SlimJobConfiguration.MIN_PHASE_SHIFT);
			phaseShiftMaskFields[i].setMaximalValue(SlimJobConfiguration.MIN_PHASE_SHIFT);
			phaseShiftMaskFields[i].setValue(configuration.getPhaseShiftMask(i));
		}
		
		attenuationFactorField.setMinimalValue(0);
		attenuationFactorField.setValue(configuration.getAttenuationFactor());
		
		phasePanel.setBorder(new TitledBorder("Phase Shift"));
		reflectorSettings.setBorder(new TitledBorder("Mask Type"));
		elementsPanel.setBorder(new TitledBorder("Camera Settings"));
		JPanel mainPanel = new JPanel(new GridLayout(1,3));
		mainPanel.add(elementsPanel);
		mainPanel.add(reflectorSettings);
		mainPanel.add(phasePanel);
		
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(mainPanel, BorderLayout.CENTER);
		contentPane.add(snapSlimButton, BorderLayout.SOUTH);
		return contentPane;
	}

	private void snapSlim()
	{
		// Create slim image window
		final ImagePanel imagePanel = new ImagePanel(getClient());
		final YouScopeFrame childFrame = imagePanel.toFrame();
		getContainingFrame().addModalChildFrame(childFrame);
		childFrame.setVisible(true);
		childFrame.startLoading();
		class ImageSnapper implements Runnable
		{
			@Override
			public void run()
			{
				try
				{
					SlimJobConfiguration config = getConfiguration();
					
					if(config.getReflectorDevice() == null)
						throw new Exception("Reflector device not set.");
					Device reflectorDevice;
					try
					{
						reflectorDevice = getServer().getMicroscope().getDevice(config.getReflectorDevice());
					}
					catch(DeviceException e1)
					{
						throw new Exception("Could not find reflector with device ID \"" + config.getReflectorDevice() + "\".", e1);
					}
					if(!reflectorDevice.getDriverID().equals("Reflector"))
					{
						throw new Exception("Device set to serve as the reflector must be of type \"Reflector\". DriverID of device \"" + reflectorDevice.getDeviceID() + "\" is \"" + reflectorDevice.getDriverID()+"\".");
					}
					// make images
					ImageEvent<?>[] images = new ImageEvent<?>[4];
					try
					{
						if(config.getMaskFileName() == null)
						{
							reflectorDevice.getProperty("mode").setValue("donut");
							reflectorDevice.getProperty("donut.centerX").setValue(Integer.toString(config.getMaskX()));
							reflectorDevice.getProperty("donut.centerY").setValue(Integer.toString(config.getMaskY()));
							reflectorDevice.getProperty("donut.innerRadius").setValue(Integer.toString(config.getInnerRadius()));
							reflectorDevice.getProperty("donut.outerRadius").setValue(Integer.toString(config.getOuterRadius()));
						}
						else
						{
							reflectorDevice.getProperty("mode").setValue("mask");
							reflectorDevice.getProperty("mask.file").setValue(config.getMaskFileName());
						}
						
						CameraDevice camera;
						if(config.getCamera() == null)
							camera = getServer().getMicroscope().getCameraDevice();
						else
							camera = getServer().getMicroscope().getCameraDevice(config.getCamera());
						if(camera == null)
							throw new Exception("Cannot find camera.");
						reflectorDevice.getProperty("phaseShiftBackground").setValue(Integer.toString(config.getPhaseShiftOutside()));
						for(int i=0; i<images.length; i++)
						{
							reflectorDevice.getProperty("phaseShiftForeground").setValue(Integer.toString(config.getPhaseShiftMask(i)));
							if(config.getSlimDelayMS()>0)
								Thread.sleep(config.getSlimDelayMS());
							images[i] = camera.makeImage(config.getChannelGroup(), config.getChannel(), config.getExposure());
						}
						reflectorDevice.getProperty("phaseShiftBackground").setValue("0");
						reflectorDevice.getProperty("phaseShiftForeground").setValue("0");
					}
					catch(Exception e)
					{
						throw new Exception("Could not take SLIM images.", e);
					}
					ImageEvent<short[]> slimImage = SlimHelper.calculateSlimImage(images, config.getAttenuationFactor());
					// Show image
					childFrame.endLoading();
					imagePanel.setImage(slimImage);
				}
				catch(Exception e)
				{
					childFrame.setToErrorState("Could not take SLIM image.", e);
					return;
				}
			}
		}
		new Thread(new ImageSnapper()).start();
	}
	
	private void snapImage(String camera, String channelGroup, String channel, double exposure)
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
			private final String camera;
			ImageSnapper(YouScopeFrame childFrame, ImagePanel imagePanel, String camera, String channelGroup, String channel, double exposure)
			{
				this.channel = channel;
				this.channelGroup = channelGroup;
				this.childFrame = childFrame;
				this.imagePanel = imagePanel;
				this.exposure = exposure;
				this.camera = camera;
			}

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
		}
		new Thread(new ImageSnapper(childFrame, imagePanel, camera, channelGroup, channel, exposure)).start();
	}

	@Override
	protected void commitChanges(SlimJobConfiguration configuration) {
		configuration.setChannel((String)configGroupField.getSelectedItem(), (String)channelField.getSelectedItem());
		configuration.setExposure(((Number)exposureField.getValue()).doubleValue());
		configuration.setSaveImages(saveImagesField.isSelected());
		configuration.setImageSaveName(nameField.getText());
		configuration.setCamera((String)cameraField.getSelectedItem());
		
		configuration.setReflectorDevice((String)reflectorField.getSelectedItem());
		configuration.setMaskX(centerXField.getValue());
		configuration.setMaskY(centerYField.getValue());
		configuration.setInnerRadius(innerRadiusField.getValue());
		configuration.setOuterRadius(outerRadiusField.getValue());
		configuration.setPhaseShiftOutside(phaseShiftBackgroundField.getValue());
		configuration.setSlimDelayMS(reflectorDelayField.getValue());
		for(int i=0; i<SlimJobConfiguration.NUM_PHASE_SHIFT_MASK; i++)
		{
			configuration.setPhaseShiftMask(i, phaseShiftMaskFields[i].getValue());
		}
		
		attenuationFactorField.setMinimalValue(0);
		configuration.setAttenuationFactor(attenuationFactorField.getValue());
		if(modeField.getSelectedIndex() == 0)
		{
			configuration.setMaskFileName(null);
		}
		else
			configuration.setMaskFileName(maskFileField.getText());
		
		getClient().getPropertyProvider().setProperty(StandardProperty.PROPERTY_LAST_CHANNEL_GROUP, configGroupField.getSelectedItem());
		
	}

	@Override
	protected void initializeDefaultConfiguration(SlimJobConfiguration configuration) throws AddonException {
		// set identified properties.
		configuration.setAttenuationFactor(new SlimProperties(getClient()).getAttenuationFactor());
	}
}
