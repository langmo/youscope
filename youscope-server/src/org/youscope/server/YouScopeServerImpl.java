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
package org.youscope.server;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.AccessControlException;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.youscope.addon.microscopeaccess.MicroscopeConnectionException;
import org.youscope.addon.microscopeaccess.MicroscopeInternal;
import org.youscope.common.MessageListener;
import org.youscope.common.YouScopeVersion;
import org.youscope.common.callback.CallbackProvider;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.MeasurementProcessingListener;
import org.youscope.serverinterfaces.ComponentProvider;
import org.youscope.serverinterfaces.MeasurementProvider;
import org.youscope.serverinterfaces.YouScopeLogin;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.serverinterfaces.YouScopeServerProperties;

/**
 * Server which controls the microscope and gets measurement jobs from (visual) clients.
 * 
 * @author Moritz Lang
 */
public class YouScopeServerImpl implements YouScopeServer
{
	/**
	 * The standard port for this server.
	 */
	public static final int					REGISTRY_PORT							= 1237;

	private static final String				PROPERTY_MICROSCOPE_DRIVER_FOLDER		= "YouScope.server.microscopeDriverBaseFolder";

	private static final String				PROPERTY_MICROSCOPE_CONNECTION_TYPE		= "YouScope.server.microscopeConnectionType";

	private static final String				PROPERTY_FORCE_CONNECTION_CONFIGURATION	= "YouScope.server.forceConnectionConfiguration";

	private static final String				PROPERTY_SERVER_PORT					= "YouScope.server.lastPort";

	private static final String CONNECTION_TYPE_DEFAULT = "YouScope.StandAlone";
	
	/**
	 * The name under which the server is exported to the registry.
	 */
	private static final String				REGISTRY_NAME							= "YouScope";

	private static volatile YouScopeServer	serverObject							= null;

	private TrayIcon						trayIcon								= null;

	/**
	 * URL to the policy file.
	 */
	private static final String				POLICY_FILE								= "org/youscope/server/YouScope.policy";

	// These objects provide the main functionality of the microscope.
	private MicroscopeInternal				microscope								= null;

	private MeasurementManager				measurementManager						= null;

	private MeasurementProvider				measurementFactory						= null;

	private ConfigFileManager				configFileManager						= null;

	private ChannelManagerImpl				channelManager							= null;

	private PixelSizeManagerImpl			pixelSizeManager						= null;

	/**
	 * The only copy of this object
	 */
	private static YouScopeServerImpl		mainProgram								= null;

	/**
	 * Access ID to the microscope used for initialization, un-initialization,and similar.
	 */
	private static final int				MAIN_PROGRAM_ACCESS_ID					= Integer.MAX_VALUE;

	private YouScopeServerImpl()
	{
		// Initialization is done elsewhere.
		ImageIO.scanForPlugins();
	}

	/**
	 * Adds the server to the registry such that it can be accessed by other processes or over the
	 * network. Queries for the port.
	 */
	public void startServer()
	{
		// Get last used port file
		int port = Integer.parseInt(ConfigurationSettings.loadProperties().getProperty(PROPERTY_SERVER_PORT, Integer.toString(REGISTRY_PORT)));

		ServerPortChooser portChooser = new ServerPortChooser(port, null);
		portChooser.waitForPort();
		String password = portChooser.getPassword();
		port = portChooser.getPort();

		// Start and run program.
		startServer(port, password);
	}

