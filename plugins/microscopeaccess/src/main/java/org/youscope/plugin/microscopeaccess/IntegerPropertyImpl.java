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

import org.youscope.addon.microscopeaccess.IntegerPropertyInternal;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.PropertyType;

/**
 * @author Moritz Lang
 *
 */
class IntegerPropertyImpl extends PropertyImpl implements IntegerPropertyInternal
{
	private final int lowerLimit;
	private final int upperLimit;
	IntegerPropertyImpl(MicroscopeImpl microscope, String deviceID, String propertyID, int lowerLimit, int upperLimit, boolean preInit, PropertyActionListener actionListener)
	{
		super(microscope, deviceID, propertyID, PropertyType.PROPERTY_INTEGER, true, preInit, actionListener);
		this.lowerLimit = lowerLimit;
		this.upperLimit = upperLimit;
	}

	@Override
	public int getIntegerValue() throws MicroscopeException, NumberFormatException, InterruptedException
	{
		return Integer.parseInt(getValue());
	}
	
	@Override
	public int getUpperLimit()
	{
		return upperLimit;
	}
	
	@Override
	public int getLowerLimit()
	{
		return lowerLimit;
	}
	
	@Override
	public void setValue(int value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
	{
		setStringValue(Integer.toString(value), accessID);
	}
	@Override
	public void setValueRelative(int offset, int accessID) throws MicroscopeException, MicroscopeLockedException, NumberFormatException, InterruptedException
	{
		setValue(getIntegerValue() + offset, accessID);
	}

	@Override
	public void setValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException, DeviceException
	{
		try
		{
			setValue(Integer.parseInt(value), accessID);
		}
		catch(NumberFormatException e)
		{
			throw new DeviceException("Property " + getDeviceID() + "." + getPropertyID() + " can only be set to integer values.", e);
		}
	}
}
