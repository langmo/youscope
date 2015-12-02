/**
 * 
 */
package ch.ethz.csb.youscope.server.microscopeaccess.access14;

import ch.ethz.csb.youscope.server.microscopeaccess.StringPropertyInternal;
import ch.ethz.csb.youscope.shared.microscope.PropertyType;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;

/**
 * @author Moritz Lang
 *
 */
class StringPropertyImpl extends PropertyImpl implements StringPropertyInternal
{

	StringPropertyImpl(MicroscopeImpl microscope, String deviceID, String propertyID, boolean preInit, PropertyActionListener actionListener)
	{
		super(microscope, deviceID, propertyID, PropertyType.PROPERTY_STRING, true, preInit, actionListener);
	}

	@Override
	public void setValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
	{
		setStringValue(value, accessID);
	}

}
