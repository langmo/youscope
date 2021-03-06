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
/**
 * 
 */
package org.youscope.uielements;

import java.util.EventListener;

import org.youscope.common.task.TaskConfiguration;

/**
 * Listener which gets notified if a task definition is finished.
 * @author langmo
 */
public interface TaskConfigurationListener extends EventListener
{
	/**
	 * Should be called when task definition is finished.
	 * @param taskConfiguration The configuration.
	 */
	void taskConfigurationFinished(TaskConfiguration taskConfiguration);
}
