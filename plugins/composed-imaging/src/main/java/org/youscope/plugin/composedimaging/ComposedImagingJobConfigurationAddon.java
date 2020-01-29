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
package org.youscope.plugin.composedimaging;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.microscope.CameraDevice;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.PixelSize;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 */
class ComposedImagingJobConfigurationAddon extends ComponentAddonUIAdapter<ComposedImagingJobConfiguration>
{

	private JComboBox<String>								configGroupField			= new JComboBox<String>();

	private JComboBox<String>								channelField				= new JComboBox<String>();

	private JFormattedTextField						exposureField				= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JCheckBox								saveImagesField				= new JCheckBox("Save images", true);

	private JTextField								nameField					= new JTextField();

	private JLabel									nameLabel					= new JLabel("Image name used for saving:");

	private JFormattedTextField						overlapField				= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField						nxField						= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JFormattedTextField						nyField						= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JFormattedTextField						pixelSizeField				= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField						numPixelXField				= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JFormattedTextField						numPixelYField				= new JFormattedTextField(StandardFormats.getIntegerFormat());

	private JFormattedTextField						areaWidthField				= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField						areaHeightField				= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField						pictureWidthField			= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JFormattedTextField						pictureHeightField			= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JRadioButton							pixelsFromCamera			= new JRadioButton("From camera configuration.", false);

	private JRadioButton							pixelsFromUser				= new JRadioButton("Enter manually.", false);

	private JComboBox<String>								cameraNamesField;

	private JComboBox<String>								pixelSizeIDsField;

	private JPanel									pixelsFromCameraPanel		= null;
	private JPanel									pixelsFromUserPanel			= null;

	private JRadioButton							pixelSizeFromConfig			= new JRadioButton("From pixel-size configuration.", false);

	private JRadioButton							pixelSizeFromUser			= new JRadioButton("Enter manually.", false);

	private JPanel									pixelSizeFromConfigPanel	= null;
	private JPanel									pixelSizeFromUserPanel		= null;

	// Names for properties which are saved to the config file
	private static final String						PROPERTY_OVERLAP			= "YouScope.ComposedImaging.overlap";
	private static final String						PROPERTY_NX					= "YouScope.ComposedImaging.nx";
	private static final String						PROPERTY_NY					= "YouScope.ComposedImaging.ny";

	ComposedImagingJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
		initializeConfiguration();
	}
    
    static ComponentMetadataAdapter<ComposedImagingJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<ComposedImagingJobConfiguration>(ComposedImagingJobConfiguration.TYPE_IDENTIFIER, 
				ComposedImagingJobConfiguration.class, 
				ComposedImagingJob.class, 
				"Composed Imaging",
				new String[]{"Imaging"},
				"Takes partly overlapping images on a rectangular grid, such that the resulting images can be stitched together using a 3rd party algorithm (stitching is not part of the job).",
				"icons/layers-group.png");
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
			getClient().sendError("Could not obtain channel group names.", e);
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

	@Override
	protected Component createUI(ComposedImagingJobConfiguration configuration) throws AddonException
	{
		setTitle("Composed Imaging");
		setResizable(false);
		setMaximizable(false);

		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();

		class AreaConfigChangedListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				updateAreaConfig();
			}
		}

		// Image area panel
		GridBagLayout partLayout = new GridBagLayout();
		JPanel partPanel = new JPanel(partLayout);
		partPanel.setBorder(new TitledBorder("Spatial Properties"));

		// Number of pixels
		StandardFormats.addGridBagElement(new JLabel("Get number of pixels:"), partLayout, newLineConstr, partPanel);
		ButtonGroup pixelsFromGroup = new ButtonGroup();
		pixelsFromGroup.add(pixelsFromCamera);
		pixelsFromGroup.add(pixelsFromUser);
		class PixelsFromListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(pixelsFromCamera.isSelected())
				{
					pixelsFromCameraPanel.setVisible(true);
					pixelsFromUserPanel.setVisible(false);
					if(cameraNamesField.getSelectedItem() != null)
						setCameraPixels(cameraNamesField.getSelectedItem().toString());
					getContainingFrame().pack();
				}
				else if(pixelsFromUser.isSelected())
				{
					pixelsFromCameraPanel.setVisible(false);
					pixelsFromUserPanel.setVisible(true);
					getContainingFrame().pack();
				}
				updateAreaConfig();
			}
		}
		pixelsFromCamera.addActionListener(new PixelsFromListener());
		pixelsFromUser.addActionListener(new PixelsFromListener());
		StandardFormats.addGridBagElement(pixelsFromCamera, partLayout, newLineConstr, partPanel);
		StandardFormats.addGridBagElement(pixelsFromUser, partLayout, newLineConstr, partPanel);

		// Pixels from camera.
		GridBagLayout pixelsFromCameraLayout = new GridBagLayout();
		pixelsFromCameraPanel = new JPanel(pixelsFromCameraLayout);
		StandardFormats.addGridBagElement(new JLabel("Select Camera:"), pixelsFromCameraLayout, newLineConstr, pixelsFromCameraPanel);
		cameraNamesField = new JComboBox<String>(getCameraNames());
		cameraNamesField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(cameraNamesField.getSelectedItem() == null)
					return;
				String cameraName = cameraNamesField.getSelectedItem().toString();
				setCameraPixels(cameraName);
				updateAreaConfig();
			}
		});
		StandardFormats.addGridBagElement(cameraNamesField, pixelsFromCameraLayout, newLineConstr, pixelsFromCameraPanel);
		StandardFormats.addGridBagElement(pixelsFromCameraPanel, partLayout, newLineConstr, partPanel);

		// Pixels from user
		GridBagLayout pixelsFromUserLayout = new GridBagLayout();
		pixelsFromUserPanel = new JPanel(pixelsFromUserLayout);
		StandardFormats.addGridBagElement(new JLabel("Number of pixels in an image (width / height):"), pixelsFromUserLayout, newLineConstr, pixelsFromUserPanel);
		JPanel numPixelPanel = new JPanel(new GridLayout(1, 2));
		numPixelXField.addActionListener(new AreaConfigChangedListener());
		numPixelYField.addActionListener(new AreaConfigChangedListener());
		numPixelPanel.add(numPixelXField);
		numPixelPanel.add(numPixelYField);
		StandardFormats.addGridBagElement(numPixelPanel, pixelsFromUserLayout, newLineConstr, pixelsFromUserPanel);
		StandardFormats.addGridBagElement(pixelsFromUserPanel, partLayout, newLineConstr, partPanel);

		// Pixel Size
		StandardFormats.addGridBagElement(new JLabel("Get pixel-size:"), partLayout, newLineConstr, partPanel);
		ButtonGroup pixelSizeFromGroup = new ButtonGroup();
		pixelSizeFromGroup.add(pixelSizeFromConfig);
		pixelSizeFromGroup.add(pixelSizeFromUser);
		class PixelSizeFromListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(pixelSizeFromConfig.isSelected())
				{
					pixelSizeFromConfigPanel.setVisible(true);
					pixelSizeFromUserPanel.setVisible(false);
					if(pixelSizeIDsField.getSelectedItem() != null)
						setPixelSizeConfigID(pixelSizeIDsField.getSelectedItem().toString());
					getContainingFrame().pack();
				}
				else if(pixelSizeFromUser.isSelected())
				{
					pixelSizeFromConfigPanel.setVisible(false);
					pixelSizeFromUserPanel.setVisible(true);
					getContainingFrame().pack();
				}
				updateAreaConfig();
			}
		}
		pixelSizeFromConfig.addActionListener(new PixelSizeFromListener());
		pixelSizeFromUser.addActionListener(new PixelSizeFromListener());
		StandardFormats.addGridBagElement(pixelSizeFromConfig, partLayout, newLineConstr, partPanel);
		StandardFormats.addGridBagElement(pixelSizeFromUser, partLayout, newLineConstr, partPanel);

		// Pixel Size from config.
		GridBagLayout pixelSizeFromConfigLayout = new GridBagLayout();
		pixelSizeFromConfigPanel = new JPanel(pixelSizeFromConfigLayout);
		StandardFormats.addGridBagElement(new JLabel("Select pixel-size setting:"), pixelSizeFromConfigLayout, newLineConstr, pixelSizeFromConfigPanel);
		pixelSizeIDsField = new JComboBox<String>(getPixelSizeNames());
		pixelSizeIDsField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(pixelSizeIDsField.getSelectedItem() == null)
					return;
				String pixelSizeID = pixelSizeIDsField.getSelectedItem().toString();
				setPixelSizeConfigID(pixelSizeID);
				updateAreaConfig();
			}
		});
		StandardFormats.addGridBagElement(pixelSizeIDsField, pixelSizeFromConfigLayout, newLineConstr, pixelSizeFromConfigPanel);
		StandardFormats.addGridBagElement(pixelSizeFromConfigPanel, partLayout, newLineConstr, partPanel);

		// Pixels from user
		GridBagLayout pixelSizeFromUserLayout = new GridBagLayout();
		pixelSizeFromUserPanel = new JPanel(pixelSizeFromUserLayout);
		StandardFormats.addGridBagElement(new JLabel("Pixel size in um:"), pixelSizeFromUserLayout, newLineConstr, pixelSizeFromUserPanel);
		pixelSizeField.addActionListener(new AreaConfigChangedListener());
		StandardFormats.addGridBagElement(pixelSizeField, pixelSizeFromUserLayout, newLineConstr, pixelSizeFromUserPanel);
		StandardFormats.addGridBagElement(pixelSizeFromUserPanel, partLayout, newLineConstr, partPanel);

		// Overlap
		StandardFormats.addGridBagElement(new JLabel("Percentage of overlap between the pictures (0.0 - 1.0):"), partLayout, newLineConstr, partPanel);
		overlapField.addActionListener(new AreaConfigChangedListener());
		StandardFormats.addGridBagElement(overlapField, partLayout, newLineConstr, partPanel);

		// Number of images
		StandardFormats.addGridBagElement(new JLabel("Number of images (x- / y-direction):"), partLayout, newLineConstr, partPanel);
		JPanel numImagesPanel = new JPanel(new GridLayout(1, 2));
		nxField.addActionListener(new AreaConfigChangedListener());
		nyField.addActionListener(new AreaConfigChangedListener());
		numImagesPanel.add(nxField);
		numImagesPanel.add(nyField);
		StandardFormats.addGridBagElement(numImagesPanel, partLayout, newLineConstr, partPanel);
		StandardFormats.addGridBagElement(new JPanel(), partLayout, StandardFormats.getBottomContstraint(), partPanel);

		// Image Area information panel
		GridBagLayout informationLayout = new GridBagLayout();
		JPanel informationPanel = new JPanel(informationLayout);
		informationPanel.setBorder(new TitledBorder("Information on Area:"));

		StandardFormats.addGridBagElement(new JLabel("Size of one image in um (width x height):"), informationLayout, newLineConstr, informationPanel);
		JPanel imageWidthPanel = new JPanel(new GridLayout(1, 2));
		pictureHeightField.setEditable(false);
		pictureWidthField.setEditable(false);
		imageWidthPanel.add(pictureWidthField);
		imageWidthPanel.add(pictureHeightField);
		StandardFormats.addGridBagElement(imageWidthPanel, informationLayout, newLineConstr, informationPanel);

		StandardFormats.addGridBagElement(new JLabel("Size of totally imaged area in um (width x height):"), informationLayout, newLineConstr, informationPanel);
		JPanel areaWidthPanel = new JPanel(new GridLayout(1, 2));
		areaWidthField.setEditable(false);
		areaHeightField.setEditable(false);
		areaWidthPanel.add(areaWidthField);
		areaWidthPanel.add(areaHeightField);
		StandardFormats.addGridBagElement(areaWidthPanel, informationLayout, newLineConstr, informationPanel);
		StandardFormats.addGridBagElement(new JPanel(), informationLayout, bottomConstr, informationPanel);

		// Image configuration panel
		GridBagLayout configurationLayout = new GridBagLayout();
		JPanel configurationPanel = new JPanel(configurationLayout);
		configurationPanel.setBorder(new TitledBorder("Imaging configuration:"));

		StandardFormats.addGridBagElement(new JLabel("Channel Group:"), configurationLayout, newLineConstr, configurationPanel);
		StandardFormats.addGridBagElement(configGroupField, configurationLayout, newLineConstr, configurationPanel);

		StandardFormats.addGridBagElement(new JLabel("Channel:"), configurationLayout, newLineConstr, configurationPanel);
		StandardFormats.addGridBagElement(channelField, configurationLayout, newLineConstr, configurationPanel);

		StandardFormats.addGridBagElement(new JLabel("Exposure (ms):"), configurationLayout, newLineConstr, configurationPanel);
		StandardFormats.addGridBagElement(exposureField, configurationLayout, newLineConstr, configurationPanel);

		StandardFormats.addGridBagElement(saveImagesField, configurationLayout, newLineConstr, configurationPanel);
		saveImagesField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = saveImagesField.isSelected();
				nameLabel.setVisible(selected);
				nameField.setVisible(selected);

				getContainingFrame().pack();
			}
		});
		StandardFormats.addGridBagElement(nameLabel, configurationLayout, newLineConstr, configurationPanel);
		StandardFormats.addGridBagElement(nameField, configurationLayout, newLineConstr, configurationPanel);

		StandardFormats.addGridBagElement(new JPanel(), configurationLayout, bottomConstr, configurationPanel);

		JPanel elementsPanel = new JPanel(new GridLayout(1, 3, 3, 3));
		elementsPanel.add(partPanel);
		elementsPanel.add(configurationPanel);
		elementsPanel.add(informationPanel);

		// Load state
		loadConfigGroupNames();
		String configGroup = configuration.getChannelGroup();
		if(configGroup == null || configGroup.length() < 1)
			configGroup = (String) getClient().getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_CHANNEL_GROUP);
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

		// Load data
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

		overlapField.setValue(configuration.getOverlap());
		nxField.setValue(configuration.getNx());
		nyField.setValue(configuration.getNy());
		pixelSizeField.setValue(configuration.getPixelSize());
		numPixelXField.setValue(configuration.getNumPixels().width);
		numPixelYField.setValue(configuration.getNumPixels().height);

		if(configuration.getCameraDevice() != null)
		{
			pixelsFromCamera.doClick();
			cameraNamesField.setSelectedItem(configuration.getCameraDevice());
		}
		else
		{
			pixelsFromUser.doClick();
		}
		
		if(configuration.getPixelSizeID() != null)
		{
			pixelSizeFromConfig.doClick();
			pixelSizeIDsField.setSelectedItem(configuration.getPixelSizeID());
		}
		else
		{
			pixelSizeFromUser.doClick();
		}
		
		updateAreaConfig();

		return elementsPanel;
	}

	private void setPixelSizeConfigID(String pixelSizeID)
	{
		try
		{
			double pixelSize = getServer().getMicroscope().getPixelSizeManager().getPixelSize(pixelSizeID).getPixelSize();
			pixelSizeField.setValue(pixelSize);
		}
		catch(Exception e1)
		{
			sendErrorMessage("Could not get pixel size.", e1);
			return;
		}
	}
	
	private void setCameraPixels(String cameraName)
	{
		Dimension imageSize;
		boolean isSwitched;
		
		try
		{
			CameraDevice cameraDevice = getServer().getMicroscope().getCameraDevice(cameraName);
			imageSize = cameraDevice.getImageSize();
			isSwitched = cameraDevice.isSwitchXY();
		}
		catch(Exception e1)
		{
			sendErrorMessage("Could not get image size of camera.", e1);
			return;
		}
		if(isSwitched)
		{
			numPixelXField.setValue(imageSize.height);
			numPixelYField.setValue(imageSize.width);
		}
		else
		{
			numPixelXField.setValue(imageSize.width);
			numPixelYField.setValue(imageSize.height);
		}
	}
	private String[] getPixelSizeNames()
	{
		try
		{
			PixelSize[] pixelSizes = getServer().getMicroscope().getPixelSizeManager().getPixelSizes();
			String[] names = new String[pixelSizes.length];
			for(int i=0; i< pixelSizes.length; i++)
			{
				names[i] = pixelSizes[i].getPixelSizeID();
			}
			
			return names;
		}
		catch(Exception e)
		{
			sendErrorMessage("Could not get IDs of pixel size settings.", e);
			return new String[0];
		}
	}
	
	private String[] getCameraNames()
	{
		String[] cameraNames;
		try
		{
			CameraDevice[] cameraDevices = getServer().getMicroscope().getCameraDevices();
			cameraNames = new String[cameraDevices.length];
			for(int i=0; i<cameraDevices.length; i++)
			{
				cameraNames[i] = cameraDevices[i].getDeviceID();
			}
		}
		catch(Exception e)
		{
			sendErrorMessage("Could not get names of cameras.", e);
			cameraNames = new String[0];
		}
		
		return cameraNames;
	}
	private void updateAreaConfig()
	{
		double pixelSize = ((Number)pixelSizeField.getValue()).doubleValue();
		int numPixelX = ((Number)numPixelXField.getValue()).intValue();
		int numPixelY = ((Number)numPixelYField.getValue()).intValue();
		int nx = ((Number)nxField.getValue()).intValue();
		int ny = ((Number)nyField.getValue()).intValue();
		double overlap = ((Number)overlapField.getValue()).doubleValue();

		double pictureWidth = pixelSize * numPixelX;
		double pictureHeight = pixelSize * numPixelY;
		double areaWidth = pictureWidth * ((1.0 - overlap) * (nx - 1) + 1);
		double areaHeight = pictureHeight * ((1.0 - overlap) * (ny - 1) + 1);

		pictureWidthField.setValue(pictureWidth);
		pictureHeightField.setValue(pictureHeight);
		areaWidthField.setValue(areaWidth);
		areaHeightField.setValue(areaHeight);
	}

	public void initializeConfiguration()
	{
		ComposedImagingJobConfiguration configuration = getConfiguration();
		double overlap = getClient().getPropertyProvider().getProperty(PROPERTY_OVERLAP, configuration.getOverlap());
		int nx = getClient().getPropertyProvider().getProperty(PROPERTY_NX, configuration.getNx());
		int ny = getClient().getPropertyProvider().getProperty(PROPERTY_NY, configuration.getNy());
		
		// Get current values for parameters where the user expects that they correspond to the current microscope settings.
		String cameraName = null;
		Dimension numPixels = null;
		double pixelSize = -1;
		String pixelSizeID = null;
		try
		{
			CameraDevice cameraDevice = getServer().getMicroscope().getCameraDevice();
			if(cameraDevice == null)
			{
				CameraDevice[] cameraDevices = getServer().getMicroscope().getCameraDevices();
				if(cameraDevices.length > 0)
					cameraDevice = cameraDevices[0];
			}
			if(cameraDevice != null)
			{
				cameraName = cameraDevice.getDeviceID();
				numPixels = cameraDevice.getImageSize();
			}
			else
			{
				numPixels = new Dimension(1344, 1024);
			}
			
			PixelSize[] pixelSizes = getServer().getMicroscope().getPixelSizeManager().getPixelSizes();
			if(pixelSizes.length > 0)
			{
				pixelSizeID = pixelSizes[0].getPixelSizeID();
				pixelSize = pixelSizes[0].getPixelSize();
			}
			else
			{
				pixelSize = 6.45;
			}
		}
		catch(Exception e)
		{
			sendErrorMessage("Could not get settings of current camera or pixel size.", e);
		}

		configuration.setOverlap(overlap);
		configuration.setPixelSizeID(pixelSizeID);
		configuration.setPixelSize(pixelSize);
		configuration.setCameraDevice(cameraName);
		configuration.setNumPixels(numPixels);
		configuration.setNx(nx);
		configuration.setNy(ny);
		try {
			setConfiguration(configuration);
		} catch (@SuppressWarnings("unused") Exception e) {
			// should not happen.
		}
	}

	@Override
	protected void commitChanges(ComposedImagingJobConfiguration configuration) {
		double overlap = ((Number)overlapField.getValue()).doubleValue();
		double pixelSize = ((Number)pixelSizeField.getValue()).doubleValue();
		int numPixelsX = ((Number)numPixelXField.getValue()).intValue();
		int numPixelsY = ((Number)numPixelYField.getValue()).intValue();
		int nx = ((Number)nxField.getValue()).intValue();
		int ny = ((Number)nyField.getValue()).intValue();

		configuration.setOverlap(overlap);
		configuration.setPixelSize(pixelSize);
		configuration.setNumPixels(new Dimension(numPixelsX, numPixelsY));
		configuration.setNx(nx);
		configuration.setNy(ny);
		configuration.setChannel((String)configGroupField.getSelectedItem(), (String)channelField.getSelectedItem());
		configuration.setExposure(((Number)exposureField.getValue()).doubleValue());
		configuration.setSaveImages(saveImagesField.isSelected());
		configuration.setImageSaveName(nameField.getText());
		
		if(pixelSizeFromConfig.isSelected() && pixelSizeIDsField.getSelectedItem() != null)
			configuration.setPixelSizeID(pixelSizeIDsField.getSelectedItem().toString());
		else
			configuration.setPixelSizeID(null);
		
		if(pixelsFromCamera.isSelected() && cameraNamesField.getSelectedItem() != null)
			configuration.setCameraDevice(cameraNamesField.getSelectedItem().toString());
		else
			configuration.setCameraDevice(null);

		getClient().getPropertyProvider().setProperty(StandardProperty.PROPERTY_LAST_CHANNEL_GROUP, configGroupField.getSelectedItem());
		getClient().getPropertyProvider().setProperty(PROPERTY_OVERLAP, overlap);
		getClient().getPropertyProvider().setProperty(PROPERTY_NX, nx);
		getClient().getPropertyProvider().setProperty(PROPERTY_NY, ny);
		
	}

	@Override
	protected void initializeDefaultConfiguration(ComposedImagingJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
