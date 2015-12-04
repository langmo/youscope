/**
 * 
 */
package org.youscope.common.measurement;

/**
 * Base class of all exceptions thrown by measurement components.
 * @author Moritz Lang
 * 
 */
public abstract class ComponentException extends Exception
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= -5853816701125425656L;

	/**
	 * Constructor.
	 * 
	 * @param description
	 */
	public ComponentException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 */
	public ComponentException(Throwable parent)
	{
		super(parent);
	}

	/**
	 * Constructor.
	 * 
	 * @param description
	 * @param parent
	 */
	public ComponentException(String description, Throwable parent)
	{
		super(description, parent);
	}
}
