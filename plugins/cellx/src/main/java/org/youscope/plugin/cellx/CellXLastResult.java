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

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Moritz Lang
 *
 */
interface CellXLastResult extends Remote
{

	/**
	 * Sets the specific key value pair.
	 * @param key
	 * @param value
	 * @throws RemoteException
	 */
	void setData(String key, Object value) throws RemoteException;

	/**
	 * Returns the value for the specific key, or null.
	 * @param key
	 * @return value for key, or null.
	 * @throws RemoteException
	 */
	Object getData(String key) throws RemoteException;
	
}
