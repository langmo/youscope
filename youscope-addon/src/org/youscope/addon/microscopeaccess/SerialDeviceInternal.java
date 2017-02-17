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
package org.youscope.addon.microscopeaccess;

import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;

/**
 * @author Moritz Lang
 *
 */
public interface SerialDeviceInternal extends DeviceInternal
{

	/**
	 * Sends a serial command to the corresponding port
	 * @param command The command to send. Use only ASCI
	 * @param accessID The access ID.
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	void sendCommand(String command, int accessID) throws MicroscopeLockedException, MicroscopeException, InterruptedException;
}
