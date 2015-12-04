/**
 * 
 */
package org.youscope.common.microscope;

import java.rmi.RemoteException;

/**
 * @author Moritz Lang
 * 
 */
public interface ShutterDevice extends Device
{
	/**
	 * Opens (open == true) or closes (open == false) the shutter.
	 * @param open True if the shutter should be opened, false if it should be closed.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws InterruptedException
	 * @throws RemoteException
	 */
	void setOpen(boolean open) throws MicroscopeException, MicroscopeLockedException, InterruptedException, RemoteException;

	/**
	 * Returns true if the shutter is open, and false if it is closed.
	 * @return True if shutter is open, false otherwise.
	 * @throws MicroscopeException
	 * @throws RemoteException
	 * @throws InterruptedException
	 */
	boolean isOpen() throws MicroscopeException, RemoteException, InterruptedException;
}
