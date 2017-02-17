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
package org.youscope.common.callback;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * A measurement callback can be used by a measurement to gain control over functionality at creation time.
 * Typically, it is used by measurements (which run on the YouScope's server) to be able to trigger certain behavior on the client
 * side, e.g. to wait for a certain user input. The client therefore passes the provider to the server when creating a new measurement.
 * Measurements or jobs can then ask the provider for a certain functionality, represented by a type identifier. If the functionality is available,
 * an object implementing the interface is returned, which can be used for communication proposes.
 * 
 * In general, measurements should not depend on callbacks. A measurement should run on the server even if a callback is not available, or if the connection
 * to the client breaks down (e.g. because the client is closed, while the server should still run).
 * Furthermore, the callback should not perform parts of the measurement itself, like controlling the microscope, or similar.
 * 
 * @author Moritz Lang
 * 
 */
public interface Callback extends Remote  
{
	/**
	 * Checks the connection to the callback. The implementation should be empty, i.e. return immediately without any action.
	 * The function can thus be used by a measurement to check if the callback is still available.
	 * @throws RemoteException
	 */
	public void pingCallback() throws RemoteException;

	/**
	 * Has to be called by the measurement component upon initialization prior to using any functionality of the callback (except {@link Callback#pingCallback()}).
	 * @param arguments Optional arguments to be send to the callback.
	 * @throws RemoteException
	 * @throws CallbackException
	 */
	public void initializeCallback(Serializable... arguments) throws RemoteException, CallbackException;

	/**
	 * Has to be called by the measurement component upon uninitialization. After calling this function, no functionality of the callback is allowed to be used anymore (except {@link Callback#pingCallback()}) until
	 * re-initialization.
	 * @throws RemoteException
	 * @throws CallbackException
	 */
	public void uninitializeCallback() throws RemoteException, CallbackException;
	
	/**
	 * Returns an identifier of the type of this callback.
	 * @return Type identifier of callback.
	 * @throws RemoteException
	 */
	public String getTypeIdentifier() throws RemoteException;
}
