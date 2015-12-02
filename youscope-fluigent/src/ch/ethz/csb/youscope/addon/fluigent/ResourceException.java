/**
 * 
 */
package ch.ethz.csb.youscope.addon.fluigent;

/**
 * Exception thrown if error occurs in the communication with the Fluigent device.
 * @author Moritz Lang
 *
 */
public class ResourceException extends Exception
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 4109397143111065911L;
	
	/**
     * Constructor.
     * 
     * @param description Human readable description.
     */
    public ResourceException(String description)
    {
        super(description);
    }

    /**
     * Constructor.
     * 
     * @param parent The parent exception.
     */
    public ResourceException(Throwable parent)
    {
        super(parent);
    }

    /**
     * Constructor.
     * 
     * @param description Human readable description.
     * @param parent The parent exception.
     */
    public ResourceException(String description, Throwable parent)
    {
        super(description, parent);
    }
}
