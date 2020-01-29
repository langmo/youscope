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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImageLoadingTools;

/**
 * @author Moritz Lang
 *
 */
class CustomSaveSettingTool extends ToolAddonUIAdapter
{
	private static class CustomSaveSettingHolder
	{
		private final String name;
		CustomSaveSettingHolder(String name)
		{
			this.name = name;
		}
		public String getCustomSaveSettingName()
		{
			return name;
		}
		@Override
		public String toString()
		{
			return  getCustomSaveSettingName();
		}
	}
	
	private JList<CustomSaveSettingHolder> customSaveSettingList;
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 * @throws AddonException 
	 */
	public CustomSaveSettingTool(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	public final static String TYPE_IDENTIFIER = "YouScope.YouScopeCustomSaveSetting";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Custom Save Settings", null, 
				"A tool to define the setup of the directory structure and file and directory names in which YouScope saves measurement images and meta-data.",
				"icons/block-share.png");
	}
	@Override
	public java.awt.Component createUI() throws AddonException
	{
		setMaximizable(false);
		setResizable(true);
		setTitle("Custom Save Settings");
		setShowCloseButton(true);
		
		Icon addButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add");
		Icon deleteButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Delete");
		Icon editButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--pencil.png", "Edit");
        
        JButton newButton = new JButton("New Custom Save Setting", addButtonIcon);
        newButton.setHorizontalAlignment(SwingConstants.LEFT);
        newButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	YouScopeFrame newFrame = getContainingFrame().createModalChildFrame();
                	CustomSaveSettingDefinitionFrame configFrame = new CustomSaveSettingDefinitionFrame(getClient(), getServer(), newFrame);
                	configFrame.addActionListener(new ActionListener() 
                	{
						@Override
						public void actionPerformed(ActionEvent e) {
							refreshCustomSaveSettingList();
						}
					});
                	newFrame.setVisible(true);
                }
            });

        JButton deleteButton = new JButton("Delete Custom Save Setting", deleteButtonIcon);
        deleteButton.setHorizontalAlignment(SwingConstants.LEFT);
        deleteButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	CustomSaveSettingHolder customSaveSetting = customSaveSettingList.getSelectedValue();
                    if (customSaveSetting == null)
                        return;
                    int shouldDelete = JOptionPane.showConfirmDialog(null, "Should the custom save setting \"" + customSaveSetting.getCustomSaveSettingName() + "\" really be deleted?", "Delete Save Setting", JOptionPane.YES_NO_OPTION);
					if(shouldDelete != JOptionPane.YES_OPTION)
						return;
					CustomSaveSettingManager.deleteCustomSaveSettingType(customSaveSetting.getCustomSaveSettingName());
					refreshCustomSaveSettingList();
                }
            });

        JButton editButton = new JButton("Edit Custom Save Setting", editButtonIcon);
        editButton.setHorizontalAlignment(SwingConstants.LEFT);
        editButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	CustomSaveSettingHolder customSaveSetting = customSaveSettingList.getSelectedValue();
                    if (customSaveSetting == null)
                        return;
                    CustomSaveSettingType configuration;
					try {
						configuration = CustomSaveSettingManager.getCustomSaveSettingType(customSaveSetting.getCustomSaveSettingName());
					} catch (CustomSaveSettingException e1) {
						sendErrorMessage("Could not load custom save setting type with type identifier "+customSaveSetting.getCustomSaveSettingName()+".", e1);
						return;
					}
                    
                    YouScopeFrame newFrame = getContainingFrame().createModalChildFrame();
                    CustomSaveSettingDefinitionFrame configFrame = new CustomSaveSettingDefinitionFrame(getClient(), getServer(), newFrame, configuration);
                	configFrame.addActionListener(new ActionListener() 
                	{
						@Override
						public void actionPerformed(ActionEvent e) 
						{
							refreshCustomSaveSettingList();
						}
					});
                    newFrame.setVisible(true);
                }
            });
        
        JButton copyButton = new JButton("Copy Custom Save Setting", editButtonIcon);
        copyButton.setHorizontalAlignment(SwingConstants.LEFT);
        copyButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	CustomSaveSettingHolder customSaveSetting = customSaveSettingList.getSelectedValue();
                    if (customSaveSetting == null)
                        return;
                    CustomSaveSettingType configuration;
					try {
						configuration = CustomSaveSettingManager.getCustomSaveSettingType(customSaveSetting.getCustomSaveSettingName());
					} catch (CustomSaveSettingException e1) {
						sendErrorMessage("Could not load custom save setting type with name "+customSaveSetting.getCustomSaveSettingName()+".", e1);
						return;
					}
					configuration.setSaveSettingName(configuration.getSaveSettingName()+"_copy");
                    try {
						CustomSaveSettingManager.saveCustomSaveSettingType(configuration);
					} catch (CustomSaveSettingException e1) {
						sendErrorMessage("Could not save custom save setting with type identifier "+configuration.getSaveSettingName()+".", e1);
						return;
					}
                    refreshCustomSaveSettingList();
                }
            });

        // add Button Panel
        DynamicPanel buttonPanel = new DynamicPanel();
        buttonPanel.add(newButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(copyButton);
        buttonPanel.addFillEmpty();

        customSaveSettingList = new JList<CustomSaveSettingHolder>();
        customSaveSettingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        class CustomJobCellRenderer extends JLabel implements ListCellRenderer<CustomSaveSettingHolder> 
		{
			/**
			 * Serial Version UID
			 */
			private static final long	serialVersionUID	= 239462111656492466L;

			@Override
			public Component getListCellRendererComponent(JList<? extends CustomSaveSettingHolder> list, CustomSaveSettingHolder value, int index, boolean isSelected, boolean cellHasFocus)
		    {
				String text = value.getCustomSaveSettingName();
		        setText(text);
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
		        setEnabled(list.isEnabled());
		        setFont(list.getFont());
		        setOpaque(true);
		        return this;
		     }
		}
        customSaveSettingList.setCellRenderer(new CustomJobCellRenderer());

        JScrollPane customSaveSettingListPane = new JScrollPane(customSaveSettingList);
        customSaveSettingListPane.setPreferredSize(new Dimension(250, 150));
        customSaveSettingListPane.setMinimumSize(new Dimension(10, 10));
		
		refreshCustomSaveSettingList();      
		
        // End initializing
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(new JLabel("Custom Save Settings:"), BorderLayout.NORTH);
		contentPane.add(customSaveSettingListPane, BorderLayout.CENTER);
		contentPane.add(buttonPanel, BorderLayout.EAST);
		return contentPane;
			
	}
	
	private void refreshCustomSaveSettingList()
	{
		String[] customSaveSettingTypeIdentifiers = CustomSaveSettingManager.getCustomSaveSettingNames();
		CustomSaveSettingHolder[] customSaveSettings = new CustomSaveSettingHolder[customSaveSettingTypeIdentifiers.length];
		for(int i=0; i<customSaveSettingTypeIdentifiers.length; i++)
		{
			customSaveSettings[i] = new CustomSaveSettingHolder(customSaveSettingTypeIdentifiers[i]);
		}
		 customSaveSettingList.setListData(customSaveSettings);
	}
}
