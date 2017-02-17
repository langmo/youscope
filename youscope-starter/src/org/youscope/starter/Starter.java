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
package org.youscope.starter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.MaskFormatter;

/**
 * @author langmo
 */
public class Starter extends JFrame
{

	/**
	 * Serial Version UID.
	 */
	private static final long			serialVersionUID				= -2212968401423506661L;

	/**
	 * The standard port the server object registers to.
	 */
	public static final int				REGISTRY_PORT					= 1237;

	// The server and the client object
	private Server						server							= new Server();

	private Client						client							= new Client();

	// General UI elements
	private JRadioButton				startClientServerButton			= new JRadioButton("Start locally", false);

	private JRadioButton				startClientButton				= new JRadioButton("Connect to microscope server", false);

	private JRadioButton				startServerButton				= new JRadioButton("Start microscope server", false);

	// panels for the different startup types
	private JPanel						serverURLPanel;

	private JPanel						serverPortPanel;

	private JPanel						passwordPanel;

	private JPanel						configFilePanel;

	// UI elements
	private JCheckBox					resetMicroManagerConfiguration	= new JCheckBox("Define microscope connection configuration.");

	private JFormattedTextField			urlField;

	private JFormattedTextField			portField;

	private JPasswordField				passwordField;

	private ConfigFileChooserComboBox	configFileField;

	private JButton						startButton						= new JButton("Start");

	// How the system should be started up
	private enum StartUpType
	{
		CLIENT_SERVER, CLIENT, SERVER
	}

	private static Starter	mainProgram	= null;

	/**
	 * Returns the only object of this class.
	 * 
	 * @return Only object of this class.
	 */
	public static Starter getMainProgram()
	{
		synchronized(Starter.class)
		{
			if(mainProgram == null)
				mainProgram = new Starter();
		}
		return mainProgram;
	}

