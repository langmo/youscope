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
public interface ShutterDeviceInternal extends DeviceInternal
{
	/**
	 * Opens (open == true) or closes (open == false) the shutter.
	 * @param open True if the shutter should be opened, false if it should be closed.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws InterruptedException 
	 */
	void setOpen(boolean open, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException;
	
	/**
	 * Returns true if the shutter is open, and false if it is closed.
	 * @return True if shutter is open, false otherwise.
	 * @throws MicroscopeException 
	 * @throws InterruptedException 
	 */
	boolean isOpen() throws MicroscopeException, InterruptedException;
}
