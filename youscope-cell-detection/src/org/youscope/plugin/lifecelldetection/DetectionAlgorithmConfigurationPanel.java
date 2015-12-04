package org.youscope.plugin.lifecelldetection;

import org.youscope.addon.celldetection.CellDetectionConfiguration;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.uielements.SingleComponentDefinitionPanel;

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
