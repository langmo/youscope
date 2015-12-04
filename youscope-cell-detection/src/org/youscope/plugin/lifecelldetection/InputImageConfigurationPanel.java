package org.youscope.plugin.lifecelldetection;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.configuration.ImageProducerConfiguration;
import org.youscope.common.configuration.JobConfiguration;
import org.youscope.uielements.SingleComponentDefinitionPanel;

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
