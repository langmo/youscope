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
package org.youscope.addon.microscopeaccess;


import java.util.Date;

import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;

/**
 * Interface represents a physical microscope device and allows setting and getting values of it.
 * @author langmo
 */
public interface DeviceInternal
{
	/**
	 * Returns the name of the device.
	 * @return Name of device.
	 */
	public String getDeviceID();

	/**
	 * Returns the library name in which this device's driver is defined.
	 * @return Name of device driver library.

	 */
	public String getLibraryID();
	
	/**
	 * Returns the Identifier under which this device's driver is defined in the library.
	 * @return Device's driver identifier.
	 */
	public String getDriverID();
	
	/**
	 * Returns the type of this device.
	 * @return Type of the device.
	 */
	public DeviceType getType();

	/**
	 * Returns a list of all properties of this device.
	 * @return List of properties.
	 */
	public PropertyInternal[] getProperties();

	/**
	 * Returns a list of all editable properties of this device.
	 * @return List of editable properties.
	 */
	public PropertyInternal[] getEditableProperties();
	
	/**
	 * Returns the property of this device with the given name, or null if this device does not have a property with this name.
	 * @param propertyID Name of the property.
	 * @return the property of this device with the given name, or null.
	 * @throws DeviceException
	 */
	public PropertyInternal getProperty(String propertyID) throws DeviceException;
	
	/**
	 * Waits for the device to have finished its latest actions before returning. 
	 * Some devices give a notification if they have finished their actions, then it is waited for this notification. 
	 * Devices which do not support this handshaking mechanism return immediately, even if they still complete an action, except an explicit delay greater 0 is set.
	 * 
	 * For all devices with explicit delays greater zero up to two mechanisms may become active: 
	 * 1) Some devices support explicit delays intrinsically. Then the set explicit delay is submitted to them and they handle this explicit delay in a driver specific way.
	 * 2) For all devices, including devices supporting the handshaking mechanism and those supporting explicit delays on their own,
	 * YouScope guarantees that at least the given time is waited since the last transmission of a command intended to change the state of the device.
	 * A changing command is a command intended to change the state of the device (e.g. setPosition(), setState(), ...), but not command intended to only
	 * read out the state of the device (e.g. getPosition(), getDeviceID(), ...), although also the latter ones might trigger some device actions for badly programmed device drivers
	 * (but usually don't). Furthermore, this mechanism only works if a device action was triggered by YouScopeClient (e.g. changes by directly manipulating the hardware or by
	 * driver-driver communication are not included).  
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	public void waitForDevice() throws MicroscopeException, InterruptedException;
	
	/**
	 * Returns the explicit device delay in ms.
	 * See waitForDevice() for more information.
	 * @return Device delay in ms.
	 */
	public double getExplicitDelay();
	
	/**
	 * Sets the explicit delay in ms.
	 * See waitForDevice() for more information.
	 * @param delay Delay iin ms.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 */
	public void setExplicitDelay(double delay, int accessID) throws MicroscopeException, MicroscopeLockedException;

	/**
	 * Returns the time when the device was initialized.
	 * @return Time when device was initialized.
	 */
	public Date getInitializationTime();
}
