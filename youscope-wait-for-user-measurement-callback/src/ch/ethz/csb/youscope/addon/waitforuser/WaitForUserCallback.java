/**
 * 
 */
package ch.ethz.csb.youscope.addon.waitforuser;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.measurement.callback.Callback;

/**
 * Simple measurement callback to show a message and wait for the user to acknowledge it.
 * @author Moritz Lang
 *
 */
public interface WaitForUserCallback extends Callback
{
	/**
	 * Type identifier of callback.
	 */
	public static final String	TYPE_IDENTIFIER	= "CSB::WaitForUserJob::Callback";
	/**
	 * Displays the message and returns after user acknowledgment.
	 * @param message Message to display
	 * @throws RemoteException 
	 * @throws InterruptedException 
	 */
	public void waitForUser(String message) throws RemoteException, InterruptedException;
}
