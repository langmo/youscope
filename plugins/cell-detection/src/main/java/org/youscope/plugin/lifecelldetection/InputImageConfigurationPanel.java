/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
package org.youscope.plugin.lifecelldetection;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.common.image.ImageProducerConfiguration;
import org.youscope.common.job.JobConfiguration;
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
