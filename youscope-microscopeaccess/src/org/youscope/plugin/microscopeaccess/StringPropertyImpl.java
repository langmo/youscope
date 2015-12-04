/**
 * 
 */
package org.youscope.plugin.microscopeaccess;

import org.youscope.addon.microscopeaccess.StringPropertyInternal;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.PropertyType;

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
