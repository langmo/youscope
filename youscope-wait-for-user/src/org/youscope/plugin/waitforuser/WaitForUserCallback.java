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
package org.youscope.plugin.waitforuser;

import java.rmi.RemoteException;

import org.youscope.common.callback.Callback;

/**
 * Simple measurement callback to show a message and wait for the user to acknowledge it.
 * @author Moritz Lang
 *
 */
public interface WaitForUserCallback extends Callback
{
	/**
	 * Type identifier of callback.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.WaitForUserJob.Callback";
	/**
	 * Displays the message and returns after user acknowledgment.
	 * @param message Message to display
	 * @throws RemoteException 
	 * @throws InterruptedException 
	 */
	public void waitForUser(String message) throws RemoteException, InterruptedException;
}
