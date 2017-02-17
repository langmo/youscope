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
package org.youscope.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * Interface to allow an object to send messages, e.g. to the YouScope logging mechanism or to whomever is interested.
 * @author Moritz Lang
 * 
 */
public interface MessageListener extends EventListener, Remote
{
	/**
	 * Sends a message to the listener.
	 * @param message The message to send.
	 * @throws RemoteException
	 */
	public void sendMessage(String message) throws RemoteException;
	
	/**
	 * Sends an error message to the listener.
	 * Should only be called when the error is produced autonomously, i.e. not as a direct response to a function call. If a function is called and an
	 * error occurs in the function call, an error should be thrown instead of an error message be send. A typical example when an error message would be send
	 * is when a thread is started as a response to a function call, and an error occurs in the thread while the original function call already succeeded.
	 * Another example are errors occurring as response to the user interacting with UI components.
	 * @param message The message to send.
	 * @param error The error which occurred.
	 * @throws RemoteException 
	 */
	public void sendErrorMessage(String message, Throwable error) throws RemoteException;
}
