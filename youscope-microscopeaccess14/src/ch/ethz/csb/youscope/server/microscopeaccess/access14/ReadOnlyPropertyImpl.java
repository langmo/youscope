/**
 * 
 */
package ch.ethz.csb.youscope.server.microscopeaccess.access14;

import ch.ethz.csb.youscope.server.microscopeaccess.ReadOnlyPropertyInternal;
import ch.ethz.csb.youscope.shared.microscope.DeviceException;
import ch.ethz.csb.youscope.shared.microscope.PropertyType;

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
