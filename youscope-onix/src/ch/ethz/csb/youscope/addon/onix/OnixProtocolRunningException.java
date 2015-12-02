/**
 * 
 */
package ch.ethz.csb.youscope.addon.onix;

/**
 * Thrown by the Onix plugin if the microfluidic device is accessed while a protocol is running, which blocks access to the device.
 * @author Moritz Lang
 *
 */
public class OnixProtocolRunningException extends Exception
{

	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 8187133506632404982L;

	/**
	 * Constructor.
	 */
	public OnixProtocolRunningException()
	{
		super("A protocol is currently running on the Onix device. Stop the protocol execution or wait until the protocol is finished before accessing the functionality of the Onix device in any other manner.");
	}
}
