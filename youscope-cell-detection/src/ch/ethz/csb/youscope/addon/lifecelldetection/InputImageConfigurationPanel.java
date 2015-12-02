package ch.ethz.csb.youscope.addon.lifecelldetection;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.uielements.SingleComponentDefinitionPanel;
import ch.ethz.csb.youscope.shared.configuration.ImageProducerConfiguration;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

class InputImageConfigurationPanel extends SingleComponentDefinitionPanel<JobConfiguration>
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -367954181545593023L;

	public InputImageConfigurationPanel(CellDetectionJobConfiguration configuration, YouScopeClient client, YouScopeFrame parentFrame) 
	{
		super(JobConfiguration.class, configuration.getDetectionJob(), client, parentFrame, ImageProducerConfiguration.class);
		setLabel("Select detection image job:");
	}
	public void commitChanges(CellDetectionJobConfiguration configuration)
	{
		configuration.setDetectionJob(getConfiguration());
	}
}
