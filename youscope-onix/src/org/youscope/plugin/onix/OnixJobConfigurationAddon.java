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
package org.youscope.plugin.onix;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.scripteditor.ScriptEditor;

/**
 * @author Moritz Lang
 */
class OnixJobConfigurationAddon  extends ComponentAddonUIAdapter<OnixJobConfiguration>
{
	private JCheckBox										waitCheckBox	= new JCheckBox("Wait until protocol is executed.", true);
	
	private final ScriptEditor protocolArea = new ScriptEditor();

	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public OnixJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ComponentMetadataAdapter<OnixJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<OnixJobConfiguration>(OnixJobConfiguration.TYPE_IDENTIFIER, 
				OnixJobConfiguration.class, 
				OnixJob.class, 
				"onix control", 
				new String[]{"microfluidics"},
				"Sets the flow rates/pressures of an Onix microfluidic device. Provides an option for pulse-width modulation to continuously mix media.", 
				"icons/beaker.png");
	}
	@Override
	protected Component createUI(OnixJobConfiguration configuration) throws AddonException
	{
		setTitle("Onix Job");
		setResizable(true);
		setMaximizable(true);
		setPreferredSize(new Dimension(800, 600));
		
		DynamicPanel contentPane = new DynamicPanel();
		
		contentPane.add(new JLabel("Execution type:"));
		waitCheckBox.setSelected(configuration.isWaitUntilFinished());
		waitCheckBox.setOpaque(false);
		contentPane.add(waitCheckBox);
		
		contentPane.add(new JLabel("Onix Protocol"));
		protocolArea.setScriptStyleID("YouScope.ScriptStyle.Onix");
		protocolArea.setText(configuration.getOnixProtocol());
		contentPane.addFill(new JScrollPane(protocolArea));
		
		JPanel loadSavePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        loadSavePanel.setOpaque(false);
        JButton loadButton = new JButton("Load");
        loadButton.setOpaque(false);
        loadSavePanel.add(loadButton);
        loadButton.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent arg0)
                {
                	String lastProtocol = getClient().getPropertyProvider().getProperty(OnixController.PROPERTY_PROTOCOL, "onix/protocol.onix");
                    JFileChooser fileChooser = new JFileChooser(lastProtocol);
                    
                    String filterDesc = "ONIX Protocol (.onix)";
                    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(filterDesc, new String[]{".onix"}));
                    fileChooser.setSelectedFile(new File(lastProtocol));      
                    
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
                    		JOptionPane.showMessageDialog(null, "File " + file.toString() + " does not exist.\nPlease select an existing file.", "File does not exist", JOptionPane. INFORMATION_MESSAGE);
                    	}
                    	else
                    		break;
                    }
                    
                    getClient().getPropertyProvider().setProperty(OnixController.PROPERTY_PROTOCOL, file.toString());
                    
                    BufferedReader reader = null;
                    String protocol = "";
                    try
					{
						reader = new BufferedReader(new FileReader(file));
						while(true)
						{
							String line = reader.readLine();
							if(line == null)
								break;
							protocol += line + "\n";
						}
					}
					catch(Exception e1)
					{
						sendErrorMessage("Could not load Onix protocol " + file.toString()+ ".", e1);
						return;
					}
					finally
					{
						if(reader != null)
						{
							try
							{
								reader.close();
							}
							catch(Exception e1)
							{
								sendErrorMessage("Could not close protocol " + file.toString()+ ".", e1);
							}
						}						
					}
					
					protocolArea.setText(protocol);
                }
            });
        
        JButton saveButton = new JButton("Save");
        saveButton.setOpaque(false);
        loadSavePanel.add(saveButton);
        saveButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
            	String lastProtocol = getClient().getPropertyProvider().getProperty(OnixController.PROPERTY_PROTOCOL, "onix/protocol.onix");
                JFileChooser fileChooser = new JFileChooser(lastProtocol);
                String filterDesc = "ONIX Protocol (.onix)";
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(filterDesc, new String[]{".onix"}));
                fileChooser.setSelectedFile(new File(lastProtocol)); 
                                   
                File file;
                while(true)
                {
                	int returnVal = fileChooser.showDialog(null, "Save");
                	if (returnVal != JFileChooser.APPROVE_OPTION)
                	{
                		return;
                	}
                	file = fileChooser.getSelectedFile().getAbsoluteFile();
                	if(file.exists())
                	{
                		returnVal = JOptionPane.showConfirmDialog(null, "File " + file.toString() + " does already exist.\nOverwrite?", "File does already exist", JOptionPane.YES_NO_OPTION);
                		if(returnVal == JOptionPane.YES_OPTION)
                			break;
                	}
                	else
                		break;
                }
                
                getClient().getPropertyProvider().setProperty(OnixController.PROPERTY_PROTOCOL, file.toString());
                
                String text = protocolArea.getText();
        		try
        		{
        			PrintStream fileStream = new PrintStream(file);
        			fileStream.print(text);
        			fileStream.close();
        		}
        		catch(Exception e)
        		{
        			sendErrorMessage("Could not save file.", e);
        			return;
        		}
            }
        });
        contentPane.add(loadSavePanel);
		
		return contentPane;
	}


	@Override
	protected void commitChanges(OnixJobConfiguration configuration)
	{
		configuration.setOnixProtocol(protocolArea.getText());
		configuration.setWaitUntilFinished(waitCheckBox.isSelected());
	}

	@Override
	protected void initializeDefaultConfiguration(OnixJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
