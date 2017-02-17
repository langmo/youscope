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
package org.youscope.plugin.dropletmicrofluidics.flexiblecontroller;

import java.awt.Component;

import javax.swing.JLabel;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonUIAdapter;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.dropletmicrofluidics.DropletControllerConfigurationAddon;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DoubleTextField;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.PeriodField;

/**
 * @author Moritz Lang
 */
class FlexibleControllerUI extends ComponentAddonUIAdapter<FlexibleControllerConfiguration> implements DropletControllerConfigurationAddon<FlexibleControllerConfiguration>
{
	
	private final DoubleTextField ratioHeightToVolumeField = new DoubleTextField();
	private final PeriodField timeConstantProportionalField = new PeriodField();
	private final PeriodField timeConstantIntegralField = new PeriodField();
	private int[] connectedSyringes = new int[0];
	
	private FlexibleSyringeTable syringeTableField = null;
    /**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public FlexibleControllerUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	
	static ComponentMetadataAdapter<FlexibleControllerConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<FlexibleControllerConfiguration>(FlexibleControllerConfiguration.TYPE_IDENTIFIER, 
				FlexibleControllerConfiguration.class, 
				FlexibleController.class, "Flexible Controller", new String[]{"droplet-based microfluidics"});
	}
	
	@Override
	protected Component createUI(FlexibleControllerConfiguration configuration) throws AddonException
	{
		setTitle("Flexible controller for droplet-based microfluidics");
		setResizable(true);
		setMaximizable(true);
		
		// get number of syringes
		
		
		DynamicPanel contentPane = new DynamicPanel();
		
		ratioHeightToVolumeField.setValue(configuration.getRatioHeightToVolume());
		ratioHeightToVolumeField.setMinimalValue(0);
		contentPane.add(new JLabel("Ratio droplet height to volume (um/ul):"));
		contentPane.add(ratioHeightToVolumeField);
		
		timeConstantProportionalField.setDuration(configuration.getTimeConstantProportional());
		contentPane.add(new JLabel("Time constant of controller's proportional part:"));
		contentPane.add(timeConstantProportionalField);
		
		timeConstantIntegralField.setDuration(configuration.getTimeConstantIntegral());
		contentPane.add(new JLabel("Time constant of controller's integral part:"));
		contentPane.add(timeConstantIntegralField);
		
		syringeTableField = new FlexibleSyringeTable(getClient(), connectedSyringes);
		syringeTableField.setRows(configuration.getSyringeTableRows()); 
		contentPane.add(new JLabel("Syringe table:"));
		contentPane.addFill(syringeTableField);
		
		return contentPane;
    }

    @Override
	protected void commitChanges(FlexibleControllerConfiguration configuration)
    {   
    	FlexibleSyringeTableRow[] rows = syringeTableField.getRows();
    	configuration.setRatioHeightToVolume(ratioHeightToVolumeField.getValue());
    	configuration.setTimeConstantIntegral(timeConstantIntegralField.getDurationLong());
    	configuration.setTimeConstantProportional(timeConstantProportionalField.getDurationLong());
    	configuration.setSyringeTableRows(rows);
    }

	@Override
	protected void initializeDefaultConfiguration(FlexibleControllerConfiguration configuration) throws AddonException {
		// do nothing.
	}

	@Override
	public void setConnectedSyringes(int[] connectedSyringes) {
		if(connectedSyringes == null)
			connectedSyringes = new int[0];
		this.connectedSyringes = connectedSyringes;
		if(isInitialized())
			syringeTableField.setConnectedSyringes(this.connectedSyringes);
	}
}
