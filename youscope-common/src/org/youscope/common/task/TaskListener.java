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
package org.youscope.common.task;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * A listener getting notified when execution of a task starts and when it finishes.
 * @author Moritz Lang
 * 
 */
public interface TaskListener extends EventListener, Remote
{	

	/**
	 * Called when the first job of a task starts to be executed.
	 * @param executionNumber The number of times the task has already been executed, starting at zero.
	 * @throws RemoteException
	 */
	void taskStarted(long executionNumber) throws RemoteException;
	
	/**
	 * Called when the last job of a task stops to be executed.
	 * @param executionNumber The number of times the task has already been executed, starting at zero.
	 * @throws RemoteException
	 */
	void taskFinished(long executionNumber) throws RemoteException;
}
