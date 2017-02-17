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
