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
package org.youscope.common.measurement;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.youscope.common.Component;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.saving.MeasurementSaver;
import org.youscope.common.task.Task;

/**
 * Represents a measurement a microscope should do.
 * 
 * @author Moritz Lang
 */
public interface Measurement extends Component
{
    /**
     * Sets a measurement context property. The measurement context of the measurement will be initialized upon measurement initialization to already
     * contain this context property. A measurement context property can be loaded and overwritten by any other measurement component in the measurement. Any property
     * with the same identifier will be replaced.
     * 
     * @param identifier a short identifier for the property.
     * @param property The property which should be saved.
     * @throws ComponentRunningException 
     * @throws RemoteException
     */
    void setInitialMeasurementContextProperty(String identifier, Serializable property) throws ComponentRunningException, RemoteException;
	/**
	 * Returns the current state of the measurement.
	 * 
	 * @return Current state.
	 * @throws RemoteException
	 */
	MeasurementState getState() throws RemoteException;

	/**
	 * Returns the metadata of this measurement.
	 * @return Metadata of this measurement.
	 * @throws RemoteException
	 */
	MeasurementMetadata getMetadata() throws RemoteException;
	/**
	 * Starts the execution of the measurement. Note: If other measurements are already running, the
	 * measurement gets queued.
	 * @throws MeasurementException 
	 * 
	 * @throws RemoteException
	 */
	void startMeasurement() throws MeasurementException, RemoteException;

	/**
	 * If measurement runs:
	 * Stops the execution of the measurement either after all queued jobs were processed, or when currently processed job finished. 
	 * If measurement is queued:
	 * Unqueues measurement.
	 * @param processJobQueue True if all queued jobs should be processed before finishing, false if finishing directly after currently processed job.
	 * @throws MeasurementException 
	 * @throws RemoteException
	 * @see #interruptMeasurement()
	 */
	void stopMeasurement(boolean processJobQueue) throws MeasurementException, RemoteException;
	
	/**
	 * If measurement runs:
	 * Pauses measurement. Measurement can be resumed with {@link #startMeasurement()}.
	 * @throws MeasurementException 
	 * @throws RemoteException
	 * @see #interruptMeasurement()
	 */
	void pauseMeasurement() throws MeasurementException, RemoteException;

	/**
	 * Interrupts the execution of the measurement. All queued jobs are discarded.
	 * 
	 * @throws RemoteException
	 */
	void interruptMeasurement() throws RemoteException;

	/**
	 * Adds a listener which is notified about the state of the measurement.
	 * 
	 * @param listener Listener which should be notified.
	 * @throws RemoteException
	 */
	void addMeasurementListener(MeasurementListener listener) throws RemoteException;

	/**
	 * Removes a previously added listener.
	 * 
	 * @param listener The listener to remove.
	 * @throws RemoteException
	 */
	void removeMeasurementListener(MeasurementListener listener) throws RemoteException;

	/**
	 * Sets the maximal runtime of the measurement after which it automatically stops.
	 * 
	 * @param measurementRuntime Runtime of the measurement in milliseconds. Set to -1 for an
	 *            infinite runtime.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setMaxRuntime(long measurementRuntime) throws RemoteException, ComponentRunningException;

	/**
	 * Returns the maximal runtime of the measurement after which it automatically stops.
	 * 
	 * @return Maximal runtime of the measurement. A value of -1 corresponds to an infinite runtime.
	 * @throws RemoteException
	 */
	long getMaxRuntime() throws RemoteException;

	/**
	 * Returns the time when the last execution of this measurement started (as returned by {@link System#currentTimeMillis()}), or -1 if it did not yet start or if unknown.
	 * @return Last measurement start time.
	 * @throws RemoteException
	 */
	long getStartTime() throws RemoteException;

	/**
	 * Returns the time when the last execution of this measurement stopped (as returned by {@link System#currentTimeMillis()}), or -1 if it did not yet stop or if unknown.
	 * @return Last measurement stop time.
	 * @throws RemoteException
	 */
	long getStopTime() throws RemoteException;
	
	/**
	 * Returns the time when the measurement was last paused (as returned by {@link System#currentTimeMillis()}), given that it is currently paused. Returns -1 if measurement was not yet paused, or if measurement was resumed after pause.
	 * @return Time when measurement was paused.
	 * @throws RemoteException
	 */
	long getPauseTime() throws RemoteException;
	
	/**
	 * Returns the time duration, in ms, during which the measurement was paused. If measurement was not paused, yet, returns 0.
	 * Note, that if the measurement is currently paused, this time will not be updated; only the pauses which got resumed count to the pause duration.
	 * @return Pause duration in ms.
	 * @throws RemoteException
	 */
	long getPauseDuration() throws RemoteException;
	
	/**
	 * Returns the time duration this measurement is/was already running. Returns -1 if measurement was not started, yet.
	 * If {@link #getState()}=={@link MeasurementState#RUNNING}, returns {@link System#currentTimeMillis()}-{@link #getStartTime()}-{@link #getPauseDuration()}.
	 * If {@link #getState()}=={@link MeasurementState#STOPPED}, returns {@link #getStopTime()}-{@link #getStartTime()}-{@link #getPauseDuration()}.
	 * If {@link #getState()}=={@link MeasurementState#PAUSED}, returns {@link #getPauseTime()}-{@link #getStartTime()}-{@link #getPauseDuration()}.
	 * @return Measurement runtime in ms.
	 * @throws RemoteException
	 */
	long getRuntime() throws RemoteException;

