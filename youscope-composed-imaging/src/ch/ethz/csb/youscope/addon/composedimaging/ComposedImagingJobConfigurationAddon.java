/**
 * 
 */
package ch.ethz.csb.youscope.addon.composedimaging;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.YouScopeProperties;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonListener;
import ch.ethz.csb.youscope.client.uielements.StandardFormats;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.microscope.CameraDevice;
import ch.ethz.csb.youscope.shared.microscope.Channel;
import ch.ethz.csb.youscope.shared.microscope.PixelSize;

/**
 * @author langmo
 */
class ComposedImagingJobConfigurationAddon implements JobConfigurationAddon
{

	private JComboBox<String>								configGroupField			= new JComboBox<String>();

	private JComboBox<String>								channelField				= new JComboBox<String>();

	private JFormattedTextField						exposureField				= new JFormattedTextField(StandardFormats.getDoubleFormat());

	private JCheckBox								saveImagesField				= new JCheckBox("Save images", true);

	private JTextField								nameField					= new JTextField();

	private JLabel									nameLabel					= new JLabel("Image name used for saving:");

	private ComposedImagingJobConfiguration		job							= null;

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

	private YouScopeFrame							frame;
	private YouScopeClient				client;
	private YouScopeServer							server;
	private Vector<JobConfigurationAddonListener>	configurationListeners		= new Vector<JobConfigurationAddonListener>();

	// Names for properties which are saved to the config file
	private static final String						PROPERTY_OVERLAP			= "CSB::ComposedImaging::overlap";
	private static final String						PROPERTY_NX					= "CSB::ComposedImaging::nx";
	private static final String						PROPERTY_NY					= "CSB::ComposedImaging::ny";

