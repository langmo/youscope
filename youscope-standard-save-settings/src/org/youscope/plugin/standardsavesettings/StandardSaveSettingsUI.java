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
package org.youscope.plugin.standardsavesettings;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.common.saving.SaveSettings;

/**
 * @author Moritz Lang
 */
class StandardSaveSettingsUI extends ComponentAddonUIAdapter<StandardSaveSettingsConfiguration>
{
	private JTextField folderField = new JTextField();

	private JComboBox<String> imageTypeField;

	private JComboBox<FolderStructureType> imageFolderTypeField = new JComboBox<FolderStructureType>(FolderStructureType.values());
	
    /**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public StandardSaveSettingsUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	
	static ComponentMetadataAdapter<StandardSaveSettingsConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<StandardSaveSettingsConfiguration>(StandardSaveSettingsConfiguration.TYPE_IDENTIFIER, 
				StandardSaveSettingsConfiguration.class, 
				SaveSettings.class, "Standard Save Settings", new String[]{"save settings"});
	}
	
	@Override
	protected Component createUI(StandardSaveSettingsConfiguration configuration) throws AddonException
	{
		setTitle("Standard save settings");
		setResizable(false);
		setMaximizable(false);
		
		// Get supported image types
		String[] imageTypes;
		try
		{
			imageTypes = getServer().getProperties().getSupportedImageFormats();
		}
		catch(RemoteException e1)
		{
			sendErrorMessage("Could not obtain supported image file types from server.", e1);
			imageTypes = new String[0];
		}
		imageTypeField = new JComboBox<String>(imageTypes);
		imageTypeField.setToolTipText("All images made during the measurement will be saved under this file type. Note, that not all file types are compatible with all camera settings. Some file types might e.g. only support 8bit pixels, or similar. An error is thrown if the chosen file type is incompatible with the camera settings. Recommended file type is tif.");
		
		folderField.setText(configuration.getBaseFolder());
		imageFolderTypeField.setSelectedItem(configuration.getFolderStructureType());
		imageTypeField.setSelectedItem(configuration.getImageFileType());
		
		DynamicPanel contentPane = new DynamicPanel();
		contentPane.add(new JLabel("Output Directory:"));
		JPanel folderPanel = new JPanel(new BorderLayout(5, 0));
		folderPanel.add(folderField, BorderLayout.CENTER);

		if(getClient().isLocalServer())
		{
			JButton openFolderChooser = new JButton("Edit");
			openFolderChooser.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					JFileChooser fileChooser = new JFileChooser(folderField.getText());
					fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					int returnVal = fileChooser.showDialog(null, "Open");
					if(returnVal == JFileChooser.APPROVE_OPTION)
					{
						folderField.setText(fileChooser.getSelectedFile().getAbsolutePath());
					}
				}
			});
			folderPanel.add(openFolderChooser, BorderLayout.EAST);
		}
		contentPane.add(folderPanel);

		contentPane.add(new JLabel("Folder structure:"));
		contentPane.add(imageFolderTypeField);

		// Panel to choose image file type
		contentPane.add(new JLabel("Image File Type:"));
		contentPane.add(imageTypeField);
		
		
		return contentPane;
    }

    @Override
	protected void commitChanges(StandardSaveSettingsConfiguration configuration)
    {   
    	configuration.setBaseFolder(folderField.getText());
    	configuration.setImageFileType((String) imageTypeField.getSelectedItem());
    	configuration.setFolderStructureType((FolderStructureType) imageFolderTypeField.getSelectedItem());
    	
    	getClient().getPropertyProvider().setProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER, configuration.getBaseFolder());
    }

	@Override
	protected void initializeDefaultConfiguration(StandardSaveSettingsConfiguration configuration) throws AddonException 
	{
		String lastFolder = (String) getClient().getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER);
		configuration.setBaseFolder(lastFolder == null ? "" : lastFolder);		
		configuration.setFolderStructureType(FolderStructureType.ALL_IN_ONE_FOLDER);
		
		String imageType = (String) getClient().getPropertyProvider().getProperty(StandardProperty.PROPERTY_MEASUREMENT_STANDARD_IMAGE_FILE_TYPE);
		configuration.setImageFileType(imageType != null ? imageType : "tif");
	}
}
