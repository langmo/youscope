/**
 * 
 */
package org.youscope.common.microscope;

/**
 * An exception thrown if a device or device property is requested that is unknown.
 * @author langmo
 * 
 */
public class DeviceException extends Exception
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 6543409535468258294L;

	/**
	 * Constructor.
	 * 
	 * @param description Human readable description of the exception.
	 */
	public DeviceException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param description Human readable description of the exception.
	 * @param cause The cause of the exception.
	 */
	public DeviceException(String description, Exception cause)
	{
		super(description, cause);
	}
}
