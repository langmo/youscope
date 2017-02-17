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

import org.youscope.addon.microscopeaccess.ReadOnlyPropertyInternal;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.PropertyType;

/**
 * @author Moritz Lang
 *
 */
class ReadOnlyPropertyImpl extends PropertyImpl implements ReadOnlyPropertyInternal
{
	ReadOnlyPropertyImpl(MicroscopeImpl microscope, String deviceID, String propertyID, boolean preInit, PropertyActionListener actionListener)
	{
		super(microscope, deviceID, propertyID, PropertyType.PROPERTY_READ_ONLY, false, preInit, actionListener);
	}

	@Override
	public void setValue(String value, int accessID) throws DeviceException
	{
		throw new DeviceException("Property " + getDeviceID() + "." + getPropertyID() + " is read only.");
	}

}
