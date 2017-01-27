/**
 * 
 */
package org.youscope.common.measurement;

/**
 * The state of a measurement.
 * @author Moritz Lang
 */
public enum MeasurementState
{
	/**
	 * The measurement is created and ready to be initialized.
	 */
	READY,
	/**
	 * The measurement is queued and will be executed (initialized) when the microscope gets free.
	 */
	QUEUED,
	/**
	 * The measurement is currently initializing (still uninitialized, but trying to initialize).
	 */
	INITIALIZING,
	/**
	 * The measurement is initialized and can be started.
	 */
	INITIALIZED,
	/**
	 * The measurement is currently running.
	 */
	RUNNING,
	/**
	 * The measurement is currently finishing/stopping (still running, but trying to finish).
	 */
	STOPPING,
	/**
	 * The measurement is finished/stopped and can be uninitialized.
	 */
	STOPPED,
	/**
	 * The measurement is currently uninitializing (i.e. finished, but trying to uninitialize).
	 */
	UNINITIALIZING,
	/**
	 * The measurement uninitialized and can be initialized again. Functionally the same as {@link #READY}.
	 */
	UNINITIALIZED,
	/**
	 * The measurement is paused, and can be resumed again.
	 */
	PAUSED,
	/**
	 * During the execution of the measurement an error occurred. The measurement stopped and cannot be restarted, but must be constructed completely again.
	 */
	ERROR;
	
	
	@Override
	public String toString() 
	{
	   return super.name().toLowerCase();
	}
}
