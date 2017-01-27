/**
 * 
 */
package org.youscope.common.measurement;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.youscope.common.Component;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.saving.MeasurementSaver;
import org.youscope.common.task.MeasurementTask;

/**
 * Represents a measurement a microscope should do.
 * 
 * @author Moritz Lang
 */
public interface Measurement extends Component
{
	/**
     * Stops the execution of the measurement after all queued jobs were finished. Note: If measurement is queued, it gets disabled but not un-queued.
	 * @throws MeasurementException 
     * 
     * @throws RemoteException
     */
    void quickStopMeasurement() throws MeasurementException, RemoteException;
    /**
     * Sets a measurement context property. The measurement context of the measurement will be initialized upon measurement initialization to already
     * contain this context property. A measurement context property can be loaded and overwritten by any other measurement component in the measurement. Any property
     * with the same identifier will be replaced.
     * 
     * @param identifier a short identifier for the property.
     * @param property The property which should be saved.
     * @throws MeasurementRunningException 
     * @throws RemoteException
     */
    void setInitialMeasurementContextProperty(String identifier, Serializable property) throws MeasurementRunningException, RemoteException;
	/**
	 * Returns the current state of the measurement.
	 * 
	 * @return Current state.
	 * @throws RemoteException
	 */
	MeasurementState getState() throws RemoteException;

	/**
	 * Starts the execution of the measurement. Note: If other measurements are already running, the
	 * measurement gets queued.
	 * @throws MeasurementException 
	 * 
	 * @throws RemoteException
	 */
	void startMeasurement() throws MeasurementException, RemoteException;

	/**
	 * Stops the execution of the measurement after all queued jobs were finished. Note: If
	 * measurement is queued, it gets disabled but not un-queued.
	 * @throws MeasurementException 
	 * 
	 * @throws RemoteException
	 */
	void stopMeasurement() throws MeasurementException, RemoteException;

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
	 * Sets the runtime of the measurement.
	 * 
	 * @param measurementRuntime Runtime of the measurement in milliseconds. Set to -1 for an
	 *            infinite runtime.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setRuntime(int measurementRuntime) throws RemoteException, MeasurementRunningException;

	/**
	 * Returns the runtime of the measurement.
	 * 
	 * @return Runtime of the measurement. A value of -1 corresponds to an infinite runtime.
	 * @throws RemoteException
	 */
	int getRuntime() throws RemoteException;

	/**
	 * Returns the time when the last execution of this measurement started (as returned by {@link System#currentTimeMillis()}), or -1 if it did not yet start or if unknown.
	 * @return Last measurement start time.
	 * @throws RemoteException
	 */
	long getStartTime() throws RemoteException;

	/**
	 * Returns the time when the last execution of this measurement stopped  (as returned by {@link System#currentTimeMillis()}), or -1 if it did not yet stop or if unknown.
	 * @return Last measurement stop time.
	 * @throws RemoteException
	 */
	long getEndTime() throws RemoteException;

	/**
	 * Sets if write access to the microscope is locked during the measurement.
	 * 
	 * @param lock True if should be locked.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setLockMicroscopeWhileRunning(boolean lock) throws RemoteException, MeasurementRunningException;

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
	 * @throws MeasurementRunningException
	 */
	void setTypeIdentifier(String type) throws RemoteException, MeasurementRunningException;

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
	 * @throws MeasurementRunningException
	 */
	MeasurementTask addTask(int period, boolean fixedTimes, int startTime) throws RemoteException, MeasurementRunningException;

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
	 * @throws MeasurementRunningException
	 */
	MeasurementTask addTask(int period, boolean fixedTimes, int startTime, int numExecutions) throws RemoteException, MeasurementRunningException;

	/**
	 * Adds a new task to the measurement so that at times {startTime, startTime + periods[0],
	 * startTime + periods[0] + periods[1], ..., startTime + sum_{i=0:size-2}(periods[i]), startTime
	 * + sum_{i=0:size-1}(periods[i]) + breakTime, startTime + sum_{i=0:size-1}(periods[i]) +
	 * breakTime + periods[0]} the jobs of the task will be executed. Same as
	 * addMultiplePeriodTask(periods, breakTime, startTime, -1).
	 * 
	 * @param periods Times between single executions (in milliseconds).
	 * @param breakTime After all periods are run through, the job will pause for <breakTime> ms and
	 *            then start with periods[0] again.
	 * @param startTime The time when the jobs should be started first (in ms).
	 * @return The newly created task. If not successful, NULL is returned.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	MeasurementTask addMultiplePeriodTask(int[] periods, int breakTime, int startTime) throws RemoteException, MeasurementRunningException;

	/**
	 * Adds a new task to the measurement so that at times {startTime, startTime + periods[0],
	 * startTime + periods[0] + periods[1], ..., startTime + sum_{i=0:size-2}(periods[i]), startTime
	 * + sum_{i=0:size-1}(periods[i]) + breakTime, startTime + sum_{i=0:size-1}(periods[i]) +
	 * breakTime + periods[0]} the jobs of the task will be executed.
	 * 
	 * @param periods Times between single executions (in milliseconds).
	 * @param breakTime After all periods are run through, the job will pause for <breakTime> ms and
	 *            then start with periods[0] again.
	 * @param startTime The time when the jobs should be started first (in ms).
	 * @param numExecutions Maximal number of executions. -1 for an infinite amount.
	 * @return The newly created task. If not successful, NULL is returned.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	MeasurementTask addMultiplePeriodTask(int[] periods, int breakTime, int startTime, int numExecutions) throws RemoteException, MeasurementRunningException;

	/**
	 * Returns a list of all tasks of this measurement.
	 * 
	 * @return List of tasks.
	 * @throws RemoteException
	 */
	MeasurementTask[] getTasks() throws RemoteException;

	/**
	 * Sets the device settings which should be applied (once) the measurement starts.
	 * 
	 * @param settings The device settings which should be applied.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setStartupDeviceSettings(DeviceSetting[] settings) throws RemoteException, MeasurementRunningException;

	/**
	 * Sets the device settings which should be applied (once) the measurement ends.
	 * 
	 * @param settings The device settings which should be applied.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void setFinishDeviceSettings(DeviceSetting[] settings) throws RemoteException, MeasurementRunningException;

	/**
	 * Adds a device setting which should be applied (once) the measurement starts.
	 * 
	 * @param setting The device settings which should be added.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void addStartupDeviceSetting(DeviceSetting setting) throws RemoteException, MeasurementRunningException;

	/**
	 * Adds a device settings which should be applied (once) the measurement ends.
	 * 
	 * @param setting The device settings which should be added.
	 * @throws RemoteException
	 * @throws MeasurementRunningException
	 */
	void addFinishDeviceSetting(DeviceSetting setting) throws RemoteException, MeasurementRunningException;
}