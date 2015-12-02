/**
 * 
 */
package ch.ethz.csb.youscope.server.microscopeaccess;


import ch.ethz.csb.youscope.shared.microscope.PropertyType;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeDriverException;

/**
 * @author langmo
 * 
 */
public interface PreInitDevicePropertyInternal
{
	
	/**
	 * Returns the default value for this property.
	 * @return Default value.
	 * @throws MicroscopeDriverException
	 */
	public String getDefaultValue() throws MicroscopeDriverException;
	
	/**
	 * Returns the name of the device property.
	 * @return Name of property.
	 * @throws MicroscopeDriverException
	 */
	public String getPropertyID() throws MicroscopeDriverException;

	/**
	 * Returns the type of this device property.
	 * @return Type of property.
	 * @throws MicroscopeDriverException

	 */
	public PropertyType getType() throws MicroscopeDriverException;

	/**
	 * Returns a list of all allowed property values. If all possible values are allowed, the allowed values are not known, or the allowed values are not discrete, returns null.
	 * @return List of all allowed values or null.
	 * @throws MicroscopeDriverException
	 */
	public String[] getAllowedPropertyValues() throws MicroscopeDriverException;

}
