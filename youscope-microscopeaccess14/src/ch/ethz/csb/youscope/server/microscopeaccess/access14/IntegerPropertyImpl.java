/**
 * 
 */
package ch.ethz.csb.youscope.server.microscopeaccess.access14;

import ch.ethz.csb.youscope.server.microscopeaccess.IntegerPropertyInternal;
import ch.ethz.csb.youscope.shared.microscope.DeviceException;
import ch.ethz.csb.youscope.shared.microscope.PropertyType;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;

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
