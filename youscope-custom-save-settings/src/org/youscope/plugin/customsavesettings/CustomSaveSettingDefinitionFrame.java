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
package org.youscope.plugin.customsavesettings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;

import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.saving.FileNameMacroConverter;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;

/**
 * @author Moritz Lang
 *
 */
class CustomSaveSettingDefinitionFrame
{
	private final YouScopeFrame									frame;
	
	private final JTextField customSaveSettingNameField = new JTextField();
	private final JTextField baseFolderField = new JTextField();
	private final JLabel baseFolderLabel = new JLabel("Measurement Base Folder:");
	private final JPanel baseFolderPanel = new JPanel(new BorderLayout(5, 0));
	
	private final JComboBox<ComboBoxExampleItem>  baseFolderExtension = new JComboBox<ComboBoxExampleItem>(getBaseFolderExtensionExamples()); 
	private final JComboBox<ComboBoxExampleItem> imageFilePath = new JComboBox<>(getImageExamples());
	private final JTextField tableFilePath = new JTextField();
	private final JTextField imageMetadataTableFilePath = new JTextField();
	private final JTextField measurementConfigurationFilePath = new JTextField();
	private final JTextField microscopeConfigurationFilePath = new JTextField();
	private final JTextField xmlInformationFilePath = new JTextField();
	private final JTextField htmlInformationFilePath = new JTextField();
	private final JTextField logOutFilePath = new JTextField();
	private final JTextField logErrFilePath = new JTextField();
	private final JComboBox<String> imageExtension;
		
	private final ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
	private final YouScopeClient client;
	private final CustomSaveSettingType customSaveSettingType;
	private final String oldCustomSaveSettingName;
	
	private JRadioButton					baseFolderByUser				= new JRadioButton("Let user decide.", false);
	private JRadioButton					baseFolderByDefinition		= new JRadioButton("Fix to pre-defined location.", false);
	
	private static class ComboBoxExampleItem
	{
		public final String	fileName;

		public final String	example;

		ComboBoxExampleItem(String fileName, String example)
		{
			this.fileName = fileName;
			this.example = example;
		}

		@Override
		public String toString()
		{
			return fileName;
		}
	}
	
	private class ComboBoxExampleItemRenderer extends JLabel implements ListCellRenderer<Object>
	{

		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -365527994444402591L;

		public ComboBoxExampleItemRenderer()
		{
			setOpaque(true);
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends Object> list, Object value,
				int index, boolean isSelected, boolean cellHasFocus)
		{
			ComboBoxExampleItem item = (ComboBoxExampleItem) value;

			if (isSelected)
			{
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			}
			else
			{
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			setText("<html><p style=\"margin-top:6px\"><code>"
					+ item.fileName
					+ "</code></p><p style=\"margin-bottom:6px\">(e.g. <samp>"
					+ item.example + "</samp>)</p><html>");

			return this;
		}
	}

	private static ComboBoxExampleItem[] getImageExamples()
	{
		ArrayList<ComboBoxExampleItem> items = new ArrayList<>();
		items.add(new ComboBoxExampleItem("%N_position%4w%2p_time%4n.tif", "RFP_position010203_time0005.tif"));
		items.add(new ComboBoxExampleItem("%N_%C_position%4w%2p_time%4n.tif", "RFP_Camera1_position010203_time0005.tif"));
		items.add(new ComboBoxExampleItem("%N_channel_%c_well_%W(pos_%4p)_time_%4y.%2m.%2d-%2H.%2M.%2s-%3S_(number_%4n).tif", "RFP_well_B3(pos_0003)_time_2011.01.01-14.30.05-365_number_0005.tif"));
		items.add(new ComboBoxExampleItem("%N_%c_%W(%2p)_%4y.%2m.%2d-%2H.%2M.%2s-%3S_(%4n).tif", "RFP_B3(0003)_2011.01.01-14.30.05-365_0005.tif"));
		
		return items.toArray(new ComboBoxExampleItem[items.size()]);
	}
	
	private static ComboBoxExampleItem[] getBaseFolderExtensionExamples()
	{
		ArrayList<ComboBoxExampleItem> items = new ArrayList<>();
		items.add(new ComboBoxExampleItem("%xN/%4xy-%2xm-%2xd_%2xH-%2xM-%2xs", "unnamed/2016-06-15_10-28-16"));
		items.add(new ComboBoxExampleItem("%xN_%4xy-%2xm-%2xd_%2xH-%2xM-%2xs", "unnamed_2016-06-15_10-28-16"));
		return items.toArray(new ComboBoxExampleItem[items.size()]);
	}
	
