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
package org.youscope.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;
import org.youscope.addon.ConfigurationManagement;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentAddonUIListener;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.addon.skin.Skin;
import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.YouScopeVersion;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.util.RMIReader;
import org.youscope.common.util.RMIWriter;
import org.youscope.common.util.TextTools;
import org.youscope.serverinterfaces.YouScopeLogin;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.serverinterfaces.YouScopeServerProperties;
import org.youscope.uielements.AddonMenuItem;
import org.youscope.uielements.ImageLoadingTools;

/**
 * Main frame of GUI to control the execution of measurements on the microscope.
 * 
 * @author Moritz Lang
 */
public class YouScopeClientImpl extends JFrame
{
	/**
	 * Serial version UID.
	 */
	private static final long				serialVersionUID					= 6618831597399735839L;

	/**
	 * The port the server object registers to.
	 */
	public static final int					REGISTRY_PORT						= 1237;

	/**
	 * The name under which the server object registers itself in the registry.
	 */
	public static final String				REGISTRY_NAME						= "YouScope";

	private final static String DEFAULT_SKIN = "YouScope.Skin.System";
	
	/**
	 * The only existing main frame in this program
	 */
	private static YouScopeClientImpl			youScopeClient;

	private static final String PROPERTY_LAST_SERVER_URL = "YouScope.Connection.lastServerUrl";
	private static final String PROPERTY_LAST_SERVER_PORT = "YouScope.Connection.lastServerPort";
	
	/**
	 * The main server object.
	 */
	private YouScopeServer				manager;

	/**
	 * URL of the policy file.
	 */
	private static final String				POLICY_FILE							= "org/youscope/client/youscope-client.policy";

	// Layout components
	private JDesktopPane					desktop								= new JDesktopPane();//ImageDesktopPane();
	private MeasurementControlManager		controlManager 						= new MeasurementControlManager(desktop);

	private LogPanel					logPanel;

	private final JMenuBar						menuBar								= new JMenuBar();

	private final JList<LastMeasurementListElement> lastMeasurementConfigurationsList = new JList<LastMeasurementListElement>();

	private JMenu							windowsMenu;
	
	private JMenu							scriptsMenu;

	private class LastMeasurementListElement
    {
        private final MeasurementConfiguration configuration;
        public LastMeasurementListElement(final MeasurementConfiguration configuration)
        {
            this.configuration = configuration;
        }
        @Override
        public String toString()
        {
            return configuration.getName(); 
        }
    }
	
	private static void initializeSkin()
	{
		// Set skin
		String skinID = (String) PropertyProviderImpl.getInstance().getProperty(StandardProperty.PROPERTY_SKIN);
		if(skinID == null)
			skinID = DEFAULT_SKIN;
		Skin skin = null;
		try
		{
			skin = ClientAddonProviderImpl.getProvider().createSkin(skinID);
		}
		catch(@SuppressWarnings("unused") AddonException e)
		{
			// do nothing.
		}
		if(skin == null)
		{
			// try default skin
			try
			{
				skin = ClientAddonProviderImpl.getProvider().createSkin(DEFAULT_SKIN);
			}
			catch(@SuppressWarnings("unused") AddonException e)
			{
				// do nothing.
			}
		}
		if(skin == null)
		{
			// try any skin
			List<String> skinIDs = ClientAddonProviderImpl.getProvider().getSkinTypeIdentifiers();
			for(String anySkinID : skinIDs)
			{
				try
				{
					skin = ClientAddonProviderImpl.getProvider().createSkin(anySkinID);
					if(skin != null)
					{
						break;
					}
				}
				catch(@SuppressWarnings("unused") AddonException e)
				{
					// do nothing.
				}
			}
		}
		if(skin == null)
		{
			ClientSystem.err.println("No skin installed. YouScope might look weird.");
		}
		else
		{
			try {
				skin.applySkin();
				PropertyProviderImpl.getInstance().setProperty(StandardProperty.PROPERTY_SKIN, skin.getMetadata().getTypeIdentifier());		
			} catch (AddonException e) {
				ClientSystem.err.println("Could not set skin.", e);
			}
				
		}
	}
	
	/**
	 * Constructor.
	 */
	private YouScopeClientImpl()
	{
		super("YouScope "+YouScopeVersion.getProductVersion()+" - The Microscope Control Software");

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

		// Load image formats
		ImageIO.scanForPlugins();
		
		// Setup log panel. If there is an error, we want to already log it
		logPanel = new LogPanel();
		ClientSystem.addMessageOutListener(logPanel.getMessageListener());
		ClientSystem.addMessageErrListener(logPanel.getMessageListener());
		
		// Create Console & other bottom elements
		lastMeasurementConfigurationsList.addMouseListener(new MouseAdapter()
		{
			@Override
            public void mouseClicked(final MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    final int index = lastMeasurementConfigurationsList.locationToIndex(e.getPoint());
                    if(index<0)
                        return;
                    addMeasurement(lastMeasurementConfigurationsList.getSelectedValue().configuration);
                }
            }
        });

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.addTab("Microscope Log", logPanel);
		refreshLastMeasurementsList();
		tabbedPane.addTab("Last Measurements", new JScrollPane(lastMeasurementConfigurationsList));
		tabbedPane.setMinimumSize(new Dimension(0,0));
		
		YouScopeToolBar toolBar = new YouScopeToolBar();
		toolBar.loadConfiguration();
		