	/**
	 * Sets if write access to the microscope is locked during the measurement.
	 * 
	 * @param lock True if should be locked.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setLockMicroscopeWhileRunning(boolean lock) throws RemoteException, ComponentRunningException;

	/**
	 * Returns if write access to the microscope is locked during the measurement.
	 * 
	 * @return True if write access is locked.
	 * @throws RemoteException
	 */
	boolean isLockMicroscopeWhileRunning() throws RemoteException;

	/**
	 * Sets an identifier for the type of this measurement. This identifier is usually the same as the one of the configuration.
	 * 
	 * @param type Type identifier.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setTypeIdentifier(String type) throws RemoteException, ComponentRunningException;

	/**
	 * Returns a previously defined type identifier.
	 * 
	 * @return Type identifier.
	 * @throws RemoteException
	 */
	String getTypeIdentifier() throws RemoteException;

	/**
	 * Returns an interface which defines how (and if) the measurement should be saved.
	 * @return Interface to measurement save options.
	 * @throws RemoteException
	 */
	MeasurementSaver getSaver() throws RemoteException;

	/**
	 * Adds a new task to the measurement so that at times (<startTime> + i * <period>) ms, i =
	 * 0,1,2,..., the jobs of the task will be executed. Same as addTask(period, fixedTimes,
	 * startTime, -1).
	 * 
	 * @param period Time between single executions (in milliseconds).
	 * @param fixedTimes TRUE if jobs should be done at fixed time intervals, FALSE if job may be
	 *            delayed if microscope runs slow.
	 * @param startTime The time when the jobs are started first (in ms).
	 * @return The newly created task. If not successful, NULL is returned.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	Task addTask(long period, boolean fixedTimes, long startTime) throws RemoteException, ComponentRunningException;

	/**
	 * Adds a new task to the measurement so that at times (<startTime> + i * <period>) ms, i =
	 * 0,1,2,..., the jobs of the task will be executed. After <numExecutions> executions of the
	 * task, the task won't be scheduled anymore
	 * 
	 * @param period Time between single executions (in milliseconds).
	 * @param fixedTimes TRUE if jobs should be done at fixed time intervals, FALSE if job may be
	 *            delayed if microscope runs slow.
	 * @param startTime The time when the jobs are started first (in ms).
	 * @param numExecutions Maximal number of executions. -1 for an infinite amount.
	 * @return The newly created task. If not successful, NULL is returned.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	Task addTask(long period, boolean fixedTimes, long startTime, long numExecutions) throws RemoteException, ComponentRunningException;

	/**
	 * Adds a new task to the measurement so that at times {startTime, startTime + periods[0],
	 * startTime + periods[0] + periods[1], ..., startTime + sum_{i=0:size-2}(periods[i]), startTime + sum_{i=0:size-1}(periods[i]) +
	 * periods[0]} the jobs of the task will be executed. Same as
	 * addMultiplePeriodTask(periods, startTime, -1).
	 * 
	 * @param periods Times between single executions (in milliseconds).
	 * @param startTime The time when the jobs should be started first (in ms).
	 * @return The newly created task. If not successful, NULL is returned.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	Task addMultiplePeriodTask(long[] periods, long startTime) throws RemoteException, ComponentRunningException;

	/**
	 * Adds a new task to the measurement so that at times {startTime, startTime + periods[0],
	 * startTime + periods[0] + periods[1], ..., startTime + sum_{i=0:size-2}(periods[i]), startTime + sum_{i=0:size-1}(periods[i]) +
	 * periods[0]} the jobs of the task will be executed. 
	 * 
	 * @param periods Times between single executions (in milliseconds).
	 * @param startTime The time when the jobs should be started first (in ms).
	 * @param numExecutions Maximal number of executions. -1 for an infinite amount.
	 * @return The newly created task. If not successful, NULL is returned.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	Task addMultiplePeriodTask(long[] periods, long startTime, long numExecutions) throws RemoteException, ComponentRunningException;

	/**
	 * Returns a list of all tasks of this measurement.
	 * 
	 * @return List of tasks.
	 * @throws RemoteException
	 */
	Task[] getTasks() throws RemoteException;

	/**
	 * Sets the device settings which should be applied (once) the measurement starts.
	 * 
	 * @param settings The device settings which should be applied.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setStartupDeviceSettings(DeviceSetting[] settings) throws RemoteException, ComponentRunningException;

	/**
	 * Sets the device settings which should be applied (once) the measurement ends.
	 * 
	 * @param settings The device settings which should be applied.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setFinishDeviceSettings(DeviceSetting[] settings) throws RemoteException, ComponentRunningException;

	/**
	 * Adds a device setting which should be applied (once) the measurement starts.
	 * 
	 * @param setting The device settings which should be added.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void addStartupDeviceSetting(DeviceSetting setting) throws RemoteException, ComponentRunningException;

	/**
	 * Adds a device settings which should be applied (once) the measurement ends.
	 * 
	 * @param setting The device settings which should be added.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void addFinishDeviceSetting(DeviceSetting setting) throws RemoteException, ComponentRunningException;
	
	/**
	 * Sets the initial runtime of the measurement when the measurement starts. Typically, this is zero.
	 * @param initialRuntime Initial runtime of the measurement. Must be greater or equal to zero.
	 * @throws RemoteException 
	 * @throws ComponentRunningException
	 * @throws IllegalArgumentException If initial runtime is smaller than zero.
	 */
	void setInitialRuntime(long initialRuntime) throws RemoteException, ComponentRunningException, IllegalArgumentException;
	/**
	 * Returns the initial runtime of the measurement when the measurement starts. Typically, this is zero.
	 * @return initial runtime of the measurement. Must be greater or equal to zero.
	 * @throws RemoteException 
	 */
	long getInitialRuntime() throws RemoteException;
}
