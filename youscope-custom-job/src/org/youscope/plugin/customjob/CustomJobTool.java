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
package org.youscope.plugin.customjob;

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
class CustomJobTool extends ToolAddonUIAdapter
{
	private static class CustomJobHolder
	{
		private final String typeIdentifier;
		CustomJobHolder(String typeIdentifier)
		{
			this.typeIdentifier = typeIdentifier;
		}
		public String getCustomJobName()
		{
			return CustomJobManager.getCustomJobName(typeIdentifier);
		}
		@Override
		public String toString()
		{
			return  getCustomJobName();
		}
		String getTypeIdentifier()
		{
			return typeIdentifier;
		}
	}
	
	private JList<CustomJobHolder> customJobsList;
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 * @throws AddonException 
	 */
	public CustomJobTool(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	public final static String TYPE_IDENTIFIER = "YouScope.YouScopeCustomJob";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Custom Jobs", null, "A tool to define custom jobs, that is, jobs consisting of a certain list of child-jobs. All defined custom jobs are displayed in the configuration like regular jobs, such that groups of related jobs which are often defined can be quickly inserted into a measurement.", "icons/block-share.png");
	}
	@Override
	public java.awt.Component createUI() throws AddonException
	{
		setMaximizable(false);
		setResizable(true);
		setTitle("Custom Job Templates");
		setShowCloseButton(true);
		
		Icon addButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add");
		Icon deleteButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Delete");
		Icon editButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--pencil.png", "Edit");
        
        JButton newCustomJobButton = new JButton("New Custom Job", addButtonIcon);
        newCustomJobButton.setHorizontalAlignment(SwingConstants.LEFT);
        newCustomJobButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	YouScopeFrame newFrame = getContainingFrame().createModalChildFrame();
                	CustomJobDefinitionFrame configFrame = new CustomJobDefinitionFrame(getClient(), getServer(), newFrame);
                	configFrame.addActionListener(new ActionListener() 
                	{
						@Override
						public void actionPerformed(ActionEvent e) {
							refreshCustomJobsList();
						}
					});
                	newFrame.setVisible(true);
                }
            });

        JButton deleteCustomJobButton = new JButton("Delete Custom Job", deleteButtonIcon);
        deleteCustomJobButton.setHorizontalAlignment(SwingConstants.LEFT);
        deleteCustomJobButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	CustomJobHolder customJob = customJobsList.getSelectedValue();
                    if (customJob == null)
                        return;
                    int shouldDelete = JOptionPane.showConfirmDialog(null, "Should the custom job template \"" + customJob.getCustomJobName() + "\" really be deleted?", "Delete Shortcut", JOptionPane.YES_NO_OPTION);
					if(shouldDelete != JOptionPane.YES_OPTION)
						return;
					CustomJobManager.deleteCustomJob(customJob.getTypeIdentifier());
					refreshCustomJobsList();
                }
            });

        JButton editCustomJobButton = new JButton("Edit Custom Job", editButtonIcon);
        editCustomJobButton.setHorizontalAlignment(SwingConstants.LEFT);
        editCustomJobButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	CustomJobHolder customJob = customJobsList.getSelectedValue();
                    if (customJob == null)
                        return;
                    CustomJobConfiguration configuration;
					try {
						configuration = CustomJobManager.getCustomJob(customJob.getTypeIdentifier());
					} catch (CustomJobException e1) {
						sendErrorMessage("Could not load custom job with type identifier "+customJob.getTypeIdentifier()+".", e1);
						return;
					}
                    
                    YouScopeFrame newFrame = getContainingFrame().createModalChildFrame();
                    CustomJobDefinitionFrame configFrame = new CustomJobDefinitionFrame(getClient(), getServer(), newFrame, configuration);
                	configFrame.addActionListener(new ActionListener() 
                	{
						@Override
						public void actionPerformed(ActionEvent e) 
						{
							refreshCustomJobsList();
						}
					});
                    newFrame.setVisible(true);
                }
            });
        
        JButton copyCustomJobButton = new JButton("Copy Custom Job", editButtonIcon);
        copyCustomJobButton.setHorizontalAlignment(SwingConstants.LEFT);
        copyCustomJobButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	CustomJobHolder customJob = customJobsList.getSelectedValue();
                    if (customJob == null)
                        return;
                    CustomJobConfiguration configuration;
					try {
						configuration = CustomJobManager.getCustomJob(customJob.getTypeIdentifier());
					} catch (CustomJobException e1) {
						sendErrorMessage("Could not load custom job with type identifier "+customJob.getTypeIdentifier()+".", e1);
						return;
					}
					configuration.setCustomJobName("Copy of "+configuration.getCustomJobName());
                    try {
						CustomJobManager.saveCustomJob(configuration);
					} catch (CustomJobException e1) {
						sendErrorMessage("Could not save custom job with type identifier "+configuration.getTypeIdentifier()+".", e1);
						return;
					}
                    refreshCustomJobsList();
                }
            });

        // add Button Panel
        DynamicPanel buttonPanel = new DynamicPanel();
        buttonPanel.add(newCustomJobButton);
        buttonPanel.add(editCustomJobButton);
        buttonPanel.add(deleteCustomJobButton);
        buttonPanel.add(copyCustomJobButton);
        buttonPanel.addFillEmpty();

        customJobsList = new JList<CustomJobHolder>();
        customJobsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        class CustomJobCellRenderer extends JLabel implements ListCellRenderer<CustomJobHolder> 
		{
			/**
			 * Serial Version UID
			 */
			private static final long	serialVersionUID	= 239462111656492466L;

			@Override
			public Component getListCellRendererComponent(JList<? extends CustomJobHolder> list, CustomJobHolder value, int index, boolean isSelected, boolean cellHasFocus)
		    {
				String text = value.getCustomJobName();
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
        customJobsList.setCellRenderer(new CustomJobCellRenderer());

        JScrollPane customJobsListPane = new JScrollPane(customJobsList);
        customJobsListPane.setPreferredSize(new Dimension(250, 150));
        customJobsListPane.setMinimumSize(new Dimension(10, 10));
		
		refreshCustomJobsList();      
		
        // End initializing
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(new JLabel("Custom Job Templates:"), BorderLayout.NORTH);
		contentPane.add(customJobsListPane, BorderLayout.CENTER);
		contentPane.add(buttonPanel, BorderLayout.EAST);
		return contentPane;
			
	}
	
	private void refreshCustomJobsList()
	{
		String[] customJobTypeIdentifiers = CustomJobManager.getCustomJobTypeIdentifiers();
		CustomJobHolder[] customJobs = new CustomJobHolder[customJobTypeIdentifiers.length];
		for(int i=0; i<customJobTypeIdentifiers.length; i++)
		{
			customJobs[i] = new CustomJobHolder(customJobTypeIdentifiers[i]);
		}
		 customJobsList.setListData(customJobs);
	}
}
