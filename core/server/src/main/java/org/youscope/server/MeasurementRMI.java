/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.server;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.MessageListener;
import org.youscope.common.PositionInformation;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.measurement.MeasurementListener;
import org.youscope.common.measurement.MeasurementMetadata;
import org.youscope.common.measurement.MeasurementState;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.saving.MeasurementSaver;
import org.youscope.common.task.Task;

/**
 * @author Moritz Lang
 */
class MeasurementRMI extends UnicastRemoteObject implements Measurement
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = -8611665286420613463L;

    private final MeasurementImpl measurement;

    private final MeasurementManager measurementManager;

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
    	if(state != MeasurementState.READY && state != MeasurementState.UNINITIALIZED && state != MeasurementState.PAUSED)
    		throw new MeasurementException("Measurement must be in state Ready, Uninitialized or Paused to be started. Current state: "+state.toString()+".");
        measurementManager.addMeasurement(measurement);
    }

    @Override
	public void stopMeasurement(boolean processJobQueue) throws MeasurementException
    {
        measurement.stopMeasurement(processJobQueue);
        measurementManager.removeMeasurement(measurement);
    }
    
    @Override
	public void pauseMeasurement() throws MeasurementException
    {
        measurement.pauseMeasurement();
        measurementManager.removeMeasurement(measurement);
    }

    @Override
	public void interruptMeasurement()
    {
    	measurementManager.interruptMeasurement(measurement);
    }

    @Override
	public void setMaxRuntime(long measurementRuntime) throws ComponentRunningException
    {
        measurement.setMaxRuntime(measurementRuntime);
    }

    @Override
	public String getName()
    {
        return measurement.getName();
    }

    @Override
	public void setName(String name) throws ComponentRunningException
    {
        measurement.setName(name);
    }

    @Override
	public void removeMeasurementListener(MeasurementListener listener)
    {
        measurement.removeMeasurementListener(listener);

    }

    @Override
	public long getMaxRuntime()
    {
        return measurement.getMaxRuntime();
    }

    @Override
	public void setLockMicroscopeWhileRunning(boolean lock) throws ComponentRunningException
    {
        measurement.setLockMicroscopeWhileRunning(lock);
    }

    @Override
	public boolean isLockMicroscopeWhileRunning()
    {
        return measurement.isLockMicroscopeWhileRunning();
    }

    @Override
	public void setTypeIdentifier(String type) throws ComponentRunningException
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
	public Task addTask(long period, boolean fixedTimes, long startTime, long numExecutions)
            throws ComponentRunningException, RemoteException
    {
        return measurement.addTask(period, fixedTimes, startTime, numExecutions);
    }

    @Override
	public Task addMultiplePeriodTask(long[] periods, long startTime,
            long numExecutions) throws ComponentRunningException, RemoteException
    {
        return measurement.addMultiplePeriodTask(periods, startTime, numExecutions);
    }

    @Override
	public Task addTask(long period, boolean fixedTimes, long startTime)
            throws ComponentRunningException, RemoteException
    {
        return measurement.addTask(period, fixedTimes, startTime);
    }

    @Override
	public Task addMultiplePeriodTask(long[] periods, long startTime)
            throws ComponentRunningException, RemoteException
    {
        return measurement.addMultiplePeriodTask(periods, startTime);
    }

    @Override
	public void setFinishDeviceSettings(DeviceSetting[] settings)
            throws ComponentRunningException
    {
        measurement.setFinishDeviceSettings(settings);
    }

    @Override
	public void setStartupDeviceSettings(DeviceSetting[] settings)
            throws ComponentRunningException
    {
        measurement.setStartupDeviceSettings(settings);
    }

    @Override
	public Task[] getTasks()
    {
        return measurement.getTasks();
    }

    @Override
	public long getStartTime()
    {
        return measurement.getStartTime();
    }

    @Override
	public long getStopTime()
    {
        return measurement.getStopTime();
    }

    @Override
	public void addStartupDeviceSetting(DeviceSetting setting) throws ComponentRunningException
    {
        measurement.addStartupDeviceSetting(setting);
    }

    @Override
	public void addFinishDeviceSetting(DeviceSetting setting) throws ComponentRunningException
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
    public void setInitialMeasurementContextProperty(String identifier, Serializable property) throws ComponentRunningException
    {
        measurement.setInitialMeasurementContextProperty(identifier, property);
    }

	@Override
	public UUID getUUID() 
{
		return measurement.getUUID();
	}

	@Override
	public long getPauseTime()
	{
		return measurement.getPauseTime();
	}

	@Override
	public long getPauseDuration()
	{
		return measurement.getPauseDuration();
	}

	@Override
	public long getRuntime()
	{
		return measurement.getRuntime();
	}

	@Override
	public MeasurementMetadata getMetadata() throws RemoteException {
		return measurement.getMetadata();
	}

	@Override
	public void setInitialRuntime(long initialRuntime)
			throws RemoteException, ComponentRunningException, IllegalArgumentException {
		measurement.setInitialRuntime(initialRuntime);
	}

	@Override
	public long getInitialRuntime() throws RemoteException {
		return measurement.getInitialRuntime();
	}
}
