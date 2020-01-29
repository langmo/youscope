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
package org.youscope.common.job.basicjobs;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.job.Job;

/**
 * @author langmo
 */
public interface FocusingJob extends Job
{
	/**
	 * The type identifier of the default implementation of this job. 
	 * Basic jobs are considered such essential to YouScope
	 * such that their interfaces are made part of the shared library. However, their implementation are not, and there
	 * might be several addons providing (different) implementations of this job. Most of these implementations, however, are specific
	 * for a given application and not general. The addon exposing this identifier should be general, that is, every other
	 * part of YouScope accessing this job over the default identifier is expecting the job to behave in the general way.
	 * Only one implementation (addon) should expose the default identifier. Typically, this implementation is already part of YouScope,
	 * such that implementing this addon is not necessary. However, there might be cases when the default implementation should be overwritten,
	 * which is why the interface, but not the implementation is part of YouScope's core elements. In this case, the default implementation
	 * already part of YouScope should be removed (i.e. the corresponding default plug-in deleted).
	 * 
	 */
	public static final String	DEFAULT_TYPE_IDENTIFIER	= "YouScope.FocusingJob";
	/**
	 * Returns the focus adjustment time (the time the microscope is paused after the new focus position was set) in ms.
	 * @return Focus adjustment time.
	 * @throws RemoteException
	 */
	int getFocusAdjustmentTime() throws RemoteException;

	/**
	 * Sets the focus adjustment time (the time the microscope is paused after the new focus position was set) in ms.
	 * @param adjustmentTime Focus adjustment time.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setFocusAdjustmentTime(int adjustmentTime) throws RemoteException, ComponentRunningException;

	/**
	 * Gets the position/offset of the focus/autofocus device.
	 * 
	 * @return The offset.
	 * @throws RemoteException
	 */
	double getPosition() throws RemoteException;

	/**
	 * Sets the position the focus/autofocus should go to.
	 * 
	 * @param position The position.
	 * @param relative True if position is relative to the current position, false if it is an absolute position.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setPosition(double position, boolean relative) throws RemoteException, ComponentRunningException;

	/**
	 * Returns true if the offset is relative to the current offset, false otherwise.
	 * 
	 * @return True if offset is relative.
	 * @throws RemoteException
	 */
	boolean isRelative() throws RemoteException;

	/**
	 * Returns the focus device name for which the position should be changed.
	 * @return Focus device name, or null, if the currently active focus device should be changed.
	 * @throws RemoteException
	 */
	String getFocusDevice() throws RemoteException;

	/**
	 * Sets the focus device name for which the position should be changed.
	 * Initialized to be null.
	 * @param focusDevice Focus device name, or null, if the currently active focus device should be changed.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setFocusDevice(String focusDevice) throws RemoteException, ComponentRunningException;
}
