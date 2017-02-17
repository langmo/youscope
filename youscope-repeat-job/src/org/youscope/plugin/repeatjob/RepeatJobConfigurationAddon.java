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
package org.youscope.plugin.repeatjob;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.IntegerTextField;
import org.youscope.uielements.JobsDefinitionPanel;

/**
 * Addon to configure repeat jobs.
 * @author Moritz Lang
 */
class RepeatJobConfigurationAddon extends ComponentAddonUIAdapter<RepeatJobConfiguration>
{
	private JobsDefinitionPanel jobPanel;
	private IntegerTextField numRepeatsField = new IntegerTextField(1);
	
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public RepeatJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ComponentMetadataAdapter<RepeatJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<RepeatJobConfiguration>(RepeatJobConfiguration.TYPE_IDENTIFIER, 
				RepeatJobConfiguration.class, 
				RepeatJob.class, 
				"Repeat Job", 
				new String[]{"Containers"}, 
				"Repeatadly executes a given list of jobs several times in a row.",
				"icons/arrow-circle.png");
	}
	@Override
	protected Component createUI(RepeatJobConfiguration configuration) throws AddonException
	{
		setTitle("Repeat Job");
		setResizable(true);
		setMaximizable(false);
 
        jobPanel = new JobsDefinitionPanel(getClient(), getServer(), getContainingFrame());
        
        DynamicPanel topPanel = new DynamicPanel();
        topPanel.add(new JLabel("Number of times sub-jobs should be repeated:"));
        numRepeatsField.setMinimalValue(0);
        numRepeatsField.setOpaque(true);
        topPanel.add(numRepeatsField);
        topPanel.add(new JLabel("Sub-jobs to be repeated:"));
        
        // load state
        jobPanel.setJobs(configuration.getJobs());
        numRepeatsField.setValue(configuration.getNumRepeats());
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(jobPanel, BorderLayout.CENTER);
        return contentPane;
    }

	@Override
	protected void commitChanges(RepeatJobConfiguration configuration) {
		configuration.setJobs(jobPanel.getJobs());
		configuration.setNumRepeats(numRepeatsField.getValue());
	}

	@Override
	protected void initializeDefaultConfiguration(RepeatJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
