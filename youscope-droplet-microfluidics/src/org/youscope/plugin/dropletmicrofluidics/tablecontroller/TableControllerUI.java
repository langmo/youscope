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
package org.youscope.plugin.dropletmicrofluidics.tablecontroller;

import java.awt.Component;

import javax.swing.JComboBox;
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
class TableControllerUI extends ComponentAddonUIAdapter<TableControllerConfiguration> implements DropletControllerConfigurationAddon<TableControllerConfiguration>
{
	
	private final DoubleTextField targetFlowRateField = new DoubleTextField();
	private final DoubleTextField maxDeltaFlowRateField = new DoubleTextField();
	private final JComboBox<String> correctionMethod = new JComboBox<String>(new String[]{"Correct by inflow", "Correct by outflow"});
	private final DoubleTextField ratioHeightToVolumeField = new DoubleTextField();
	private final PeriodField timeConstantProportionalField = new PeriodField();
	private final PeriodField timeConstantIntegralField = new PeriodField();
	private int[] connectedSyringes = new int[0];
	
	private SyringeTable syringeTableField = null;
    /**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public TableControllerUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	
	static ComponentMetadataAdapter<TableControllerConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<TableControllerConfiguration>(TableControllerConfiguration.TYPE_IDENTIFIER, 
				TableControllerConfiguration.class, 
				TableController.class, "Syringe-Table Controller", new String[]{"droplet-based microfluidics"});
	}
	
	@Override
	protected Component createUI(TableControllerConfiguration configuration) throws AddonException
	{
		setTitle("Syringe-table controller for droplet-based microfluidics");
		setResizable(true);
		setMaximizable(true);
		
		// get number of syringes
		
		
		DynamicPanel contentPane = new DynamicPanel();
		
		targetFlowRateField.setValue(configuration.getTargetFlowRate());
		targetFlowRateField.setMinimalValue(0);
		contentPane.add(new JLabel("Target flow rate (ul/min):"));
		contentPane.add(targetFlowRateField);
		
		maxDeltaFlowRateField.setValue(configuration.getMaxDeltaFlowRate());
		maxDeltaFlowRateField.setMinimalValue(0);
		contentPane.add(new JLabel("Maximal delta flow rate used for control (ul/min):"));
		contentPane.add(maxDeltaFlowRateField);
		
		correctionMethod.setSelectedIndex(configuration.isCorrectByOutflow() ? 1 : 0);
		contentPane.add(new JLabel("Vary inflow or outflow to control droplet height:"));
		contentPane.add(correctionMethod);
		
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
		
		syringeTableField = new SyringeTable(getClient(), connectedSyringes);
		syringeTableField.setRows(configuration.getSyringeTableRows()); 
		contentPane.add(new JLabel("Syringe table:"));
		contentPane.addFill(syringeTableField);
		
		return contentPane;
    }

    @Override
	protected void commitChanges(TableControllerConfiguration configuration)
    {   
    	SyringeTableRow[] rows = syringeTableField.getRows();
    	double targetFlowRate = targetFlowRateField.getValue();
    	double maxDeltaFlowRate = maxDeltaFlowRateField.getValue();
    	boolean correctByOutflow = correctionMethod.getSelectedIndex()==1;
    	
    	configuration.setTargetFlowRate(targetFlowRate);
    	configuration.setMaxDeltaFlowRate(maxDeltaFlowRate);
    	configuration.setCorrectByOutflow(correctByOutflow);
    	configuration.setRatioHeightToVolume(ratioHeightToVolumeField.getValue());
    	configuration.setTimeConstantIntegral(timeConstantIntegralField.getDurationLong());
    	configuration.setTimeConstantProportional(timeConstantProportionalField.getDurationLong());
    	configuration.setSyringeTableRows(rows);
    }

	@Override
	protected void initializeDefaultConfiguration(TableControllerConfiguration configuration) throws AddonException {
		// do nothing.
	}

	@Override
	public void setConnectedSyringes(int[] connectedSyringes) {
		if(connectedSyringes == null)
			connectedSyringes = new int[0];
		this.connectedSyringes = connectedSyringes;
		if(isInitialized())
			syringeTableField.setConnectedSyringes(connectedSyringes);
	}
}
