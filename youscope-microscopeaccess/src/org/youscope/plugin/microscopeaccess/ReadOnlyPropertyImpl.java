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
