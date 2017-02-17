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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.uielements.StandardFormats;

/**
 * @author Moritz Lang
 *
 */
class ConfigFileChooserFrame
{
	private String						configFile			= null;
	private boolean						finished			= false;
	private JButton						loadConfigurationButton;
	private ConfigFileChooserComboBox	fileLocationField;
	private final YouScopeFrame frame;

	ConfigFileChooserFrame(YouScopeFrame frame, String lastConfigFile, Exception lastError)
	{
		frame.setTitle("Load Microscope Configuration");
		frame.setResizable(false);
		frame.setClosable(true);
		frame.setMaximizable(false);
		this.frame = frame;
		
		fileLocationField = new ConfigFileChooserComboBox(lastConfigFile);

		// Initialize layout
		GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
		GridBagLayout contentlayout = new GridBagLayout();
		JPanel contentPanel = new JPanel(contentlayout);

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
			addConfElement(errorScrollPane, contentlayout, newLineConstr, contentPanel);
		}

		// Explanation what to do.
		JEditorPane explanationArea = new JEditorPane();
		explanationArea.setEditable(false);
		explanationArea.setContentType("text/html");
		explanationArea.setText("<html><p style=\"font-size:small;margin-top:0px;\"><b>Please insert the location of the microscope configuration file.</b></p>" + "<p style=\"font-size:small;margin-top:8px;margin-bottom:0px\">The configuration file usually has the ending \".cfg\".\nIf no configuration is available, you can start YouScope with the setting \"Empty Configuration\" and configure the microscope using the respective settings in YouScope.</p></html>");
		JScrollPane explanationScrollPane = new JScrollPane(explanationArea);
		explanationScrollPane.setPreferredSize(new Dimension(450, 115));
		addConfElement(explanationScrollPane, contentlayout, newLineConstr, contentPanel);

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
		addConfElement(folderPanel, contentlayout, newLineConstr, contentPanel);

		loadConfigurationButton = new JButton("Load configuration");
		loadConfigurationButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				fileLocationField.saveToConfiguration();
				loadConfiguration(fileLocationField.getText());
			}
		});
		
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(contentPanel, BorderLayout.CENTER);
		contentPane.add(loadConfigurationButton, BorderLayout.SOUTH);
		frame.setContentPane(contentPane);
		frame.pack();
	}

	private void loadConfiguration(final String configFile)
	{
		frame.startLoading();

		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				YouScopeClientImpl.getMainProgram().loadMicroscopeConfiguration(configFile);
				ConfigFileChooserFrame.this.frame.endLoading();
				ConfigFileChooserFrame.this.frame.setVisible(false);
			}
			
		}).start();
	}
	
	private void chooseFile()
	{
		JFileChooser fileChooser = new JFileChooser(fileLocationField.getText());
		fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Microscope Configuration Files (.cfg)", "cfg"));
		int returnVal = fileChooser.showDialog(null, "Open");

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

		private final String[]		settingsList		= {PropertyProviderImpl.SETTINGS_CONFIG_FILE_LAST_0, PropertyProviderImpl.SETTINGS_CONFIG_FILE_LAST_1, PropertyProviderImpl.SETTINGS_CONFIG_FILE_LAST_2, PropertyProviderImpl.SETTINGS_CONFIG_FILE_LAST_3

														};

		ConfigFileChooserComboBox(String lastConfigFile)
		{
			setEditable(true);

			addItem(DEFINE_LOCATION);

			for(String setting : settingsList)
			{
				String conf = PropertyProviderImpl.getInstance().getProperty(setting, (String)null);
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

		public void saveToConfiguration()
		{
			String value = getText();
			if(value == null)
				return;
			// Remove item from list if its already in.
			PropertyProviderImpl settings = PropertyProviderImpl.getInstance();
			for(int i = 0; i < settingsList.length; i++)
			{
				if(value.equals(settings.getProperty(settingsList[i], (String)null)))
				{
					settings.deleteProperty(settingsList[i]);
				}
			}
			// Insert item in list at first position, move everything downwards.
			for(int i = 0; i < settingsList.length; i++)
			{
				String tempItem = settings.getProperty(settingsList[i], (String)null);
				settings.setProperty(settingsList[i], value);
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
