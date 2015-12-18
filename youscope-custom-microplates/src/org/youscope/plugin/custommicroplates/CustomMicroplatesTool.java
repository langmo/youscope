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

import org.youscope.addon.AddonException;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.measurement.microplate.Microplate;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.StandardFormats;

/**
 * @author langmo
 *
 */
class CustomMicroplatesTool extends ToolAddonUIAdapter implements ActionListener
{	
	private JList<Microplate> microplateTypesList;
	/**
	 * Constructor.
	 * @param client Interface to the YouScope client.
	 * @param server Interface to the YouScope server.
	 * @throws AddonException 
	 */
	public CustomMicroplatesTool(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	public final static String TYPE_IDENTIFIER = "YouScope.CustomMicroplates";
	
	static ToolMetadata getMetadata()
	{
		return new ToolMetadataAdapter(TYPE_IDENTIFIER, "Custom Microplates", null, "icons/table.png");
	}
	@Override
	public java.awt.Component createUI()
	{
		setMaximizable(false);
		setResizable(true);
		setTitle("Custom Microplates Configuration");
		setShowCloseButton(true);
		
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
                	YouScopeFrame newFrame = getContainingFrame().createModalChildFrame();
                	CustomMicroplatesConfigurationFrame configFrame = new CustomMicroplatesConfigurationFrame(getClient(), newFrame);
                	configFrame.addActionListener(CustomMicroplatesTool.this);
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
                	Microplate microplateType = microplateTypesList.getSelectedValue();
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
						sendErrorMessage("Could not delete microplate type definition.", e1);
						return;
					}
					CustomMicroplatesTool.this.actionPerformed(e);
                }
            });

        JButton editMicroplateTypeButton = new JButton("Edit Microplate Type", editButtonIcon);
        editMicroplateTypeButton.setHorizontalAlignment(SwingConstants.LEFT);
        editMicroplateTypeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	Microplate microplateType = microplateTypesList.getSelectedValue();
                    if (microplateType == null)
                        return;
               
                    YouScopeFrame newFrame = getContainingFrame().createModalChildFrame();
                    CustomMicroplatesConfigurationFrame configFrame = new CustomMicroplatesConfigurationFrame(getClient(), newFrame, microplateType);
                	configFrame.addActionListener(CustomMicroplatesTool.this);
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

        microplateTypesList = new JList<Microplate>();
        microplateTypesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        class MicroplateTypesCellRenderer extends JLabel implements ListCellRenderer<Microplate> 
		{
			/**
			 * Serial Version UID
			 */
			private static final long	serialVersionUID	= 239461111656492466L;

			@Override
			public Component getListCellRendererComponent(JList<? extends Microplate> list, Microplate value, int index, boolean isSelected, boolean cellHasFocus)
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
        
		refreshMicroplateTypesList();        
		
        // End initializing
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.add(new JLabel("Microplate Types:"), BorderLayout.NORTH);
		contentPane.add(shortcutListPane, BorderLayout.CENTER);
		contentPane.add(buttonPanel, BorderLayout.EAST);
		return contentPane;
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