	/**
	 * Adds the server to the registry such that it can be accessed by other processes or over the
	 * network.
	 * 
	 * @param port Port at which the server should be available.
	 * @param password The password for this server.
	 */
	public void startServer(int port, String password)
	{
		ServerSystem.out.println("Starting up server...");

		// Set policy
		URL policyURL = getClass().getClassLoader().getResource(POLICY_FILE);
		if(policyURL == null)
		{
			quitDueToFatalError("Could not find policy file (" + POLICY_FILE + ").", null);
		}
		System.setProperty("java.security.policy", policyURL.toString());
		System.setProperty("java.rmi.server.randomIDs", "true");

		addSystemTray();

		if(System.getSecurityManager() == null)
		{
			System.setSecurityManager(new SecurityManager());
		}

		// Export server to registry
		ServerSystem.out.println("Exporting server to the registry...");
		YouScopeLogin loginObject;
		while(true)
		{
			try
			{
				if(password == null || password.length() < 6)
					throw new Exception("Password must be at least 6 characters long.");
				if(port < 1 || port > 65535)
					throw new Exception("Port must be a number between 1 and 65535.");
				loginObject = new MicroscopeLoginImpl(password);
				Registry registry;
				try
				{
					registry = LocateRegistry.createRegistry(port);
					ServerSystem.out.println("Registry created on port " + Integer.toString(port) + ".");
				}
				catch(@SuppressWarnings("unused") Exception e)
				{
					registry = LocateRegistry.getRegistry(port);
					ServerSystem.out.println("Existing registry loaded on port " + Integer.toString(port) + ".");
				}
				registry.rebind(REGISTRY_NAME, loginObject);

				ConfigurationSettings configuration = ConfigurationSettings.loadProperties();
				configuration.setProperty(PROPERTY_SERVER_PORT, Integer.toString(port));
				configuration.saveProperties();
				break;
			}
			catch(Exception e)
			{
				// Construct error message
				String lastErrorMessage = "Could not startup server on port " + Integer.toString(port) + ".\nDetailed error desciption:";
				Throwable error = e;
				for(; error != null; error = error.getCause())
				{
					lastErrorMessage += "\n" + error.getMessage();
				}

				// Ask for port to use.
				ServerPortChooser portChooser = new ServerPortChooser(port, lastErrorMessage);
				portChooser.waitForPort();
				password = portChooser.getPassword();
				port = portChooser.getPort();
			}
		}
	}

	/**
	 * If true, sets the YouScope server to ask for the microscope connection the next time. If
	 * false, the YouScope tries to connect to the microscope using the last settings. However, if
	 * this does not work, the user is queried, too.
	 * 
	 * @param forceConfiguration True if before the next time a microscope connection is
	 *            established, the user should be asked for the connection type.
	 */
	public void setConfigureMicroscopeConnection(boolean forceConfiguration)
	{
		ConfigurationSettings configuration = ConfigurationSettings.loadProperties();
		configuration.setProperty(PROPERTY_FORCE_CONNECTION_CONFIGURATION, true);
		configuration.saveProperties();
	}

	private MicroscopeInternal tryMicroscopeConnection(String connectionType, String driverFolder) throws MicroscopeConnectionException
	{
		// Try to connect to the drivers.
		MicroscopeInternal microscope = MicroscopeAccess.getMicroscope(connectionType, driverFolder);

		// Connection successful, save settings
		ServerSystem.out.println("Successfully connected to microscope using connection type " + connectionType + ".");
		ConfigurationSettings configuration = ConfigurationSettings.loadProperties();
		configuration.setProperty(PROPERTY_MICROSCOPE_DRIVER_FOLDER, driverFolder);
		configuration.setProperty(PROPERTY_MICROSCOPE_CONNECTION_TYPE, connectionType);
		configuration.saveProperties();

		// Initialize microscope
		microscope.addMessageListener(new MessageListener()
		{
			@Override
			public void sendMessage(String message) throws RemoteException {
				ServerSystem.out.println(message);
			}

			@Override
			public void sendErrorMessage(String message, Throwable error) throws RemoteException {
				ServerSystem.err.println(message, error);
			}
		});
		return microscope;
	}

