/**
 * 
 */
package org.youscope.common.measurement;

import org.youscope.common.ComponentException;

/**
 * Exception thrown if a measurement component should be changed while measurement is running.
 * 
 * @author Moritz Lang
 */
public class MeasurementRunningException extends ComponentException
{

	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= -3628890068985604340L;

	/**
	 * Constructor.
	 */
	public MeasurementRunningException()
	{
		this("Measurement component cannot be modified while measurement is running.");
	}
	/**
	 * Constructor.
	 * @param message Error message.
	 */
	public MeasurementRunningException(String message)
	{
		super(message);
	}
}
