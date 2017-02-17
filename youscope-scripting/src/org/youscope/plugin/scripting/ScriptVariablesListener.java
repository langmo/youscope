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
