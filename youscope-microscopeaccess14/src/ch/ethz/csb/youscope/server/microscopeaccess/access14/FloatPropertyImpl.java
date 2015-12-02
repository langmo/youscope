/**
 * 
 */
package ch.ethz.csb.youscope.server.microscopeaccess.access14;

import ch.ethz.csb.youscope.server.microscopeaccess.FloatPropertyInternal;
import ch.ethz.csb.youscope.shared.microscope.DeviceException;
import ch.ethz.csb.youscope.shared.microscope.PropertyType;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;

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
