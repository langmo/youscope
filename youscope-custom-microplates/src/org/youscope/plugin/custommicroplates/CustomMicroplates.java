/**
 * 
 */
package org.youscope.plugin.custommicroplates;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

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

import org.youscope.addon.tool.ToolAddon;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.MicroplateType;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.StandardFormats;

/**
 * @author langmo
 *
 */
class CustomMicroplates implements ToolAddon, ActionListener
{
	protected YouScopeServer server;
	protected YouScopeClient client;
	protected YouScopeFrame						frame;	
	
	protected JList<MicroplateType> microplateTypesList;
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 */
	public CustomMicroplates(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
	@Override
	public void createUI(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setClosable(true);
		frame.setMaximizable(false);
		frame.setResizable(true);
		frame.setTitle("Custom Microplates Configuration");
		
		frame.startInitializing();
		(new Thread(new FrameInitializer())).start();
	}
	protected class FrameInitializer implements Runnable
	{
		@Override
		public void run()
		{
			// Grid Bag Layouts
			GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
			GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();
			
	        ImageIcon addButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add");
	        ImageIcon deleteButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Delete");
	        ImageIcon editButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--pencil.png", "Edit");
	        
	        JButton newMicroplateTypeButton = new JButton("New Microplate Type", addButtonIcon);
	        newMicroplateTypeButton.setHorizontalAlignment(SwingConstants.LEFT);
	        newMicroplateTypeButton.addActionListener(new ActionListener()
	            {
	                @Override
	                public void actionPerformed(ActionEvent e)
	                {
	                	YouScopeFrame newFrame = CustomMicroplates.this.frame.createModalChildFrame();
	                	CustomMicroplatesConfigurationFrame configFrame = new CustomMicroplatesConfigurationFrame(client, newFrame);
	                	configFrame.addActionListener(CustomMicroplates.this);
	                	newFrame.setVisible(true);
	                }
	            });

	        JButton deleteMicroplateTypeButton = new JButton("Delete Microplate Type", deleteButtonIcon);
	        deleteMicroplateTypeButton.setHorizontalAlignment(SwingConstants.LEFT);
	        deleteMicroplateTypeButton.addActionListener(new ActionListener()
	            {
	                @Override
	                public void actionPerformed(ActionEvent e)
	                {
	                	MicroplateType microplateType = microplateTypesList.getSelectedValue();
	                    if (microplateType == null)
	                        return;
	                    int shouldDelete = JOptionPane.showConfirmDialog(null, "Should the microplate type \"" + microplateType.getMicroplateName() + "\" really be deleted?", "Delete Shortcut", JOptionPane.YES_NO_OPTION);
						if(shouldDelete != JOptionPane.YES_OPTION)
							return;
						try
						{
							CustomMicroplatesManager.deleteMicroplateTypeDefinition(microplateType);
						}
						catch(FileNotFoundException e1)
						{
							client.sendError("Could not delete microplate type definition.", e1);
							return;
						}
						CustomMicroplates.this.actionPerformed(e);
	                }
	            });

	        JButton editMicroplateTypeButton = new JButton("Edit Microplate Type", editButtonIcon);
	        editMicroplateTypeButton.setHorizontalAlignment(SwingConstants.LEFT);
	        editMicroplateTypeButton.addActionListener(new ActionListener()
	            {
	                @Override
	                public void actionPerformed(ActionEvent e)
	                {
	                	MicroplateType microplateType = microplateTypesList.getSelectedValue();
	                    if (microplateType == null)
	                        return;
	               
	                    YouScopeFrame newFrame = CustomMicroplates.this.frame.createModalChildFrame();
	                    CustomMicroplatesConfigurationFrame configFrame = new CustomMicroplatesConfigurationFrame(client, newFrame, microplateType);
	                	configFrame.addActionListener(CustomMicroplates.this);
	                    newFrame.setVisible(true);
	                }
	            });

	        // add Button Panel
	        GridBagLayout elementsLayout = new GridBagLayout();
	        JPanel buttonPanel = new JPanel(elementsLayout);
	        StandardFormats.addGridBagElement(newMicroplateTypeButton, elementsLayout, newLineConstr, buttonPanel);
	        StandardFormats.addGridBagElement(editMicroplateTypeButton, elementsLayout, newLineConstr, buttonPanel);
	        StandardFormats.addGridBagElement(deleteMicroplateTypeButton, elementsLayout, newLineConstr, buttonPanel);
	        StandardFormats.addGridBagElement(new JPanel(), elementsLayout, bottomConstr, buttonPanel);

	        microplateTypesList = new JList<MicroplateType>();
	        microplateTypesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	        class MicroplateTypesCellRenderer extends JLabel implements ListCellRenderer<MicroplateType> 
			{
				/**
				 * Serial Version UID
				 */
				private static final long	serialVersionUID	= 239461111656492466L;

				@Override
				public Component getListCellRendererComponent(JList<? extends MicroplateType> list, MicroplateType value, int index, boolean isSelected, boolean cellHasFocus)
			    {
					String text = value.getMicroplateName();
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
	        microplateTypesList.setCellRenderer(new MicroplateTypesCellRenderer());

	        JScrollPane shortcutListPane = new JScrollPane(microplateTypesList);
	        shortcutListPane.setPreferredSize(new Dimension(250, 150));
	        shortcutListPane.setMinimumSize(new Dimension(10, 10));
	        
	        JButton closeButton = new JButton("Close");
			closeButton.addActionListener(new ActionListener()
	            {
	                @Override
	                public void actionPerformed(ActionEvent e)
	                {
	                	CustomMicroplates.this.frame.setVisible(false);
	                }
	            });
			
			refreshMicroplateTypesList();        
			
	        // End initializing
			JPanel contentPane = new JPanel(new BorderLayout());
			contentPane.add(new JLabel("Microplate Types:"), BorderLayout.NORTH);
			contentPane.add(shortcutListPane, BorderLayout.CENTER);
			contentPane.add(closeButton, BorderLayout.SOUTH);
			contentPane.add(buttonPanel, BorderLayout.EAST);
			frame.setContentPane(contentPane);
			
			frame.pack();
			frame.endLoading();
		}
	}
	
	private void refreshMicroplateTypesList()
	{
		 microplateTypesList.setListData(CustomMicroplatesManager.getMicroplateTypes());
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		refreshMicroplateTypesList();	
	}
}
