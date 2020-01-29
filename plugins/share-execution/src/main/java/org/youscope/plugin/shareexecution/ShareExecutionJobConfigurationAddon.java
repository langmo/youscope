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
package org.youscope.plugin.shareexecution;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JCheckBox;
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
 * Addon to configure share execution jobs.
 * @author Moritz Lang
 */
class ShareExecutionJobConfigurationAddon extends ComponentAddonUIAdapter<ShareExecutionJobConfiguration>
{
	private JobsDefinitionPanel jobPanel;
	private IntegerTextField numShareField = new IntegerTextField(1);
	private IntegerTextField shareIDField = new IntegerTextField(1);
	private JCheckBox separateForEachWellField = new JCheckBox("Separate counting for each well");
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public ShareExecutionJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(),  client, server);
	}
    
	static ComponentMetadataAdapter<ShareExecutionJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<ShareExecutionJobConfiguration>(ShareExecutionJobConfiguration.TYPE_IDENTIFIER, 
				ShareExecutionJobConfiguration.class, 
				ShareExecutionJob.class, 
				"Share Execution Job", 
				new String[]{"Containers"}, 
				"Of all shared execution job with the same ID defined in a measurement, only the child jobs of a certain number is executed every iteration. The job ensures that the child jobs of all shared execution jobs are executed equally often.",
				"icons/arrow-switch.png");
	}
	@Override
	protected Component createUI(ShareExecutionJobConfiguration configuration) throws AddonException
	{
		setTitle("Share Execution Job");
		setResizable(true);
		setMaximizable(false);
 
        jobPanel = new JobsDefinitionPanel(getClient(), getServer(), getContainingFrame());
        
        DynamicPanel topPanel = new DynamicPanel();
        topPanel.add(new JLabel("Number of share execution jobs executed per iteration:"));
        numShareField.setMinimalValue(1);
        topPanel.add(numShareField);
        topPanel.add(new JLabel("Share ID (allows for different shared executions):"));
        shareIDField.setMinimalValue(0);
        topPanel.add(shareIDField);
        separateForEachWellField.setOpaque(false);
        topPanel.add(separateForEachWellField);
        
        
        topPanel.add(new JLabel("Jobs to be shared executed:"));
        
        // load state
        jobPanel.setJobs(configuration.getJobs());
        numShareField.setValue(configuration.getNumShare());
        shareIDField.setValue(configuration.getShareID());
        separateForEachWellField.setSelected(configuration.isSeparateForEachWell());
        
        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(jobPanel, BorderLayout.CENTER);
        return contentPane;
    }

	@Override
	protected void commitChanges(ShareExecutionJobConfiguration configuration) {
		configuration.setJobs(jobPanel.getJobs());
		configuration.setNumShare(numShareField.getValue());
		configuration.setShareID(shareIDField.getValue());
		configuration.setSeparateForEachWell(separateForEachWellField.isSelected());
	}

	@Override
	protected void initializeDefaultConfiguration(ShareExecutionJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
