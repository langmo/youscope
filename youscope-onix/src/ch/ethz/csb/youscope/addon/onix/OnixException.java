/**
 * 
 */
package ch.ethz.csb.youscope.addon.onix;

/**
 * General purpose exception thrown by the Onix microfluidic device.
 * @author Moritz Lang
 *
 */
public class OnixException extends Exception
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
    public OnixException(String description)
    {
        super(description);
    }

    /**
     * Constructor.
     * 
     * @param parent The parent exception.
     */
    public OnixException(Throwable parent)
    {
        super(parent);
    }

    /**
     * Constructor.
     * 
     * @param description Human readable description.
     * @param parent The parent exception.
     */
    public OnixException(String description, Throwable parent)
    {
        super(description, parent);
    }

}
