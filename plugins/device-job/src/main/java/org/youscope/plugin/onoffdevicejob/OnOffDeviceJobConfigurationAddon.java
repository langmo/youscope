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
package org.youscope.plugin.onoffdevicejob;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.job.basicjobs.SimpleCompositeJob;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DeviceSettingsPanel;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.PeriodField;

/**
 * @author Moritz Lang
 */
class OnOffDeviceJobConfigurationAddon extends ComponentAddonUIAdapter<OnOffDeviceJobConfiguration>
{
    private static final String[] TYPES_STRINGS =
        { "Set Devices", "Set Devices - Wait", "Set Devices - Wait - Set Devices" };

    private final JComboBox<String> typeChooserBox = new JComboBox<String>(TYPES_STRINGS);

    private DeviceSettingsPanel deviceSettingsOnField;
    private final JLabel deviceSettingsOnLabel = new JLabel("Device Settings upon activation:");

    private DeviceSettingsPanel deviceSettingsOffField;
    private final JLabel deviceSettingsOffLabel = new JLabel("Device Settings upon deactivation:");
    
    private final PeriodField exposureField = new PeriodField();
    private final JLabel exposureLabel = new JLabel("Exposure:");

    OnOffDeviceJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
    {
    	super(getMetadata(),  client, server);	
	}
    
    static ComponentMetadataAdapter<OnOffDeviceJobConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<OnOffDeviceJobConfiguration>(OnOffDeviceJobConfiguration.TYPE_IDENTIFIER, 
				OnOffDeviceJobConfiguration.class, 
				SimpleCompositeJob.class, 
				"Set Device - Wait - Set Device", 
				new String[]{"Misc"},
				"Sets a set of device settings, that is, changes one or more hardware settings of the microscope. Then, the job waits for a given time, and applies another set of device settings. Useful e.g. to illuminate cells with light of certain wavelengths without taking an image.",
				"icons/property.png");
	}
    
    @Override
	protected Component createUI(OnOffDeviceJobConfiguration configuration) throws AddonException
	{
		setTitle("Device Settings Job");
		setResizable(true);
		setMaximizable(false);
		
		deviceSettingsOnField = new DeviceSettingsPanel(configuration.getDeviceSettingsOn(), getClient(), getServer());
		deviceSettingsOffField = new DeviceSettingsPanel(configuration.getDeviceSettingsOff(), getClient(), getServer());
		
        // Initialize layout
        DynamicPanel contentPane = new DynamicPanel();
        
        if(configuration.getDeviceSettingsOff().length > 0)
		{
			typeChooserBox.setSelectedIndex(2);
		}
		else if(configuration.getExposure() > 0)
		{
			typeChooserBox.setSelectedIndex(1);
		}
		else
		{
			typeChooserBox.setSelectedIndex(0);
		}
        typeChooserBox.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
            	hideElements();
            	notifyLayoutChanged();
            }
        });
        contentPane.add(new JLabel("Type:"));
        contentPane.add(typeChooserBox);

        contentPane.add(deviceSettingsOnLabel);
        contentPane.addFill(deviceSettingsOnField);
        contentPane.add(exposureLabel);
        exposureField.setDuration((int)configuration.getExposure());
        contentPane.add(exposureField);
        contentPane.add(deviceSettingsOffLabel);
        contentPane.addFill(deviceSettingsOffField);
        
        hideElements();
        return contentPane;
    }

    private void hideElements()
    {
    	int idx = typeChooserBox.getSelectedIndex(); 
    	deviceSettingsOffLabel.setVisible(idx >= 2);
    	deviceSettingsOffField.setVisible(idx >= 2);
    	exposureLabel.setVisible(idx >= 1);
    	exposureField.setVisible(idx >= 1);
    }

    @Override
	protected void commitChanges(OnOffDeviceJobConfiguration configuration) 
	{
		if(typeChooserBox.getSelectedIndex() == 0)
		{
			configuration.setDeviceSettingsOn(deviceSettingsOnField.getSettings());
			configuration.setExposure(0);
			configuration.setDeviceSettingsOff(new DeviceSetting[0]);
		}
		else if(typeChooserBox.getSelectedIndex() == 1)
		{
			configuration.setDeviceSettingsOn(deviceSettingsOnField.getSettings());
			configuration.setExposure(exposureField.getDuration());
			configuration.setDeviceSettingsOff(new DeviceSetting[0]);
		}
		else
		{
			configuration.setDeviceSettingsOn(deviceSettingsOnField.getSettings());
			configuration.setExposure(exposureField.getDuration());
			configuration.setDeviceSettingsOff(deviceSettingsOffField.getSettings());
		}
	}

	@Override
	protected void initializeDefaultConfiguration(OnOffDeviceJobConfiguration configuration) throws AddonException {
		// do nothing.
	}
}