	private static String getImageToolTip()
	{
		String fileToolTip = "<html><body><h1>File path template for image files.</h1>"
				+ "<p>This path must be unique for every image name, position and index; otherwise, already existing images might be overwritten.<br />"
				+ "In this path, macros of the type <code>%[0-9]*[a-zA-Z]?</code> are replaced automatically. The (optional) number thereby defines the maximal length of the replacing string, <br />"
				+ "and the character(s) define the replacement, e.g. <code>%y</code> is replaced by <code>2016</code>, and <code>%2y</code> with <code>16</code>. Following characters are defined: <br />"
				+ "<ul>";
		FileNameMacroConverter.ReplacePattern[] fileNameMacros = FileNameMacroConverter.getImagePathMacros();
		for (int i = 0; i < fileNameMacros.length; i++)
		{
			fileToolTip += "<li><code>" + fileNameMacros[i].pattern
					+ "</code>: " + fileNameMacros[i].description + "</li>";
		}
		fileToolTip += "</ul></p></body></html>";
		return fileToolTip;
	}
	private static String getTableToolTip()
	{
		String fileToolTip = "<html><body><h1>File path template for table files.</h1>"
				+ "<p>This path must be unique for every table name; otherwise, already existing tables might be overwritten.<br />"
				+ "In this path, macros of the type <code>%[0-9]*[a-zA-Z]?</code> are replaced automatically. The (optional) number thereby defines the maximal length of the replacing string, <br />"
				+ "and the character(s) define the replacement, e.g. <code>%xy</code> is replaced by <code>2016</code>, and <code>%2y</code> with <code>16</code>. Following characters are defined: <br />"
				+ "<ul>";
		FileNameMacroConverter.ReplacePattern[] fileNameMacros = FileNameMacroConverter.getTablePathMacros();
		for (int i = 0; i < fileNameMacros.length; i++)
		{
			fileToolTip += "<li><code>" + fileNameMacros[i].pattern
					+ "</code>: " + fileNameMacros[i].description + "</li>";
		}
		fileToolTip += "</ul></p></body></html>";
		return fileToolTip;
	}
	private static String getGeneralToolTip()
	{
		String fileToolTip = "<html><body><h1>General Template.</h1>"
				+ "<p>In this path, macros of the type <code>%[0-9]*[a-zA-Z]?</code> are replaced automatically. The (optional) number thereby defines the maximal length of the replacing string, <br />"
				+ "and the character(s) define the replacement, e.g. <code>%xy</code> is replaced by <code>2016</code>, and <code>%2y</code> with <code>16</code>. Following characters are defined: <br />"
				+ "<ul>";
		FileNameMacroConverter.ReplacePattern[] fileNameMacros = FileNameMacroConverter.getGeneralPathMacros();
		for (int i = 0; i < fileNameMacros.length; i++)
		{
			fileToolTip += "<li><code>" + fileNameMacros[i].pattern
					+ "</code>: " + fileNameMacros[i].description + "</li>";
		}
		fileToolTip += "</ul></p></body></html>";
		return fileToolTip;
	}
	
	CustomSaveSettingDefinitionFrame(YouScopeClient client, YouScopeServer server, YouScopeFrame frame)
	{
		this(client, server, frame, null);
	}
	CustomSaveSettingDefinitionFrame(YouScopeClient client, YouScopeServer server, final YouScopeFrame frame, CustomSaveSettingType customSaveSettingType)
	{
		this.frame = frame;
		this.client = client;
		
		if(customSaveSettingType != null)
			oldCustomSaveSettingName = customSaveSettingType.getSaveSettingName(); 
		else
		{
			oldCustomSaveSettingName = null;
			customSaveSettingType =new CustomSaveSettingType();
		}
		this.customSaveSettingType = customSaveSettingType;
		
		frame.setTitle("Custom Save Setting Definition");
		frame.setResizable(false);
		frame.setClosable(true);
		frame.setMaximizable(false);
		
		DynamicPanel elementsPanel = new DynamicPanel();
		
		elementsPanel.add(new JLabel("Name of custom save setting:"));
		elementsPanel.add(customSaveSettingNameField);
		
		elementsPanel.add(new JLabel("Definition of measurement base folder:"));
		ButtonGroup baseFolderGroup = new ButtonGroup();
		baseFolderGroup.add(baseFolderByUser);
		baseFolderGroup.add(baseFolderByDefinition);
		elementsPanel.add(baseFolderByUser);
		elementsPanel.add(baseFolderByDefinition);
		ActionListener folderGroupListener = new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent arg0) 
					{
						boolean byUser = baseFolderByUser.isSelected();
						baseFolderLabel.setVisible(!byUser);
						baseFolderPanel.setVisible(!byUser);
						frame.pack();
					}
				};
		baseFolderByDefinition.addActionListener(folderGroupListener);
		baseFolderByUser.addActionListener(folderGroupListener);
		
