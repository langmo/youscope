/**
 * 
 */
package ch.ethz.csb.youscope.addon.taskmeasurement;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
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
import ch.ethz.csb.youscope.client.uielements.FileNameComboBox;
import ch.ethz.csb.youscope.client.uielements.ImageLoadingTools;
import ch.ethz.csb.youscope.client.uielements.StandardFormats;
import ch.ethz.csb.youscope.client.uielements.TasksDefinitionPanel;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.ImageFolderStructure;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.measurement.MeasurementSaveSettings;
import ch.ethz.csb.youscope.shared.microscope.CameraDevice;
import ch.ethz.csb.youscope.shared.microscope.Property;
import ch.ethz.csb.youscope.shared.microscope.DeviceSettingDTO;
import ch.ethz.csb.youscope.shared.microscope.Microscope;

/**
 * @author Moritz Lang
 */
class TaskMeasurementConfigurationAddon implements MeasurementConfigurationAddon
{

	private Vector<MeasurementConfigurationAddonListener> configurationListeners = new Vector<MeasurementConfigurationAddonListener>();
	
	private int											currentPage				= 1;

	private static final int							numPages				= 4;

	protected CardLayout								pagesLayout				= new CardLayout();

	protected JPanel									pagesPanel				= new JPanel(
																						pagesLayout);

	protected JButton									previousButton			= new JButton(
																						"Previous");

	protected JButton									nextButton				= new JButton(
																						"Next");

	protected JTextField								nameField				= new JTextField(
																						"unnamed");

	private JLabel										runtimeFieldLabel		= new JLabel(
																						"Measurement Total Runtime (seconds):");

	protected JFormattedTextField						runtimeField			= new JFormattedTextField(
																						StandardFormats
																								.getIntegerFormat());

	protected JTextField								folderField				= new JTextField();

	protected JComboBox<String>									imageTypeField;

	protected TaskMeasurementConfiguration	measurementConfiguration;

	protected MeasurementConfigurationAddonListener		listener;

	protected JList<String>										taskList;

	private DeviceSettingsPanel							deviceSettingsOn;

	private DeviceSettingsPanel							deviceSettingsOff;

	private JRadioButton								stopByUser				= new JRadioButton(
																						"When stopped manually / after tasks finished.",
																						false);

	private JRadioButton								stopByRuntime			= new JRadioButton(
																						"After a given time.",
																						false);

	protected JComboBox<ImageFolderStructure>									imageFolderTypeField	= new JComboBox<ImageFolderStructure>(
																						ImageFolderStructure
																								.values());

	protected FileNameComboBox									imageFileField			= new FileNameComboBox(
																						FileNameComboBox.Type.FILE_NAME);

	private YouScopeFrame									frame;
	private YouScopeClient client;
	private YouScopeServer server;
	
	protected GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();

