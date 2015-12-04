/**
 * 
 */
package org.youscope.common.microscope;


/**
 * Exception thrown when having problems with the drivers of the microscope.
 * @author langmo
 */
public class MicroscopeDriverException extends MicroscopeException
{

	/**
	 * SerialDeviceVersion UID.
	 */
	private static final long	serialVersionUID	= -3697167024422903422L;

	/**
	 * Constructor.
	 * 
	 * @param description Human readable description of the exception.
	 */
	public MicroscopeDriverException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param description Human readable description of the exception.
	 * @param cause The cause of the exception.
	 */
	public MicroscopeDriverException(String description, Exception cause)
	{
		super(description, cause);
	}
}
