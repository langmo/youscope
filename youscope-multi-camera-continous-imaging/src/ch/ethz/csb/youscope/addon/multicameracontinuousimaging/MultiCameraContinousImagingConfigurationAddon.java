/**
 * 
 */
package ch.ethz.csb.youscope.addon.multicameracontinuousimaging;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.rmi.RemoteException;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.YouScopeProperties;
import ch.ethz.csb.youscope.client.addon.measurement.MeasurementConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.measurement.MeasurementConfigurationAddonListener;
import ch.ethz.csb.youscope.client.uielements.DeviceSettingsPanel;
import ch.ethz.csb.youscope.client.uielements.ImageLoadingTools;
import ch.ethz.csb.youscope.client.uielements.StandardFormats;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.ImageFolderStructure;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.measurement.MeasurementSaveSettings;
import ch.ethz.csb.youscope.shared.microscope.CameraDevice;
import ch.ethz.csb.youscope.shared.microscope.Channel;
import ch.ethz.csb.youscope.shared.microscope.Device;
import ch.ethz.csb.youscope.shared.microscope.DeviceType;
import ch.ethz.csb.youscope.shared.microscope.Property;
import ch.ethz.csb.youscope.shared.microscope.DeviceSettingDTO;
import ch.ethz.csb.youscope.shared.microscope.Microscope;

/**
 * @author Moritz Lang
 */
class MultiCameraContinousImagingConfigurationAddon implements MeasurementConfigurationAddon
{

	private Vector<MeasurementConfigurationAddonListener> configurationListeners = new Vector<MeasurementConfigurationAddonListener>();
	
	private int											currentPage				= 1;

	private static final int							numPages				= 4;

	private CardLayout								pagesLayout				= new CardLayout();

	private JPanel									pagesPanel				= new JPanel(
																						pagesLayout);

	private JButton									previousButton			= new JButton(
																						"Previous");

	private JButton									nextButton				= new JButton(
																						"Next");

	private JTextField								nameField				= new JTextField(
																						"unnamed");

	private JLabel										runtimeFieldLabel		= new JLabel(
																						"Measurement Total Runtime (seconds):");

	private JFormattedTextField						runtimeField			= new JFormattedTextField(
																						StandardFormats
																								.getIntegerFormat());

	private JTextField								folderField				= new JTextField();

	private JComboBox<String>									imageTypeField;
	
	private final JLabel periodLabel = new JLabel("Imaging Period:");
	private final JLabel periodTypeLabel = new JLabel("Imaging Type:");
	private final JFormattedTextField					periodField			= new JFormattedTextField(StandardFormats.getIntegerFormat());
	private final JComboBox<String>									periodTypeField = new JComboBox<String>(new String[]{"Burst", "Given Period"});

	private MultiCameraContinousImagingConfigurationDTO	measurementConfiguration;

	private MeasurementConfigurationAddonListener		listener;

	private DeviceSettingsPanel							deviceSettingsOn;

	private DeviceSettingsPanel							deviceSettingsOff;

	private JRadioButton								stopByUser				= new JRadioButton(
																						"When stopped manually.",
																						false);

	private JRadioButton								stopByRuntime			= new JRadioButton(
																						"After a given time.",
																						false);

	private JComboBox<String>								configGroupField		= new JComboBox<String>();

	private JComboBox<String>								channelField			= new JComboBox<String>();

	private Vector<CameraPanel>								cameraPanels					= new Vector<CameraPanel>();

	private JCheckBox								saveImagesField			= new JCheckBox("Save images", true);

	private JTextField									imageNameField				= new JTextField();

	private JLabel										imageNameLabel				= new JLabel(
																							"Image name used for saving:");
	
	private YouScopeFrame									frame;
	private YouScopeClient client;
	private YouScopeServer server;
	
	private GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();

