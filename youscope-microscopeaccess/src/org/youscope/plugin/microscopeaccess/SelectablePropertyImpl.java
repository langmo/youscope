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
package org.youscope.plugin.microscopeaccess;

import java.util.Arrays;

import org.youscope.addon.microscopeaccess.SelectablePropertyInternal;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.PropertyType;

/**
 * @author Moritz Lang
 *
 */
class SelectablePropertyImpl extends PropertyImpl implements SelectablePropertyInternal
{

	private final String[] allowedPropertyValues;
	SelectablePropertyImpl(MicroscopeImpl microscope, String deviceID, String propertyID, String[] allowedPropertyValues, boolean preInit, PropertyActionListener actionListener)
	{
		super(microscope, deviceID, propertyID, PropertyType.PROPERTY_SELECTABLE, true, preInit, actionListener);
		this.allowedPropertyValues = allowedPropertyValues;
	}

	@Override
	public String[] getAllowedPropertyValues()
	{
		return Arrays.copyOf(allowedPropertyValues, allowedPropertyValues.length);
	}

	@Override
	public void setValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException, DeviceException
	{
		boolean found = false;
		for(String allowedValue : getAllowedPropertyValues())
		{
			if(allowedValue.equals(value))
			{
				found = true;
				break;
			}
		}
		if(!found)
			throw new DeviceException("Value for property " + getDeviceID() + "." + getPropertyID() + " is not one of the allowed values. Query getAllowedPropertyValues() to obtain allowed values.");
		setStringValue(value, accessID);
	}

}
