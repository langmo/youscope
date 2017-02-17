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
package org.youscope.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.uielements.ImageLoadingTools;
import org.youscope.uielements.StandardFormats;

/**
 * @author langmo
 *
 */
class ScriptDefinitionManagerFrame implements ActionListener
{
	protected YouScopeFrame									frame;
	    
	protected JList<ScriptDefinition> scriptList;
	
	private Vector<ActionListener> listeners = new Vector<ActionListener>();
	
	ScriptDefinitionManagerFrame(YouScopeFrame frame)
	{
		this.frame = frame;
		frame.setTitle("Script Shortcut Manager");
		frame.setResizable(true);
		frame.setClosable(true);
		frame.setMaximizable(false);
		
		Icon addButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--plus.png", "Add");
		Icon deleteButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--minus.png", "Delete");
		Icon editButtonIcon = ImageLoadingTools.getResourceIcon("icons/block--pencil.png", "Edit");
        
        JButton newShortcutButton = new JButton("New Shortcut", addButtonIcon);
        newShortcutButton.setHorizontalAlignment(SwingConstants.LEFT);
        newShortcutButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	YouScopeFrame newFrame = ScriptDefinitionManagerFrame.this.frame.createModalChildFrame();
                	ScriptDefinitionConfigurationFrame configFrame = new ScriptDefinitionConfigurationFrame(newFrame);
                	configFrame.addActionListener(ScriptDefinitionManagerFrame.this);
                	newFrame.setVisible(true);
                }
            });

        JButton deleteShortcutButton = new JButton("Delete Shortcut", deleteButtonIcon);
        deleteShortcutButton.setHorizontalAlignment(SwingConstants.LEFT);
        deleteShortcutButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	ScriptDefinition scriptDefinition = scriptList.getSelectedValue();
                    if (scriptDefinition == null)
                        return;
                    int shouldDelete = JOptionPane.showConfirmDialog(null, "Should the script shortcut (" + scriptDefinition.getName() + ") really be deleted?", "Delete Shortcut", JOptionPane.YES_NO_OPTION);
					if(shouldDelete != JOptionPane.YES_OPTION)
						return;
					ScriptDefinitionManager.deleteScriptDefinition(scriptDefinition);
					ScriptDefinitionManagerFrame.this.actionPerformed(e);
                }
            });

        JButton editShortcutButton = new JButton("Edit Shortcut", editButtonIcon);
        editShortcutButton.setHorizontalAlignment(SwingConstants.LEFT);
        editShortcutButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    ScriptDefinition scriptDefinition = scriptList.getSelectedValue();
                    if (scriptDefinition == null)
                        return;
               
                    YouScopeFrame newFrame = ScriptDefinitionManagerFrame.this.frame.createModalChildFrame();
                    ScriptDefinitionConfigurationFrame configFrame = new ScriptDefinitionConfigurationFrame(newFrame, scriptDefinition);
                	configFrame.addActionListener(ScriptDefinitionManagerFrame.this);
                    newFrame.setVisible(true);
                }
            });

        // add Button Panel
        GridBagLayout elementsLayout = new GridBagLayout();
        GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
        GridBagConstraints bottomConstr = StandardFormats.getBottomContstraint();

        JPanel buttonPanel = new JPanel(elementsLayout);
        StandardFormats.addGridBagElement(newShortcutButton, elementsLayout, newLineConstr, buttonPanel);
        StandardFormats.addGridBagElement(editShortcutButton, elementsLayout, newLineConstr, buttonPanel);
        StandardFormats.addGridBagElement(deleteShortcutButton, elementsLayout, newLineConstr, buttonPanel);
        StandardFormats.addGridBagElement(new JPanel(), elementsLayout, bottomConstr, buttonPanel);

        scriptList = new JList<ScriptDefinition>();
        scriptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane shortcutListPane = new JScrollPane(scriptList);
        shortcutListPane.setPreferredSize(new Dimension(250, 150));
        shortcutListPane.setMinimumSize(new Dimension(10, 10));
        
        JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	ScriptDefinitionManagerFrame.this.frame.setVisible(false);
                }
            });
		

        refreshShortcutList();
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(new JLabel("Shortcuts:"), BorderLayout.NORTH);
        contentPane.add(closeButton, BorderLayout.SOUTH);
        contentPane.add(shortcutListPane, BorderLayout.CENTER);
        contentPane.add(buttonPanel, BorderLayout.EAST);

		frame.setContentPane(contentPane);
		frame.pack();
	}
	
	private void refreshShortcutList()
	{
		 scriptList.setListData(ScriptDefinitionManager.getScriptDefinitions());
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		refreshShortcutList();	
		for(ActionListener listener : listeners)
        {
        	listener.actionPerformed(new ActionEvent(this, 155, "Script shortcut created or edited."));
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
