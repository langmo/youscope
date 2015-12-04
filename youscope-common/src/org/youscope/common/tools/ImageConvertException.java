/**
 * 
 */
package org.youscope.common.tools;

/**
 * @author langmo
 *
 */
public class ImageConvertException extends Exception
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -4128343535296513649L;
	
	/**
     * Constructor.
     * 
     * @param description Human readable description of the exception.
     */
    public ImageConvertException(String description)
    {
        super(description);
    }

    /**
     * Constructor.
     * 
     * @param description Human readable description of the exception.
     * @param cause The cause of the exception.
     */
    public ImageConvertException(String description, Exception cause)
    {
        super(description, cause);
    }

}
