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
import java.util.Vector;

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
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.tool.ToolAddonUIAdapter;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.addon.tool.ToolMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.StandardFormats;

/**
 * @author langmo
 *
 */
class CustomMicroplatesTool extends ToolAddonUIAdapter implements ActionListener
{	
	private JList<ComponentMetadataAdapter<CustomMicroplateConfiguration>> microplateTypesList;
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
                	CustomMicroplatesConfigurationPanel configPanel = new CustomMicroplatesConfigurationPanel(getClient(), getServer(), newFrame);
                	configPanel.addActionListener(CustomMicroplatesTool.this);
                	newFrame.setTitle("New Custom Microplate");
                	newFrame.setContentPane(configPanel);
                	newFrame.pack();
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
                	ComponentMetadataAdapter<CustomMicroplateConfiguration> microplateType = microplateTypesList.getSelectedValue();
                    if (microplateType == null)
                        return;
                    int shouldDelete = JOptionPane.showConfirmDialog(null, "Should the microplate type \"" + microplateType.getTypeName() + "\" really be deleted?", "Delete Microplate", JOptionPane.YES_NO_OPTION);
					if(shouldDelete != JOptionPane.YES_OPTION)
						return;
					if(!CustomMicroplateManager.deleteCustomMicroplate(microplateType.getTypeIdentifier()))
					{
						sendErrorMessage("Could not delete microplate type definition.", null);
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
                	ComponentMetadataAdapter<CustomMicroplateConfiguration> microplateType = microplateTypesList.getSelectedValue();
                    if (microplateType == null)
                        return;
                    CustomMicroplateDefinition microplateDefinition;
					try {
						microplateDefinition = CustomMicroplateManager.getCustomMicroplate(microplateType.getTypeIdentifier());
					} catch (CustomMicroplateException e1) {
						sendErrorMessage("Could not load custom microplate definition.", e1);
						return;
					}
                    YouScopeFrame newFrame = getContainingFrame().createModalChildFrame();
                	CustomMicroplatesConfigurationPanel configPanel = new CustomMicroplatesConfigurationPanel(getClient(), getServer(), newFrame, microplateDefinition);
                	configPanel.addActionListener(CustomMicroplatesTool.this);
                	newFrame.setTitle("New Custom Microplate");
                	newFrame.setContentPane(configPanel);
                	newFrame.pack();
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

        microplateTypesList = new JList<ComponentMetadataAdapter<CustomMicroplateConfiguration>>();
        microplateTypesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        class MicroplateTypesCellRenderer extends JLabel implements ListCellRenderer<ComponentMetadataAdapter<CustomMicroplateConfiguration>> 
		{
			/**
			 * Serial Version UID
			 */
			private static final long	serialVersionUID	= 239461111656492466L;

			@Override
			public Component getListCellRendererComponent(JList<? extends ComponentMetadataAdapter<CustomMicroplateConfiguration>> list, ComponentMetadataAdapter<CustomMicroplateConfiguration> value, int index, boolean isSelected, boolean cellHasFocus)
		    {
				String text = value.getTypeName();
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
		String identifiers[] = CustomMicroplateManager.getCustomMicroplateTypeIdentifiers();
		Vector<ComponentMetadataAdapter<CustomMicroplateConfiguration>> metadata = new Vector<>(identifiers.length);
		for(String identifier : identifiers)
		{
			metadata.addElement(CustomMicroplateManager.getMetadata(identifier));
		}
		microplateTypesList.setListData(metadata);
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		refreshMicroplateTypesList();	
	}
}