		String folderName = customSaveSettingType.getBaseFolder();
		if(folderName == null)
		{
			folderName = (String) client.getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER);
			baseFolderByUser.doClick();
		}
		else
			baseFolderByDefinition.doClick();
		baseFolderField.setText(folderName==null ? "" : folderName);
		
		elementsPanel.add(baseFolderLabel);
		baseFolderPanel.add(baseFolderField, BorderLayout.CENTER);
		if(client.isLocalServer())
		{
			JButton openFolderChooser = new JButton("Edit");
			openFolderChooser.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					JFileChooser fileChooser = new JFileChooser(baseFolderField.getText());
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fileChooser.showDialog(null, "Open");
					if(returnVal == JFileChooser.APPROVE_OPTION)
					{
						baseFolderField.setText(fileChooser.getSelectedFile().getAbsolutePath());
					}
				}
			});
			baseFolderPanel.add(openFolderChooser, BorderLayout.EAST);
		}
		elementsPanel.add(baseFolderPanel);
		
		String imageToolTip = getImageToolTip();
		String tableToolTip = getTableToolTip();
		String generalToolTip = getGeneralToolTip();
		
		
		elementsPanel.add(new JLabel("Measurement specific folder under base folder:"));
		baseFolderExtension.setRenderer(new ComboBoxExampleItemRenderer());
		baseFolderExtension.setEditable(true);
		baseFolderExtension.setSelectedItem(customSaveSettingType.getBaseFolderExtension());
		baseFolderExtension.setPreferredSize(tableFilePath.getPreferredSize());
		baseFolderExtension.setToolTipText(generalToolTip);
		elementsPanel.add(baseFolderExtension);
		
		elementsPanel.add(new JLabel("Path of images (with extension, typically .tif):"));
		imageFilePath.setRenderer(new ComboBoxExampleItemRenderer());
		imageFilePath.setEditable(true);
		imageFilePath.setSelectedItem(customSaveSettingType.getImageFilePath());
		imageFilePath.setPreferredSize(tableFilePath.getPreferredSize());
		imageFilePath.setToolTipText(imageToolTip);
		elementsPanel.add(imageFilePath);
		
		elementsPanel.add(new JLabel("Type of images (typically tif):"));
		// Get supported image types
		String[] imageTypes;
		try
		{
			imageTypes = server.getProperties().getSupportedImageFormats();
		}
		catch(RemoteException e1)
		{
			client.sendError("Could not obtain supported image file types from server.", e1);
			imageTypes = new String[0];
		}
		imageExtension = new JComboBox<String>(imageTypes);
		imageExtension.setSelectedItem(customSaveSettingType.getImageExtension());
		elementsPanel.add(imageExtension);
		
		elementsPanel.add(new JLabel("Path of tables (with extension, typically .csv):"));
		tableFilePath.setText(customSaveSettingType.getTableFilePath());
		tableFilePath.setToolTipText(tableToolTip);
		elementsPanel.add(tableFilePath);
		
		elementsPanel.add(new JLabel("Path of image metadata table (with extension, typically .csv):"));
		imageMetadataTableFilePath.setText(customSaveSettingType.getImageMetadataTableFilePath());
		imageMetadataTableFilePath.setToolTipText(generalToolTip);
		elementsPanel.add(imageMetadataTableFilePath);
		
		elementsPanel.add(new JLabel("Path of measurement configuration (with extension, typically .csb):"));
		measurementConfigurationFilePath.setText(customSaveSettingType.getMeasurementConfigurationFilePath());
		measurementConfigurationFilePath.setToolTipText(generalToolTip);
		elementsPanel.add(measurementConfigurationFilePath);
		
		elementsPanel.add(new JLabel("Path of microscope configuration (with extension, typically .cfg):"));
		microscopeConfigurationFilePath.setText(customSaveSettingType.getMicroscopeConfigurationFilePath());
		microscopeConfigurationFilePath.setToolTipText(generalToolTip);
		elementsPanel.add(microscopeConfigurationFilePath);
		
		elementsPanel.add(new JLabel("Path of XML file storing measurement metadata, channels, and initial microscope state (with extension, typically .xml):"));
		xmlInformationFilePath.setText(customSaveSettingType.getXMLInformationFilePath());
		xmlInformationFilePath.setToolTipText(generalToolTip);
		elementsPanel.add(xmlInformationFilePath);
		
		elementsPanel.add(new JLabel("Path of HTML file storing measurement metadata, channels, and initial microscope state (with extension, typically .html):"));
		htmlInformationFilePath.setText(customSaveSettingType.getHTMLInformationFilePath());
		htmlInformationFilePath.setToolTipText(generalToolTip);
		elementsPanel.add(htmlInformationFilePath);
		
		elementsPanel.add(new JLabel("Path of regular log file (with extension, typically .txt):"));
		logOutFilePath.setText(customSaveSettingType.getLogOutFilePath());
		logOutFilePath.setToolTipText(generalToolTip);
		elementsPanel.add(logOutFilePath);
		
		elementsPanel.add(new JLabel("Path of log file to store error information (with extension, typically .txt):"));
		logErrFilePath.setText(customSaveSettingType.getLogErrFilePath());
		logErrFilePath.setToolTipText(generalToolTip);
		elementsPanel.add(logErrFilePath);
		
        elementsPanel.addFillEmpty();
        
		JButton closeButton = new JButton("Save");
		closeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	saveSettings();
                }
            });
		
		customSaveSettingNameField.setText(customSaveSettingType.getSaveSettingName());
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(elementsPanel, BorderLayout.CENTER);
        contentPane.add(closeButton, BorderLayout.SOUTH);
        frame.setContentPane(contentPane);
        frame.pack();
	}
	private void saveSettings()
	{
		String typeName = customSaveSettingNameField.getText();
    	if(typeName.length()<3)
    	{
    		JOptionPane.showMessageDialog(null, "Save type name must be longer than three characters.", "Could not create save type", JOptionPane.INFORMATION_MESSAGE);
    		return;
    	}
    	customSaveSettingType.setSaveSettingName(typeName);
    	if(baseFolderByUser.isSelected())
    		customSaveSettingType.setBaseFolder(null);
    	else
    		customSaveSettingType.setBaseFolder(baseFolderField.getText());
    	
    	customSaveSettingType.setBaseFolderExtension(baseFolderExtension.getSelectedItem().toString());
		customSaveSettingType.setImageFilePath(imageFilePath.getSelectedItem().toString());
		customSaveSettingType.setTableFilePath(tableFilePath.getText());
		customSaveSettingType.setImageMetadataTableFilePath(imageMetadataTableFilePath.getText());
		customSaveSettingType.setMeasurementConfigurationFilePath(measurementConfigurationFilePath.getText());
		customSaveSettingType.setMicroscopeConfigurationFilePath(microscopeConfigurationFilePath.getText());
		customSaveSettingType.setXMLInformationFilePath(xmlInformationFilePath.getText());
		customSaveSettingType.setHTMLInformationFilePath(htmlInformationFilePath.getText());
		customSaveSettingType.setLogOutFilePath(logOutFilePath.getText());
		customSaveSettingType.setLogErrFilePath(logErrFilePath.getText());
		customSaveSettingType.setImageExtension((String) imageExtension.getSelectedItem());

    	// we first delete the old custom type. This is important if the name changed in the meantime
    	if(oldCustomSaveSettingName != null)
    		CustomSaveSettingManager.deleteCustomSaveSettingType(oldCustomSaveSettingName);
    	
    	try
		{
			CustomSaveSettingManager.saveCustomSaveSettingType(CustomSaveSettingDefinitionFrame.this.customSaveSettingType);
		}
		catch(CustomSaveSettingException e1)
		{
			CustomSaveSettingDefinitionFrame.this.client.sendError("Could not save custom save setting type.", e1);
			return;
		}
    	
        CustomSaveSettingDefinitionFrame.this.frame.setVisible(false); 
        for(ActionListener listener : listeners)
        {
        	listener.actionPerformed(new ActionEvent(this, 154, "Custom save setting type created or edited."));
        }
	}
	
	public void addActionListener(ActionListener listener)
	{
		listeners.add(listener);
	}
	public void removeActionListener(ActionListener listener)
	{
		listeners.remove(listener);
	}
}
