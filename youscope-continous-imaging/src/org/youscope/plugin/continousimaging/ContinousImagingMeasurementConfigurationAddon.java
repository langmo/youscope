/**
 * 
 */
package org.youscope.plugin.continousimaging;

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
import java.util.ArrayList;

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

import org.youscope.addon.component.ComponentAddonUIListener;
import org.youscope.addon.measurement.MeasurementConfigurationAddon;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeProperties;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.ImageFolderStructure;
import org.youscope.common.measurement.MeasurementSaveSettings;
import org.youscope.common.microscope.CameraDevice;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.DeviceSettingDTO;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.Property;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DeviceSettingsPanel;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 */
class ContinousImagingMeasurementConfigurationAddon implements MeasurementConfigurationAddon<ContinousImagingMeasurementConfiguration>
{

	private ArrayList<ComponentAddonUIListener<? super ContinousImagingMeasurementConfiguration>> configurationListeners = new ArrayList<ComponentAddonUIListener<? super ContinousImagingMeasurementConfiguration>>();
	
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
	
	private JComboBox<String>									imagingSpeedField = new JComboBox<String>(new String[]{"Burst", "Given Period"});

	private ContinousImagingMeasurementConfiguration	measurementConfiguration;

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

	private JFormattedTextField					exposureField			= new JFormattedTextField(StandardFormats.getDoubleFormat());
	private JFormattedTextField					imagingPeriodField			= new JFormattedTextField(StandardFormats.getIntegerFormat());
	private JLabel imagingPeriodLabel = new JLabel("Imaging Period (ms):");

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
	ContinousImagingMeasurementConfigurationAddon(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
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
					ContinousImagingMeasurementConfigurationAddon.this.frame.setToErrorState("Could not initialize frame", e);
				}
				ContinousImagingMeasurementConfigurationAddon.this.frame.endLoading();
			}
		})).start();
	}

	@Override
	public  ContinousImagingMeasurementConfiguration getConfiguration()
	{
		if(measurementConfiguration == null)
		{
			ContinousImagingMeasurementConfiguration configuration = new ContinousImagingMeasurementConfiguration();
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
				setConfiguration(configuration);
			}
			catch(ConfigurationException e)
			{
				client.sendError("Could not create new empty measurement.", e);
			}
		}
		return measurementConfiguration;
	}
	
	@Override
	public void setConfiguration(Configuration measurementConfiguration) throws ConfigurationException
	{
		if(!(measurementConfiguration instanceof ContinousImagingMeasurementConfiguration))
			throw new ConfigurationException("Only configurable measurement configurations accepted by this addon.");
		this.measurementConfiguration = (ContinousImagingMeasurementConfiguration)measurementConfiguration;
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

				ContinousImagingMeasurementConfigurationAddon.this.frame.pack();
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

		// Load data from configuration
		ContinousImagingMeasurementConfiguration configuration = getConfiguration();
		
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
		ContinousImagingMeasurementConfiguration configuration = getConfiguration();
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
		
		
		// Save some of the configurations
		client.getProperties().setProperty(YouScopeProperties.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER, saveSettings.getFolder());

		// Inform listener that configuration is finished.
		for(ComponentAddonUIListener<? super ContinousImagingMeasurementConfiguration> listener : configurationListeners)
		{
			listener.configurationFinished(configuration);
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
			JLabel textLabel = new JLabel("<html>A continuous imaging measurement is<br />used to (rapidly) take images at the current<br />position every given period.<br />One can select the channel,<br />the exposure time and the<br />imaging period.<br />Instead of choosing an imaging period, one<br />can also choose to \"bulk image\",<br />which means to image as fast as possible.</html>", SwingConstants.LEFT);
			textLabel.setVerticalAlignment(SwingConstants.TOP);
			textLabel.setBorder(new TitledBorder("Description"));
			add(textLabel, BorderLayout.CENTER);
			// Descriptive image
			ImageIcon microplateMeasurementIcon = ImageLoadingTools.getResourceIcon("org/youscope/plugin/continousimaging/images/continous-imaging.jpg", "Continous Imaging Measurement");
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
			ContinousImagingMeasurementConfiguration configuration = getConfiguration();
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
	public void addUIListener(ComponentAddonUIListener<? super ContinousImagingMeasurementConfiguration> listener)
	{
		configurationListeners.add(listener);
	}

	@Override
	public void removeUIListener(ComponentAddonUIListener<? super ContinousImagingMeasurementConfiguration> listener)
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
