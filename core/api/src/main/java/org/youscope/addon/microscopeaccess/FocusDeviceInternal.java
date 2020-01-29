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
 * @author langmo
 * 
 */
public interface FocusDeviceInternal extends DeviceInternal
{
	/**
	 * Returns the position of the focus device.
	 * 
	 * @return Position of the focus device.
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	double getFocusPosition() throws MicroscopeException, InterruptedException;

	/**
	 * Sets the position of the current focus device.
	 * 
	 * @param position The new position.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	void setFocusPosition(double position, int accessID) throws MicroscopeLockedException, MicroscopeException, InterruptedException;

	/**
	 * Sets the position of the current focus device relative to the current
	 * focus.
	 * 
	 * @param offset The offset.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	void setRelativeFocusPosition(double offset, int accessID) throws MicroscopeLockedException, MicroscopeException, InterruptedException;
}
