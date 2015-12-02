/**
 * 
 */
package ch.ethz.csb.youscope.server.microscopeaccess.access14;

import mmcorej.CMMCore;
import ch.ethz.csb.youscope.server.microscopeaccess.ShutterDeviceInternal;
import ch.ethz.csb.youscope.shared.microscope.DeviceType;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;

/**
 * @author Moritz Lang
 *
 */
class ShutterDeviceImpl extends DeviceImpl implements ShutterDeviceInternal
{
	ShutterDeviceImpl(MicroscopeImpl microscope, String deviceName, String libraryID, String driverID)
	{
		super(microscope, deviceName, libraryID, driverID, DeviceType.ShutterDevice);
	}

	@Override
	public void setOpen(boolean open, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		try
		{
			CMMCore core = microscope.startWrite(accessID);
			core.setShutterDevice(getDeviceID());
			core.setShutterOpen(open);
		}
		catch(MicroscopeLockedException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not open/close shutter device " + getDeviceID() + ".", e);
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}
	}

	@Override
	public boolean isOpen() throws MicroscopeException, InterruptedException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		try
		{
			CMMCore core = microscope.startRead();
			core.setShutterDevice(getDeviceID());
			return core.getShutterOpen();
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not detect if shutter device " + getDeviceID() + " is open.", e);
		}
		finally
		{
			microscope.unlockRead();
		}
	}
}
