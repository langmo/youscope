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


import org.youscope.common.microscope.MicroscopeDriverException;
import org.youscope.common.microscope.PropertyType;

/**
 * @author langmo
 * 
 */
public interface PreInitDevicePropertyInternal
{
	
	/**
	 * Returns the default value for this property.
	 * @return Default value.
	 * @throws MicroscopeDriverException
	 */
	public String getDefaultValue() throws MicroscopeDriverException;
	
	/**
	 * Returns the name of the device property.
	 * @return Name of property.
	 * @throws MicroscopeDriverException
	 */
	public String getPropertyID() throws MicroscopeDriverException;

	/**
	 * Returns the type of this device property.
	 * @return Type of property.
	 * @throws MicroscopeDriverException

	 */
	public PropertyType getType() throws MicroscopeDriverException;

	/**
	 * Returns a list of all allowed property values. If all possible values are allowed, the allowed values are not known, or the allowed values are not discrete, returns null.
	 * @return List of all allowed values or null.
	 * @throws MicroscopeDriverException
	 */
	public String[] getAllowedPropertyValues() throws MicroscopeDriverException;

}
