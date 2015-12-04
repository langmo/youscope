/**
 * 
 */
package org.youscope.server;

import java.rmi.RemoteException;

import org.youscope.addon.microscopeaccess.SerialDeviceInternal;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.SerialDevice;

/**
 * @author Moritz Lang
 * 
 */
class SerialDeviceRMI extends DeviceRMI implements SerialDevice
{

	/**
	 * Serial Version UID.
	 */
	private static final long		serialVersionUID	= -7624821113540201850L;

	private SerialDeviceInternal	serialDevice;

	SerialDeviceRMI(SerialDeviceInternal serialDevice, int accessID) throws RemoteException
	{
		super(serialDevice, accessID);
		this.serialDevice = serialDevice;
	}

	@Override
	public void sendCommand(String command) throws MicroscopeLockedException, MicroscopeException, InterruptedException, RemoteException
	{
		serialDevice.sendCommand(command, accessID);
	}

}
