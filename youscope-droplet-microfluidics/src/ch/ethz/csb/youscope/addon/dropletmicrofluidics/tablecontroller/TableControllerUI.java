/**
 * 
 */
package ch.ethz.csb.youscope.addon.dropletmicrofluidics.tablecontroller;

import java.awt.Component;

import javax.swing.JComboBox;
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
class TableControllerUI extends ConfigurationAddonAdapter<TableControllerConfiguration> implements DropletControllerConfigurationAddon<TableControllerConfiguration>
{
	
	private final DoubleTextField targetFlowRateField = new DoubleTextField();
	private final DoubleTextField maxDeltaFlowRateField = new DoubleTextField();
	private final JComboBox<String> correctionMethod = new JComboBox<String>(new String[]{"Correct by inflow", "Correct by outflow"});
	private final DoubleTextField ratioHeightToVolumeField = new DoubleTextField();
	private final PeriodField timeConstantProportionalField = new PeriodField();
	private final PeriodField timeConstantIntegralField = new PeriodField();
	private int numFlowUnits = 0;
	
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
	
	static ConfigurationMetadataAdapter<TableControllerConfiguration> getMetadata()
	{
		return new ConfigurationMetadataAdapter<TableControllerConfiguration>(TableControllerConfiguration.TYPE_IDENTIFIER, 
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
		
		syringeTableField = new SyringeTable(getClient(), numFlowUnits);
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
	public void setNumFlowUnits(int numFlowUnits) {
		this.numFlowUnits = numFlowUnits > 0 ? numFlowUnits : 0;
		if(isInitialized())
			syringeTableField.setNumSyringes(this.numFlowUnits);
	}
}
