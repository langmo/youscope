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
package org.youscope.plugin.onix;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.job.Job;
import org.youscope.common.table.TableConsumer;

/**
 * A job to control the CellAsic Onix microfluidic system.
 * @author Moritz Lang
 */
public interface OnixJob extends Job, TableConsumer
{
	/**
	 * Sets the protocol which gets evaluated on the onix device every time the job gets evaluated.
	 * @param onixProtocol The onix protocol.
	 * @throws RemoteException 
	 * @throws ComponentRunningException 
	 */
	public void setOnixProtocol(String onixProtocol) throws RemoteException, ComponentRunningException;

	/**
	 * Returns the onix protocol.
	 * @return Onix protocol.
	 * @throws RemoteException 
	 */
	public String getOnixProtocol() throws RemoteException;

	/**
	 * If true, the job waits when evaluating the onix protocol until the protocol is finished. If false, the onix protocol gets evaluated in parallel.
	 * @param waitUntilFinished True if job should wait until end of onix protocol evaluation.
	 * @throws RemoteException 
	 * @throws ComponentRunningException 
	 */
	public void setWaitUntilFinished(boolean waitUntilFinished) throws RemoteException, ComponentRunningException;

	/**
	 * If true, the job waits when evaluating the onix protocol until the protocol is finished. If false, the onix protocol gets evaluated in parallel.
	 * @return True if job should wait until end of onix protocol evaluation.
	 * @throws RemoteException 
	 */
	public boolean isWaitUntilFinished() throws RemoteException;
}
