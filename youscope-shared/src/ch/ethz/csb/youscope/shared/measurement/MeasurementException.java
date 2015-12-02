/**
 * 
 */
package ch.ethz.csb.youscope.shared.measurement;

/**
 * Exception thrown by a measurement to indicate that its initialization, execution or deinitialization failed.
 * @author Moritz Lang
 * 
 */
public class MeasurementException extends ComponentException
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 5219435175494966494L;

	/**
	 * Constructor.
	 * 
	 * @param description Description of the error.
	 */
	public MeasurementException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent Parent exception.
	 */
	public MeasurementException(Throwable parent)
	{
		super(parent);
	}

	/**
	 * Constructor.
	 * 
	 * @param description Description of the error.
	 * @param parent Parent exception.
	 */
	public MeasurementException(String description, Throwable parent)
	{
		super(description, parent);
	}
}
