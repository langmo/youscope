package org.youscope.plugin.lifecelldetection;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.configuration.ImageProducerConfiguration;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.DynamicPanel;
import org.youscope.uielements.JobsDefinitionPanel;

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
