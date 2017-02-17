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
public interface AutoFocusDeviceInternal extends DeviceInternal
{
	/**
	 * Returns the last focus score.
	 * @return Last focus score.
	 * @throws MicroscopeException
	 */
	double getLastScore() throws MicroscopeException;

	/**
	 * Returns the current focus score
	 * @return Current focus score
	 * @throws MicroscopeException
	 */
	double getCurrentScore() throws MicroscopeException;

	/**
	 * Enables or disables the auto-focus device.
	 * @param enable True, if focus should be engabled.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 */
	void setEnabled(boolean enable, int accessID) throws MicroscopeException, MicroscopeLockedException;

	/**
	 * Returns true if the autofocus is enabled.
	 * @return True if enabled.
	 * @throws MicroscopeException
	 */
	boolean isEnabled() throws MicroscopeException;

	/**
	 * Returns true if auto-focus is locked.
	 * @return True if locked.
	 * @throws MicroscopeException
	 */
	boolean isLocked() throws MicroscopeException;

	/**
	 * Runs a full focus search.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 */
	void runFullFocus(int accessID) throws MicroscopeException, MicroscopeLockedException;

	/**
	 * Runs an incremental focus search.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 */
	void runIncrementalFocus(int accessID) throws MicroscopeException, MicroscopeLockedException;

	/**
	 * Sets the current offset.
	 * @param offset Offset to set.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 */
	void setOffset(double offset, int accessID) throws MicroscopeException, MicroscopeLockedException;

	/**
	 * Returns the current offset.
	 * @return Current offset.
	 * @throws MicroscopeException
	 */
	double getOffset() throws MicroscopeException;
}
