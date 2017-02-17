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
package org.youscope.plugin.devicejob;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.job.basicjobs.DeviceSettingJob;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DeviceSettingsPanel;
import org.youscope.uielements.DynamicPanel;

/**
 * @author Moritz Lang
 */
class DeviceJobConfigurationAddon extends ComponentAddonUIAdapter<DeviceJobConfiguration>
{

    private DeviceSettingsPanel deviceSettingsField;
    
    DeviceJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
    {
    	super(getMetadata(),  client, server);
	}
    
    static ComponentMetadataAdapter<DeviceJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<DeviceJobConfiguration>(DeviceSettingJob.DEFAULT_TYPE_IDENTIFIER, 
				DeviceJobConfiguration.class, 
				DeviceSettingJob.class, 
				"Device Settings", 
				new String[]{"Elementary"},
				"Sets a set of device settings, that is, changes one or more hardware settings of the microscope.",
				"icons/property-blue.png");
	}
    
    @Override
	protected Component createUI(DeviceJobConfiguration configuration) throws AddonException
	{
		setTitle("Device Settings");
		setResizable(true);
		setMaximizable(false);
		
		deviceSettingsField = new DeviceSettingsPanel(configuration.getDeviceSettings(), getClient(), getServer());

        // Initialize layout
        DynamicPanel contentPane = new DynamicPanel();
        contentPane.add(new JLabel("Device Settings:"));
        contentPane.addFill(deviceSettingsField);
        setPreferredSize(new Dimension(600,400));
        return contentPane;
    }

	@Override
	protected void commitChanges(DeviceJobConfiguration configuration) 
	{
		configuration.setDeviceSettings(deviceSettingsField.getSettings());
	}

	@Override
	protected void initializeDefaultConfiguration(DeviceJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