	private Starter()
	{
		super("YouScope - Microscope Control");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		try
		{
			setAlwaysOnTop(true);
		}
		catch(@SuppressWarnings("unused") SecurityException e)
		{
			// Do nothing.
		}
		setUndecorated(true);
		

		/*****************************************
		 * Get last settings
		 *****************************************/
		ConfigurationSettings configuration = ConfigurationSettings.loadProperties();
		configFileField = new ConfigFileChooserComboBox(null, configuration);
		// Get last used config
		String lastURL = configuration.getProperty(ConfigurationSettings.SETTINGS_SERVER_URL, "127.000.000.001");
		int lastPort = Integer.parseInt(configuration.getProperty(ConfigurationSettings.SETTINGS_SERVER_PORT, ((Integer)REGISTRY_PORT).toString()));
		StartUpType lastStartupSelection;
		try
		{
			lastStartupSelection = Enum.valueOf(StartUpType.class, configuration.getProperty(ConfigurationSettings.SETTINGS_STARTUP_TYPE, StartUpType.CLIENT_SERVER.name()));
		}
		catch(@SuppressWarnings("unused") IllegalArgumentException e)
		{
			// Set to standard
			lastStartupSelection = StartUpType.CLIENT_SERVER;
		}

		/*****************************************
		 * Initialize layout
		 *****************************************/
		setResizable(true);
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setOpaque(true);
		contentPane.setBackground(Color.WHITE);
		
		GridBagConstraints newLineConstr = new GridBagConstraints();
		newLineConstr.fill = GridBagConstraints.HORIZONTAL;
		newLineConstr.gridwidth = GridBagConstraints.REMAINDER;
		newLineConstr.anchor = GridBagConstraints.NORTHWEST;
		newLineConstr.gridx = 0;
		newLineConstr.weightx = 1.0;
		newLineConstr.weighty = 0;
		
		GridBagConstraints bottomConstr = new GridBagConstraints();
		bottomConstr.weighty = 1.0;
		bottomConstr.weightx = 1.0;
		bottomConstr.fill = GridBagConstraints.BOTH;
		bottomConstr.gridwidth = GridBagConstraints.REMAINDER;
		
		GridBagLayout layout = new GridBagLayout();
		JPanel contentPanel = new JPanel(layout);
		contentPanel.setOpaque(false);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		/****************************************
		 * Add icon and teaser images
		 ****************************************/
		String leftImageFile = "org/youscope/starter/images/logo.jpg";
		try
		{
			URL leftImageURL = getClass().getClassLoader().getResource(leftImageFile);
			if(leftImageURL != null)
			{
				BufferedImage leftImage = ImageIO.read(leftImageURL);
				JLabel imageLabel = new JLabel(new ImageIcon(leftImage));
				imageLabel.setHorizontalAlignment(SwingConstants.LEFT);
				imageLabel.setVerticalAlignment(SwingConstants.TOP);
				imageLabel.setBackground(Color.WHITE);
				imageLabel.setOpaque(true);
				imageLabel.setBorder(new EmptyBorder(2, 6, 6, 4));
				contentPane.add(imageLabel, BorderLayout.WEST); 
			}
		}
		catch(@SuppressWarnings("unused") Exception e)
		{
			// Do nothing.
		}
		
		// Set tray icon image.
		final String TRAY_ICON_URL16 = "org/youscope/starter/images/icon-16.png";
		final String TRAY_ICON_URL32 = "org/youscope/starter/images/icon-32.png";
		final String TRAY_ICON_URL96 = "org/youscope/starter/images/icon-96.png";
		final String TRAY_ICON_URL194 = "org/youscope/starter/images/icon-194.png";
		URL trayIconURL16 = getClass().getClassLoader().getResource(TRAY_ICON_URL16);
		URL trayIconURL32 = getClass().getClassLoader().getResource(TRAY_ICON_URL32);
		URL trayIconURL96 = getClass().getClassLoader().getResource(TRAY_ICON_URL96);
		URL trayIconURL194 = getClass().getClassLoader().getResource(TRAY_ICON_URL194);
		Vector<Image> trayIcons = new Vector<Image>();
		if(trayIconURL16 != null)
			trayIcons.addElement((new ImageIcon(trayIconURL16, "tray icon")).getImage());
		if(trayIconURL32 != null)
			trayIcons.addElement((new ImageIcon(trayIconURL32, "tray icon")).getImage());
		if(trayIconURL96 != null)
			trayIcons.addElement((new ImageIcon(trayIconURL96, "tray icon")).getImage());
		if(trayIconURL194 != null)
			trayIcons.addElement((new ImageIcon(trayIconURL194, "tray icon")).getImage());
		if(trayIcons.size() > 0)
			this.setIconImages(trayIcons);

		/****************************************
		 * Add startup type chooser
		 ****************************************/
		GridBagLayout startTypeLayout = new GridBagLayout();
		JPanel startTypePanel = new JPanel(startTypeLayout);
		startTypePanel.setOpaque(false);
		startTypePanel.setBorder(new TitledBorder("Startup Type"));

		ButtonGroup startTypeButtonGroup = new ButtonGroup();
		startClientServerButton.setOpaque(false);
		startTypeButtonGroup.add(startClientServerButton);
		startClientButton.setOpaque(false);
		startTypeButtonGroup.add(startClientButton);
		startServerButton.setOpaque(false);
		startTypeButtonGroup.add(startServerButton);
		boolean serverExists = new Server().exists();
		boolean clientExists = new Client().exists();

		addConfElement(startClientServerButton, startTypeLayout, newLineConstr, startTypePanel);
		addConfElement(startClientButton, startTypeLayout, newLineConstr, startTypePanel);
		addConfElement(startServerButton, startTypeLayout, newLineConstr, startTypePanel);

		addConfElement(startTypePanel, layout, newLineConstr, contentPanel);

		class StartTypeChangedListener implements ActionListener
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// Server URL panel:
				if(startClientButton.isSelected())
				{
					serverURLPanel.setVisible(true);
					resetMicroManagerConfiguration.setVisible(false);
				}
				else
				{
					serverURLPanel.setVisible(false);
					resetMicroManagerConfiguration.setVisible(true);
				}

				if(startServerButton.isSelected() || startClientButton.isSelected())
				{
					serverPortPanel.setVisible(true);
					passwordPanel.setVisible(true);
				}
				else
				{
					serverPortPanel.setVisible(false);
					passwordPanel.setVisible(false);
				}

				if(startServerButton.isSelected() || startClientServerButton.isSelected())
					configFilePanel.setVisible(true);
				else
					configFilePanel.setVisible(false);

				pack();
			}
		}
		startClientServerButton.addActionListener(new StartTypeChangedListener());
		startClientButton.addActionListener(new StartTypeChangedListener());
		startServerButton.addActionListener(new StartTypeChangedListener());

		/***********************************
		 * Generate UI elements
		 ***********************************/
		try
		{
			MaskFormatter mfMyFormatter = new MaskFormatter("###.###.###.###");
			mfMyFormatter.setPlaceholderCharacter('0');

			urlField = new JFormattedTextField(mfMyFormatter);
			urlField.setValue(lastURL);
		}
		catch(@SuppressWarnings("unused") ParseException e)
		{
			urlField = new JFormattedTextField(lastURL);
		}
		portField = new JFormattedTextField(lastPort);

		// Panel to choose config file
		JPanel configFileChooserPanel = new JPanel(new BorderLayout(5, 0));
		configFileChooserPanel.setOpaque(false);
		configFileChooserPanel.add(configFileField, BorderLayout.CENTER);
		JButton openFolderChooser = new JButton("Select");
		openFolderChooser.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				chooseFile();
			}
		});
		configFileChooserPanel.add(openFolderChooser, BorderLayout.EAST);

		/****************************************
		 * Add UI elements which can be switched on and off
		 ****************************************/
		GridBagLayout startOptionsLayout = new GridBagLayout();
		JPanel startOptionsPanel = new JPanel(startOptionsLayout);
		startOptionsPanel.setOpaque(false);
		startOptionsPanel.setBorder(new TitledBorder("Startup Options"));

		GridBagLayout serverURLLayout = new GridBagLayout();
		serverURLPanel = new JPanel(serverURLLayout);
		serverURLPanel.setOpaque(false);
		addConfElement(new JLabel("Server URL:"), serverURLLayout, newLineConstr, serverURLPanel);
		addConfElement(urlField, serverURLLayout, newLineConstr, serverURLPanel);
		addConfElement(serverURLPanel, startOptionsLayout, newLineConstr, startOptionsPanel);

		GridBagLayout serverPortLayout = new GridBagLayout();
		serverPortPanel = new JPanel(serverPortLayout);
		serverPortPanel.setOpaque(false);
		addConfElement(new JLabel("Server port:"), serverPortLayout, newLineConstr, serverPortPanel);
		addConfElement(portField, serverPortLayout, newLineConstr, serverPortPanel);
		addConfElement(serverPortPanel, startOptionsLayout, newLineConstr, startOptionsPanel);

		GridBagLayout passwordLayout = new GridBagLayout();
		passwordField = new JPasswordField();
		passwordPanel = new JPanel(passwordLayout);
		passwordPanel.setOpaque(false);
		addConfElement(new JLabel("Password:"), passwordLayout, newLineConstr, passwordPanel);
		addConfElement(passwordField, passwordLayout, newLineConstr, passwordPanel);
		addConfElement(passwordPanel, startOptionsLayout, newLineConstr, startOptionsPanel);

		GridBagLayout configFileLayout = new GridBagLayout();
		configFilePanel = new JPanel(configFileLayout);
		configFilePanel.setOpaque(false);
		addConfElement(new JLabel("Microscope configuration file:"), configFileLayout, newLineConstr, configFilePanel);
		addConfElement(configFileChooserPanel, configFileLayout, newLineConstr, configFilePanel);
		addConfElement(configFilePanel, startOptionsLayout, newLineConstr, startOptionsPanel);

		resetMicroManagerConfiguration.setOpaque(false);
		resetMicroManagerConfiguration.setSelected(false);
		addConfElement(resetMicroManagerConfiguration, startOptionsLayout, newLineConstr, startOptionsPanel);

		addConfElement(startOptionsPanel, layout, newLineConstr, contentPanel);

		/******************************************
		 * Add Buttons
		 ******************************************/
		getRootPane().setDefaultButton(startButton);
		startButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(!checkSettings())
					return;
				startProgram();
				dispose();
			}
		});
		JButton cancelButton = new JButton("Exit");
		cancelButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				System.exit(0);
			}
		});

		JPanel emptyPanel = new JPanel();
		emptyPanel.setOpaque(false);
		addConfElement(emptyPanel, layout, bottomConstr, contentPanel);
		
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		buttonPanel.setOpaque(false);
		buttonPanel.add(startButton);
		buttonPanel.add(cancelButton);
		buttonPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
		addConfElement(buttonPanel, layout, newLineConstr, contentPanel);
		
		
		contentPane.add(contentPanel, BorderLayout.CENTER);
		
		if(!serverExists && !clientExists)
		{
			startButton.setEnabled(false);
			startClientServerButton.setEnabled(false);
			startClientButton.setEnabled(false);
			startServerButton.setEnabled(false);

			serverURLPanel.setVisible(false);
			serverPortPanel.setVisible(false);
			passwordPanel.setVisible(false);
			configFilePanel.setVisible(false);
			resetMicroManagerConfiguration.setVisible(false);
		}
		else if(!serverExists)
		{
			startClientServerButton.setEnabled(false);
			startServerButton.setEnabled(false);
			lastStartupSelection = StartUpType.CLIENT;
		}
		else if(!clientExists)
		{
			startClientServerButton.setEnabled(false);
			startClientButton.setEnabled(false);
			lastStartupSelection = StartUpType.SERVER;
		}

		// Set startup setting to last choice
		switch(lastStartupSelection)
		{
			case CLIENT_SERVER:
				startClientServerButton.doClick();
				break;
			case CLIENT:
				startClientButton.doClick();
				break;
			case SERVER:
				startServerButton.doClick();
				break;
			default:
				startClientServerButton.doClick();
				break;
		}
		
		contentPane.setBorder(new LineBorder(Color.BLACK, 1));
		setContentPane(contentPane);
		
		// Get the size of the default screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		pack();
		setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);
	}

	private static void addConfElement(Component component, GridBagLayout layout, GridBagConstraints constr, Container panel)
	{
		layout.setConstraints(component, constr);
		panel.add(component);
	}

	private boolean checkSettings()
	{
		if(configFileField.getText() != null && (startServerButton.isSelected() || startClientServerButton.isSelected()))
		{
			if(!(new File(configFileField.getText())).exists())
			{
				JOptionPane.showMessageDialog(this, "The configuration file\n" + configFileField.getText() + "\ncould not be found.\nPlease enter a valid location.", "Configuration File Not Found", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}

	private void startProgram()
	{
		// Get settings
		StartUpType startUpType;
		if(startClientButton.isSelected())
			startUpType = StartUpType.CLIENT;
		else if(startServerButton.isSelected())
			startUpType = StartUpType.SERVER;
		else
			startUpType = StartUpType.CLIENT_SERVER;

		String serverUrl = (String)urlField.getValue();
		int serverPort = ((Number)portField.getValue()).intValue();
		String configFile = configFileField.getText();
		String password = new String(passwordField.getPassword());
		boolean shouldReset = resetMicroManagerConfiguration.isSelected();

		// Save settings
		ConfigurationSettings configuration = ConfigurationSettings.loadProperties();
		configFileField.saveToConfiguration(configuration);
		configuration.setProperty(ConfigurationSettings.SETTINGS_SERVER_URL, serverUrl);
		configuration.setProperty(ConfigurationSettings.SETTINGS_SERVER_PORT, ((Integer)serverPort).toString());
		configuration.setProperty(ConfigurationSettings.SETTINGS_STARTUP_TYPE, startUpType.name());
		configuration.saveProperties();

		// Start program
		Runnable starter;
		switch(startUpType)
		{
			case CLIENT_SERVER:
				starter = new ClientServerStarter(configFile, shouldReset);
				break;
			case SERVER:
				starter = new ServerStarter(configFile, serverPort, password, shouldReset);
				break;
			case CLIENT:
				starter = new ClientStarter(serverUrl, serverPort, password);
				break;
			default:
				return;
		}
		Thread thread = new Thread(starter);
		thread.setDaemon(false);
		thread.start();
	}

	private class ServerStarter implements Runnable
	{
		private final String configFile;
		private final int port;
		private final String password;
		private final boolean shouldReset;
		ServerStarter(String configFile, int port, String password, boolean shouldReset)
		{
			this.configFile = configFile;
			this.port = port;
			this.password = password;
			this.shouldReset = shouldReset;
		}
		@SuppressWarnings("resource")
		@Override
		public void run()
		{
			SplashScreen splashScreen = new SplashScreen("Loading libraries...");
			try
			{
				splashScreen.setVisible(true);
	
				// Load necessary JAR files.
				ClassLoader oldClassLoader = Starter.class.getClassLoader();
				HashSet<URL> necessaryJARs;
				try
				{
					necessaryJARs = server.getNecessaryJARs();
				}
				catch(MalformedURLException e1)
				{
					ErrorConsumer.consumeException("JAR file names malformed.", e1);
					System.exit(1);
					return;
				}
				// TODO: When to close the class laoder?
				final ClassLoader classLoader = new URLClassLoader(necessaryJARs.toArray(new URL[necessaryJARs.size()]), oldClassLoader);
	
				// Set class loader as default for all relevant threads.
				try {
					SwingUtilities.invokeAndWait(new Runnable()
							{
								@Override
								public void run() {
									Thread.currentThread().setContextClassLoader(classLoader);
								}
							});
				} catch (InvocationTargetException | InterruptedException e4) {
					ErrorConsumer.consumeException("Could not set UI class loader. Shutting down.", e4);
					System.exit(1);
				}
				Thread.currentThread().setContextClassLoader(classLoader);
				
				// Extend java classpath
				Properties prop = System.getProperties();
		        String javaclasspath = prop.getProperty("java.class.path", "");
		        for(URL url : necessaryJARs)
		        {
		        	try
					{
						javaclasspath += ";" + new File(url.toURI()).getAbsolutePath();
					}
					catch(@SuppressWarnings("unused") URISyntaxException e)
					{
						// do nothing
					}
		        }
		        prop.setProperty("java.class.path", javaclasspath);
				
				splashScreen.setProgress(20, "Connecting to microscope...");
				try
				{
					server.connectToServer(classLoader, configFile, port, password, shouldReset);
				}
				catch(ConnectionFailedException e)
				{
					ErrorConsumer.consumeException("Could not start up server. Shutting down.", e);
					System.exit(1);
				}
	
				splashScreen.setProgress(60, "Initializing server...");
				try
				{
					server.runServer(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e)
						{
							System.exit(0);
						}
					});
				}
				catch(Exception e)
				{
					ErrorConsumer.consumeException("Could not start up server. Shutting down.", e);
					System.exit(1);
				}
	
				splashScreen.setProgress(100, "Finished...");
				try
				{
					Thread.sleep(1000);
				}
				catch(@SuppressWarnings("unused") InterruptedException e2)
				{
					// Do noting...
				}
			}
			finally
			{
				splashScreen.dispose();
			}
		}
	}

	private class ClientStarter implements Runnable
	{
		private final String url;
		private final int port;
		private final String password;
		ClientStarter(String url, int port, String password)
		{
			this.url = url;
			this.port = port;
			this.password = password;
		}
		@SuppressWarnings("resource")
		@Override
		public void run()
		{
			SplashScreen splashScreen = new SplashScreen("Loading libraries...");
			try
			{
				splashScreen.setVisible(true);
	
				// Load necessary JAR files.
				ClassLoader oldClassLoader = Starter.class.getClassLoader();
				HashSet<URL> necessaryJARs;
				try
				{
					necessaryJARs = client.getNecessaryJARs();
				}
				catch(MalformedURLException e1)
				{
					ErrorConsumer.consumeException("JAR file names malformed.", e1);
					System.exit(1);
					return;
				}
				//TODO: when to close?
				final ClassLoader classLoader = new URLClassLoader(necessaryJARs.toArray(new URL[necessaryJARs.size()]), oldClassLoader);
	
				// Set class loader as default for all relevant threads.
				try {
					SwingUtilities.invokeAndWait(new Runnable()
							{
								@Override
								public void run() {
									Thread.currentThread().setContextClassLoader(classLoader);
								}
							});
				} catch (InvocationTargetException | InterruptedException e4) {
					ErrorConsumer.consumeException("Could not set UI class loader. Shutting down.", e4);
					System.exit(1);
				}
				Thread.currentThread().setContextClassLoader(classLoader);
				
				// Extend java classpath
				Properties prop = System.getProperties();
		        String javaclasspath = prop.getProperty("java.class.path", "");
		        for(URL url : necessaryJARs)
		        {
		        	try
					{
						javaclasspath += ";" + new File(url.toURI()).getAbsolutePath();
					}
					catch(@SuppressWarnings("unused") URISyntaxException e)
					{
						// do nothing
					}
		        }
		        prop.setProperty("java.class.path", javaclasspath);
				
				splashScreen.setProgress(30, "Initializing UI...");
				try
				{
					client.connectToClient(classLoader);
				}
				catch(ConnectionFailedException e)
				{
					ErrorConsumer.consumeException("Could not start up client. Shutting down.", e);
					System.exit(1);
				}
	
				splashScreen.setProgress(50, "Connecting to server...");
				try
				{
					client.connectToServer(url, port, password);
				}
				catch(ConnectionFailedException e)
				{
					ErrorConsumer.consumeException("Could not connect server to client. Shutting down.", e);
					System.exit(1);
				}
	
				splashScreen.setProgress(80, "Building UI...");
				try
				{
					client.runClient(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e)
						{
							System.exit(0);
						}
					});
				}
				catch(ConnectionFailedException e)
				{
					ErrorConsumer.consumeException("Could not run client. Shutting down.", e);
					System.exit(1);
				}
	
				splashScreen.setProgress(100, "Finished...");
				try
				{
					Thread.sleep(1000);
				}
				catch(@SuppressWarnings("unused") InterruptedException e2)
				{
					// Do noting...
				}
			}
			finally
			{
				splashScreen.dispose();
			}
		}
	}

	private class ClientServerStarter implements Runnable
	{
		private final String configFile;
		private final boolean shouldReset;
		ClientServerStarter(String configFile, boolean shouldReset)
		{
			this.configFile = configFile;
			this.shouldReset = shouldReset;
		}
		@SuppressWarnings("resource")
		@Override
		public void run()
		{
			SplashScreen splashScreen = new SplashScreen("Loading libraries...");
			try
			{
				splashScreen.setVisible(true);
				// Load necessary JAR files.
				ClassLoader oldclassLoader = Starter.class.getClassLoader();
				HashSet<URL> necessaryJARs;
				try
				{
					necessaryJARs = client.getNecessaryJARs();
	
					necessaryJARs.addAll(server.getNecessaryJARs());
				}
				catch(MalformedURLException e1)
				{
					ErrorConsumer.consumeException("JAR file names malformed.", e1);
					System.exit(1);
					return;
				}
				// TODO: When to close?
				final ClassLoader classLoader = new URLClassLoader(necessaryJARs.toArray(new URL[necessaryJARs.size()]), oldclassLoader);
				// Set class loader as default for all relevant threads.
				try {
					SwingUtilities.invokeAndWait(new Runnable()
							{
								@Override
								public void run() {
									Thread.currentThread().setContextClassLoader(classLoader);
								}
							});
				} catch (InvocationTargetException | InterruptedException e4) {
					ErrorConsumer.consumeException("Could not set UI class loader. Shutting down.", e4);
					System.exit(1);
				}
				Thread.currentThread().setContextClassLoader(classLoader);
				
				// Extend java classpath
				Properties prop = System.getProperties();
		        String javaclasspath = prop.getProperty("java.class.path", "");
		        for(URL url : necessaryJARs)
		        {
		        	try
					{
						javaclasspath += ";" + new File(url.toURI()).getAbsolutePath();
					}
					catch(@SuppressWarnings("unused") URISyntaxException e)
					{
						// do nothing
					}
		        }
		        prop.setProperty("java.class.path", javaclasspath);
				
				// Start server
				splashScreen.setProgress(20, "Connecting to microscope...");
				try
				{
					server.connectToServer(classLoader, configFile, shouldReset);
				}
				catch(ConnectionFailedException e)
				{
					ErrorConsumer.consumeException("Could not start up server. Shutting down.", e);
					System.exit(1);
				}
				try
				{
					server.runServer(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e)
						{
							System.exit(0);
						}
					});
				}
				catch(Exception e3)
				{
					ErrorConsumer.consumeException("Could not run server. Shutting down.", e3);
					System.exit(1);
				}
	
				// Start client.
				splashScreen.setProgress(40, "Initializing UI...");
				try
				{
					client.connectToClient(classLoader);
				}
				catch(ConnectionFailedException e)
				{
					ErrorConsumer.consumeException("Could not start up the YouScope client.", e);
					System.exit(1);
				}
	
				splashScreen.setProgress(60, "Connecting UI to microscope...");
				try
				{
					client.connectToServer(server.getServerInterfaceClass(), server.getServer());
				}
				catch(ConnectionFailedException e)
				{
					ErrorConsumer.consumeException("Could not connect server to client. Shutting down.", e);
					System.exit(1);
				}
	
				splashScreen.setProgress(80, "Building UI...");
				try
				{
					client.runClient(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e)
						{
							try
							{
								server.quitServer();
							}
							catch(ConnectionFailedException e1)
							{
								ErrorConsumer.consumeException("Could not quit server regularly. Shutting down.", e1);
								System.exit(1);
							}
						}
					});
				}
				catch(ConnectionFailedException e)
				{
					ErrorConsumer.consumeException("Could not run client. Shutting down.", e);
					System.exit(1);
				}
	
				splashScreen.setProgress(100, "Finished...");
				try
				{
					Thread.sleep(1000);
				}
				catch(@SuppressWarnings("unused") InterruptedException e2)
				{
					// Do noting...
				}
			}
			finally
			{
				splashScreen.dispose();
			}
		}
	}

	/**
	 * Main method.
	 * 
	 * @param args Currently not used.
	 */
	public static void main(String[] args)
	{
		System.out.println(System.getProperty("java.class.path"));

		// Set system look and feel.
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(@SuppressWarnings("unused") Exception e)
		{
			// Don't care, take standard L&F...
		}

		// Startup program.
		getMainProgram().setVisible(true);
	}

	private void chooseFile()
	{
		JFileChooser fileChooser = new JFileChooser(configFileField.getText());
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Microscope Configuration File (.cfg)", "cfg"));
		int returnVal = fileChooser.showDialog(Starter.this, "Open");

		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			File file = fileChooser.getSelectedFile();
			configFileField.insertItemAt(file, 0);
			configFileField.setSelectedItem(file);
		}
	}

	private class ConfigFileChooserComboBox extends JComboBox<Object>
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -4785284511036306325L;
		private final static String	SEPARATOR			= "SEPARATOR";
		private final static String	EMPTY_CONFIGURATION	= "&lt;Empty Configuration&gt;";
		private final static String	DEFINE_LOCATION		= "&lt;Define location&gt;";

		private final String[]		settingsList		= {ConfigurationSettings.SETTINGS_CONFIG_FILE_LAST_0, ConfigurationSettings.SETTINGS_CONFIG_FILE_LAST_1, ConfigurationSettings.SETTINGS_CONFIG_FILE_LAST_2, ConfigurationSettings.SETTINGS_CONFIG_FILE_LAST_3};

		ConfigFileChooserComboBox(String lastConfigFile, ConfigurationSettings configuration)
		{
			setEditable(false);
			boolean atLeastOne = false;
			for(String setting : settingsList)
			{
				String conf = configuration.getProperty(setting, null);
				if(conf != null) 
				{
					File file = new File(conf).getAbsoluteFile();
					if(file.exists())
					{
						addItem(file);
						atLeastOne = true;
					}
				}
			}
			if(!atLeastOne)
			{
				// try to add demo file.
				File file = new File("YSConfig_demo.cfg").getAbsoluteFile();
				if(file.exists())
					addItem(file);
			}
			addItem(SEPARATOR);
			addItem(DEFINE_LOCATION);
			addItem(EMPTY_CONFIGURATION);

			if(lastConfigFile != null)
				setSelectedItem(new File(lastConfigFile));
			else
			{
				if(getItemCount() > 3)
					setSelectedIndex(0);
				else
					setSelectedIndex(2);
			}

			addActionListener(new ConfigFileChooserComboBoxListener());
			setRenderer(new ConfigFileChooserComboBoxRenderer());

		}

		public void saveToConfiguration(ConfigurationSettings configuration)
		{
			String value = getText();
			if(value == null)
				return;
			// Remove item from list if its already in.
			for(int i = 0; i < settingsList.length; i++)
			{
				if(value.equals(configuration.getProperty(settingsList[i], null)))
				{
					configuration.deleteProperty(settingsList[i]);
				}
			}
			// Insert item in list at first position, move everything downwards.
			for(int i = 0; i < settingsList.length; i++)
			{
				String tempItem = configuration.getProperty(settingsList[i], null);
				configuration.setProperty(settingsList[i], value);
				if(tempItem == null)
					break;
				value = tempItem;
			}
		}

		public String getText()
		{
			String value = getSelectedItem().toString();
			if(value.equals(SEPARATOR) || value.equals(EMPTY_CONFIGURATION))
				return null;
			return value;
		}

		class ConfigFileChooserComboBoxRenderer extends JLabel implements ListCellRenderer<Object>
		{
			/**
			 * Serial Version UID.
			 */
			private static final long	serialVersionUID	= 4144921105492912843L;
			private final JSeparator					separator;
			private final String foregroundUnselected;
			private final String foregroundSelected;
			private final String foregroundSubUnselected;
			private final String foregroundSubSelected;
			public ConfigFileChooserComboBoxRenderer()
			{
				setBorder(new EmptyBorder(1, 1, 1, 1));
				separator = new JSeparator(JSeparator.HORIZONTAL);
				JList<?> list = new JList<Object>();
				foregroundUnselected = Integer.toHexString(list.getForeground().getRGB()).substring(2);
				foregroundSelected = Integer.toHexString(list.getSelectionForeground().getRGB()).substring(2);
				foregroundSubUnselected = Integer.toHexString(list.getForeground().brighter().brighter().getRGB()).substring(2);
				foregroundSubSelected = Integer.toHexString(list.getSelectionForeground().brighter().brighter().getRGB()).substring(2);
				setBackground(list.getBackground());
			}
			@Override
			public Dimension getPreferredSize() 
			{
				Dimension dim = super.getPreferredSize();
				dim.width = 220;
				return dim;
			}
			@Override
			public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				String pNormal;
				String pSub;
				int width = 200;
				if(index < 0)
				{
					setOpaque(false);
					pNormal = "<p style=\"width:"+width+"px;word-wrap:break-word;padding-left: 8px;text-indent:-6px;color:#"+foregroundUnselected+"\">";
					pSub = "<p style=\"width:"+width+"px;word-wrap:break-word;padding-left: 8px;color:#"+foregroundSubUnselected+"\">";
				}
				else
				{
					setOpaque(true);
					if(isSelected)
					{
						pNormal = "<p style=\"width:"+width+"px;word-wrap:break-word;padding-left: 8px;text-indent:-6px;color:#"+foregroundSelected+"\">";
						pSub = "<p style=\"width:"+width+"px;word-wrap:break-word;padding-left: 8px;color:#"+foregroundSubSelected+"\">";
					}
					else
					{
						pNormal = "<p style=\"width:"+width+"px;word-wrap:break-word;padding-left: 8px;text-indent:-6px;color:#"+foregroundUnselected+"\">";
						pSub = "<p style=\"width:"+width+"px;word-wrap:break-word;padding-left: 8px;color:#"+foregroundSubUnselected+"\">";
					}
				}
				if(value == DEFINE_LOCATION)
				{
					setText("<html>"+pNormal+DEFINE_LOCATION+"</p></html>");
				}
				else if(value == SEPARATOR)
				{
					return separator;
				}
				else if(value == EMPTY_CONFIGURATION)
				{
					setText("<html>"+pNormal+EMPTY_CONFIGURATION+"</p></html>");
				}
				else if(value instanceof File)
				{
					File file = (File)value;
					String text = "<html>"
							+ pNormal
							+ addBreaks(file.getName())
							+ "</p>"
							+ pSub
							+ addBreaks(file.getParent())
							+ "</p></html>";
					setText(text);
				}
				else if(value != null)
				{
					setText("<html>"+pNormal+value.toString()+"</p></html>");
				}
				else
				{
					setText("<html>"+pNormal+"unknown</p></html>");
				}

				if(isSelected)
				{
					setBackground(list.getSelectionBackground());
				}
				else
				{
					setBackground(list.getBackground());
				}
				setFont(list.getFont());
				return this;
			}
			
			public String addBreaks(String text)
			{
				String insert ="<wbr />";
				int period = 5;
				StringBuilder builder = new StringBuilder(
				         text.length() + insert.length() * (text.length()/period)+1);
				int index = 0;
			    String prefix = "";
			    while (index < text.length())
			    {
			        builder.append(prefix);
			        prefix = insert;
			        builder.append(text.substring(index, 
			            Math.min(index + period, text.length())));
			        index += period;
			    }
				return builder.toString();
			}
		}

		private class ConfigFileChooserComboBoxListener implements ActionListener
		{
			private Object	currentItem;

			ConfigFileChooserComboBoxListener()
			{
				currentItem = getSelectedItem();
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				Object item = ConfigFileChooserComboBox.this.getSelectedItem();
				if(item == null)
					return;
				if(item == DEFINE_LOCATION)
				{
					setSelectedItem("");
					chooseFile();
				}
				else if(item == SEPARATOR)
				{
					setSelectedItem(currentItem);
					return;
				}
				else if(item == EMPTY_CONFIGURATION)
				{
					currentItem = getSelectedItem();
				}
				else
				{
					currentItem = getSelectedItem();
				}
			}
		}
	}
}