	/**
	 * Constructor.
	 * @param microscope Interface to the microscope.
	 * @param client Interface to the client.
	 */
	TaskMeasurementConfigurationAddon(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
	
	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setTitle("Task Measurement Configuration");
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
					TaskMeasurementConfigurationAddon.this.frame.setToErrorState("Could not initialize frame", e);
				}
				TaskMeasurementConfigurationAddon.this.frame.endLoading();
			}
		})).start();
	}

	@Override
	public  TaskMeasurementConfiguration getConfigurationData()
	{
		if(measurementConfiguration == null)
		{
			TaskMeasurementConfiguration configuration = new TaskMeasurementConfiguration();
			MeasurementSaveSettings saveSettings = new MeasurementSaveSettings();
			saveSettings.setFolder(client.getProperties().getProperty(YouScopeProperties.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER, ""));
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
		if(!(measurementConfiguration instanceof TaskMeasurementConfiguration))
			throw new ConfigurationException("Only configurable measurement configurations accepted by this addon.");
		this.measurementConfiguration = (TaskMeasurementConfiguration)measurementConfiguration;
	}

	protected void initilizeFrame() throws RemoteException
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
		
		// Load data from configuration
		TaskMeasurementConfiguration configuration = getConfigurationData();
		nameField.setText(configuration.getName());
		if (configuration.getMeasurementRuntime() >= 0)
			runtimeField.setValue(configuration.getMeasurementRuntime() / 1000);
		else
			runtimeField.setValue(3600);
		
		MeasurementSaveSettings saveSettings = configuration.getSaveSettings();
		if(saveSettings != null)
		{
			folderField.setText(saveSettings.getFolder());
			imageFolderTypeField.setSelectedItem(saveSettings.getImageFolderStructure());
			imageFileField.setSelectedItem(saveSettings.getImageFileName());
			imageTypeField.setSelectedItem(saveSettings.getImageFileType());
		}

		// Load the single configuration pages.
		pagesPanel.add(new StartPage(), "Description");
		pagesPanel.add(new GeneralSettingsPage(), "General Settings");
		pagesPanel.add(new StartAndEndSettingsPage(), "Start and End Settings");
		pagesPanel.add(new TaskDefinitionPage(), "Task Definition");

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

	protected void createMeasurement()
	{
		TaskMeasurementConfiguration configuration = getConfigurationData();
		configuration.setName(nameField.getText());
		if (stopByRuntime.isSelected())
			configuration.setMeasurementRuntime(((Number) runtimeField
					.getValue()).intValue() * 1000);
		else
			configuration.setMeasurementRuntime(-1);
		
		MeasurementSaveSettings saveSettings = new MeasurementSaveSettings();
		saveSettings.setFolder(folderField.getText());
		saveSettings.setImageFileType((String) imageTypeField.getSelectedItem());
		saveSettings.setImageFolderStructure((ImageFolderStructure) imageFolderTypeField.getSelectedItem());
		saveSettings.setImageFileName(imageFileField.getSelectedItem().toString());
		configuration.setSaveSettings(saveSettings);
		
		configuration.setDeviseSettingsOn(deviceSettingsOn.getSettings());
		configuration.setDeviseSettingsOff(deviceSettingsOff.getSettings());
		
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

	protected class StartPage extends JPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 2394617369656492466L;

		StartPage()
		{
			setLayout(new BorderLayout(5, 5));
			setOpaque(false);
			JLabel textLabel = new JLabel("<html>A task measurement helps to<br />perform several different<br />tasks a microscope should do<br />in parallel (see Figure 1).<br />" +
					"Every task is repeated regularly with<br />a given period and start time,<br />and is composed of several<br />subelements, called jobs.<br />" +
					"One job thereby corresponds to a single<br />action of the microscope, like taking<br />a bright-field image or changing the<br /> stage position.<br />" +
					"Every task either has a fixed period length,<br />meaning that its jobs are executed<br />e.g. every two minutes,<br />or a variable period length,<br />meaning that its jobs are executed<br />a given time span after they finished<br />"+
					"The latter one is useful if a task of lower<br />priority should be executed with a<br />high frequency, but without<br />blocking the exectution of tasks<br /> of higher priority.</html>", SwingConstants.LEFT);
			textLabel.setVerticalAlignment(SwingConstants.TOP);
			textLabel.setBorder(new TitledBorder("Description"));
			add(textLabel, BorderLayout.CENTER);
			// Descriptive image
			ImageIcon microplateMeasurementIcon = ImageLoadingTools.getResourceIcon("ch/ethz/csb/youscope/addon/taskmeasurement/images/taskMeasurement.jpg", "Task Measurement");
			if(microplateMeasurementIcon != null)
			{
				JPanel imagePanel = new JPanel(new BorderLayout());
				imagePanel.setOpaque(false);
				JLabel imageLabel = new JLabel(microplateMeasurementIcon, SwingConstants.LEFT);
				imageLabel.setBackground(Color.WHITE);
				imageLabel.setOpaque(true);
				imageLabel.setBorder(new LineBorder(Color.BLACK, 1));
				JLabel legendLabel = new JLabel("<html><b>Figure 1:</b> Flowchart of a task measurement.</html>", SwingConstants.LEFT);
				imagePanel.add(imageLabel, BorderLayout.CENTER);
				imagePanel.add(legendLabel, BorderLayout.SOUTH);
				add(imagePanel, BorderLayout.WEST);
			}
		}
	}
	
	protected class StartAndEndSettingsPage extends JPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -8833466993051293347L;

		StartAndEndSettingsPage()
		{
			TaskMeasurementConfiguration configuration = getConfigurationData();
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

	protected class TaskDefinitionPage extends JPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -8833466993051293407L;

		TaskDefinitionPage()
		{
			setLayout(new BorderLayout());
			add(new TasksDefinitionPanel(client, server, frame, getConfigurationData()), BorderLayout.CENTER);

			setBorder(new TitledBorder("Definition of Measurement Tasks"));
		}
	}

	protected class GeneralSettingsPage extends JPanel
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -8833466993053293407L;

		GridBagLayout				layout				= new GridBagLayout();

		JLabel						fixedPeriodLabel	= new JLabel(
																"Fixed period length (seconds):");

		JLabel						fixedWellTimeLabel	= new JLabel(
																"Fixed Runtime per Well (ms):");

		protected final String		addButtonFile		= "icons/block--plus.png";

		protected final String		deleteButtonFile	= "icons/block--minus.png";

		protected final String		upButtonFile		= "icons/arrow-090.png";

		protected final String		downButtonFile		= "icons/arrow-270.png";

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

			StandardFormats.addGridBagElement(new JLabel("Image folder structure:"), layout,
					newLineConstr, this);
			StandardFormats.addGridBagElement(imageFolderTypeField, layout, newLineConstr, this);

			StandardFormats.addGridBagElement(new JLabel("Image filename:"), layout,
					newLineConstr, this);
			StandardFormats.addGridBagElement(imageFileField, layout, newLineConstr, this);

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
}
