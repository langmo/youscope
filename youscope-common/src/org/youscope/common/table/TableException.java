/**
 * 
 */
package org.youscope.common.table;

/**
 * Exception thrown by table entries.
 * @author Moritz Lang
 */
public class TableException extends Exception
{
    /**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -1654663380102330692L;

	/**
     * Constructor.
     * 
     * @param description Description of the exception.
     */
    public TableException(String description)
    {
        super(description);
    }

    /**
     * Constructor.
     * 
     * @param parent Parent exception.
     */
    public TableException(Throwable parent)
    {
        super(parent);
    }

    /**
     * Constructor.
     * 
     * @param description Description of the exception.
     * @param parent Parent exception.
     */
    public TableException(String description, Throwable parent)
    {
        super(description, parent);
    }
}