	ComposedImagingJobConfigurationAddon(YouScopeClient client, YouScopeServer server)
	{
		this.client = client;
		this.server = server;
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
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setTitle("Composed Imaging Job");
		frame.setResizable(false);
		frame.setClosable(true);
		frame.setMaximizable(false);

		ComposedImagingJobConfiguration job = getConfigurationData();
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
					ComposedImagingJobConfigurationAddon.this.frame.pack();
				}
				else if(pixelsFromUser.isSelected())
				{
					pixelsFromCameraPanel.setVisible(false);
					pixelsFromUserPanel.setVisible(true);
					ComposedImagingJobConfigurationAddon.this.frame.pack();
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
					ComposedImagingJobConfigurationAddon.this.frame.pack();
				}
				else if(pixelSizeFromUser.isSelected())
				{
					pixelSizeFromConfigPanel.setVisible(false);
					pixelSizeFromUserPanel.setVisible(true);
					ComposedImagingJobConfigurationAddon.this.frame.pack();
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
		StandardFormats.addGridBagElement(new JLabel("Pixel size in μm:"), pixelSizeFromUserLayout, newLineConstr, pixelSizeFromUserPanel);
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

		StandardFormats.addGridBagElement(new JLabel("Size of one image in μm (width x height):"), informationLayout, newLineConstr, informationPanel);
		JPanel imageWidthPanel = new JPanel(new GridLayout(1, 2));
		pictureHeightField.setEditable(false);
		pictureWidthField.setEditable(false);
		imageWidthPanel.add(pictureWidthField);
		imageWidthPanel.add(pictureHeightField);
		StandardFormats.addGridBagElement(imageWidthPanel, informationLayout, newLineConstr, informationPanel);

		StandardFormats.addGridBagElement(new JLabel("Size of totally imaged area in μm (width x height):"), informationLayout, newLineConstr, informationPanel);
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

				ComposedImagingJobConfigurationAddon.this.frame.pack();
			}
		});
		StandardFormats.addGridBagElement(nameLabel, configurationLayout, newLineConstr, configurationPanel);
		StandardFormats.addGridBagElement(nameField, configurationLayout, newLineConstr, configurationPanel);

		StandardFormats.addGridBagElement(new JPanel(), configurationLayout, bottomConstr, configurationPanel);

		// Add job button
		JButton addJobButton = new JButton("Add Job");
		addJobButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setConfigDataToUI();

				for(JobConfigurationAddonListener listener : configurationListeners)
				{
					listener.jobConfigurationFinished(ComposedImagingJobConfigurationAddon.this.job);
				}
				try
				{
					ComposedImagingJobConfigurationAddon.this.frame.setVisible(false);
				}
				catch(Exception e1)
				{
					client.sendError("Could not close window.", e1);
				}
			}
		});

		JPanel elementsPanel = new JPanel(new GridLayout(1, 3, 3, 3));
		elementsPanel.add(partPanel);
		elementsPanel.add(configurationPanel);
		elementsPanel.add(informationPanel);

		// Load state
		loadConfigGroupNames();
		String configGroup = job.getChannelGroup();
		if(configGroup == null || configGroup.length() < 1)
			configGroup = client.getProperties().getProperty(YouScopeProperties.PROPERTY_LAST_CHANNEL_GROUP, "");
		for(int i = 0; i < configGroupField.getItemCount(); i++)
		{
			if(configGroup.compareTo(configGroupField.getItemAt(i).toString()) == 0)
				configGroupField.setSelectedIndex(i);
		}

		loadChannels();
		for(int i = 0; i < channelField.getItemCount(); i++)
		{
			if(job.getChannel().compareTo(channelField.getItemAt(i).toString()) == 0)
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
		exposureField.setValue(job.getExposure());
		saveImagesField.setSelected(job.isSaveImages());
		String name = job.getImageSaveName();
		if(name.length() < 1)
		{
			name = channelField.getSelectedItem().toString();
			if(name.length() > 3)
				name = name.substring(0, 3);
		}
		nameField.setText(name);

		overlapField.setValue(job.getOverlap());
		nxField.setValue(job.getNx());
		nyField.setValue(job.getNy());
		pixelSizeField.setValue(job.getPixelSize());
		numPixelXField.setValue(job.getNumPixels().width);
		numPixelYField.setValue(job.getNumPixels().height);

		if(job.getCameraDevice() != null)
		{
			pixelsFromCamera.doClick();
			cameraNamesField.setSelectedItem(job.getCameraDevice());
		}
		else
		{
			pixelsFromUser.doClick();
		}
		
		if(job.getPixelSizeID() != null)
		{
			pixelSizeFromConfig.doClick();
			pixelSizeIDsField.setSelectedItem(job.getPixelSizeID());
		}
		else
		{
			pixelSizeFromUser.doClick();
		}
		
		updateAreaConfig();

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(elementsPanel, BorderLayout.CENTER);
		contentPane.add(addJobButton, BorderLayout.SOUTH);
		frame.setContentPane(contentPane);
		frame.pack();
	}

	private void setPixelSizeConfigID(String pixelSizeID)
	{
		try
		{
			double pixelSize = server.getMicroscope().getPixelSizeManager().getPixelSize(pixelSizeID).getPixelSize();
			pixelSizeField.setValue(pixelSize);
		}
		catch(Exception e1)
		{
			client.sendError("Could not get pixel size.", e1);
			return;
		}
	}
	
	private void setCameraPixels(String cameraName)
	{
		Dimension imageSize;
		boolean isSwitched;
		
		try
		{
			CameraDevice cameraDevice = server.getMicroscope().getCameraDevice(cameraName);
			imageSize = cameraDevice.getImageSize();
			isSwitched = cameraDevice.isSwitchXY();
		}
		catch(Exception e1)
		{
			client.sendError("Could not get image size of camera.", e1);
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
			PixelSize[] pixelSizes = server.getMicroscope().getPixelSizeManager().getPixelSizes();
			String[] names = new String[pixelSizes.length];
			for(int i=0; i< pixelSizes.length; i++)
			{
				names[i] = pixelSizes[i].getPixelSizeID();
			}
			
			return names;
		}
		catch(Exception e)
		{
			client.sendError("Could not get IDs of pixel size settings.", e);
			return new String[0];
		}
	}
	
	private String[] getCameraNames()
	{
		String[] cameraNames;
		try
		{
			CameraDevice[] cameraDevices = server.getMicroscope().getCameraDevices();
			cameraNames = new String[cameraDevices.length];
			for(int i=0; i<cameraDevices.length; i++)
			{
				cameraNames[i] = cameraDevices[i].getDeviceID();
			}
		}
		catch(Exception e)
		{
			client.sendError("Could not get names of cameras.", e);
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

	@Override
	public void setConfigurationData(JobConfiguration job) throws ConfigurationException
	{
		if(!(job instanceof ComposedImagingJobConfiguration))
			throw new ConfigurationException("Configuration not supported by this addon.");
		this.job = (ComposedImagingJobConfiguration)job;
	}

	@Override
	public ComposedImagingJobConfiguration getConfigurationData()
	{
		if(job == null)
		{
			job = new ComposedImagingJobConfiguration();
			double overlap = client.getProperties().getProperty(PROPERTY_OVERLAP, job.getOverlap());
			int nx = client.getProperties().getProperty(PROPERTY_NX, job.getNx());
			int ny = client.getProperties().getProperty(PROPERTY_NY, job.getNy());
			
			// Get current values for parameters where the user expects that they correspond to the current microscope settings.
			String cameraName = null;
			Dimension numPixels = null;
			double pixelSize = -1;
			String pixelSizeID = null;
			try
			{
				CameraDevice cameraDevice = server.getMicroscope().getCameraDevice();
				if(cameraDevice == null)
				{
					CameraDevice[] cameraDevices = server.getMicroscope().getCameraDevices();
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
				
				PixelSize[] pixelSizes = server.getMicroscope().getPixelSizeManager().getPixelSizes();
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
				client.sendError("Could not get settings of current camera or pixel size.", e);
			}

			job.setOverlap(overlap);
			job.setPixelSizeID(pixelSizeID);
			job.setPixelSize(pixelSize);
			job.setCameraDevice(cameraName);
			job.setNumPixels(numPixels);
			job.setNx(nx);
			job.setNy(ny);
		}
		return job;
	}

	private void setConfigDataToUI()
	{
		double overlap = ((Number)overlapField.getValue()).doubleValue();
		double pixelSize = ((Number)pixelSizeField.getValue()).doubleValue();
		int numPixelsX = ((Number)numPixelXField.getValue()).intValue();
		int numPixelsY = ((Number)numPixelYField.getValue()).intValue();
		int nx = ((Number)nxField.getValue()).intValue();
		int ny = ((Number)nyField.getValue()).intValue();

		ComposedImagingJobConfiguration job = getConfigurationData();
		job.setOverlap(overlap);
		job.setPixelSize(pixelSize);
		job.setNumPixels(new Dimension(numPixelsX, numPixelsY));
		job.setNx(nx);
		job.setNy(ny);
		job.setChannel((String)configGroupField.getSelectedItem(), (String)channelField.getSelectedItem());
		job.setExposure(((Number)exposureField.getValue()).doubleValue());
		job.setSaveImages(saveImagesField.isSelected());
		job.setImageSaveName(nameField.getText());
		
		if(pixelSizeFromConfig.isSelected() && pixelSizeIDsField.getSelectedItem() != null)
			job.setPixelSizeID(pixelSizeIDsField.getSelectedItem().toString());
		else
			job.setPixelSizeID(null);
		
		if(pixelsFromCamera.isSelected() && cameraNamesField.getSelectedItem() != null)
			job.setCameraDevice(cameraNamesField.getSelectedItem().toString());
		else
			job.setCameraDevice(null);
		
		try
		{
			setConfigurationData(job);
		}
		catch(ConfigurationException e)
		{
			client.sendError("Could not update configuration data.", e);
		}

		client.getProperties().setProperty(YouScopeProperties.PROPERTY_LAST_CHANNEL_GROUP, (String)configGroupField.getSelectedItem());
		client.getProperties().setProperty(PROPERTY_OVERLAP, overlap);
		client.getProperties().setProperty(PROPERTY_NX, nx);
		client.getProperties().setProperty(PROPERTY_NY, ny);
	}

	@Override
	public void addConfigurationListener(JobConfigurationAddonListener listener)
	{
		configurationListeners.add(listener);
	}

	@Override
	public void removeConfigurationListener(JobConfigurationAddonListener listener)
	{
		configurationListeners.remove(listener);
	}
	@Override
	public String getConfigurationID()
	{
		return ComposedImagingJobConfiguration.TYPE_IDENTIFIER;
	}
}
