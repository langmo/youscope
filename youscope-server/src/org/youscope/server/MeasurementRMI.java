/**
 * 
 */
package org.youscope.server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.UUID;

import org.youscope.common.MessageListener;
import org.youscope.common.PositionInformation;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.measurement.MeasurementListener;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.MeasurementState;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.saving.MeasurementSaver;
import org.youscope.common.task.MeasurementTask;

/**
 * @author Moritz Lang
 */
class MeasurementRMI extends UnicastRemoteObject implements Measurement
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = -8611665286420613463L;

    protected final MeasurementImpl measurement;

    protected final MeasurementManager measurementManager;

    private final MeasurementSaver measurementSaver;

    /**
     * Constructor.
     * 
     * @throws RemoteException
     */
    protected MeasurementRMI(MeasurementImpl measurement, MeasurementManager measurementManager) throws RemoteException
    {
        this.measurement = measurement;
        this.measurementManager = measurementManager;
        this.measurementSaver = new MeasurementSaverImpl(measurement);
    }

    @Override
	public void addMeasurementListener(MeasurementListener listener)
    {
        measurement.addMeasurementListener(listener);
    }

    @Override
	public void startMeasurement() throws MeasurementException
    {
    	MeasurementState state = measurement.getState();
    	if(state != MeasurementState.READY && state != MeasurementState.UNINITIALIZED)
    		throw new MeasurementException("Measurement must be in state Ready or Uninitialized to be started. Current state: "+state.toString()+".");
        measurementManager.addMeasurement(measurement);
    }

    @Override
	public void stopMeasurement() throws MeasurementException
    {
        measurement.stopMeasurement();
        measurementManager.removeMeasurement(measurement);
    }

    @Override
	public void interruptMeasurement()
    {
    	try {
			measurement.stopMeasurement();
		} catch (@SuppressWarnings("unused") MeasurementException e) {
			// do nothing, only thrown if execution is not currently active.
		}
    	measurementManager.interruptMeasurement(measurement);
    }

    @Override
	public void setRuntime(int measurementRuntime) throws MeasurementRunningException
    {
        measurement.setRuntime(measurementRuntime);
    }

    @Override
	public String getName()
    {
        return measurement.getName();
    }

    @Override
	public void setName(String name)
    {
        measurement.setName(name);
    }

    @Override
	public void removeMeasurementListener(MeasurementListener listener)
    {
        measurement.removeMeasurementListener(listener);

    }

    @Override
	public int getRuntime()
    {
        return measurement.getRuntime();
    }

    @Override
	public void setLockMicroscopeWhileRunning(boolean lock) throws MeasurementRunningException
    {
        measurement.setLockMicroscopeWhileRunning(lock);
    }

    @Override
	public boolean isLockMicroscopeWhileRunning()
    {
        return measurement.isLockMicroscopeWhileRunning();
    }

    @Override
	public void setTypeIdentifier(String type) throws MeasurementRunningException
    {
        measurement.setTypeIdentifier(type);
    }

    @Override
	public String getTypeIdentifier()
    {
        return measurement.getTypeIdentifier();
    }

    @Override
	public MeasurementState getState()
    {
        return measurement.getState();
    }

    @Override
	public MeasurementSaver getSaver()
    {
        return measurementSaver;
    }

    @Override
	public MeasurementTask addTask(int period, boolean fixedTimes, int startTime, int numExecutions)
            throws MeasurementRunningException, RemoteException
    {
        return measurement.addTask(period, fixedTimes, startTime, numExecutions);
    }

    @Override
	public MeasurementTask addMultiplePeriodTask(int[] periods, int breakTime, int startTime,
            int numExecutions) throws MeasurementRunningException, RemoteException
    {
        return measurement.addMultiplePeriodTask(periods, breakTime, startTime, numExecutions);
    }

    @Override
	public MeasurementTask addTask(int period, boolean fixedTimes, int startTime)
            throws MeasurementRunningException, RemoteException
    {
        return measurement.addTask(period, fixedTimes, startTime);
    }

    @Override
	public MeasurementTask addMultiplePeriodTask(int[] periods, int breakTime, int startTime)
            throws MeasurementRunningException, RemoteException
    {
        return measurement.addMultiplePeriodTask(periods, breakTime, startTime);
    }

    @Override
	public void setFinishDeviceSettings(DeviceSetting[] settings)
            throws MeasurementRunningException
    {
        measurement.setFinishDeviceSettings(settings);
    }

    @Override
	public void setStartupDeviceSettings(DeviceSetting[] settings)
            throws MeasurementRunningException
    {
        measurement.setStartupDeviceSettings(settings);
    }

    @Override
	public MeasurementTask[] getTasks()
    {
        return measurement.getTasks();
    }

    @Override
	public long getStartTime()
    {
        return measurement.getStartTime();
    }

    @Override
	public long getEndTime()
    {
        return measurement.getEndTime();
    }

    @Override
	public void addStartupDeviceSetting(DeviceSetting setting) throws MeasurementRunningException
    {
        measurement.addStartupDeviceSetting(setting);
    }

    @Override
	public void addFinishDeviceSetting(DeviceSetting setting) throws MeasurementRunningException
    {
        measurement.addFinishDeviceSetting(setting);
    }

	@Override
	public void addMessageListener(MessageListener writer) 
	{
		measurement.addMessageListener(writer);
	}

	@Override
	public void removeMessageListener(MessageListener writer) 
	{
		measurement.removeMessageListener(writer);
	}

	@Override
	public PositionInformation getPositionInformation()
	{
		return new PositionInformation();
	}

	@Override
    public void quickStopMeasurement()
    {
        measurement.quickStopMeasurement();
        measurementManager.quickStopMeasurement(measurement);
    }

	@Override
    public void setInitialMeasurementContextProperty(String identifier, Serializable property) throws MeasurementRunningException
    {
        measurement.setInitialMeasurementContextProperty(identifier, property);
    }

	@Override
	public UUID getUUID() 
{
		return measurement.getUUID();
	}
}
