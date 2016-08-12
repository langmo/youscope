/**
 * 
 */
package org.youscope.plugin.custommicroplates;

/**
 * Exception thrown to indicate that initializing a custom microplate, creating or deleting it, or similar, failed.
 * @author Moritz Lang
 * 
 */
public class CustomMicroplateException extends Exception
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 1834665729033073553L;

	/**
	 * Constructor.
	 * 
	 * @param description
	 */
	public CustomMicroplateException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 */
	public CustomMicroplateException(Throwable parent)
	{
		super(parent);
	}

	/**
	 * Constructor.
	 * 
	 * @param description
	 * @param parent
	 */
	public CustomMicroplateException(String description, Throwable parent)
	{
		super(description, parent);
	}
}
