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
package org.youscope.plugin.controller;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 */
public class ControllerJobConfigurationAddon extends ComponentAddonUIAdapter<ControllerJobConfiguration>
{
    private OutputConfigurationPanel outputJobConfigurationPanel = null;
    private InputConfigurationPanel inputJobConfigurationPanel = null;
    private AlgorithmConfigurationPanel algorithmConfigurationPanel = null;
	private MiscConfigurationPanel miscConfigurationPanel = null;
    
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public ControllerJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ComponentMetadataAdapter<ControllerJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<ControllerJobConfiguration>(ControllerJobConfiguration.TYPE_IDENTIFIER, 
				ControllerJobConfiguration.class, 
				ControllerJob.class, 
				"controller", 
				new String[]{"feedback"},
				"A job consuming the output (in form of table data) of its first child job, performing computations on this data, and sending, based on the result, certain commands (in form of table data) to its second child job. Thus, this job realizes a control algorithm, with its first child job being the input/observer, and the second child job being the output/plant.",
				"icons/robot.png");
	}
    
	@Override
	protected Component createUI(ControllerJobConfiguration configuration) throws AddonException 
	{
		setTitle("Controller Job");
		setResizable(true);
		setMaximizable(true);
		setPreferredSize(new Dimension(800, 600));
		
		outputJobConfigurationPanel = new OutputConfigurationPanel(configuration, getClient(), getContainingFrame());
	    inputJobConfigurationPanel = new InputConfigurationPanel(configuration, getClient(), getContainingFrame());
	    algorithmConfigurationPanel = new AlgorithmConfigurationPanel(configuration, getClient(), getServer());
	    miscConfigurationPanel = new MiscConfigurationPanel(configuration, getContainingFrame());
	    
		JTabbedPane centralPanel = new JTabbedPane(SwingConstants.TOP);
        centralPanel.addTab("Input", inputJobConfigurationPanel);
        centralPanel.addTab("Output", outputJobConfigurationPanel);
        centralPanel.addTab("Control Algorithm", algorithmConfigurationPanel);
        centralPanel.addTab("Misc", miscConfigurationPanel);
        
        centralPanel.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				algorithmConfigurationPanel.setOutputColumns(outputJobConfigurationPanel.getOutputColumns());
				algorithmConfigurationPanel.setInputColumns(inputJobConfigurationPanel.getInputColumns());
			}
		});
        
		return centralPanel;
    }
	
	@Override
	protected void commitChanges(ControllerJobConfiguration configuration)
	{
		inputJobConfigurationPanel.commitChanges(configuration);
		outputJobConfigurationPanel.commitChanges(configuration);
		miscConfigurationPanel.commitChanges(configuration);
		algorithmConfigurationPanel.commitChanges(configuration);
    }

	@Override
	protected void initializeDefaultConfiguration(ControllerJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
