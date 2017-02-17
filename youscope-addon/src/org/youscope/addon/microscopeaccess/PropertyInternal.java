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

import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.PropertyType;

/**
 * Represents a property of a device.
 * @author langmo
 */
public interface PropertyInternal
{
	/**
	 * Returns the name of the device where this property belongs to.
	 * @return Name of device.
	 */
	public String getDeviceID();
	
	/**
	 * Returns the name of the device property.
	 * @return Name of property.
	 */
	public String getPropertyID();

	/**
	 * Returns the type of this device property.
	 * @return Type of property.
	 */
	public PropertyType getType();

	/**
	 * Returns the current value of the device property as a string.
	 * @return Value of property.
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	public String getValue() throws MicroscopeException, InterruptedException;
	
	/**
	 * Sets the current value of the property. Throws a device exception if the value is not valid (i.e. if the property is an integer property and the value is not an integer),
	 * or if the property is not editable.
	 * @param value Value to set the property to.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws InterruptedException
	 * @throws DeviceException Thrown if value does not correspond to property type.
	 */
	public void setValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException, DeviceException;
	
	/**
	 * Returns true if property is editable and false if not.
	 * @return True if editable, otherwise false.
	 */
	public boolean isEditable();

	/**
	 * Returns true if the value of this property has to be set prior to initialization
	 * @return True if pre-init property, false otherwise.
	 */
	public boolean isPreInitializationProperty();
}
