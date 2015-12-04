/**
 * 
 */
package org.youscope.server;

import java.rmi.RemoteException;

import org.youscope.addon.microscopeaccess.ShutterDeviceInternal;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.ShutterDevice;

/**
 * @author Moritz Lang
 */
class ShutterDeviceRMI extends DeviceRMI implements ShutterDevice
{
    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = -7502926052853797709L;

    private final ShutterDeviceInternal shutterDevice;

    ShutterDeviceRMI(ShutterDeviceInternal shutterDevice, int accessID) throws RemoteException
    {
        super(shutterDevice, accessID);
        this.shutterDevice = shutterDevice;
    }

    @Override
	public void setOpen(boolean open) throws MicroscopeException, MicroscopeLockedException,
            InterruptedException, RemoteException
    {
        shutterDevice.setOpen(open, accessID);
    }

    @Override
	public boolean isOpen() throws MicroscopeException, RemoteException, InterruptedException
    {
        return shutterDevice.isOpen();
    }
}
