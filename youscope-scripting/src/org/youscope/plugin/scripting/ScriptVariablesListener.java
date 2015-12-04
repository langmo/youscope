/**
 * 
 */
package org.youscope.plugin.scripting;

import java.util.EventListener;
import java.util.Set;
import java.util.Map.Entry;

/**
 * @author langmo
 *
 */
interface ScriptVariablesListener extends EventListener
{
	/**
	 * Event executed if the variables defined in the script engine might have changed, e.g. due to the execution of one or
	 * more lines of script.
	 * @param variables The variables defined in the script engine.
	 */
	public void variablesChanged(Set<Entry<String, Object>> variables);
}
