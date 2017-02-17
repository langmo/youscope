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
public interface SerialDevice extends Device
{
	/**
	 * Sends a serial command to the corresponding port
	 * @param command The command to send. Use only ASCI
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 * @throws RemoteException
	 */
	void sendCommand(String command) throws MicroscopeLockedException, MicroscopeException, InterruptedException, RemoteException;
}
