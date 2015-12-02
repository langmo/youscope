/**
 * 
 */
package ch.ethz.csb.youscope.addon.lifecelldetection;

import java.awt.Component;

import javax.swing.JTabbedPane;

import ch.ethz.csb.youscope.client.addon.ConfigurationAddonAdapter;
import ch.ethz.csb.youscope.client.addon.ConfigurationMetadataAdapter;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.addon.AddonException;

/**
 * @author langmo
 */
public class CellDetectionJobConfigurationAddon extends ConfigurationAddonAdapter<CellDetectionJobConfiguration>
{	
	private InputImageConfigurationPanel inputImageConfigurationPanel;
	private DetectionAlgorithmConfigurationPanel detectionAlgorithmConfigurationPanel;
	private VisualizationAlgorithmConfigurationPanel visualizationAlgorithmConfigurationPanel;
	private QuantificationPanel quantificationPanel;
	private MiscPanel miscPanel;
	
	/**
	 * Constructor.
	 * @param client Interface to the client.
	 * @param server Interface to the server.
	 * @throws AddonException 
	 */
	public CellDetectionJobConfigurationAddon(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);
	}
	
	static ConfigurationMetadataAdapter<CellDetectionJobConfiguration> getMetadata()
	{
		return new ConfigurationMetadataAdapter<CellDetectionJobConfiguration>(CellDetectionJobConfiguration.TYPE_IDENTIFIER, 
				CellDetectionJobConfiguration.class, 
				CellDetectionJob.class, "Cell-Detection", new String[]{"feedback"}, "icons/smiley-mr-green.png");
	}
    
	@Override
	protected Component createUI(CellDetectionJobConfiguration configuration) throws AddonException 
	{
		setTitle("Life Cell Detection");
		setResizable(true);
		setMaximizable(false);
		
		inputImageConfigurationPanel = new InputImageConfigurationPanel(configuration, getClient(), getContainingFrame());
		detectionAlgorithmConfigurationPanel = new DetectionAlgorithmConfigurationPanel(configuration, getClient(), getContainingFrame());
		visualizationAlgorithmConfigurationPanel = new VisualizationAlgorithmConfigurationPanel(configuration, getClient(), getContainingFrame());
		quantificationPanel = new QuantificationPanel(configuration, getClient(), getServer(), getContainingFrame());
		miscPanel = new MiscPanel(configuration, getContainingFrame());
		
		JTabbedPane contentPane = new JTabbedPane(JTabbedPane.TOP);
        contentPane.addTab("Detection Image", inputImageConfigurationPanel);
        contentPane.addTab("Quantification Images", quantificationPanel);
        contentPane.addTab("Detection Algorithm", detectionAlgorithmConfigurationPanel);
        contentPane.addTab("Visualization Algorithm", visualizationAlgorithmConfigurationPanel);
        contentPane.addTab("Misc", miscPanel);
        
		return contentPane;
    }

	@Override
	protected void commitChanges(CellDetectionJobConfiguration configuration) {
		
		inputImageConfigurationPanel.commitChanges(configuration);
		detectionAlgorithmConfigurationPanel.commitChanges(configuration);
		visualizationAlgorithmConfigurationPanel.commitChanges(configuration);
		quantificationPanel.commitChanges(configuration);
		miscPanel.commitChanges(configuration);
	}
}
