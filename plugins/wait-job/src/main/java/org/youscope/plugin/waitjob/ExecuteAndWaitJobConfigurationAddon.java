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
package org.youscope.plugin.waitjob;

import java.awt.Component;

import javax.swing.JLabel;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.job.basicjobs.WaitJob;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.JobsDefinitionPanel;
import org.youscope.uielements.PeriodField;

/**
 * @author Moritz Lang
 */
class ExecuteAndWaitJobConfigurationAddon extends ComponentAddonUIAdapter<ExecuteAndWaitJobConfiguration>
{
    private JobsDefinitionPanel jobPanel;
	private final PeriodField waitTimeField = new PeriodField();
	
    ExecuteAndWaitJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
    {
    	super(getMetadata(), client, server);
	}
    
    static ComponentMetadataAdapter<ExecuteAndWaitJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<ExecuteAndWaitJobConfiguration>(ExecuteAndWaitJobConfiguration.TYPE_IDENTIFIER, 
				ExecuteAndWaitJobConfiguration.class, 
				WaitJob.class, 
				"Execute and Wait", 
				new String[]{"Elementary"},
				"Measures the time it takes to executes its child jobs. If the time is lower than a user-defined period, it waits for the rest of the remainder of the period. Useful to precisely time events.",
				"icons/alarm-clock--exclamation.png");
	}
    
    
    @Override
	protected Component createUI(ExecuteAndWaitJobConfiguration configuration) throws AddonException {
		setTitle("Execute and Wait");
		setResizable(true);
		setMaximizable(false);
        
        jobPanel = new JobsDefinitionPanel(getClient(), getServer(), getContainingFrame());
        jobPanel.setJobs(configuration.getJobs());
        waitTimeField.setDuration((int)configuration.getWaitTime());
        
        DynamicPanel contentPane = new DynamicPanel();
        contentPane.add(new JLabel("Total fixed execution time:"));
        contentPane.add(waitTimeField);
        contentPane.add(new JLabel("Jobs to execute:"));
        contentPane.addFill(jobPanel);
        return contentPane;
    }

	@Override
	protected void commitChanges(ExecuteAndWaitJobConfiguration configuration)
	{
		configuration.setJobs(jobPanel.getJobs());
		configuration.setWaitTime(waitTimeField.getDuration());
	}

	@Override
	protected void initializeDefaultConfiguration(ExecuteAndWaitJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
