/**
 * 
 */
package org.youscope.addon.microscopeaccess;

/**
 * @author langmo
 */
public class MicroscopeConnectionException extends Exception
{
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -2633690413777862118L;

    /**
     * Constructor.
     * 
     * @param description Human readable description of the exception.
     */
    public MicroscopeConnectionException(String description)
    {
        super(description);
    }

    /**
     * Constructor.
     * 
     * @param description Human readable description of the exception.
     * @param cause The cause of the exception.
     */
    public MicroscopeConnectionException(String description, Throwable cause)
    {
        super(description, cause);
    }
}