	/**
	 * Establishes the direction to the microscope. If a new
	 * 
	 * @return
	 * @throws MicroscopeException
	 */
	private MicroscopeInternal connectToMicroscope() throws MicroscopeException
	{
		ConfigurationSettings configuration = ConfigurationSettings.loadProperties();
		String driverFolder = configuration.getProperty(PROPERTY_MICROSCOPE_DRIVER_FOLDER, null);
		String connectionType = configuration.getProperty(PROPERTY_MICROSCOPE_CONNECTION_TYPE, "==UNSET==");
		boolean forceConfiguration = configuration.getProperty(PROPERTY_FORCE_CONNECTION_CONFIGURATION, false);
		configuration.setProperty(PROPERTY_FORCE_CONNECTION_CONFIGURATION, false);
		configuration.saveProperties();
		
		Exception lastException = null;
		while(true)
		{
			if(connectionType == null || forceConfiguration || lastException != null)
			{
				MicroscopeConnectionTypeChooser connectionTypeChooser = new MicroscopeConnectionTypeChooser(driverFolder, connectionType, lastException);
				connectionTypeChooser.waitForUserInput();
				driverFolder = connectionTypeChooser.getDriverPath();
				connectionType = connectionTypeChooser.getConnectionType();
				configuration.setProperty(PROPERTY_MICROSCOPE_CONNECTION_TYPE, connectionType);
				configuration.setProperty(PROPERTY_MICROSCOPE_DRIVER_FOLDER, driverFolder);
				configuration.saveProperties();
			}
			if(connectionType!=null && connectionType.equals("==UNSET=="))
			{
				// try win32 and win64 default types
				try 
				{
					MicroscopeInternal microscope = tryMicroscopeConnection(CONNECTION_TYPE_DEFAULT, driverFolder);
					configuration = ConfigurationSettings.loadProperties();
					configuration.setProperty(PROPERTY_MICROSCOPE_CONNECTION_TYPE, CONNECTION_TYPE_DEFAULT);
					configuration.saveProperties();
					return microscope;
				} 
				catch (@SuppressWarnings("unused") MicroscopeConnectionException e) 
				{
					// probably not win64
					// OK, now, process normally by forcing user decision
					connectionType = null;
				}
			}
			else if(connectionType!=null)
			{
				try {
					return tryMicroscopeConnection(connectionType, driverFolder);
				} catch (MicroscopeConnectionException e) 
				{
					// First: print error in console.
					e.printStackTrace();
					
					// Show error next time
					lastException = e;
				}
			}
		}
	}

	/**
	 * Initializes the program by connecting and initializing the microscope.
	 * 
	 * @throws MicroscopeException
	 */
	public void initializeProgram() throws MicroscopeException
	{
		ConfigFileChooser configFileChooser = new ConfigFileChooser(null, null);
		String lastConfigFile = configFileChooser.waitForConfigFile();

		// Start and run program.
		initializeProgram(lastConfigFile);
	}

	/**
	 * Initializes the program by connecting and initializing the microscope.
	 * 
	 * @param configFile The microscope configuration file with which the server should be started.
	 *            If set to null, no drivers are initialized.
	 * @throws MicroscopeException
	 */
	public synchronized void initializeProgram(String configFile) throws MicroscopeException
	{
		ServerSystem.out.println("Connecting to microscope...");
		try
		{
			setMicroscope(connectToMicroscope());
		}
		catch(MicroscopeException e)
		{
			quitDueToFatalError("Could not connect to microscope.", e);
		}

		// Initialize and run microscope.
		while(true)
		{
			try
			{
				ServerSystem.out.println("Initializing microscope...");
				configFileManager.loadConfiguration(configFile == null ? null : new FileReader(configFile), MAIN_PROGRAM_ACCESS_ID);
				break;
			}
			catch(Exception e)
			{
				ServerSystem.err.println("Error while loading microscope configuration.", e);

				// show UI again, which also displays error. User should choose another file.
				ConfigFileChooser configFileChooser = new ConfigFileChooser(configFile, e);
				configFile = configFileChooser.waitForConfigFile();
			}
		}

		try
		{
			measurementManager = new MeasurementManager(getMicroscope());
			measurementFactory = new MeasurementProviderImpl(measurementManager);
		}
		catch(RemoteException e)
		{
			throw new MicroscopeException("Remote Exception: " + e.getMessage());
		}
	}

