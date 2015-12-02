/**
 * 
 */
package ch.ethz.csb.youscope.addon.openbis;

/**
 * @author Moritz Lang
 *
 */
public class OpenBISException extends Exception
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 4209397143032065915L;
	
	/**
     * Constructor.
     * 
     * @param description Human readable description.
     */
    public OpenBISException(String description)
    {
        super(description);
    }

    /**
     * Constructor.
     * 
     * @param parent The parent exception.
     */
    public OpenBISException(Throwable parent)
    {
        super(parent);
    }

    /**
     * Constructor.
     * 
     * @param description Human readable description.
     * @param parent The parent exception.
     */
    public OpenBISException(String description, Throwable parent)
    {
        super(description, parent);
    }

}
