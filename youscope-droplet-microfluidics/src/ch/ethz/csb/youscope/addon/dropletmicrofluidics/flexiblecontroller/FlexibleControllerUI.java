/**
 * 
 */
package ch.ethz.csb.youscope.addon.dropletmicrofluidics.flexiblecontroller;

import java.awt.Component;

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
class FlexibleControllerUI extends ConfigurationAddonAdapter<FlexibleControllerConfiguration> implements DropletControllerConfigurationAddon<FlexibleControllerConfiguration>
{
	
	private final DoubleTextField ratioHeightToVolumeField = new DoubleTextField();
	private final PeriodField timeConstantProportionalField = new PeriodField();
	private final PeriodField timeConstantIntegralField = new PeriodField();
	private int numFlowUnits = 0;
	
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
	
	static ConfigurationMetadataAdapter<FlexibleControllerConfiguration> getMetadata()
	{
		return new ConfigurationMetadataAdapter<FlexibleControllerConfiguration>(FlexibleControllerConfiguration.TYPE_IDENTIFIER, 
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
		
		syringeTableField = new FlexibleSyringeTable(getClient(), numFlowUnits);
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
	public void setNumFlowUnits(int numFlowUnits) {
		this.numFlowUnits = numFlowUnits > 0 ? numFlowUnits : 0;
		if(isInitialized())
			syringeTableField.setNumSyringes(this.numFlowUnits);
	}
}
