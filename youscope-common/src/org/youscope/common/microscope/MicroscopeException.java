package org.youscope.common.microscope;

/**
 * Class representing an exception which occurred in the execution of some microscope tasks.
 * 
 * @author Moritz Lang
 */
public class MicroscopeException extends Exception
{
    /**
     * Version UID.
     */
    private static final long serialVersionUID = 4055279582579151582L;

    /**
     * Constructor.
     * 
     * @param description Human readable description of the exception.
     */
    public MicroscopeException(String description)
    {
        super(description);
    }

    /**
     * Constructor.
     * 
     * @param description Human readable description of the exception.
     * @param cause The cause of the exception.
     */
    public MicroscopeException(String description, Exception cause)
    {
        super(description, cause);
    }
}
