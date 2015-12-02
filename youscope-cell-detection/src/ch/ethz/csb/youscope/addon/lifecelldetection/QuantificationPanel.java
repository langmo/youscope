package ch.ethz.csb.youscope.addon.lifecelldetection;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.uielements.DynamicPanel;
import ch.ethz.csb.youscope.client.uielements.JobsDefinitionPanel;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.configuration.ImageProducerConfiguration;

class QuantificationPanel extends DynamicPanel {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -8640406993680299322L;
	private final JobsDefinitionPanel jobsPanel;
	public QuantificationPanel(CellDetectionJobConfiguration configuration, YouScopeClient client, YouScopeServer server, YouScopeFrame parentFrame) 
	{
		jobsPanel = new JobsDefinitionPanel(client, server, parentFrame, ImageProducerConfiguration.class);
		addFill(jobsPanel);
		jobsPanel.setJobs(configuration.getJobs());
	}
	public void commitChanges(CellDetectionJobConfiguration configuration)
	{
		configuration.setJobs(jobsPanel.getJobs());
	}
}
