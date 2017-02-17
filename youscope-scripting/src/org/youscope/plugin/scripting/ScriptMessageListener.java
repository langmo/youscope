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

/**
 * @author langmo
 *
 */
interface ScriptMessageListener extends EventListener
{
	/**
	 * Executed if engine produced a text message due to the execution of one or more lines of code.
	 * @param message Message the engine did send.
	 */
	public void outputMessage(String message);
	
	/**
	 * Executed if code was send to the engine to be executed.
	 * @param message Code which will be executed.
	 */
	public void inputMessage(String message);
}
