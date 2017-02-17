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
import org.youscope.uielements.PeriodField;

/**
 * @author Moritz Lang
 */
class WaitJobConfigurationAddon extends ComponentAddonUIAdapter<WaitJobConfiguration>
{
    private final PeriodField waitTimeField = new PeriodField();
	
    /**
     * Constructor
	 * @param client  
     * @param server 
     * @throws AddonException 
	 */
    WaitJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
    {
    	super(getMetadata(), client, server);
	}
    
    static ComponentMetadataAdapter<WaitJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<WaitJobConfiguration>(WaitJob.DEFAULT_TYPE_IDENTIFIER, 
				WaitJobConfiguration.class, 
				WaitJob.class, 
				"Wait", 
				new String[]{"Elementary"},
				"Waits for a given amount of time.",
				"icons/alarm-clock-select.png");
	}
    
    @Override
	protected Component createUI(WaitJobConfiguration configuration) throws AddonException {
		setTitle("Wait");
		setResizable(false);
		setMaximizable(false);
        
        waitTimeField.setDuration((int)configuration.getWaitTime());
        
        DynamicPanel contentPane = new DynamicPanel();
        contentPane.add(new JLabel("Wait time:"));
        contentPane.add(waitTimeField);

        return contentPane;
    }

	@Override
	protected void commitChanges(WaitJobConfiguration configuration)
	{
		configuration.setWaitTime(waitTimeField.getDuration());
	}

	@Override
	protected void initializeDefaultConfiguration(WaitJobConfiguration configuration) throws AddonException {
		// do nothing.
	}

    
}
