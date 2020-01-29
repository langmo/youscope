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
