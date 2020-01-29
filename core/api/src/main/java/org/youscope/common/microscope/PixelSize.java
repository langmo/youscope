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

import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * @author Moritz Lang
 * 
 */
public interface PixelSize extends Remote
{
	/**
	 * Returns the ID of the pixel size setting.
	 * @return The ID of the pixel size setting.
	 * @throws RemoteException
	 */
	public String getPixelSizeID() throws RemoteException;

	/**
	 * Returns all device settings corresponding to this pixel size.
	 * @return Set of device settings necessary for the pixel size setting to get active.
	 * @throws RemoteException
	 */
	DeviceSetting[] getPixelSizeSettings() throws RemoteException;

	/**
	 * Sets all device settings corresponding to this pixel size. All previously set settings get deleted.
	 * @param newSettings device settings which have to be active for this pixel size setting to be actual.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException
	 * @throws SettingException
	 * @throws RemoteException
	 */
	void setPixelSizeSettings(DeviceSetting[] newSettings) throws MicroscopeLockedException, SettingException, RemoteException;

	/**
	 * Adds a setting to the list of settings.
	 * @param setting Setting to add.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException
	 * @throws SettingException
	 * @throws RemoteException
	 */
	void addPixelSizeSetting(DeviceSetting setting) throws MicroscopeLockedException, SettingException, RemoteException;

	/**
	 * Returns the pixel size in micro meters.
	 * @return Pixel size in mico meters.
	 * @throws RemoteException
	 */
	double getPixelSize() throws RemoteException;

	/**
	 * Sets the pixel size in micro meters.
	 * @param pixelSize Pixel size in mico meters.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException
	 * @throws SettingException
	 * @throws RemoteException
	 */
	void setPixelSize(double pixelSize) throws MicroscopeLockedException, SettingException, RemoteException;
}
