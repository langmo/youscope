/**
 * 
 */
package ch.ethz.csb.youscope.client.uielements;

import java.util.EventListener;

import ch.ethz.csb.youscope.shared.configuration.TaskConfiguration;

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
