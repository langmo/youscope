/**
 * 
 */
package org.youscope.common.microscope;

import java.rmi.RemoteException;

/**
 * @author Moritz Lang
 * 
 */
public interface SerialDevice extends Device
{
	/**
	 * Sends a serial command to the corresponding port
	 * @param command The command to send. Use only ASCI
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 * @throws RemoteException
	 */
	void sendCommand(String command) throws MicroscopeLockedException, MicroscopeException, InterruptedException, RemoteException;
}
