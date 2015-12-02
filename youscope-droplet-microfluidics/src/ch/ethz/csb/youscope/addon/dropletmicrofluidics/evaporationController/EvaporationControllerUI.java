/**
 * 
 */
package ch.ethz.csb.youscope.addon.dropletmicrofluidics.evaporationController;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import ch.ethz.csb.youscope.client.addon.ConfigurationAddonAdapter;
import ch.ethz.csb.youscope.client.addon.ConfigurationMetadataAdapter;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.uielements.DoubleTextField;
import ch.ethz.csb.youscope.client.uielements.DynamicPanel;
import ch.ethz.csb.youscope.client.uielements.PeriodField;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.resource.dropletmicrofluidics.DropletControllerConfigurationAddon;

/**
 * @author Moritz Lang
 */
class EvaporationControllerUI extends ConfigurationAddonAdapter<EvaporationControllerConfiguration> implements DropletControllerConfigurationAddon<EvaporationControllerConfiguration>
{
	private final DoubleTextField maxDeltaFlowRateField = new DoubleTextField();
	private final DoubleTextField ratioHeightToVolumeField = new DoubleTextField();
	private final PeriodField timeConstantProportionalField = new PeriodField();
	private final PeriodField timeConstantIntegralField = new PeriodField();
	private int numFlowUnits = 0;
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
	
	static ConfigurationMetadataAdapter<EvaporationControllerConfiguration> getMetadata()
	{
		return new ConfigurationMetadataAdapter<EvaporationControllerConfiguration>(EvaporationControllerConfiguration.TYPE_IDENTIFIER, 
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
		useSyringeFields = new JCheckBox[numFlowUnits];
		boolean[] useSyringe = configuration.getUseSyringe();
		for(int i=0; i< useSyringeFields.length; i++)
		{
			useSyringeFields[i] = new JCheckBox("Flow Unit "+Integer.toString(i+1));
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
	public void setNumFlowUnits(int numFlowUnits) {
		this.numFlowUnits = numFlowUnits > 0 ? numFlowUnits : 0;
		if(isInitialized() && numFlowUnits != useSyringeFields.length)
		{
			boolean[] useSyringes = new boolean[useSyringeFields.length];
	    	for(int i=0; i<useSyringeFields.length; i++)
	    	{
	    		useSyringes[i] = useSyringeFields[i].isSelected();
	    	}
	    	flowUnitsPanel.removeAll();
	    	useSyringeFields = new JCheckBox[numFlowUnits];
	    	for(int i=0; i<numFlowUnits; i++)
	    	{
	    		useSyringeFields[i] = new JCheckBox("Flow Unit "+Integer.toString(i+1));
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
}
