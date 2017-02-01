package org.youscope.common.task;

import org.youscope.common.ComponentException;

/**
 * Exception thrown by a task to indicate that its initialization, execution or deinitialization failed.
 * @author Moritz Lang
 * 
 */
public class TaskException extends ComponentException
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 5218835175494966494L;

	/**
	 * Constructor.
	 * 
	 * @param description Description of the error.
	 */
	public TaskException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent Parent exception.
	 */
	public TaskException(Throwable parent)
	{
		super(parent);
	}

	/**
	 * Constructor.
	 * 
	 * @param description Description of the error.
	 * @param parent Parent exception.
	 */
	public TaskException(String description, Throwable parent)
	{
		super(description, parent);
	}
}
