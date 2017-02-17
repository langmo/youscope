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
	 * The measurement is currently in the process to be stopped (still running, but trying to finish).
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
	 * The measurement is currently in the process to be paused (still running, but trying to pause).
	 */
	PAUSING,
	
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
	/**
	 * Returns true if the measurement is in an error state.
	 * Currently, this is true if the state is {@link #ERROR}.
	 * @return True if measurement is in error state.
	 */
	public boolean isError()
	{
		return this == MeasurementState.ERROR;
	}
	/**
	 * Returns true if the measurement is currently executed.
	 * Currently, this is true if the state is either {@link #RUNNING}, {@link #STOPPING}, or {@link #PAUSING}.
	 * @return True if measurement is currently executed.
	 */
	public boolean isRunning()
	{
		return this == MeasurementState.RUNNING || this == MeasurementState.STOPPING || this == MeasurementState.PAUSING;
	}
	
	/**
	 * Returns true if the current state allows for safe editing of the measurement.
	 * Currently, this is true if this state is either {@link #READY} or {@link #UNINITIALIZED}.
	 * @return True if safe editing is possible.
	 */
	public boolean isEditable()
	{
		return this == MeasurementState.READY || this == MeasurementState.UNINITIALIZED;
	}
}
