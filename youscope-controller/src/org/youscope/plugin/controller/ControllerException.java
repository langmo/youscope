/**
 * 
 */
package org.youscope.plugin.controller;

/**
 * Exception thrown by the controller if control algorithm failed.
 * @author Moritz Lang
 *
 */
public class ControllerException extends Exception
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 4209397143111065915L;
	
	/**
     * Constructor.
     * 
     * @param description Human readable description.
     */
    public ControllerException(String description)
    {
        super(description);
    }

    /**
     * Constructor.
     * 
     * @param parent The parent exception.
     */
    public ControllerException(Throwable parent)
    {
        super(parent);
    }

    /**
     * Constructor.
     * 
     * @param description Human readable description.
     * @param parent The parent exception.
     */
    public ControllerException(String description, Throwable parent)
    {
        super(description, parent);
    }
}
