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

import javax.swing.JButton;
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

/**
 * @author Moritz Lang
 */
class CustomSaveSettingConfigurationUI extends ComponentAddonUIAdapter<CustomSaveSettingConfiguration>
{
	private final JTextField folderField = new JTextField();
	private final CustomSaveSettingType type;
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @param type Type of the custom save setting.
	 * @throws AddonException 
	 */
	public CustomSaveSettingConfigurationUI(YouScopeClient client, YouScopeServer server, CustomSaveSettingType type) throws AddonException
	{
		super(getMetadata(type.getSaveSettingName()),  client, server);
		this.type =type;
	}
    
	static ComponentMetadataAdapter<CustomSaveSettingConfiguration> getMetadata(String name)
	{
		String typeIdentifier = CustomSaveSettingManager.getCustomSaveSettingTypeIdentifier(name);
		return new ComponentMetadataAdapter<CustomSaveSettingConfiguration>(typeIdentifier, 
				CustomSaveSettingConfiguration.class, 
				CustomSaveSetting.class, 
				name, 
				new String[]{"save settings"},
				"User defined setting of the directory structure and file and directory names in which YouScope saves measurement images and meta-data.",
				"icons/block-share.png");
	}
    
    @Override
	protected Component createUI(CustomSaveSettingConfiguration configuration) throws AddonException
	{
		setTitle(configuration.getCustomSaveSettingTypeName());
		setResizable(true);
		setMaximizable(false);
        
		DynamicPanel elementsPanel = new DynamicPanel();
		if(type.getBaseFolder() == null)
		{
			String folderName = configuration.getBaseFolder();
			if(folderName == null)
			{
				folderName = (String) getClient().getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER);
			}
			folderField.setText(folderName==null ? "" : folderName);
			elementsPanel.add(new JLabel("Measurement Base Folder:"));
			
			JPanel baseFolderPanel = new JPanel(new BorderLayout(5, 0));
			baseFolderPanel.add(folderField, BorderLayout.CENTER);
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
				baseFolderPanel.add(openFolderChooser, BorderLayout.EAST);
			}
			elementsPanel.add(baseFolderPanel);
		}
		
		return elementsPanel;
    }

	@Override
	protected void commitChanges(CustomSaveSettingConfiguration configuration) 
	{
		if(type.getBaseFolder() == null)
		{
			configuration.setBaseFolder(folderField.getText());
			getClient().getPropertyProvider().setProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER, folderField.getText());
		}
		else
			configuration.setBaseFolder(null);
		configuration.setCustomSaveSettingTypeName(type.getSaveSettingName());
	}

	@Override
	protected void initializeDefaultConfiguration(CustomSaveSettingConfiguration configuration) throws AddonException 
	{
		if(type.getBaseFolder() == null)
		{
			String lastFolder = (String) getClient().getPropertyProvider().getProperty(StandardProperty.PROPERTY_LAST_MEASUREMENT_SAVE_FOLDER);
			configuration.setBaseFolder(lastFolder == null ? "" : lastFolder);
		}
		configuration.setCustomSaveSettingTypeName(type.getSaveSettingName());
	}
}
