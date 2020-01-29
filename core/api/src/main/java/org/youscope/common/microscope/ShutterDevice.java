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
package org.youscope.common.microscope;

import java.rmi.RemoteException;

/**
 * @author Moritz Lang
 * 
 */
public interface ShutterDevice extends Device
{
	/**
	 * Opens (open == true) or closes (open == false) the shutter.
	 * @param open True if the shutter should be opened, false if it should be closed.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws InterruptedException
	 * @throws RemoteException
	 */
	void setOpen(boolean open) throws MicroscopeException, MicroscopeLockedException, InterruptedException, RemoteException;

	/**
	 * Returns true if the shutter is open, and false if it is closed.
	 * @return True if shutter is open, false otherwise.
	 * @throws MicroscopeException
	 * @throws RemoteException
	 * @throws InterruptedException
	 */
	boolean isOpen() throws MicroscopeException, RemoteException, InterruptedException;
}
