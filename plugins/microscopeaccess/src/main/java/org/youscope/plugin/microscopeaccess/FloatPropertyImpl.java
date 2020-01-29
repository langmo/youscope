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

import org.youscope.addon.microscopeaccess.FloatPropertyInternal;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.PropertyType;

/**
 * @author Moritz Lang
 *
 */
class FloatPropertyImpl extends PropertyImpl implements FloatPropertyInternal
{
	private final float lowerLimit;
	private final float upperLimit;
	FloatPropertyImpl(MicroscopeImpl microscope, String deviceID, String propertyID, float lowerLimit, float upperLimit, boolean preInit, PropertyActionListener actionListener)
	{
		super(microscope, deviceID, propertyID, PropertyType.PROPERTY_FLOAT, true, preInit, actionListener);
		this.upperLimit = upperLimit;
		this.lowerLimit = lowerLimit;
	}

	@Override
	public float getFloatValue() throws MicroscopeException, NumberFormatException, InterruptedException
	{
		return Float.parseFloat(getValue());
	}
	
	@Override
	public void setValue(float value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
	{
		setStringValue(Float.toString(value), accessID);
	} 

	@Override
	public void setValueRelative(float offset, int accessID) throws MicroscopeException, MicroscopeLockedException, NumberFormatException, InterruptedException
	{
		setValue(getFloatValue() + offset, accessID);
	}

	@Override
	public void setValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException, DeviceException
	{
		try
		{
			setValue(Float.parseFloat(value), accessID);
		}
		catch(NumberFormatException e)
		{
			throw new DeviceException("Property " + getDeviceID() + "." + getPropertyID() + " can only be set to float values.", e);
		}
	}

	@Override
	public float getLowerLimit()
	{
		return lowerLimit;
	}

	@Override
	public float getUpperLimit()
	{
		return upperLimit;
	}
}
