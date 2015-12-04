/**
 * 
 */
package org.youscope.uielements;

import java.util.EventListener;

import org.youscope.common.configuration.TaskConfiguration;

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
