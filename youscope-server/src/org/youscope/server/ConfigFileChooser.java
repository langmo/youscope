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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.youscope.uielements.ImageLoadingTools;

/**
 * Small user interface to choose the microscope configuration file to load.
 * This UI is only shown if the server is started directly (not using the youscope-starter), or if an error occurred while loading a configuration.
 * @author Moritz Lang
 */
class ConfigFileChooser extends JFrame
{
	/**
	 * Serial version UID.
	 */
	private static final long			serialVersionUID	= 7901351915328801441L;

	private String						configFile			= null;

	private boolean						finished			= false;

	private JButton						startMicroscopeButton;

	private ConfigFileChooserComboBox	fileLocationField;

	ConfigFileChooser(String lastConfigFile, Exception lastError)
	{
		super("Choose config file!");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		try
		{
			setAlwaysOnTop(true);
		}
		catch(@SuppressWarnings("unused") SecurityException e)
		{
			// Do nothing.
		}

		fileLocationField = new ConfigFileChooserComboBox(lastConfigFile, ConfigurationSettings.loadProperties());

		// Initialize layout
		getContentPane().setLayout(new BorderLayout());
		GridBagConstraints newLineConstr = new GridBagConstraints();
		GridBagLayout layout = new GridBagLayout();
		newLineConstr.fill = GridBagConstraints.HORIZONTAL;
		newLineConstr.gridwidth = GridBagConstraints.REMAINDER;
		newLineConstr.anchor = GridBagConstraints.NORTHWEST;
		newLineConstr.gridx = 0;
		newLineConstr.weightx = 1.0;
		newLineConstr.weighty = 0;
		newLineConstr.insets = new Insets(5, 5, 5, 5);
		JPanel contentPanel = new JPanel(layout);

		// Set tray icon image.
		final String TRAY_ICON_URL16 = "org/youscope/server/images/icon-16.png";
		final String TRAY_ICON_URL32 = "org/youscope/server/images/icon-32.png";
		final String TRAY_ICON_URL96 = "org/youscope/server/images/icon-96.png";
		final String TRAY_ICON_URL194 = "org/youscope/server/images/icon-194.png";
		ArrayList<Image> trayIcons = new ArrayList<Image>();
		Image trayIcon16 = ImageLoadingTools.getResourceImage(TRAY_ICON_URL16, "tray icon");
		if(trayIcon16 != null)
			trayIcons.add(trayIcon16);
		Image trayIcon32 = ImageLoadingTools.getResourceImage(TRAY_ICON_URL32, "tray icon");
		if(trayIcon32 != null)
			trayIcons.add(trayIcon32);
		Image trayIcon96 = ImageLoadingTools.getResourceImage(TRAY_ICON_URL96, "tray icon");
		if(trayIcon96 != null)
			trayIcons.add(trayIcon96);
		Image trayIcon194 = ImageLoadingTools.getResourceImage(TRAY_ICON_URL194, "tray icon");
		if(trayIcon194 != null)
			trayIcons.add(trayIcon194);
		if(trayIcons.size()>0)
			this.setIconImages(trayIcons);

		if(lastError != null)
		{
			// Display a message why last input was incorrect
			JEditorPane errorArea = new JEditorPane();
			errorArea.setEditable(false);
			errorArea.setContentType("text/html");

			String errorMessage = "<html><p style=\"font-size:small;color:EE2222;margin-top:0px;\"><b>Could not load config file.</b></p>" + "<p style=\"font-size:small;margin-top:8px;margin-bottom:0px\"><b>Detailed error desciption:</b></p>";
			Throwable error = lastError;
			for(; error != null; error = error.getCause())
			{
				errorMessage += "<p style=\"font-size:small;margin-top:4px;margin-bottom:0px\">";
				if(error.getMessage() != null)
					errorMessage += "<i>" + error.getClass().getSimpleName() + "</i>: " + error.getMessage().replace("\n", "<br />");
				else
					errorMessage += "<i>" + error.getClass().getSimpleName() + "</i>: No further information.";
				errorMessage += "</p>";
			}
			errorArea.setText(errorMessage);

			JScrollPane errorScrollPane = new JScrollPane(errorArea);
			errorScrollPane.setPreferredSize(new Dimension(450, 115));
			addConfElement(errorScrollPane, layout, newLineConstr, contentPanel);
		}

		// Explanation what to do.
		JEditorPane explanationArea = new JEditorPane();
		explanationArea.setEditable(false);
		explanationArea.setContentType("text/html");
		explanationArea.setText("<html><p style=\"font-size:small;margin-top:0px;\"><b>Please insert the location of the microscope configuration file.</b></p>" + "<p style=\"font-size:small;margin-top:8px;margin-bottom:0px\">The configuration file usually has the ending \".cfg\".\nIf no configuration is available, you can start YouScope with the setting \"Empty Configuration\" and configure the microscope using the respective settings in YouScope.</p></html>");
		JScrollPane explanationScrollPane = new JScrollPane(explanationArea);
		explanationScrollPane.setPreferredSize(new Dimension(450, 115));
		addConfElement(explanationScrollPane, layout, newLineConstr, contentPanel);

		JPanel folderPanel = new JPanel(new BorderLayout(5, 0));
		folderPanel.setBorder(new TitledBorder("Microscope Configuration File"));

		folderPanel.add(fileLocationField, BorderLayout.CENTER);
		JButton openFolderChooser = new JButton("Edit");
		openFolderChooser.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				chooseFile();
			}
		});
		folderPanel.add(openFolderChooser, BorderLayout.EAST);
		addConfElement(folderPanel, layout, newLineConstr, contentPanel);

		getContentPane().add(contentPanel, BorderLayout.CENTER);

		JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		startMicroscopeButton = new JButton("Start");
		startMicroscopeButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				configFile = fileLocationField.getText();
				finished = true;
				if(configFile != null && !(new File(configFile)).exists())
				{
					JOptionPane.showMessageDialog(ConfigFileChooser.this, "The configuration file\n" + configFile + "\ncould not be found.\nPlease enter a valid location.", "Configuration File Not Found", JOptionPane.ERROR_MESSAGE);
					return;
				}

				synchronized(ConfigFileChooser.this)
				{
					ConfigFileChooser.this.notifyAll();
				}
				ConfigurationSettings configuration = ConfigurationSettings.loadProperties();
				fileLocationField.saveToConfiguration(configuration);
				configuration.saveProperties();
				ConfigFileChooser.this.dispose();
			}
		});
		buttonsPanel.add(startMicroscopeButton);
		JButton cancelButton = new JButton("Exit");
		cancelButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				System.exit(0);
			}
		});
		buttonsPanel.add(cancelButton);

		addWindowFocusListener(new WindowAdapter()
		{
			@Override
			public void windowGainedFocus(WindowEvent e)
			{
				startMicroscopeButton.requestFocusInWindow();
			}
		});

		buttonsPanel.setBorder(new EmptyBorder(0, 5, 5, 5));
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

		pack();
		// Get the size of the default screen
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);

		setVisible(true);
	}

	private void chooseFile()
	{
		JFileChooser fileChooser = new JFileChooser(fileLocationField.getText());
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Microscope Configuration Files (.cfg)", "cfg"));
		int returnVal = fileChooser.showDialog(ConfigFileChooser.this, "Open");

		if(returnVal == JFileChooser.APPROVE_OPTION)
		{
			fileLocationField.setText(fileChooser.getSelectedFile().getAbsolutePath());
		}
	}

	private class ConfigFileChooserComboBox extends JComboBox<Object>
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -4785284511036306325L;

		private final static String	SEPARATOR			= "SEPARATOR";

		private final static String	EMPTY_CONFIGURATION	= "Empty Configuration";

		private final static String	DEFINE_LOCATION		= "<Define location>";

		private final String[]		settingsList		= {ConfigurationSettings.SETTINGS_CONFIG_FILE_LAST_0, ConfigurationSettings.SETTINGS_CONFIG_FILE_LAST_1, ConfigurationSettings.SETTINGS_CONFIG_FILE_LAST_2, ConfigurationSettings.SETTINGS_CONFIG_FILE_LAST_3

														};

		ConfigFileChooserComboBox(String lastConfigFile, ConfigurationSettings configuration)
		{
			setEditable(true);

			addItem(DEFINE_LOCATION);

			for(String setting : settingsList)
			{
				String conf = configuration.getProperty(setting, null);
				if(conf != null)
				{
					File file = new File(conf);
					if(file.exists())
						addItem(file);
				}
			}
			addItem(SEPARATOR);
			addItem(EMPTY_CONFIGURATION);

			if(lastConfigFile != null)
				setSelectedItem(lastConfigFile);
			else
			{
				if(getItemCount() > 3)
					setSelectedIndex(1);
				else
					setSelectedItem("");
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

		public void setText(String text)
		{
			setSelectedItem(text);
			setEditable(true);
		}

		class ConfigFileChooserComboBoxRenderer extends JLabel implements ListCellRenderer<Object>
		{
			/**
			 * Serial Version UID.
			 */
			private static final long	serialVersionUID	= 4144921105492912843L;

			JSeparator					separator;

			public ConfigFileChooserComboBoxRenderer()
			{
				setOpaque(true);
				setBorder(new EmptyBorder(1, 1, 1, 1));
				separator = new JSeparator(JSeparator.HORIZONTAL);
			}

			@Override
			public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				if(index == 0)
				{
					setText(DEFINE_LOCATION);
				}
				else if(index == ConfigFileChooserComboBox.this.getItemCount() - 2)
				{
					return separator;
				}
				else if(index == ConfigFileChooserComboBox.this.getItemCount() - 1)
				{
					setText(EMPTY_CONFIGURATION);
				}
				else if(value instanceof File)
				{
					setText(((File)value).getName());
				}
				else if(value != null)
				{
					setText(value.toString());
				}
				else
				{
					setText("unknown");
				}

				if(isSelected && index != ConfigFileChooserComboBox.this.getItemCount() - 2)
				{
					setBackground(list.getSelectionBackground());
					setForeground(list.getSelectionForeground());
				}
				else
				{
					setBackground(list.getBackground());
					setForeground(list.getForeground());
				}
				setFont(list.getFont());
				return this;
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
				int index = ConfigFileChooserComboBox.this.getSelectedIndex();
				if(index == 0)
				{
					ConfigFileChooserComboBox.this.setEditable(true);
					setSelectedItem("");
					chooseFile();
				}
				else if(index == ConfigFileChooserComboBox.this.getItemCount() - 2)
				{
					setSelectedItem(currentItem);
					return;
				}
				else if(index == ConfigFileChooserComboBox.this.getItemCount() - 1)
				{
					ConfigFileChooserComboBox.this.setEditable(false);
					currentItem = getSelectedItem();
				}
				else
				{
					ConfigFileChooserComboBox.this.setEditable(true);
					currentItem = getSelectedItem();
				}
			}
		}

	}

	protected static void addConfElement(Component component, GridBagLayout layout, GridBagConstraints constr, Container panel)
	{
		layout.setConstraints(component, constr);
		panel.add(component);
	}

	String getConfigFile()
	{
		return configFile;
	}

	synchronized String waitForConfigFile()
	{
		while(finished == false)
		{
			try
			{
				wait();
			}
			catch(@SuppressWarnings("unused") InterruptedException e)
			{
				// Do nothing
			}
		}
		return getConfigFile();
	}
}
