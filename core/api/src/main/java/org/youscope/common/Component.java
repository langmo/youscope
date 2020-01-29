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
package org.youscope.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.UUID;

/**
 * Represents the base interface of all components of a measurement, that is, the measurement itself, its tasks, the jobs and any
 * eventually used resources.
 * @author Moritz Lang
 *
 */
public interface Component extends Remote 
{
	/**
	 * Adds a message listener to which (non-error) status information is send.
	 * @param writer Listener to which output should be written.
	 * @throws RemoteException 
	 */
	public void addMessageListener(MessageListener writer) throws RemoteException;
	
	/**
	 * Removes a previously added listener.
	 * @param writer Listener which should be removed.
	 * @throws RemoteException 
	 */
	public void removeMessageListener(MessageListener writer) throws RemoteException;
	/**
	 * Returns the component's position information, e.g. in which well, tile, or z-stack location it is.
	 * 
	 * @return Position information vector.
	 * @throws RemoteException
	 */
	public PositionInformation getPositionInformation() throws RemoteException;
	
	/**
	 * Returns a name of the component meant for human interpretation.
	 * @return Name/short description of component.
	 * @throws RemoteException
	 */
	public String getName() throws RemoteException;

	/**
	 * Sets the name of the component meant for human interpretation.
	 * Usually, the default name of a component contains already sufficient information. However, for some measurement types it can be
	 * of advantage to rename certain components which have a specific meaning for the respective measurement.
	 * @param name Name/short description of the component.
	 * @throws RemoteException
	 * @throws ComponentRunningException 
	 */
	public void setName(String name) throws RemoteException, ComponentRunningException;
	
	/**
	 * Returns a unique ID of the component. No two components have the same unique ID, even if they are created using the same configuration.
	 * See {@link UUID#equals(Object)} and {@link UUID#randomUUID()}.
	 * @return Unique ID of the component.
	 * @throws RemoteException
	 */
	public UUID getUUID() throws RemoteException;
}
