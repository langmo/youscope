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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.script.ScriptEngineFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.youscope.addon.AddonException;
import org.youscope.clientinterfaces.StandardProperty;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.uielements.StandardFormats;

/**
 * @author langmo
 *
 */
class ScriptDefinitionConfigurationFrame
{	
	private YouScopeFrame									frame;
	
	private JComboBox<String> engineNamesField = new JComboBox<String>();
	private JTextField nameField = new JTextField();
	protected JTextField								scriptFileField				= new JTextField();
	
	private ScriptDefinition scriptDefinition;
	private Vector<ActionListener> listeners = new Vector<ActionListener>();
	
	ScriptDefinitionConfigurationFrame(YouScopeFrame frame)
	{
		this(frame, null);
	}
	ScriptDefinitionConfigurationFrame(YouScopeFrame frame, ScriptDefinition scriptDefinition)
	{
		this.frame = frame;
		this.scriptDefinition = scriptDefinition;
		
		frame.setTitle("Script Shortcut Manager");
		frame.setResizable(false);
		frame.setClosable(true);
		frame.setMaximizable(false);
		
		intitializeSupportedScriptEngines();
		
		GridBagLayout elementsLayout = new GridBagLayout();
        GridBagConstraints newLineConstr = StandardFormats.getNewLineConstraint();
        
        JPanel elementsPanel = new JPanel(elementsLayout);
        StandardFormats.addGridBagElement(new JLabel("Name of Shortcut:"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(nameField, elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(new JLabel("Script Engine:"), elementsLayout, newLineConstr, elementsPanel);
        StandardFormats.addGridBagElement(engineNamesField, elementsLayout, newLineConstr, elementsPanel);
        
        StandardFormats.addGridBagElement(new JLabel("Script to execute when shortcut is activated:"), elementsLayout, newLineConstr, elementsPanel);
        JPanel scriptButtonsPanel = new JPanel(new BorderLayout());
        scriptFileField.setEditable(false);
		scriptButtonsPanel.add(scriptFileField, BorderLayout.CENTER);		
        JButton loadButton = new JButton("Select");
        loadButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                    JFileChooser fileChooser =
                            new JFileChooser((String) PropertyProviderImpl.getInstance().getProperty(StandardProperty.PROPERTY_LAST_SCRIPT_PATH));
                    
                    String engineName = (String) engineNamesField.getSelectedItem();
                    if(engineName == null)
                    {
                    	ClientSystem.err.println("No script engine selected.");
                    	return;
                    }
                    ScriptEngineFactory engineFactory;
					try {
						engineFactory = ClientAddonProviderImpl.getProvider().getScriptEngineFactory(engineName);
					} catch (AddonException e) {
						ClientSystem.err.println("Could not load script engine.", e);
                    	return;
					}
                    
                    String[] engineFileEndings;
                    if(engineFactory == null)
                    {
                    	engineFileEndings = new String[0];
                    }
                    else
                    {
                    	engineFileEndings = engineFactory.getExtensions().toArray(new String[0]);
                    }
                    
                    if(engineFileEndings.length > 0)
                    {
                    	fileChooser.setSelectedFile(new File("unnamed." + engineFileEndings[0]));
                    	String filterDesc = "Script Files (";
                    	boolean first = true;
                    	for(String engineFileEnding : engineFileEndings)
                    	{
                    		if(first)
                    			first = false;
                    		else
                    			filterDesc += ", ";
                    		filterDesc += "." + engineFileEnding;
                    	}
                    	filterDesc += ")";
                    	fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(filterDesc, engineFileEndings));
                    }
                                       
                    File file;
                    while(true)
                    {
                    	int returnVal = fileChooser.showDialog(null, "Load");
                    	if (returnVal != JFileChooser.APPROVE_OPTION)
                    	{
                    		return;
                    	}
                    	file = fileChooser.getSelectedFile().getAbsoluteFile();
                    	if(!file.exists())
                    	{
                    		JOptionPane.showMessageDialog(null, "File " + file.toString() + " does not exist.\nTo create a yet not existing file, use the \"New File\" button.", "File does not exist", JOptionPane. INFORMATION_MESSAGE);
                    	}
                    	else
                    		break;
                    }
                    
                    PropertyProviderImpl.getInstance().setProperty(StandardProperty.PROPERTY_LAST_SCRIPT_PATH, fileChooser
                            .getCurrentDirectory().getAbsolutePath());
                    String relative = new File(".").toURI().relativize(file.toURI()).getPath();
                    scriptFileField.setText(relative);
                }
            });
        scriptButtonsPanel.add(loadButton, BorderLayout.EAST);
        StandardFormats.addGridBagElement(scriptButtonsPanel, elementsLayout, newLineConstr, elementsPanel);
		        
        // Button to save setting
        JButton closeButton = new JButton("Save");
		closeButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                	String name = nameField.getText();
                	if(name.length()<3)
                	{
                		JOptionPane.showMessageDialog(null, "Name must be longer than three characters.", "Could not initialize scripting shortcut", JOptionPane.INFORMATION_MESSAGE);
                		return;
                	}
                	String engine = engineNamesField.getSelectedItem().toString();
                	if(engine.length() < 1)
                	{
                		JOptionPane.showMessageDialog(null, "Please select a script engine.", "Could not initialize scripting shortcut", JOptionPane.INFORMATION_MESSAGE);
                		return;
                	}
                	String file =scriptFileField.getText(); 
                	if(file.length() < 3)
                	{
                		JOptionPane.showMessageDialog(null, "Please select a file which should be run when shortcut is activated.", "Could not initialize scripting shortcut", JOptionPane.INFORMATION_MESSAGE);
                		return;
                	}
					if(!(new File(file)).exists())
					{
						JOptionPane.showMessageDialog(null, "Script file (" + file + ") does not exist.", "Could not initialize scripting shortcut", JOptionPane.INFORMATION_MESSAGE);
						return;
					}
                	if(ScriptDefinitionConfigurationFrame.this.scriptDefinition != null)
                		ScriptDefinitionManager.deleteScriptDefinition(ScriptDefinitionConfigurationFrame.this.scriptDefinition);
                	ScriptDefinitionManager.saveScriptDefinition(new ScriptDefinition(name, engine, file));
                    ScriptDefinitionConfigurationFrame.this.frame.setVisible(false); 
                    for(ActionListener listener : listeners)
                    {
                    	listener.actionPerformed(new ActionEvent(this, 155, "Script shortcut created or edited."));
                    }
                }
            });
		
        // Load data
        if(scriptDefinition != null)
        {
        	nameField.setText(scriptDefinition.getName());
        	scriptFileField.setText(scriptDefinition.getScriptFile());
        	String engine = scriptDefinition.getEngine();
        	for(int i=0; i<engineNamesField.getItemCount(); i++)
        	{
        		if(engineNamesField.getItemAt(i).toString().compareToIgnoreCase(engine) == 0)
        		{
        			engineNamesField.setSelectedIndex(i);
        		}
        	}
        }
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(elementsPanel, BorderLayout.CENTER);
        contentPane.add(closeButton, BorderLayout.SOUTH);
        
        frame.setContentPane(contentPane);
        frame.pack();
	}
	
	protected void intitializeSupportedScriptEngines()
    {
        for (ScriptEngineFactory factory : ClientAddonProviderImpl.getProvider().getScriptEngineFactories())
        {
           	engineNamesField.addItem(factory.getEngineName());
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
