package ch.ethz.csb.youscope.addon.lifecelldetection;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.uielements.SingleComponentDefinitionPanel;
import ch.ethz.csb.youscope.shared.addon.celldetection.CellDetectionConfiguration;

class DetectionAlgorithmConfigurationPanel  extends SingleComponentDefinitionPanel<CellDetectionConfiguration> {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 8441062694566624365L;

	public DetectionAlgorithmConfigurationPanel(CellDetectionJobConfiguration configuration, YouScopeClient client, YouScopeFrame parentFrame) 
	{
		super(CellDetectionConfiguration.class, configuration.getDetectionAlgorithmConfiguration(), client, parentFrame);
		setLabel("Cell detection algorithm:");
	}
	public void commitChanges(CellDetectionJobConfiguration configuration)
	{
		configuration.setDetectionAlgorithmConfiguration(getConfiguration());
	}
}