	/**
	 * Constructor.
	 * @param microscope Interface to the microscope.
	 * @param client Interface to the client.
	 */
	MultiCameraContinousImagingConfigurationAddon(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
	
	private class CameraPanel extends JPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 2734953846897939031L;
		private final JCheckBox			shouldUseBox;
		private final JFormattedTextField	exposureField			= new JFormattedTextField(StandardFormats.getDoubleFormat());
		
		private final JLabel exposureLabel;
		private final String				camera;

		CameraPanel(String camera)
		{
			this.camera = camera;
			exposureLabel = new JLabel("Exposure Camera \"" + camera + "\":");
			shouldUseBox = new JCheckBox("Image Camera \"" + camera + "\"");
			shouldUseBox.setSelected(false);
			shouldUseBox.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					exposureLabel.setVisible(shouldUseBox.isSelected());
					exposureField.setVisible(shouldUseBox.isSelected());
					frame.pack();
				}
			});
			
			GridBagLayout elementsLayout = new GridBagLayout();
			setLayout(elementsLayout);
			StandardFormats.addGridBagElement(shouldUseBox, elementsLayout, newLineConstr, this);
			StandardFormats.addGridBagElement(exposureLabel, elementsLayout, newLineConstr, this);
			StandardFormats.addGridBagElement(exposureField, elementsLayout, newLineConstr, this);
			
			setSelected(true);
			setExposure(20.0);
		}

		public void setSelected(boolean selected)
		{
			shouldUseBox.setSelected(selected);
			exposureLabel.setVisible(shouldUseBox.isSelected());
			exposureField.setVisible(shouldUseBox.isSelected());
		}

		public boolean isSelected()
		{
			return shouldUseBox.isSelected();
		}

		public String getDevice()
		{
			return camera;
		}

		public void setExposure(double exposure)
		{
			this.exposureField.setValue(exposure);
		}

		public double getExposure()
		{
			return ((Number)exposureField.getValue()).doubleValue();
		}
	}
	
	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setTitle("Continuous Imaging Measurement Configuration");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(false);

		frame.startInitializing();
		(new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					initilizeFrame();
				}
				catch(RemoteException e)
				{
					MultiCameraContinousImagingConfigurationAddon.this.frame.setToErrorState("Could not initialize frame", e);
				}
				MultiCameraContinousImagingConfigurationAddon.this.frame.endLoading();
			}
		})).start();
	}

	@Override
	public  MultiCameraContinousImagingConfigurationDTO getConfigurationData()
	{
		if(measurementConfiguration == null)
		{
			MultiCameraContinousImagingConfigurationDTO configuration = new MultiCameraContinousImagingConfigurationDTO();
			MeasurementSaveSettings saveSettings = new MeasurementSaveSettings();
			saveSettings.setFolder(client.getProperties().getProperty(YouScopeProperties.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER, ""));
			saveSettings.setImageFolderStructure(ImageFolderStructure.ALL_IN_ONE_FOLDER);
			saveSettings.setImageFileName("%N_time%n");
			configuration.setSaveSettings(saveSettings);
			if (client.getProperties().getProperty(YouScopeProperties.PROPERTY_PREINITIALIZE_CAMERA_SETTINGS, true))
			{
				try
				{
					Microscope microscope = server.getMicroscope();
					CameraDevice camera = microscope.getCameraDevice();
					String deviceName = camera.getDeviceID();
					Property[] properties = camera.getEditableProperties();
					DeviceSettingDTO[] cameraDeviceSettings = new DeviceSettingDTO[properties.length];
					for(int i=0; i< properties.length; i++)
					{
						cameraDeviceSettings[i] = new DeviceSettingDTO();
						cameraDeviceSettings[i].setAbsoluteValue(true);
						cameraDeviceSettings[i].setDeviceProperty(deviceName, properties[i].getPropertyID());
						cameraDeviceSettings[i].setValue(properties[i].getValue());
					}
					configuration.setDeviseSettingsOn(cameraDeviceSettings);
				}
				catch (Exception e)
				{
					client.sendError("Could not pre-initialize measurement startup settings. Letting these settings empty and continuing.", e);
					configuration.setDeviseSettingsOn(new DeviceSettingDTO[0]);
				}
			}
			try
			{
				setConfigurationData(configuration);
			}
			catch(ConfigurationException e)
			{
				client.sendError("Could not create new empty measurement.", e);
			}
		}
		return measurementConfiguration;
	}
	
	@Override
	public void setConfigurationData(MeasurementConfiguration measurementConfiguration) throws ConfigurationException
	{
		if(!(measurementConfiguration instanceof MultiCameraContinousImagingConfigurationDTO))
			throw new ConfigurationException("Only configurable measurement configurations accepted by this addon.");
		this.measurementConfiguration = (MultiCameraContinousImagingConfigurationDTO)measurementConfiguration;
	}

	private void initilizeFrame() throws RemoteException
	{
		// Get supported image types
		String[] imageTypes;
		try
		{
			imageTypes = server.getConfiguration().getSupportedImageFormats();
		}
		catch (RemoteException e1)
		{
			client.sendError("Could not obtain supported image file types from server.", e1);
			imageTypes = new String[0];
		}
		imageTypeField = new JComboBox<String>(imageTypes);

		// Next & Last Buttons
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		previousButton.setEnabled(false);
		previousButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (currentPage <= 1)
					return;
				pagesLayout.previous(pagesPanel);
				if (--currentPage <= 1)
					previousButton.setEnabled(false);
				nextButton.setText("Next");
				frame.pack();
			}
		});

		nextButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (currentPage >= numPages)
				{
					createMeasurement();
					return;
				}
				pagesLayout.next(pagesPanel);
				if (++currentPage >= numPages)
					nextButton.setText("Finish");
				previousButton.setEnabled(true);
				frame.pack();
			}
		});
		buttonPanel.add(previousButton);
		buttonPanel.add(nextButton);
		
		saveImagesField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				boolean selected = saveImagesField.isSelected();
				imageNameLabel.setVisible(selected);
				imageNameField.setVisible(selected);

				MultiCameraContinousImagingConfigurationAddon.this.frame.pack();
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

		// Load data from configuration
		MultiCameraContinousImagingConfigurationDTO configuration = getConfigurationData();
		
		loadConfigGroupNames();
		String configGroup = configuration.getChannelGroup();
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
		
		saveImagesField.setSelected(configuration.isSaveImages());
		
		nameField.setText(configuration.getName());
		if (configuration.getMeasurementRuntime() >= 0)
			runtimeField.setValue(configuration.getMeasurementRuntime() / 1000);
		else
			runtimeField.setValue(3600);
		
		MeasurementSaveSettings saveSettings = configuration.getSaveSettings();
		if(saveSettings != null)
		{
			folderField.setText(saveSettings.getFolder());
			imageTypeField.setSelectedItem(saveSettings.getImageFileType());
		}

		// Load the single configuration pages.
		pagesPanel.add(new StartPage(), "Description");
		pagesPanel.add(new GeneralSettingsPage(), "General Settings");
		pagesPanel.add(new StartAndEndSettingsPage(), "Start and End Settings");
		pagesPanel.add(new ImagingDefinitionPage(), "Imaging Definitions");

		String[] cameras = configuration.getCameras();
		double[] exposures = configuration.getExposures();
		int imagingPeriod = configuration.getImagingPeriod();
		
		periodField.setValue(imagingPeriod);
		
		if(imagingPeriod <= 0)
		{
			periodField.setVisible(false);
			periodLabel.setVisible(false);
			periodTypeField.setSelectedIndex(0);
		}
		else
		{
			periodField.setVisible(true);
			periodLabel.setVisible(true);
			periodTypeField.setSelectedIndex(1);
		}
		
		if(cameras.length != 0 && exposures.length != 0)
		{
			for(CameraPanel cameraPanel : cameraPanels)
			{
				cameraPanel.setSelected(false);
			}
			for(int i=0; i<exposures.length && i < cameras.length; i++)
			{
				for(CameraPanel cameraPanel : cameraPanels)
				{
					if(cameraPanel.getDevice().compareTo(cameras[i]) == 0)
					{
						cameraPanel.setSelected(true);
						cameraPanel.setExposure(exposures[i]);
						break;
					}
				}
			}
		}
		else
		{
			for(CameraPanel cameraPanel : cameraPanels)
			{
				cameraPanel.setSelected(true);
				cameraPanel.setExposure(50);
			}
		}
		
		if (configuration.getMeasurementRuntime() >= 0)
		{
			stopByRuntime.doClick();
		}
		else
		{
			stopByUser.doClick();
		}

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(buttonPanel, BorderLayout.SOUTH);
		contentPane.add(pagesPanel, BorderLayout.CENTER);
		frame.setContentPane(contentPane);
		frame.pack();
	}

	private void createMeasurement()
	{
		MultiCameraContinousImagingConfigurationDTO configuration = getConfigurationData();
		configuration.setName(nameField.getText());
		if (stopByRuntime.isSelected())
			configuration.setMeasurementRuntime(((Number) runtimeField.getValue()).intValue() * 1000);
		else
			configuration.setMeasurementRuntime(-1);
		
		MeasurementSaveSettings saveSettings = new MeasurementSaveSettings();
		saveSettings.setFolder(folderField.getText());
		saveSettings.setImageFileType((String) imageTypeField.getSelectedItem());
		configuration.setSaveSettings(saveSettings);
		
		configuration.setDeviseSettingsOn(deviceSettingsOn.getSettings());
		configuration.setDeviseSettingsOff(deviceSettingsOff.getSettings());
		
		// Imaging settings
		configuration.setChannel((String)configGroupField.getSelectedItem(), (String)channelField.getSelectedItem());
		
		Vector<CameraPanel> selectedCameraPanels = new Vector<CameraPanel>();
		for(CameraPanel cameraPanel : cameraPanels)
		{
			if(cameraPanel.isSelected())
				selectedCameraPanels.addElement(cameraPanel);
		}
		String[] selectedCameras = new String[selectedCameraPanels.size()];
		double[] selectedExposures = new double[selectedCameraPanels.size()];
		for(int i=0; i<selectedCameraPanels.size(); i++)
		{
			CameraPanel selectedCameraPanel = selectedCameraPanels.elementAt(i);
			selectedCameras[i] = selectedCameraPanel.getDevice();
			selectedExposures[i] = selectedCameraPanel.getExposure();
		}
		
		int imagingPeriod;
		if(periodTypeField.getSelectedIndex() == 0)
			imagingPeriod = 0;
		else
			imagingPeriod = ((Number)periodField.getValue()).intValue();
		
		configuration.setCameras(selectedCameras);
		configuration.setExposures(selectedExposures);
		configuration.setImagingPeriod(imagingPeriod);
		configuration.setSaveImages(saveImagesField.isSelected());
		if(imageNameField.getText().length() > 3)
			configuration.setImageSaveName(imageNameField.getText().substring(0, 3));
		else
			configuration.setImageSaveName(imageNameField.getText());
		
		
		// Save some of the configurations
		client.getProperties().setProperty(YouScopeProperties.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER, saveSettings.getFolder());

		if (listener != null)
			listener.measurementConfigurationFinished(configuration);

		// Inform listener that configuration is finished.
		for(MeasurementConfigurationAddonListener listener : configurationListeners)
		{
			listener.measurementConfigurationFinished(configuration);
		}

		frame.setVisible(false);
	}
	
	private class StartPage extends JPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 2394617369656492466L;

		StartPage()
		{
			setLayout(new BorderLayout(5, 5));
			setOpaque(false);
			JLabel textLabel = new JLabel("<html>A multi-camera continuous imaging measurement is<br />used to (rapidly) take images with several cameras<br />in parallel at the current<br />position every given period.<br />One can select the channel,<br />the exposure time and the<br />imaging period.<br />Instead of choosing an imaging period, one<br />can also choose to \"bulk image\",<br />which means to image as fast as possible.</html>", SwingConstants.LEFT);
			textLabel.setVerticalAlignment(SwingConstants.TOP);
			textLabel.setBorder(new TitledBorder("Description"));
			add(textLabel, BorderLayout.CENTER);
			// Descriptive image
			ImageIcon microplateMeasurementIcon = ImageLoadingTools.getResourceIcon("ch/ethz/csb/youscope/addon/multicameracontinuousimaging/images/continous-imaging.jpg", "Multi-Camera Continous Imaging Measurement");
			if(microplateMeasurementIcon != null)
			{
				JPanel imagePanel = new JPanel(new BorderLayout());
				imagePanel.setOpaque(false);
				JLabel imageLabel = new JLabel(microplateMeasurementIcon, SwingConstants.LEFT);
				imageLabel.setBackground(Color.BLACK);
				imageLabel.setOpaque(true);
				imageLabel.setBorder(new LineBorder(Color.BLACK, 1));
				JLabel legendLabel = new JLabel("<html><b>Figure 1:</b> Picture of a microscope.</html>", SwingConstants.LEFT);
				imagePanel.add(imageLabel, BorderLayout.CENTER);
				imagePanel.add(legendLabel, BorderLayout.SOUTH);
				add(imagePanel, BorderLayout.WEST);
			}
		}
	}

	private class StartAndEndSettingsPage extends JPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -8833466993051293347L;

		StartAndEndSettingsPage()
		{
			MultiCameraContinousImagingConfigurationDTO configuration = getConfigurationData();
			setLayout(new GridLayout(2, 1));
			deviceSettingsOn = new DeviceSettingsPanel(client, server, true);
			deviceSettingsOn.setSettings(configuration.getDeviseSettingsOn());
			deviceSettingsOff = new DeviceSettingsPanel(client, server);
			deviceSettingsOff.setSettings(configuration.getDeviseSettingsOff());

			JPanel onPanel = new JPanel(new BorderLayout(2, 2));
			onPanel.add(new JLabel("Device Settings when measurement starts:"),
					BorderLayout.NORTH);
			onPanel.add(deviceSettingsOn, BorderLayout.CENTER);
			add(onPanel);
			JPanel offPanel = new JPanel(new BorderLayout(2, 2));
			offPanel.add(new JLabel("Device Settings when measurement ends:"),
					BorderLayout.NORTH);
			offPanel.add(deviceSettingsOff, BorderLayout.CENTER);
			add(offPanel);

			setBorder(new TitledBorder("Measurement Start and End Settings"));
		}
	}

	private class ImagingDefinitionPage extends JPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -8833466993051293407L;

		ImagingDefinitionPage()
		{
			GridBagLayout elementsLayout = new GridBagLayout();
			setLayout(elementsLayout);
			
			StandardFormats.addGridBagElement(new JLabel("Channel Group:"), elementsLayout, newLineConstr, this);
			StandardFormats.addGridBagElement(configGroupField, elementsLayout, newLineConstr, this);

			StandardFormats.addGridBagElement(new JLabel("Channel:"), elementsLayout, newLineConstr, this);
			StandardFormats.addGridBagElement(channelField, elementsLayout, newLineConstr, this);

			periodTypeField.addItemListener(new ItemListener()
			{
				@Override
				public void itemStateChanged(ItemEvent itemEvent)
				{
					if(periodTypeField.getSelectedIndex() == 0)
					{
						periodLabel.setVisible(false);
						periodField.setVisible(false);
					}
					else
					{
						periodLabel.setVisible(true);
						periodField.setVisible(true);
					}
					frame.pack();
				}
			});
			
			StandardFormats.addGridBagElement(periodTypeLabel, elementsLayout, newLineConstr, this);
			StandardFormats.addGridBagElement(periodTypeField, elementsLayout, newLineConstr, this);
			StandardFormats.addGridBagElement(periodLabel, elementsLayout, newLineConstr, this);
			StandardFormats.addGridBagElement(periodField, elementsLayout, newLineConstr, this);
			
			String[] cameraDevices;
			try
			{
				Device[] cameras = server.getMicroscope().getDevices(DeviceType.CameraDevice);
				cameraDevices = new String[cameras.length];
				for(int i=0; i<cameras.length; i++)
				{
					cameraDevices[i] = cameras[i].getDeviceID();
				}
			}
			catch (Exception e2)
			{
				client.sendError("Could not detect installed cameras.", e2);
				cameraDevices = new String[0];
			}
			for (String camera : cameraDevices)
			{
				CameraPanel cameraPanel = new CameraPanel(camera);
				cameraPanels.addElement(cameraPanel);
				
				StandardFormats.addGridBagElement(cameraPanel, elementsLayout, newLineConstr, this);
			}
			
			StandardFormats.addGridBagElement(saveImagesField, elementsLayout, newLineConstr, this);
			StandardFormats.addGridBagElement(imageNameLabel, elementsLayout, newLineConstr,
					this);
			StandardFormats.addGridBagElement(imageNameField, elementsLayout, newLineConstr,
					this);
			
			StandardFormats.addGridBagElement(new JPanel(), elementsLayout, StandardFormats.getBottomContstraint(), this);

			setBorder(new TitledBorder("Imaging Properties"));
		}
	}

	private class GeneralSettingsPage extends JPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -8833466993053293407L;

		GridBagLayout				layout				= new GridBagLayout();

		
		// Instance Initializer
		GeneralSettingsPage()
		{
			setLayout(layout);
			StandardFormats.addGridBagElement(new JLabel("Name:"), layout, newLineConstr, this);
			StandardFormats.addGridBagElement(nameField, layout, newLineConstr, this);

			StandardFormats.addGridBagElement(new JLabel("Measurement finishes:"), layout,
					newLineConstr, this);
			ButtonGroup stopConditionGroup = new ButtonGroup();
			stopConditionGroup.add(stopByUser);
			stopConditionGroup.add(stopByRuntime);
			class StopTypeChangedListener implements ActionListener
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					if (stopByUser.isSelected())
					{
						runtimeFieldLabel.setVisible(false);
						runtimeField.setVisible(false);
						frame.pack();
					}
					else
					{
						// stopByRuntime
						runtimeFieldLabel.setVisible(true);
						runtimeField.setVisible(true);
						frame.pack();
					}
				}
			}
			stopByUser.addActionListener(new StopTypeChangedListener());
			stopByRuntime.addActionListener(new StopTypeChangedListener());

			StandardFormats.addGridBagElement(stopByUser, layout, newLineConstr, this);
			StandardFormats.addGridBagElement(stopByRuntime, layout, newLineConstr, this);

			StandardFormats.addGridBagElement(runtimeFieldLabel, layout, newLineConstr, this);
			StandardFormats.addGridBagElement(runtimeField, layout, newLineConstr, this);

			// Panel to choose files
			StandardFormats.addGridBagElement(new JLabel("Output Directory:"), layout,
					newLineConstr, this);
			JPanel folderPanel = new JPanel(new BorderLayout(5, 0));
			folderPanel.add(folderField, BorderLayout.CENTER);

			if (client.isLocalServer())
			{
				JButton openFolderChooser = new JButton("Edit");
				openFolderChooser.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						JFileChooser fileChooser = new JFileChooser(folderField
								.getText());
						fileChooser
								.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						int returnVal = fileChooser.showDialog(
								null,
								"Open");
						if (returnVal == JFileChooser.APPROVE_OPTION)
						{
							folderField.setText(fileChooser.getSelectedFile()
									.getAbsolutePath());
						}
					}
				});
				folderPanel.add(openFolderChooser, BorderLayout.EAST);
			}
			StandardFormats.addGridBagElement(folderPanel, layout, newLineConstr, this);

			// Panel to choose image file type
			StandardFormats.addGridBagElement(new JLabel("Image File Type:"), layout,
					newLineConstr, this);
			StandardFormats.addGridBagElement(imageTypeField, layout, newLineConstr, this);
			StandardFormats.addGridBagElement(new JPanel(), layout, StandardFormats.getBottomContstraint(), this);

			setBorder(new TitledBorder("Measurement Properties"));
		}
	}
	
	@Override
	public void addConfigurationListener(MeasurementConfigurationAddonListener listener)
	{
		configurationListeners.add(listener);
	}

	@Override
	public void removeConfigurationListener(MeasurementConfigurationAddonListener listener)
	{
		configurationListeners.remove(listener);
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
}
