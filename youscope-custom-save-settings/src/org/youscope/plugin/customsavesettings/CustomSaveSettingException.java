/**
 * 
 */
package org.youscope.plugin.customsavesettings;

/**
 * Exception thrown to indicate that initializing a custom save setting, creating or deleting it, or similar, failed (typically because custom save setting type definition is missing).
 * @author Moritz Lang
 * 
 */
public class CustomSaveSettingException extends Exception
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= -5888816707725425651L;

	/**
	 * Constructor.
	 * 
	 * @param description
	 */
	public CustomSaveSettingException(String description)
	{
		super(description);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 */
	public CustomSaveSettingException(Throwable parent)
	{
		super(parent);
	}

	/**
	 * Constructor.
	 * 
	 * @param description
	 * @param parent
	 */
	public CustomSaveSettingException(String description, Throwable parent)
	{
		super(description, parent);
	}
}
