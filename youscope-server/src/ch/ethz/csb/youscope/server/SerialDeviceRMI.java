/**
 * 
 */
package ch.ethz.csb.youscope.server;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.microscopeaccess.SerialDeviceInternal;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;
import ch.ethz.csb.youscope.shared.microscope.SerialDevice;

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
