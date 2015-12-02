/**
 * 
 */
package ch.ethz.csb.youscope.addon.nemesys;

/**
 * Exception thrown if error occurs in the communication with the Nemesys device.
 * @author Moritz Lang
 *
 */
public class NemesysException extends Exception
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 4109397143111065915L;
	
	/**
     * Constructor.
     * 
     * @param description Human readable description.
     */
    public NemesysException(String description)
    {
        super(description);
    }

    /**
     * Constructor.
     * 
     * @param parent The parent exception.
     */
    public NemesysException(Throwable parent)
    {
        super(parent);
    }

    /**
     * Constructor.
     * 
     * @param description Human readable description.
     * @param parent The parent exception.
     */
    public NemesysException(String description, Throwable parent)
    {
        super(description, parent);
    }
}
