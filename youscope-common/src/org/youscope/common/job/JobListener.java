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
package org.youscope.common.job;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

import org.youscope.common.ExecutionInformation;

/**
 * A listener to get informed about the state of a job.
 * @author langmo
 * 
 */
public interface JobListener extends EventListener, Remote
{
	/**
	 * Called when job is initialized.
	 * @throws RemoteException
	 */
	void jobInitialized() throws RemoteException;

	/**
	 * Called when job is uninitialized.
	 * Might be called more than once.
	 * @throws RemoteException
	 */
	void jobUninitialized() throws RemoteException;

	/**
	 * Called when evaluation of job is started.
	 * @param executionInformation Information about the number of the execution of the job.
	 * @throws RemoteException
	 */
	void jobStarted(ExecutionInformation executionInformation) throws RemoteException;

	/**
	 * Called when one evaluation of the job is finished.
	 * @param executionInformation Information about the number of the execution of the job.
	 * @throws RemoteException
	 */
	void jobFinished(ExecutionInformation executionInformation) throws RemoteException;
}
