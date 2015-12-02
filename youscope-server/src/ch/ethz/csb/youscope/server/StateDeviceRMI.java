/**
 * 
 */
package ch.ethz.csb.youscope.server;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.microscopeaccess.StateDeviceInternal;
import ch.ethz.csb.youscope.shared.microscope.DeviceException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;
import ch.ethz.csb.youscope.shared.microscope.StateDevice;

/**
 * @author langmo
 */
class StateDeviceRMI extends DeviceRMI implements StateDevice
{
    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 1756414115236261908L;

    private StateDeviceInternal stateDevice;

    StateDeviceRMI(StateDeviceInternal stateDevice, int accessID) throws RemoteException
    {
        super(stateDevice, accessID);
        this.stateDevice = stateDevice;
    }

    @Override
	public int getNumStates() throws RemoteException
    {
        return stateDevice.getNumStates();
    }

    @Override
	public int getState() throws MicroscopeException, RemoteException
    {
        return stateDevice.getState();
    }

    @Override
	public String[] getStateLabels() throws RemoteException
    {
        return stateDevice.getStateLabels();
    }

    @Override
	public String getStateLabel() throws MicroscopeException, RemoteException
    {
        return stateDevice.getStateLabel();
    }

    @Override
	public String getStateLabel(int state) throws RemoteException
    {
        return stateDevice.getStateLabel(state);
    }

    @Override
	public void setStateLabel(int state, String label) throws MicroscopeException, RemoteException,
            MicroscopeLockedException
    {
        stateDevice.setStateLabel(state, label, accessID);
    }

    @Override
	public void setStateLabels(String[] labels) throws MicroscopeException, RemoteException,
            MicroscopeLockedException
    {
        stateDevice.setStateLabels(labels, accessID);
    }

    @Override
	public void setState(int state) throws MicroscopeException, RemoteException,
            MicroscopeLockedException
    {
        stateDevice.setState(state, accessID);
    }

    @Override
	public void setState(String label) throws MicroscopeException, RemoteException,
            MicroscopeLockedException, DeviceException
    {
        stateDevice.setState(label, accessID);
    }
}
