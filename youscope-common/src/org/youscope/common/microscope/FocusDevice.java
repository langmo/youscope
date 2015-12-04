/**
 * 
 */
package org.youscope.common.microscope;

import java.rmi.RemoteException;

/**
 * @author langmo
 * 
 */
public interface FocusDevice extends Device
{
	/**
	 * Returns the position of the focus device.
	 * 
	 * @return Position of the focus device.
	 * @throws RemoteException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	double getFocusPosition() throws RemoteException, MicroscopeException, InterruptedException;

	/**
	 * Sets the position of the current focus device.
	 * 
	 * @param position
	 *            The new position.
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	void setFocusPosition(double position) throws MicroscopeLockedException, RemoteException, MicroscopeException, InterruptedException;

	/**
	 * Sets the position of the current focus device relative to the current
	 * focus.
	 * 
	 * @param offset
	 *            The offset.
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	void setRelativeFocusPosition(double offset) throws MicroscopeLockedException, RemoteException, MicroscopeException, InterruptedException;
}
