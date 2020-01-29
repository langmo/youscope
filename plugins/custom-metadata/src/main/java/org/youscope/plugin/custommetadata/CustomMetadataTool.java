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
package org.youscope.plugin.custommetadata;

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
import org.youscope.clientinterfaces.MetadataDefinition;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeClientException;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.ImageLoadingTools;

/**
 * @author Moritz Lang
 *
 */
class CustomMetadataTool extends ToolAddonUIAdapter 
{	
	private JList<MetadataDefinition> metadataList;
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 * @throws AddonException 
	 */
	public CustomMetadataTool(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	public final static String TYPE_IDENTIFIER = "YouScope.CustomMetadata";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Custom Metadata", null, "A tool to define custom metadata entries, that is, pre-defined metadata for a measurement.", "icons/clipboard-list.png");
	}
	@Override
	public java.awt.Component createUI() throws AddonException
	{
		setMaximizable(false);
		setResizable(true);
		setTitle("Custom Metadata");
		setShowCloseButton(true);
		
		Icon addButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add");
		Icon deleteButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Delete");
		Icon editButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--pencil.png", "Edit");
        
        JButton newButton = new JButton("New Metadata", addButtonIcon);
        newButton.setHorizontalAlignment(SwingConstants.LEFT);
        newButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	YouScopeFrame newFrame = getContainingFrame().createModalChildFrame();
                	CustomMetadataDefinitionFrame configFrame = new CustomMetadataDefinitionFrame(getClient(), newFrame);
                	configFrame.addActionListener(new ActionListener() 
                	{
						@Override
						public void actionPerformed(ActionEvent e) {
							refreshMetadataList();
						}
					});
                	newFrame.setVisible(true);
                }
            });

        JButton deleteButton = new JButton("Delete Metadata", deleteButtonIcon);
        deleteButton.setHorizontalAlignment(SwingConstants.LEFT);
        deleteButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	MetadataDefinition metadataDefinition = metadataList.getSelectedValue();
                    if (metadataDefinition == null)
                        return;
                    int shouldDelete = JOptionPane.showConfirmDialog(null, "Should the metadata definition \"" + metadataDefinition.getName() + "\" really be deleted?", "Delete Metadata", JOptionPane.YES_NO_OPTION);
					if(shouldDelete != JOptionPane.YES_OPTION)
						return;
					try {
						getClient().getMeasurementMetadataProvider().deleteMetadataDefinition(metadataDefinition.getName());
					} catch (YouScopeClientException e1) {
						sendErrorMessage("Could not delete "+metadataDefinition.getName(), e1);
						return;
					}
					refreshMetadataList();
                }
            });

        JButton editButton = new JButton("Edit Metadata", editButtonIcon);
        editButton.setHorizontalAlignment(SwingConstants.LEFT);
        editButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	MetadataDefinition metadataDefinition = metadataList.getSelectedValue();
                    if (metadataDefinition == null)
                        return;
                    
                    YouScopeFrame newFrame = getContainingFrame().createModalChildFrame();
                    CustomMetadataDefinitionFrame configFrame = new CustomMetadataDefinitionFrame(getClient(), newFrame, metadataDefinition);
                	configFrame.addActionListener(new ActionListener() 
                	{
						@Override
						public void actionPerformed(ActionEvent e) 
						{
							refreshMetadataList();
						}
					});
                    newFrame.setVisible(true);
                }
            });

        // add Button Panel
        DynamicPanel buttonPanel = new DynamicPanel();
        buttonPanel.add(newButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.addFillEmpty();

        metadataList = new JList<MetadataDefinition>();
        metadataList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        class MetadataListCellRenderer extends JLabel implements ListCellRenderer<MetadataDefinition> 
		{
			/**
			 * Serial Version UID
			 */
			private static final long	serialVersionUID	= 239462111656492466L;

			@Override
			public Component getListCellRendererComponent(JList<? extends MetadataDefinition> list, MetadataDefinition value, int index, boolean isSelected, boolean cellHasFocus)
		    {
				String text = value.getName();
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
        metadataList.setCellRenderer(new MetadataListCellRenderer());

        JScrollPane metadataListPane = new JScrollPane(metadataList);
        metadataListPane.setPreferredSize(new Dimension(250, 150));
        metadataListPane.setMinimumSize(new Dimension(100, 100));
		
		refreshMetadataList();      
		
        // End initializing
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(new JLabel("Defined Measurement Metadata:"), BorderLayout.NORTH);
		contentPane.add(metadataListPane, BorderLayout.CENTER);
		contentPane.add(buttonPanel, BorderLayout.EAST);
		return contentPane;
			
	}
	
	private void refreshMetadataList()
	{	
		metadataList.setListData(getClient().getMeasurementMetadataProvider().getMetadataDefinitions().toArray(new MetadataDefinition[0]));
	}
}