	/**
	 * Starts and run the main loop of the server. Has to be invoked for the server to accept client
	 * inputs. Must be invoked not before initialization.
	 * 
	 * @throws MicroscopeException
	 */
	public void startProgram() throws MicroscopeException
	{
		if(microscope == null || measurementFactory == null)
			throw new MicroscopeException("Program not yet initialized");
		// Start execution of measurements in new thread
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				ServerSystem.out.println("Starting measurement processing...");
				measurementManager.runMeasurements();

				// Quit server
				quitProgramInternal();
			}
		}, "YouScope_measurement_runner");
		thread.setDaemon(false);
		thread.start();
	}

	@Override
	public YouScopeServerProperties getProperties() throws RemoteException
	{
		return new ServerConfigurationImpl();
	}

	protected void quitDueToFatalError(String errorDescription, Throwable exception)
	{
		ServerSystem.err.println("Fatal Error occured, quitting program.", exception);
		ServerSystem.out.println("Fatal error occured, shutting down program  (see error log)...");
		JOptionPane.showMessageDialog(null, "A fatal error occured.\nProgram will be shut down.!\n\nError Description:\n" + errorDescription + " (" + (exception != null ? exception.getMessage() : "no further information") + ").", "Fatal error occured", JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}

	private void quitProgramInternal()
	{
		synchronized(this)
		{
			if(serverUninitialized)
				return;
			serverUninitialized = true;
		}
		ServerSystem.out.println("Uninitializing microscope...");
		try
		{
			if(microscope != null)
				microscope.uninitializeMicroscope(MAIN_PROGRAM_ACCESS_ID);
		}
		catch(MicroscopeException e1)
		{
			ServerSystem.err.println("Could not uninitialize microscope due to internal exception.", e1);
		}
		catch(MicroscopeLockedException e)
		{
			ServerSystem.err.println("Could not uninitialize microscope since microscope is locked.", e);
		}
		catch(InterruptedException e)
		{
			ServerSystem.err.println("Uninitialization of microscope was interrupted.", e);
		}
		removeSystemTray();
		// Notify listeners that server stopped working.
		serverFinished();
	}

	@Override
	protected void finalize() throws Throwable
	{
		removeSystemTray();
		super.finalize();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// Set system look and feel.
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(@SuppressWarnings("unused") Exception e)
		{
			// Don't care, take standard L&F...
		}

		// Export server to the registry.
		YouScopeServerImpl program = getMainProgram();
		program.startServer();

		// Connect to the microscope.
		try
		{
			program.initializeProgram();
		}
		catch(MicroscopeException e)
		{
			program.quitDueToFatalError("Could not initialize microscope.", e);
		}

		// Finish java if server finished
		program.addServerFinishListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ServerSystem.out.println("Shutting down program...");
				System.exit(0);
			}
		});

		// run server
		try
		{
			program.startProgram();
		}
		catch(MicroscopeException e)
		{
			program.quitDueToFatalError("Could not start program or error occured during program execution.", e);
		}
	}

	/**
	 * Returns the one and only object of this class.
	 * 
	 * @return Object of this class.
	 */
	public static synchronized YouScopeServerImpl getMainProgram()
	{
		synchronized(YouScopeServerImpl.class)
		{
			if(mainProgram == null)
				mainProgram = new YouScopeServerImpl();
		}

		return mainProgram;
	}

	synchronized void removeSystemTray()
	{
		if(!SystemTray.isSupported() || trayIcon == null)
		{
			return;
		}
		final SystemTray tray = SystemTray.getSystemTray();
		if(tray != null)
			tray.remove(trayIcon);
		trayIcon = null;
	}

	protected boolean addSystemTray()
	{
		// Check the SystemTray is supported
		if(!SystemTray.isSupported())
		{
			ServerSystem.out.println("System tray is not supported.");
			return false;
		}
		final PopupMenu popup = new PopupMenu();

		// Get tray icon image.
		final String TRAY_ICON_URL96 = "org/youscope/server/images/icon-96.png";
		URL trayIconURL96 = getClass().getClassLoader().getResource(TRAY_ICON_URL96);

		if(trayIconURL96 == null)
		{
			ServerSystem.err.println("Could not find system tray icon.", null);
			return false;
		}
		Image trayIconImage = (new ImageIcon(trayIconURL96, "tray icon")).getImage();

		// Set tray icon
		trayIcon = new TrayIcon(trayIconImage);
		trayIcon.setImageAutoSize(true);
		final String versionString = YouScopeVersion.getFullVersion();
		trayIcon.setToolTip("YouScope " + versionString + "\nThe microscope control environment\n\n© Moritz Lang, ETH Zurich, Switzerland");
		final SystemTray tray = SystemTray.getSystemTray();

		// Create a pop-up menu components
		MenuItem aboutItem = new MenuItem("About");
		MenuItem exitItem = new MenuItem("Exit");

		// Add components to pop-up menu
		popup.add(aboutItem);
		popup.addSeparator();
		popup.add(exitItem);

		trayIcon.setPopupMenu(popup);

		trayIcon.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JOptionPane.showMessageDialog(null, "YouScope " + versionString + "\nThe microscope control environment\n\n© Moritz Lang, ETH Zurich, Switzerland");
			}
		});
		aboutItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JOptionPane.showMessageDialog(null, "YouScope " + versionString + "\nThe microscope control environment\n\n© Moritz Lang, ETH Zurich, Switzerland");
			}
		});
		exitItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				quitProgram();
			}
		});
		try
		{
			tray.add(trayIcon);
		}
		catch(AWTException e)
		{
			ServerSystem.err.println("TrayIcon could not be added.", e);
			trayIcon = null;
			return false;
		}
		return true;

	}

	/**
	 * Shut down the program. The microscope is uninitialized.
	 */
	public void quitProgram()
	{
		// Try to shut down. If system is not exiting, wait a maximum of
		// 5 seconds, than
		// stop hard if still running...

		class ExitTimer implements Runnable
		{
			private int	delay;

			ExitTimer(int delay)
			{
				this.delay = delay;
			}

			@Override
			public void run()
			{
				// Wait several seconds...
				try
				{
					Thread.sleep(delay);
				}
				catch(@SuppressWarnings("unused") Exception e1)
				{
					// System is exiting regularly...
					return;
				}

				// Exit
				ServerSystem.err.println("System did not exit regularly after 5s. Making a hard shutdown...", null);
				quitProgramInternal();
			}
		}

		// Stop Timer: If after 5s regular shutdown is not complete,
		// make a hard shut
		// down.
		Thread thread = new Thread(new ExitTimer(5000));
		thread.start();

		try
		{
			if(measurementManager != null)
				measurementManager.stop();
		}
		catch(Exception e1)
		{
			ServerSystem.err.println("Could not send exit signal to the measurement manager.", e1);
		}
	}

	private volatile Vector<ActionListener>	serverFinishListeners	= new Vector<ActionListener>();

	private volatile boolean				serverUninitialized		= false;

	/**
	 * Adds a listener which gets notified if the server finished execution. The listener than e.g.
	 * can call System.exit(0).
	 * 
	 * @param listener The listener which should be added.
	 */
	public void addServerFinishListener(ActionListener listener)
	{
		if(listener != null)
		{
			synchronized(serverFinishListeners)
			{
				serverFinishListeners.add(listener);
			}
		}
	}

	/**
	 * Removes a previously added listener.
	 * 
	 * @param listener The listener to be removed.
	 */
	public void removeServerFinishListener(ActionListener listener)
	{
		if(listener != null)
		{
			synchronized(serverFinishListeners)
			{
				serverFinishListeners.remove(listener);
			}
		}
	}

	private void serverFinished()
	{
		synchronized(serverFinishListeners)
		{
			// Send notification to the listeners that server finished.
			for(ActionListener listener : serverFinishListeners)
			{
				listener.actionPerformed(new ActionEvent(this, 0, "Server execution finished."));
			}
		}
	}

	private class MicroscopeLoginImpl extends UnicastRemoteObject implements YouScopeLogin
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 7964200348163885797L;

		private String				password;

		/**
		 * Constructor
		 * 
		 * @throws RemoteException
		 */
		protected MicroscopeLoginImpl(String password) throws RemoteException
		{
			super();
			this.password = password;
		}

		@Override
		public YouScopeServer login(String password) throws SecurityException, RemoteException
		{
			if((password == null) || !password.equals(this.password))
			{
				throw new AccessControlException("Authentification failed (password incorrect).");
			}
			synchronized(YouScopeServerImpl.this)
			{
				if(serverObject == null)
					serverObject = (YouScopeServer)UnicastRemoteObject.exportObject(YouScopeServerImpl.this, 0);
			}
			return serverObject;
		}
	}

	@Override
	public MeasurementProvider getMeasurementProvider()
	{
		return measurementFactory;
	}

	MeasurementManager getMeasurementManager()
	{
		return measurementManager;
	}

	@Override
	public void addMessageListener(MessageListener listener)
	{
		ServerSystem.addMessageOutListener(listener);
		ServerSystem.addMessageErrListener(listener);
	}

	@Override
	public void removeMessageListener(MessageListener listener)
	{
		ServerSystem.removeMessageOutListener(listener);
		ServerSystem.removeMessageErrListener(listener);
	}

	private void setMicroscope(MicroscopeInternal microscope)
	{
		this.microscope = microscope;
		this.pixelSizeManager = microscope != null ? new PixelSizeManagerImpl(microscope) : null;
		this.channelManager = microscope != null ? new ChannelManagerImpl(microscope) : null;
		this.configFileManager = microscope != null ? new ConfigFileManager(microscope, channelManager, pixelSizeManager) : null;
	}

	@Override
	public Microscope getMicroscope() throws RemoteException
	{
		if(microscope == null)
			throw new IllegalStateException("Micropscope yet not initialized.");
		return new MicroscopeRMI(microscope, configFileManager, channelManager, pixelSizeManager);
	}

	@Override
	public Measurement getCurrentMeasurement() throws RemoteException
	{
		if(measurementManager == null)
			throw new IllegalStateException("Server not initialized yet.");
		return measurementManager.getCurrentMeasurement();
	}

	@Override
	public Measurement[] getMeasurementQueue() throws RemoteException
	{
		if(measurementManager == null)
			throw new IllegalStateException("Server not initialized yet.");
		return measurementManager.getMeasurementQueue();
	}

	@Override
	public void addMeasurementProcessingListener(MeasurementProcessingListener listener)
	{
		if(measurementManager == null)
			throw new IllegalStateException("Server not initialized yet.");
		measurementManager.addMeasurementProcessingListener(listener);
	}

	@Override
	public void removeMeasurementProcessingListener(MeasurementProcessingListener listener)
	{
		if(measurementManager == null)
			throw new IllegalStateException("Server not initialized yet.");
		measurementManager.removeMeasurementProcessingListener(listener);
	}

	@Override
	public void emergencyStop()
	{
		if(microscope == null)
			throw new IllegalStateException("Micropscope yet not initialized.");
		microscope.emergencyStop();
		measurementManager.interruptCurrentMeasurement();
	}

	@Override
	public void resetEmergencyStop()
	{
		if(microscope == null)
			throw new IllegalStateException("Micropscope yet not initialized.");
		microscope.resetEmergencyStop();
		measurementManager.resumeAfterEmergencyStop();
	}

	@Override
	public boolean isEmergencyStopped()
	{
		if(microscope == null)
			throw new IllegalStateException("Micropscope yet not initialized.");
		return microscope.isEmergencyStopped();
	}

	@Override
	public ComponentProvider getComponentProvider(CallbackProvider callbackProvider) throws RemoteException {
		return new ComponentProviderImpl(new ConstructionContextImpl(null, callbackProvider));
	}
}
