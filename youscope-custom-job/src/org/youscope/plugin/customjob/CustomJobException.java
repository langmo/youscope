/**
 * 
 */
package org.youscope.plugin.customjob;

/**
 * Exception thrown by a job to indicate that initializing a custom job, creating or deleting it, or similar, failed.
 * @author Moritz Lang
 * 
 */
public class CustomJobException extends Exception
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= -5888816707725425656L;

	/**
	 * Constructor.
	 * 
	 * @param description
	 */
	public CustomJobException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 */
	public CustomJobException(Throwable parent)
	{
		super(parent);
	}

	/**
	 * Constructor.
	 * 
	 * @param description
	 * @param parent
	 */
	public CustomJobException(String description, Throwable parent)
	{
		super(description, parent);
	}
}
