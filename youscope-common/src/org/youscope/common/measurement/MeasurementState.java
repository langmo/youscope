/**
 * 
 */
package org.youscope.common.measurement;

/**
 * @author langmo
 */
public enum MeasurementState
{
	/**
	 * The measurement is queued and will be executed when the microscope gets free.
	 */
	QUEUED,
	/**
	 * The measurement was removed from the queue without having run. It may be started again.
	 */
	UNQUEUED,
	/**
	 * The measurement is currently running.
	 */
	RUNNING,
	/**
	 * The measurement is finished and can be started again.
	 */
	FINISHED,
	/**
	 * The measurement is ready to be started.
	 */
	READY,
	/**
	 * During the execution of the measurement an error occurred. It stopped and may be started
	 * again.
	 */
	ERROR,
	/**
	 * The measurement is trying to stop itself.
	 */
	STOPPING,
	/**
	 * The measurement is trying to interrupt itself
	 */
	INTERRUPTING,
	/**
	 * The measurement is currently initializing.
	 */
	INITIALIZING,
	/**
	 * The measurement is currently uninitializing.
	 */
	UNINITIALIZING
}