		// Set layout to desktop pane.
		JSplitPane desktopSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, controlManager, tabbedPane);
		desktopSplitPane.setBorder(new EmptyBorder(0,0,0,0));
		desktopSplitPane.setDividerLocation((desktopSplitPane.getMaximumDividerLocation() - 40));
		desktopSplitPane.setResizeWeight(1.0);
		
		// Set main panel
		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(toolBar, BorderLayout.NORTH);
		mainPanel.add(desktopSplitPane, BorderLayout.CENTER);
		setContentPane(mainPanel);
		
		// Register listener to update list of windows
		desktop.addContainerListener(new ContainerListener()
		{

			@Override
			public void componentAdded(ContainerEvent e)
			{
				refreshWindowsMenu();
			}

			@Override
			public void componentRemoved(ContainerEvent e)
			{
				refreshWindowsMenu();
			}

		});
		
		// Add error frame as an listener.
		ErrorFrame errorFrame = new ErrorFrame(YouScopeFrameImpl.createTopLevelFrame());
		ClientSystem.addMessageErrListener(errorFrame.getMicroscopeMessageListener());

		// Initialize menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		
		JMenu newMeasurementMenuItem = new JMenu("New Measurement");
		Icon newMeasurementIcon = ImageLoadingTools.getResourceIcon("icons/receipt--plus.png", "new measurement");
		if(newMeasurementIcon != null)
			newMeasurementMenuItem.setIcon(newMeasurementIcon);
		for(JMenuItem item : getMeasurementMenuItems())
		{
			newMeasurementMenuItem.add(item);
		}
		fileMenu.add(newMeasurementMenuItem);
		
		JMenuItem openMeasurementMenuItem = new JMenuItem("Load Measurement", KeyEvent.VK_O);
		Icon openMeasurementIcon = ImageLoadingTools.getResourceIcon("icons/receipt-import.png", "open measurement");
		if(openMeasurementIcon != null)
			openMeasurementMenuItem.setIcon(openMeasurementIcon);
		openMeasurementMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				loadMeasurement();
			}
		});
		fileMenu.add(openMeasurementMenuItem);
		fileMenu.addSeparator();
		
		JMenuItem manageDevicesMenuItem = new JMenuItem("Edit Microscope Configuration", KeyEvent.VK_C);
		Icon manageDevicesMenuItemIcon = ImageLoadingTools.getResourceIcon("icons/database--pencil.png", "edit microscope configuration");
		if(manageDevicesMenuItemIcon != null)
			manageDevicesMenuItem.setIcon(manageDevicesMenuItemIcon);
		manageDevicesMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				YouScopeFrame frame = YouScopeFrameImpl.createTopLevelFrame(); 
				@SuppressWarnings("unused")
				ManageFrame manageFrame = new ManageFrame(frame);
				frame.setVisible(true);
			}
		});
		fileMenu.add(manageDevicesMenuItem);
		
		JMenuItem loadConfigurationMenuItem = new JMenuItem("Load Microscope Configuration", KeyEvent.VK_C);
		Icon loadConfigurationMenuItemIcon = ImageLoadingTools.getResourceIcon("icons/database-import.png", "load microscope configuration");
		if(loadConfigurationMenuItemIcon != null)
			loadConfigurationMenuItem.setIcon(loadConfigurationMenuItemIcon);
		loadConfigurationMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				askForConfiguration(null, null);
			}
		});
		fileMenu.add(loadConfigurationMenuItem);
		
		JMenuItem saveConfigurationMenuItem = new JMenuItem("Save Microscope Configuration", KeyEvent.VK_C);
		Icon saveConfigurationMenuItemIcon = ImageLoadingTools.getResourceIcon("icons/database-export.png", "save microscope configuration");
		if(saveConfigurationMenuItemIcon != null)
			saveConfigurationMenuItem.setIcon(saveConfigurationMenuItemIcon);
		saveConfigurationMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				saveMicroscopeConfiguration();
			}
		});
		fileMenu.add(saveConfigurationMenuItem);		
		
		fileMenu.addSeparator();
		
		JMenuItem configurationMenuItem = new JMenuItem("Customization", KeyEvent.VK_C);
		Icon configurationIcon = ImageLoadingTools.getResourceIcon("icons/wrench.png", "configuration");
		if(configurationIcon != null)
			configurationMenuItem.setIcon(configurationIcon);
		configurationMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				YouScopeFrame confFrame = YouScopeFrameImpl.createTopLevelFrame(); 
				@SuppressWarnings("unused")
				CustomizationFrame customizationFrame = new CustomizationFrame(confFrame);
				confFrame.setVisible(true);
			}
		});
		fileMenu.add(configurationMenuItem);
		
		JMenu skinMenu = new JMenu("Skin");
		Icon defaultLookAndFeelIcon = ImageLoadingTools.getResourceIcon("icons/application-blog.png", "skin");
		int numSkins = 0;
		for(final AddonMetadata metadata : ClientAddonProviderImpl.getProvider().getSkinMetadata())
		{
			numSkins++;
			String addonName = metadata.getName();
			if(addonName == null || addonName.length() <= 0)
				addonName = "Unnamed Look-and-Feel";
			
			JMenuItem skinMenuItem = new JMenuItem(TextTools.capitalize(addonName));
			
			Icon skinIcon = metadata.getIcon();
			if(skinIcon == null)
				skinIcon = defaultLookAndFeelIcon;
			if(skinIcon != null)
				skinMenuItem.setIcon(skinIcon);
			String description = metadata.getDescription();
			if(description != null)
				skinMenuItem.setToolTipText(description);
			skinMenuItem.addActionListener(new ActionListener()
			{
				
				@Override
				public void actionPerformed(ActionEvent e)
				{
					Skin addon;
					try {
						addon = ClientAddonProviderImpl.getProvider().createSkin(metadata);
					} catch (AddonException e1) {
						ClientSystem.err.println("Could not create look and feel.", e1);
						return;
					}
					setSkin(addon);
				}
			});
			skinMenu.add(skinMenuItem);
		}
		// Only add if there is really a choice...
		if(numSkins > 1)
			fileMenu.add(skinMenu);
		
		fileMenu.addSeparator();
		
		JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_E);
		Icon exitIcon = ImageLoadingTools.getResourceIcon("icons/slash.png", "exit");
		if(exitIcon != null)
			exitMenuItem.setIcon(exitIcon);
		exitMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				quitClient();
			}
		});
		fileMenu.add(exitMenuItem);
				
		JMenu measurementMenu = new JMenu("Create Measurement");
		measurementMenu.setMnemonic(KeyEvent.VK_M);
		for(JMenuItem item : getMeasurementMenuItems())
		{
			measurementMenu.add(item);
		}
	
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic(KeyEvent.VK_T);
		JMenuItem measurementQueueMenuItem = new JMenuItem("Measurement Manager", KeyEvent.VK_P);
		Icon measurementQueueIcon = ImageLoadingTools.getResourceIcon("icons/receipts.png", "Measurement Manager");
		if(measurementQueueIcon != null)
			measurementQueueMenuItem.setIcon(measurementQueueIcon);
		measurementQueueMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				YouScopeFrame newFrame = YouScopeFrameImpl.createTopLevelFrame(); 
				@SuppressWarnings("unused")
				MeasurementQueueFrame measurementQueueFrame = new MeasurementQueueFrame(newFrame);
				newFrame.setVisible(true);
			}
		});
		toolsMenu.add(measurementQueueMenuItem);
		toolsMenu.addSeparator();
		for(JMenuItem item : getToolsMenuItems())
		{
			toolsMenu.add(item);
		}

		JMenu emergencyMenu = new JMenu("Emergency Stop");
		emergencyMenu.setMnemonic(KeyEvent.VK_S);
		JMenuItem emergencyOnMenuItem = new JMenuItem("Emergency Stop", KeyEvent.VK_S);
		Icon emergencyOnIcon = ImageLoadingTools.getResourceIcon("icons/cross-button.png", "Emergency Stop");
		if(emergencyOnIcon != null)
			emergencyOnMenuItem.setIcon(emergencyOnIcon);
		emergencyOnMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				try
				{
					getServer().emergencyStop();
				}
				catch(RemoteException e)
				{
					ClientSystem.err.println("Could not emergency stop microscope.", e);
				}

			}
		});
		JMenuItem emergencyOffMenuItem = new JMenuItem("Reset Emergency Stop", KeyEvent.VK_R);
		Icon emergencyOffIcon = ImageLoadingTools.getResourceIcon("icons/tick-button.png", "Emergency Reset");
		if(emergencyOffIcon != null)
			emergencyOffMenuItem.setIcon(emergencyOffIcon);
		emergencyOffMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				int answer = JOptionPane.showConfirmDialog(YouScopeClientImpl.this, "Should the emergency stop really be resetted?\n\nPlease make sure that the microscope is in a save state before confirming.", "Confirm Reset of Emergency Stop", JOptionPane.YES_NO_OPTION);
				if(answer == JOptionPane.NO_OPTION)
					return;
				try
				{

					getServer().resetEmergencyStop();
				}
				catch(RemoteException e)
				{
					ClientSystem.err.println("Could not emergency stop microscope.", e);
				}

			}
		});
		emergencyMenu.add(emergencyOnMenuItem);
		emergencyMenu.add(emergencyOffMenuItem);

		scriptsMenu = new JMenu("Scripts");
		refreshScriptsMenu();
		
		windowsMenu = new JMenu("Windows");
		refreshWindowsMenu();

		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		if(Desktop.isDesktopSupported())
		{
			JMenuItem documentationMenuItem = new JMenuItem("Online Tutorial", KeyEvent.VK_D);
			Icon documentationIcon = ImageLoadingTools.getResourceIcon("icons/book-question.png", "Tutorial");
			if(documentationIcon != null)
				documentationMenuItem.setIcon(documentationIcon);
			documentationMenuItem.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					try
					{
						Desktop.getDesktop().browse(new URI("http://onlinelibrary.wiley.com/doi/10.1002/0471142727.mb1421s98/abstract"));
					}
					catch(IOException e)
					{
						ClientSystem.err.println("Could not open http://onlinelibrary.wiley.com/doi/10.1002/0471142727.mb1421s98/abstract", e);
					}
					catch(URISyntaxException e)
					{
						ClientSystem.err.println("Could not open http://onlinelibrary.wiley.com/doi/10.1002/0471142727.mb1421s98/abstract", e);
					}
				}
			});
			helpMenu.add(documentationMenuItem);
			
			
			JMenuItem visitYouScopeMenuItem = new JMenuItem("Visit www.youscope.org", KeyEvent.VK_V);
			Icon visitYouScopeIcon = ImageLoadingTools.getResourceIcon("icons/globe-green.png", "visit YouScope");
			if(visitYouScopeIcon != null)
				visitYouScopeMenuItem.setIcon(visitYouScopeIcon);
			visitYouScopeMenuItem.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					try
					{
						Desktop.getDesktop().browse(new URI("http://www.youscope.org"));
					}
					catch(IOException e)
					{
						ClientSystem.err.println("Could not open http://www.youscope.org.", e);
					}
					catch(URISyntaxException e)
					{
						ClientSystem.err.println("Could not open http://www.youscope.org.", e);
					}

				}
			});
			helpMenu.add(visitYouScopeMenuItem);
			
			helpMenu.addSeparator();
		}

		JMenuItem aboutMenuItem = new JMenuItem("About YouScope", KeyEvent.VK_A);
		Icon aboutIcon = ImageLoadingTools.getResourceIcon("icons/question-button.png", "About");
		if(aboutIcon != null)
			aboutMenuItem.setIcon(aboutIcon);
		aboutMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				final YouScopeFrame newFrame = YouScopeFrameImpl.createTopLevelFrame();
                @SuppressWarnings("unused")
				AboutFrame aboutFrame = new AboutFrame(newFrame);
                newFrame.setVisible(true);
			}
		});
		helpMenu.add(aboutMenuItem);

		menuBar.add(fileMenu);
		menuBar.add(measurementMenu);
		menuBar.add(toolsMenu);
		menuBar.add(scriptsMenu);
		menuBar.add(emergencyMenu);
		menuBar.add(windowsMenu);
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);
		
		// Set tray icon image.
		final String TRAY_ICON_URL16 = "org/youscope/client/images/icon-16.png";
		final String TRAY_ICON_URL32 = "org/youscope/client/images/icon-32.png";
		final String TRAY_ICON_URL96 = "org/youscope/client/images/icon-96.png";
		final String TRAY_ICON_URL194 = "org/youscope/client/images/icon-194.png";
		ArrayList<Image> trayIcons = new ArrayList<Image>(4);
		Image trayIcon16 = ImageLoadingTools.getResourceImage(TRAY_ICON_URL16, "YouScope");
		if(trayIcon16 != null)
			trayIcons.add(trayIcon16);
		Image trayIcon32 = ImageLoadingTools.getResourceImage(TRAY_ICON_URL32, "YouScope");
		if(trayIcon32 != null)
			trayIcons.add(trayIcon32);
		Image trayIcon96 = ImageLoadingTools.getResourceImage(TRAY_ICON_URL96, "YouScope");
		if(trayIcon96 != null)
			trayIcons.add(trayIcon96);
		Image trayIcon194 = ImageLoadingTools.getResourceImage(TRAY_ICON_URL194, "YouScope");
		if(trayIcon194 != null)
			trayIcons.add(trayIcon194);
		if(trayIcons.size()>0)
			this.setIconImages(trayIcons);

		// Set exit procedure
		this.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent arg0)
			{
				quitClient();
			}
		});
		
		// Initialize main window layout.
		setSize(600, 400);
		setExtendedState(MAXIMIZED_BOTH);
	}
	
	private List<JMenuItem> getToolsMenuItems()
	{
		ArrayList<JMenuItem> toolMenuItems = new ArrayList<JMenuItem>();
		for(final ToolMetadata metadata : ClientAddonProviderImpl.getProvider().getToolMetadata())
		{
			String[] addonFolder = metadata.getClassification();
			AddonMenuItem<ToolMetadata> newToolMenuItem = new AddonMenuItem<>(metadata, ImageLoadingTools.DEFAULT_TOOL_ICON);
			newToolMenuItem.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					openAddon();
				}
				private void openAddon()
				{
					ToolAddonUI addon;
					try
					{
						addon = ClientAddonProviderImpl.getProvider().createToolUI(metadata);
						YouScopeFrame toolFrame = addon.toFrame();
						toolFrame.setVisible(true);
					}
					catch (AddonException e)
					{
						ClientSystem.err.println("Error creating tool UI.", e);
						return;
					}
				}
			});
			
			// Setup folder structure
			JMenu parentMenu = null;
			for(int i=0; i<addonFolder.length;i++)
			{
				// Iterate over all menus to check if it already exists
				boolean found = false;
				for(Component existingItem : (parentMenu == null ? toolMenuItems.toArray(new JMenuItem[0]) : parentMenu.getMenuComponents()))
				{
					if(!(existingItem instanceof JMenu))
						continue;
					if(((JMenu)existingItem).getText().compareToIgnoreCase(addonFolder[i]) == 0)
					{
						parentMenu = (JMenu)existingItem;
						found = true;
						break;
					}
				}
				if(!found)
				{
					JMenu newMenu = new JMenu(TextTools.capitalize(addonFolder[i]));
					if(ImageLoadingTools.FOLDER_ICON != null)
						newMenu.setIcon(ImageLoadingTools.FOLDER_ICON);
					if(null == parentMenu)
						toolMenuItems.add(newMenu);
					else
						parentMenu.add(newMenu);
					parentMenu = newMenu;
				}
			}
			if(null == parentMenu)
				toolMenuItems.add(newToolMenuItem);
			else
				parentMenu.add(newToolMenuItem);
		}
		Collections.sort(toolMenuItems, new Comparator<JMenuItem>()
				{
					@Override
					public int compare(JMenuItem o1, JMenuItem o2)
					{
						if(o1 instanceof JMenu)
						{
							if(o2 instanceof JMenu)
								return o1.getText().compareTo(o2.getText());
							return -100;
						}
						else if(o2 instanceof JMenu)
							return 100;
						else
							return o1.getText().compareTo(o2.getText());
					}
				});
		return toolMenuItems;
	}
	
	private List<JMenuItem> getMeasurementMenuItems()
	{
		ArrayList<JMenuItem> measurementMenuItems = new ArrayList<JMenuItem>();
		for(final ComponentMetadata<? extends MeasurementConfiguration> metadata : ClientAddonProviderImpl.getProvider().getComponentMetadata(MeasurementConfiguration.class))
		{
			String[] addonFolder = metadata.getClassification();
			
			AddonMenuItem<ComponentMetadata<? extends MeasurementConfiguration>> newMeasurementMenuItem = new AddonMenuItem<ComponentMetadata<? extends MeasurementConfiguration>>(metadata, ImageLoadingTools.DEFAULT_MEASUREMENT_ICON);
			newMeasurementMenuItem.addActionListener(new ActionListener()
			{
				
				@Override
				public void actionPerformed(ActionEvent e)
				{
					openAddon();
				}
				private void openAddon()
				{
					ComponentAddonUI<? extends MeasurementConfiguration> addon;
					try {
						addon = ClientAddonProviderImpl.getProvider().createComponentUI(metadata);
					} catch (AddonException e1) {
						ClientSystem.err.println("Could not create measurement configuration UI.", e1);
						return;
					}
					addon.addUIListener(new ComponentAddonUIListener<MeasurementConfiguration>()
					{
						@Override
						public void configurationFinished(MeasurementConfiguration configuration) {
							Measurement measurement = YouScopeClientImpl.addMeasurement(configuration);
							if(measurement == null)
							{
								openAddon();
								return;
							}
						}
					});
					YouScopeFrame confFrame;
					try {
						confFrame = addon.toFrame();
					} catch (AddonException e) {
						ClientSystem.err.println("Could not initialize measurement configuration UI.", e);
						return;
					}
					confFrame.setVisible(true);
				}
			});
			
			// Setup folder structure
			JMenu parentMenu = null;
			for(int i=0; i<addonFolder.length;i++)
			{
				// Iterate over all menus to check if it already exists
				boolean found = false;
				for(Component existingItem : (parentMenu == null ? measurementMenuItems.toArray(new JMenuItem[measurementMenuItems.size()]) : parentMenu.getMenuComponents()))
				{
					if(!(existingItem instanceof JMenu))
						continue;
					if(((JMenu)existingItem).getText().compareToIgnoreCase(addonFolder[i]) == 0)
					{
						parentMenu = (JMenu)existingItem;
						found = true;
						break;
					}
				}
				if(!found)
				{
					JMenu newMenu = new JMenu(TextTools.capitalize(addonFolder[i]));
					if(ImageLoadingTools.FOLDER_ICON != null)
						newMenu.setIcon(ImageLoadingTools.FOLDER_ICON);
					if(parentMenu == null)
						measurementMenuItems.add(newMenu);
					else
						parentMenu.add(newMenu);
						
					parentMenu = newMenu;
				}
			}
			if(parentMenu == null)
				measurementMenuItems.add(newMeasurementMenuItem);
			else
				parentMenu.add(newMeasurementMenuItem);
	
		}
		Collections.sort(measurementMenuItems, new Comparator<JMenuItem>()
				{
					@Override
					public int compare(JMenuItem o1, JMenuItem o2)
					{
						if(o1 instanceof JMenu)
						{
							if(o2 instanceof JMenu)
								return o1.getText().compareTo(o2.getText());
							return -100;
						}
						else if(o2 instanceof JMenu)
							return 100;
						else
							return o1.getText().compareTo(o2.getText());
					}
				});
		return measurementMenuItems;
	}
	
	private void refreshWindowsMenu()
	{
		class WindowsMenuItemActivationListener implements ActionListener
		{
			private JInternalFrame	frame;

			WindowsMenuItemActivationListener(JInternalFrame frame)
			{
				this.frame = frame;
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(frame.isResizable() && frame.getWidth() > desktop.getWidth())
				{
					frame.setSize(new Dimension(desktop.getWidth(), frame.getHeight()));
				}
				if(frame.isResizable() && frame.getHeight() > desktop.getHeight())
				{
					frame.setSize(new Dimension(frame.getWidth(), desktop.getHeight()));
				}
				frame.setLocation((desktop.getWidth() - frame.getWidth()) / 2, (desktop.getHeight() - frame.getHeight()) / 2);
				if(!frame.isVisible())
					frame.setVisible(true);
				frame.toFront();
			}
		}

		windowsMenu.removeAll();
		Icon frameIcon = ImageLoadingTools.getResourceIcon("icons/application-blue.png", "show window");
		for(JInternalFrame frame : desktop.getAllFrames())
		{
			JMenuItem frameMenuItem = new JMenuItem(frame.getTitle());
			if(frameIcon != null)
				frameMenuItem.setIcon(frameIcon);
			windowsMenu.add(frameMenuItem);
			frameMenuItem.addActionListener(new WindowsMenuItemActivationListener(frame));
		}
		windowsMenu.addSeparator();
		JMenuItem closeAllMenuItem = new JMenuItem("Close All");
		Icon closeAllIcon = ImageLoadingTools.getResourceIcon("icons/applications-blue.png", "close all windows");
		if(closeAllIcon != null)
			closeAllMenuItem.setIcon(closeAllIcon);
		closeAllMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				for(JInternalFrame frame : desktop.getAllFrames())
				{
					frame.dispose();
				}
			}
		});
		windowsMenu.add(closeAllMenuItem);
	}
	
	private void refreshScriptsMenu()
	{
		class ScriptsMenuItemActivationListener implements ActionListener
		{
			private ScriptDefinition	scriptDefinition;

			ScriptsMenuItemActivationListener(ScriptDefinition	scriptDefinition)
			{
				this.scriptDefinition = scriptDefinition;
			}

			@Override
			public void actionPerformed(ActionEvent e)
			{
				ScriptDefinitionManager.runScript(scriptDefinition);
			}
		}

		scriptsMenu.removeAll();
		Icon scriptsIcon = ImageLoadingTools.getResourceIcon("icons/script--arrow.png", "execute script");
		
		for(ScriptDefinition scriptDefinition : ScriptDefinitionManager.getScriptDefinitions())
		{
			JMenuItem scriptsMenuItem = new JMenuItem(scriptDefinition.getName());
			if(scriptsIcon != null)
				scriptsMenuItem.setIcon(scriptsIcon);
			scriptsMenu.add(scriptsMenuItem);
			scriptsMenuItem.addActionListener(new ScriptsMenuItemActivationListener(scriptDefinition));
		}
		
		scriptsMenu.addSeparator();
		JMenuItem manageScriptsMenuItem = new JMenuItem("Manage Script Shortcuts");
		Icon manageScriptsIcon = ImageLoadingTools.getResourceIcon("icons/scripts.png", "manage scripts");
		if(manageScriptsIcon != null)
			manageScriptsMenuItem.setIcon(manageScriptsIcon);
		manageScriptsMenuItem.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				YouScopeFrame frame = YouScopeFrameImpl.createTopLevelFrame();
				ScriptDefinitionManagerFrame manager = new ScriptDefinitionManagerFrame(frame);
				manager.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e)
						{
							refreshScriptsMenu();
						}
					});
				frame.setVisible(true);
			}
		});
		scriptsMenu.add(manageScriptsMenuItem);
	}
	
	private void checkLastConfigFileVersion()
	{
		String warningMessage;
		try
		{
			warningMessage = manager.getMicroscope().getLastConfigurationWarning();
		}
		catch(RemoteException e)
		{
			ClientSystem.err.println("Could not detect if last config file was valid.", e);
			return;
		}
		// If there is no warning, everything is OK.
		if(warningMessage == null)
			return;
		// Display warning.
		YouScopeFrame newFrame = YouScopeFrameImpl.createTopLevelFrame();
		@SuppressWarnings("unused")
		ConfigFileInvalidFrame configFileInvalidFrame = new ConfigFileInvalidFrame(newFrame, warningMessage);
		newFrame.setVisible(true);
	}
	
	static YouScopeServer getServer()
	{
		return getMainProgram().manager;
	}

	static Microscope getMicroscope() throws RemoteException
	{
		return getServer().getMicroscope();
	}

	static YouScopeServerProperties getServerConfiguration() throws RemoteException
	{
		return getServer().getProperties();
	}

	/**
	 * Connects to the server over a network. Queries for connection details.
	 */
	public void connectToServer()
	{
		String lastIP = PropertyProviderImpl.getInstance().getProperty(PROPERTY_LAST_SERVER_URL, "127.000.000.001");
		int lastPort = Integer.parseInt(PropertyProviderImpl.getInstance().getProperty(PROPERTY_LAST_SERVER_PORT, Integer.toString(YouScopeClientImpl.REGISTRY_PORT)));

		ServerChooserFrame serverChooser = new ServerChooserFrame(lastIP, lastPort, null);
		serverChooser.selectURL();
		String url = serverChooser.getURL();
		int port = serverChooser.getPort();
		String password = serverChooser.getPassword();
		connectToServer(url, port, password);
	}

	/**
	 * Connects to the server over a network.
	 * 
	 * @param ip
	 *            IP address of the server.
	 * @param port
	 *            Port of the server.
	 * @param password
	 *            The password to access the server.
	 */
	public void connectToServer(String ip, int port, String password)
	{

		// Set policy
		URL policyURL = getClass().getClassLoader().getResource(POLICY_FILE);
		if(policyURL == null)
		{
			ClientSystem.err.println("Could not find policy file (" + POLICY_FILE + "). Quitting...");
			System.exit(1);
		}
		System.setProperty("java.security.policy", policyURL.toString());

		// Set security manager.
		if(System.getSecurityManager() == null)
		{
			System.setSecurityManager(new SecurityManager());
		}

		// Find server object in the registry.
		YouScopeServer manager;
		while(true)
		{
			try
			{
				Registry registry = LocateRegistry.getRegistry(ip, port);
				if(registry == null)
				{
					throw new Exception("Registry is null.");
				}
				YouScopeLogin login = (YouScopeLogin)registry.lookup(REGISTRY_NAME);
				manager = login.login(password);

				if(manager == null)
				{
					throw new Exception("Server object NULL.");
				}
				break;
			}
			catch(Exception e)
			{
				// Construct error message
				String lastErrorMessage = "Could not connect to server at IP " + ip + " and port " + Integer.toString(port) + ".\nDetailed error desciption:";
				Throwable error = e;
				for(; error != null; error = error.getCause())
				{
					lastErrorMessage += "\n" + error.getMessage();
				}

				// Ask for new connection details.
				ServerChooserFrame serverChooser = new ServerChooserFrame(ip, port, lastErrorMessage);
				serverChooser.selectURL();
				ip = serverChooser.getURL();
				port = serverChooser.getPort();
				password = serverChooser.getPassword();
			}
		}
		PropertyProviderImpl.getInstance().setProperty(PROPERTY_LAST_SERVER_URL, ip);
		PropertyProviderImpl.getInstance().setProperty(PROPERTY_LAST_SERVER_PORT, Integer.toString(port));

		connectToServer(manager);
	}

	/**
	 * Connects to the provided server object.
	 * 
	 * @param manager
	 *            The server object.
	 */
	public void connectToServer(YouScopeServer manager)
	{
		if(manager == null)
			throw new NullPointerException();
		this.manager = manager;

		try
		{
			// TODO: Remove listener when window is closed.
			manager.addMessageListener(ClientSystem.getMicroscopeMessageListener());
		}
		catch(RemoteException e)
		{
			ClientSystem.err.println("Couldn't register message consumer.", e);
		}

		// Check if the configuration file loaded is OK...
		checkLastConfigFileVersion();
	}

	private static String[] getAutoStartIDs()
    {
        return PropertyProviderImpl.getInstance().getProperty("auto-start", new String[0]);
    }
	
	/**
	 * Starts and runs the client.
	 * 
	 * @throws IllegalStateException
	 *             Thrown if not connected to the server, yet.
	 */
	public void startProgram() throws IllegalStateException
	{
		if(manager == null)
			throw new IllegalStateException("Not connected to server, yet.");
		setVisible(true);
		
		final String[] autoStartIDs = getAutoStartIDs();
        for(String autoStartID : autoStartIDs)
        {
        	try
        	{
	        	ToolAddonUI addon = ClientAddonProviderImpl.getProvider().createToolUI(autoStartID);
	        	YouScopeFrame toolFrame = addon.toFrame();
	            toolFrame.setVisible(true);
	            continue;
        	}
        	catch(@SuppressWarnings("unused") AddonException e)
        	{
        		// do nothing, probably not a tool.
        	}
        	
        	try
        	{
        		ComponentAddonUI<? extends MeasurementConfiguration> addon = ClientAddonProviderImpl.getProvider().createComponentUI(autoStartID, MeasurementConfiguration.class);
	        	YouScopeFrame toolFrame = addon.toFrame();
	            toolFrame.setVisible(true);
	            continue;
        	}
        	catch(@SuppressWarnings("unused") AddonException e)
        	{
        		// do nothing, probably not a measurement.
        	}
            
            // if we arrive here, we didn't find autostart tool or measurement configuration
            ClientSystem.err.println("Could not find autostart tool or measurement type with type identifier " + autoStartID);
        }
	}

	/**
	 * Returns the main window.
	 * 
	 * @return Microscope control element.
	 */
	public synchronized static YouScopeClientImpl getMainProgram()
	{
		if(youScopeClient == null)
		{
			// Set uncaught exception handler
			Thread.currentThread().setContextClassLoader(YouScopeClientImpl.class.getClassLoader());
			Thread.setDefaultUncaughtExceptionHandler(new ClientSystem.YouScopeUncaughtExceptionHandler());
		    System.setProperty("sun.awt.exception.handler",
		    		ClientSystem.YouScopeUncaughtExceptionHandler.class.getName());
			initializeSkin();
			youScopeClient = new YouScopeClientImpl();
			SwingUtilities.updateComponentTreeUI(youScopeClient);
		}
		return youScopeClient;
	}
	
	private void askForConfiguration(String lastConfigFile, Exception lastException)
	{
		YouScopeFrame frame = YouScopeFrameImpl.createTopLevelFrame(); 
		@SuppressWarnings("unused")
		ConfigFileChooserFrame configFileChooserFrame = new ConfigFileChooserFrame(frame, lastConfigFile, lastException);
		frame.setVisible(true);
	}
	
	void loadMicroscopeConfiguration(String configurationFile)
	{
		try
		{
			if(configurationFile != null)
			{
				FileReader fileReader = new FileReader(configurationFile);
				RMIReader rmiReader = new RMIReader(fileReader);
				getMicroscope().loadConfiguration(rmiReader);
				rmiReader.close();
				fileReader.close();
			}
			else
				getMicroscope().loadConfiguration(null);
		}
		catch(Exception e)
		{
			askForConfiguration(configurationFile, e);
		}
		checkLastConfigFileVersion();
	}
	
	boolean saveMicroscopeConfiguration()
	{
		PropertyProviderImpl settings = PropertyProviderImpl.getInstance();
		
		String fileToOpen = settings.getProperty(PropertyProviderImpl.SETTINGS_CONFIG_FILE_LAST_0, "YSMicroscopeConfiguration.cfg");
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setSelectedFile(new File(fileToOpen));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Configuration Files (.cfg)", "cfg"));
		
		int returnVal = fileChooser.showDialog(null, "Save");
		if(returnVal != JFileChooser.APPROVE_OPTION)
			return false;
			
		File file = fileChooser.getSelectedFile();
		String filePath = file.getAbsolutePath();
		if(file.exists())
		{
			int shouldOverwrite = JOptionPane.showConfirmDialog(null, "File "+filePath +" does already exist.\nOverwrite?", "File already exists", JOptionPane. YES_NO_OPTION);
			if(shouldOverwrite != JOptionPane.YES_OPTION)
    			return false;
		}
		
		// Save content to file
		RMIWriter rmiWriter = null;
		try
		{
			rmiWriter = new RMIWriter(new FileWriter(file));
			YouScopeClientImpl.getMicroscope().saveConfiguration(rmiWriter);
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not create or save configuration.", e);
			return false;
		}
		finally
		{
			if(rmiWriter != null)
			{
				try {
					rmiWriter.close();
				} catch (@SuppressWarnings("unused") IOException e1) {
					// do nothing.
				}
			}
		}
		
		// Add file to list of last configurations
		// Remove item from list if its already in.
		String[]		settingsList		= {PropertyProviderImpl.SETTINGS_CONFIG_FILE_LAST_0, PropertyProviderImpl.SETTINGS_CONFIG_FILE_LAST_1, PropertyProviderImpl.SETTINGS_CONFIG_FILE_LAST_2, PropertyProviderImpl.SETTINGS_CONFIG_FILE_LAST_3};
		for(int i = 0; i < settingsList.length; i++)
		{
			if(filePath.equals(settings.getProperty(settingsList[i], (String)null)))
			{
				settings.deleteProperty(settingsList[i]);
			}
		}
		// Insert item in list at first position, move everything downwards.
		for(int i = 0; i < settingsList.length; i++)
		{
			String tempItem = settings.getProperty(settingsList[i], (String)null);
			settings.setProperty(settingsList[i], filePath);
			if(tempItem == null)
				break;
			filePath = tempItem;
		}
		
		return true;
	}
	
	void setSkin(Skin skin)
	{
		JOptionPane.showMessageDialog(this, "The change of the skin will have effect only after restarting YouScope.", "Change Requires Restart", JOptionPane.INFORMATION_MESSAGE);
		PropertyProviderImpl.getInstance().setProperty(StandardProperty.PROPERTY_SKIN, skin.getMetadata().getTypeIdentifier());
	}
	
	static boolean addMeasurement(Measurement measurement)
	{
		try
		{
			getMainProgram().controlManager.addMeasurement(measurement);
			return true;
		}
		catch(RemoteException e)
		{
			ClientSystem.err.println("Could not open measurement.", e);
			return false;
		}
	}
	
	static Measurement addMeasurement(final MeasurementConfiguration configuration)
    {
        final YouScopeClientImpl client = getMainProgram();
        Measurement measurement = null;
        try
        {
            measurement = client.controlManager.addMeasurement(configuration);
        } catch (final Exception e1)
        {
            ClientSystem.err.println("Could not initialize measurement.", e1);
        }
        if (measurement == null)
        {
            return null;
        }
        LastMeasurementManager.getLastMeasurementManager().addMeasurement(configuration);
        client.refreshLastMeasurementsList();
        return measurement;
    }

	/**
	 * Shows the frame on the desktop content pane.
	 * 
	 * @param frame
	 *            Frame to show.
	 */
	public void showFrame(final YouScopeFrameImpl frame)
    {
        if (!desktop.isAncestorOf(frame))
        {
            desktop.add(frame);
        }
        // set to default location
        // (if frame has stored location, it will be updated anyways)
        frame.setLocation(desktop.getComponentCount() * 30, desktop.getComponentCount() * 20);
        FramePositionStorage.getInstance().setFramePosition(frame);
    }

	private void refreshLastMeasurementsList()
    {
        Runnable runner = new Runnable()
            {
                @Override
                public void run()
                {
                    LastMeasurementListElement lastSelection = lastMeasurementConfigurationsList.getSelectedValue();
                    MeasurementConfiguration[] lastMeasurements = LastMeasurementManager.getLastMeasurementManager().getMeasurements();
                    LastMeasurementListElement[] listElements = new LastMeasurementListElement[lastMeasurements.length];
                    int lastIndex = -1;
                    for(int i=0; i<lastMeasurements.length; i++)
                    {
                        listElements[lastMeasurements.length - i - 1] = new LastMeasurementListElement(lastMeasurements[i]);
                        if(lastSelection!=null && listElements[lastMeasurements.length - i - 1].toString().equals(lastSelection.toString()))
                            lastIndex = lastMeasurements.length - i - 1;
                    }
                    lastMeasurementConfigurationsList.setListData(listElements);
                    if(lastIndex >= 0)
                    {
                        lastMeasurementConfigurationsList.setSelectedIndex(lastIndex);
                        lastMeasurementConfigurationsList.ensureIndexIsVisible(lastIndex);
                    }
                }
            };

            if(SwingUtilities.isEventDispatchThread())
                runner.run();
            else
                SwingUtilities.invokeLater(runner);
    }

	

	/**
	 * Tries to quit the client. The user is asked if the client should be really quit.
	 */
	public void quitClient()
	{
		int answer = JOptionPane.showConfirmDialog(this, "Should the program really quit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
		if(answer == JOptionPane.NO_OPTION)
			return;
		
		// Close all frames
		// run several times, to ensure that frames which did not close because child frames were still active, which are now closed
		// will close...
		boolean allFramesClosed = true;
		for(int run=0; run<5; run++)
		{
			JInternalFrame[] frames = desktop.getAllFrames();
			allFramesClosed = true;
			for(int i = 0;i<frames.length;i++)
			{
				try
				{
					frames[i].setClosed(true);
				}
				catch(@SuppressWarnings("unused") PropertyVetoException e)
				{
					allFramesClosed = false;
				}
			}
			if(allFramesClosed)
				break;
		}

		if(!allFramesClosed)
		{
			answer = JOptionPane.showConfirmDialog(this, "Not all frames could be closed.\nThis might indicate unsaved data or running experiments.\nQuit anyway?", "Confirm Hard Exit", JOptionPane.YES_NO_OPTION);
			if(answer == JOptionPane.NO_OPTION)
				return;
		}
		
		// Store frame positions (only does so if activated).
		FramePositionStorage.getInstance().saveToDisk();
		
		dispose();
		clientFinished();
	} 
	
	private volatile Vector<ActionListener>	clientFinishListeners	= new Vector<ActionListener>();

	/**
	 * Adds a listener which gets notified if the server finished execution. The listener than e.g. can call System.exit(0).
	 * @param listener The listener which should be added.
	 */
	public void addClientFinishListener(ActionListener listener)
	{
		if(listener != null)
		{
			synchronized(clientFinishListeners)
			{
				clientFinishListeners.add(listener);
			}
		}
	}

	/**
	 * Removes a previously added listener.
	 * @param listener The listener to be removed.
	 */
	public void removeClientFinishListener(ActionListener listener)
	{
		if(listener != null)
		{
			synchronized(clientFinishListeners)
			{
				clientFinishListeners.remove(listener);
			}
		}
	}

	private void clientFinished()
	{
		synchronized(clientFinishListeners)
		{
			// Send notification to the listeners that server finished.
			for(ActionListener listener : clientFinishListeners)
			{
				listener.actionPerformed(new ActionEvent(this, 0, "Client execution finished."));
			}
		}
	}

	static void loadMeasurement()
	{
		JFileChooser fileChooser = new JFileChooser((String) PropertyProviderImpl.getInstance().getProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Measurement Configuration Files (.csb)", "csb"));
		int returnVal = fileChooser.showDialog(null, "Load Measurement Configuration");
		String fileName;
		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			fileName = fileChooser.getSelectedFile().getPath();
			File folder = fileChooser.getCurrentDirectory();
			if(folder != null)
			{
				folder = folder.getParentFile();
				if(folder != null)
				{
					folder = folder.getParentFile();
					if(folder != null)
						PropertyProviderImpl.getInstance().setProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER, folder.getAbsolutePath());
				}
			}
			
		}
		else
		{
			return;
		}

		MeasurementConfiguration configuration;
		try
		{
			configuration = (MeasurementConfiguration) ConfigurationManagement.loadConfiguration(fileName);
		}
		catch(Throwable e)
		{
			JOptionPane.showMessageDialog(getMainProgram(), "Could not load measurement Configuration!\n\nError Message is:\n" + e.getMessage(), "Measurement Configuration Load Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		YouScopeClientImpl.addMeasurement(configuration);
	}

	static void saveMeasurement(MeasurementConfiguration measurement)
	{
		if(measurement == null)
			return;
		JFileChooser fileChooser = new JFileChooser((String) PropertyProviderImpl.getInstance().getProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER));
		fileChooser.setSelectedFile(new File(measurement.getName() + ".csb"));
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Measurement Configuration Files (.csb)", "csb"));
		int returnVal = fileChooser.showDialog(null, "Save Measurement Configuration");
		String fileName;
		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			fileName = fileChooser.getSelectedFile().getPath();
			saveMeasurement(measurement, fileName);
			
			File folder = fileChooser.getCurrentDirectory();
			if(folder != null)
			{
				folder = folder.getParentFile();
				if(folder != null)
				{
					folder = folder.getParentFile();
					if(folder != null)
						PropertyProviderImpl.getInstance().setProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER, folder.getAbsolutePath());
				}
			}
		}
		else
		{
			return;
		}

	}

	static void saveMeasurement(MeasurementConfiguration measurement, String fileName)
	{
		try
		{
			ConfigurationManagement.saveConfiguration(fileName, measurement);
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(getMainProgram(), "Could not save measurement Configuration!\n\nError Message is:\n" + e.getMessage(), "Measurement Configuration Save Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
