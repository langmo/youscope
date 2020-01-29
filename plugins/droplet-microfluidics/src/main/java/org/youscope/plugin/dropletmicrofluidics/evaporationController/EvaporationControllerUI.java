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
package org.youscope.plugin.dropletmicrofluidics.evaporationController;

import java.awt.Component;

import javax.swing.JCheckBox;
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
class EvaporationControllerUI extends ComponentAddonUIAdapter<EvaporationControllerConfiguration> implements DropletControllerConfigurationAddon<EvaporationControllerConfiguration>
{
	private final DoubleTextField maxDeltaFlowRateField = new DoubleTextField();
	private final DoubleTextField ratioHeightToVolumeField = new DoubleTextField();
	private final PeriodField timeConstantProportionalField = new PeriodField();
	private final PeriodField timeConstantIntegralField = new PeriodField();
	private int[] connectedSyringes = new int[0];
	private JCheckBox[] useSyringeFields;
	private final DynamicPanel flowUnitsPanel = new DynamicPanel();
	
    /**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public EvaporationControllerUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	
	static ComponentMetadataAdapter<EvaporationControllerConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<EvaporationControllerConfiguration>(EvaporationControllerConfiguration.TYPE_IDENTIFIER, 
				EvaporationControllerConfiguration.class, 
				EvaporationController.class, "Evaporation Controller", new String[]{"droplet-based microfluidics"});
	}
	
	@Override
	protected Component createUI(EvaporationControllerConfiguration configuration) throws AddonException
	{
		setTitle("Syringe-table controller for droplet-based microfluidics");
		setResizable(true);
		setMaximizable(true);
		
		DynamicPanel contentPane = new DynamicPanel();
		
		maxDeltaFlowRateField.setValue(configuration.getMaxDeltaFlowRate());
		maxDeltaFlowRateField.setMinimalValue(0);
		contentPane.add(new JLabel("Maximal delta flow rate used for control (ul/min):"));
		contentPane.add(maxDeltaFlowRateField);
		
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
		
		contentPane.add(new JLabel("Flow units used for evaporation correction (only inflow):"));
		useSyringeFields = new JCheckBox[connectedSyringes.length];
		boolean[] useSyringe = configuration.getUseSyringe();
		for(int i=0; i< useSyringeFields.length; i++)
		{
			useSyringeFields[i] = new JCheckBox("Flow Unit "+Integer.toString(connectedSyringes[i]+1));
			useSyringeFields[i].setOpaque(false);
			if((useSyringe == null ||useSyringe.length <= i)&& i==0)
				useSyringeFields[i].setSelected(true);
			else if(useSyringe == null||useSyringe.length <= i)
				useSyringeFields[i].setSelected(false);
			else
				useSyringeFields[i].setSelected(useSyringe[i]);
			flowUnitsPanel.add(useSyringeFields[i]);
		}
		contentPane.add(flowUnitsPanel);
		contentPane.addFillEmpty();
		return contentPane;
    }

    @Override
	protected void commitChanges(EvaporationControllerConfiguration configuration)
    {   	
    	boolean[] useSyringes = new boolean[useSyringeFields.length];
    	boolean atLeastOne = false;
    	for(int i=0; i<useSyringeFields.length; i++)
    	{
    		useSyringes[i] = useSyringeFields[i].isSelected();
    		atLeastOne = atLeastOne || useSyringes[i];
    	}
    	double deltaFlow = maxDeltaFlowRateField.getValue();
    	configuration.setUseSyringe(useSyringes);
    	configuration.setMaxDeltaFlowRate(deltaFlow);
    	configuration.setRatioHeightToVolume(ratioHeightToVolumeField.getValue());
    	configuration.setTimeConstantIntegral(timeConstantIntegralField.getDurationLong());
    	configuration.setTimeConstantProportional(timeConstantProportionalField.getDurationLong());
    }

    @Override
	public void setConnectedSyringes(int[] connectedSyringes) {
		if(connectedSyringes == null)
			connectedSyringes = new int[0];
		this.connectedSyringes = connectedSyringes;
		
		if(isInitialized() && connectedSyringes.length != useSyringeFields.length)
		{
			boolean[] useSyringes = new boolean[useSyringeFields.length];
	    	for(int i=0; i<useSyringeFields.length; i++)
	    	{
	    		useSyringes[i] = useSyringeFields[i].isSelected();
	    	}
	    	flowUnitsPanel.removeAll();
	    	useSyringeFields = new JCheckBox[connectedSyringes.length];
	    	for(int i=0; i<connectedSyringes.length; i++)
	    	{
	    		useSyringeFields[i] = new JCheckBox("Flow Unit "+Integer.toString(connectedSyringes[i]+1));
	    		useSyringeFields[i].setOpaque(false);
	    		if(useSyringes.length > i)
	    		{
	    			useSyringeFields[i].setSelected(useSyringes[i]);
	    		}
	    		else
	    			useSyringeFields[i].setSelected(i==0);
	    		flowUnitsPanel.add(useSyringeFields[i]);
	    	}
	    	flowUnitsPanel.revalidate();
	    	getContainingFrame().pack();
		}
	}

	@Override
	protected void initializeDefaultConfiguration(EvaporationControllerConfiguration configuration)
			throws AddonException {
		// do nothing.
	}
}
