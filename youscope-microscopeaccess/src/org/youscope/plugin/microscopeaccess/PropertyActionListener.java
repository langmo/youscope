/**
 * 
 */
package org.youscope.plugin.microscopeaccess;

import java.util.EventListener;

/**
 * Listener which gets notified whenever a command intended to change the state of a device (e.g. setPosition) returned. Intended to be used
 * for the internal event mechanism. Since
 * @author Moritz Lang
 *
 */
interface PropertyActionListener extends EventListener
{
	/**
	 * Called whenever a device performed an action.
	 */
	public void deviceStateModified();
}
