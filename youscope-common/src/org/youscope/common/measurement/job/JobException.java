/**
 * 
 */
package org.youscope.common.measurement.job;

import org.youscope.common.measurement.ComponentException;

/**
 * Exception thrown by a job to indicate that its initialization, execution or uninitialization failed.
 * @author Moritz Lang
 * 
 */
public class JobException extends ComponentException
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= -5853816707725425656L;

	/**
	 * Constructor.
	 * 
	 * @param description
	 */
	public JobException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 */
	public JobException(Throwable parent)
	{
		super(parent);
	}

	/**
	 * Constructor.
	 * 
	 * @param description
	 * @param parent
	 */
	public JobException(String description, Throwable parent)
	{
		super(description, parent);
	}
}
