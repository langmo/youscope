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
package org.youscope.plugin.cellx;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * @author Moritz Lang
 *
 */
class CellXLastResultImpl extends UnicastRemoteObject implements CellXLastResult
{
	private final HashMap<String, Object> lastResult = new HashMap<String, Object>();
	
	/**
	 * SerializableVersion UID.
	 */
	private static final long	serialVersionUID	= 1110798742199344228L;

	/**
	 * Constructor.
	 * @throws RemoteException
	 */
	public CellXLastResultImpl() throws RemoteException
	{
		super();
	}

	@Override
	public synchronized void setData(String key, Object value) throws RemoteException
	{
		lastResult.put(key, value);
	}
	
	@Override
	public synchronized Object getData(String key) throws RemoteException
	{
		return lastResult.get(key);
	}
}
