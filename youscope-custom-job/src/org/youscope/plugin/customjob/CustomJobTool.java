/**
 * 
 */
package org.youscope.plugin.customjob;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
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
class CustomJobTool extends ToolAddonUIAdapter implements ActionListener
{
	private JList<CustomJobConfiguration> customJobsList;
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
	public final static String TYPE_IDENTIFIER = "CSB::YouScopeCustomJob";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Custom Job Configuration", null, "icons/block-share.png");
	}
	@Override
	public java.awt.Component createUI() throws AddonException
	{
		setMaximizable(false);
		setResizable(true);
		setTitle("Custom Job Templates");
		setCloseButton(true);
		
		ImageIcon addButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add");
        ImageIcon deleteButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Delete");
        ImageIcon editButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--pencil.png", "Edit");
        
        JButton newCustomJobButton = new JButton("New Custom Job", addButtonIcon);
        newCustomJobButton.setHorizontalAlignment(SwingConstants.LEFT);
        newCustomJobButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	YouScopeFrame newFrame = getContainingFrame().createModalChildFrame();
                	CustomJobDefinitionFrame configFrame = new CustomJobDefinitionFrame(getClient(), getServer(), newFrame);
                	configFrame.addActionListener(CustomJobTool.this);
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
                	CustomJobConfiguration customJob = customJobsList.getSelectedValue();
                    if (customJob == null)
                        return;
                    int shouldDelete = JOptionPane.showConfirmDialog(null, "Should the custom job template \"" + customJob.getCustomJobName() + "\" really be deleted?", "Delete Shortcut", JOptionPane.YES_NO_OPTION);
					if(shouldDelete != JOptionPane.YES_OPTION)
						return;
					try
					{
						CustomJobManager.deleteCustomJob(customJob);
					}
					catch(CustomJobException e1)
					{
						sendErrorMessage("Could not delete custom job template.", e1);
						return;
					}
						
					CustomJobTool.this.actionPerformed(e);
                }
            });

        JButton editCustomJobButton = new JButton("Edit Custom Job", editButtonIcon);
        editCustomJobButton.setHorizontalAlignment(SwingConstants.LEFT);
        editCustomJobButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	CustomJobConfiguration customJob = customJobsList.getSelectedValue();
                    if (customJob == null)
                        return;
               
                    YouScopeFrame newFrame = getContainingFrame().createModalChildFrame();
                    CustomJobDefinitionFrame configFrame = new CustomJobDefinitionFrame(getClient(), getServer(), newFrame, customJob);
                	configFrame.addActionListener(CustomJobTool.this);
                    newFrame.setVisible(true);
                }
            });

        // add Button Panel
        DynamicPanel buttonPanel = new DynamicPanel();
        buttonPanel.add(newCustomJobButton);
        buttonPanel.add(editCustomJobButton);
        buttonPanel.add(deleteCustomJobButton);
        buttonPanel.addFillEmpty();

        customJobsList = new JList<CustomJobConfiguration>();
        customJobsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        class CustomJobCellRenderer extends JLabel implements ListCellRenderer<CustomJobConfiguration> 
		{
			/**
			 * Serial Version UID
			 */
			private static final long	serialVersionUID	= 239462111656492466L;

			@Override
			public Component getListCellRendererComponent(JList<? extends CustomJobConfiguration> list, CustomJobConfiguration value, int index, boolean isSelected, boolean cellHasFocus)
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
		
		try
		{
			refreshCustomJobsList();
		}
		catch(CustomJobException e1)
		{
			throw new AddonException("Custom jobs loading failed.", e1);
		}        
		
        // End initializing
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(new JLabel("Custom Job Templates:"), BorderLayout.NORTH);
		contentPane.add(customJobsListPane, BorderLayout.CENTER);
		contentPane.add(buttonPanel, BorderLayout.EAST);
		return contentPane;
			
	}
	
	private void refreshCustomJobsList() throws CustomJobException
	{
		 customJobsList.setListData(CustomJobManager.loadCustomJobs());
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		try
		{
			refreshCustomJobsList();
		}
		catch(CustomJobException e)
		{
			sendErrorMessage("Custom jobs loading failed.", e);
		}	
	}
}
