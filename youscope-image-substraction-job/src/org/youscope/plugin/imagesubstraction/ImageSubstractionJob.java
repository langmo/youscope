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
package org.youscope.plugin.imagesubstraction;

import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.image.ImageProducer;
import org.youscope.common.job.Job;

/**
 * A job which makes a microscope image in the defined channel with a defined focus offset.
 * 
 * @author langmo
 */
public interface ImageSubstractionJob extends Job, ImageProducer
{
	/**
	 * Sets the channel.
	 * 
	 * @param channelGroup The channel group where the channel is defined.
	 * @param channel The channel.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setChannel(String channelGroup, String channel) throws RemoteException, ComponentRunningException;

	/**
	 * Gets the channel.
	 * 
	 * @return The channel.
	 * @throws RemoteException
	 */
	String getChannel() throws RemoteException;

	/**
	 * Gets the channel group.
	 * 
	 * @return The channel group.
	 * @throws RemoteException
	 */
	String getChannelGroup() throws RemoteException;

	/**
	 * Sets the exposure. If more than one camera is initialized, it sets the exposure of all cameras.
	 * Set to -1 if exposure should not be set.
	 * 
	 * @param exposure The exposure.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	public void setExposure(double exposure) throws RemoteException, ComponentRunningException;

	/**
	 * Gets the exposure. If more than one camera is initialized, returns the exposure of the first camera.
	 * 
	 * @return The exposure.
	 * @throws RemoteException
	 */
	double getExposure() throws RemoteException;

	/**
	 * Sets the camera with which it should be imaged. If set to null, the default (currently initialized) camera is used.
	 * 
	 * @param camera Device names of the camera.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setCamera(String camera) throws RemoteException, ComponentRunningException;

	/**
	 * Returns the camera with which it is imaged, or null if the default camera is used. If with more than one camera it is imaged,
	 * returns the first camera.
	 * 
	 * @return The name of the (first) camera, or null for default camera.
	 * @throws RemoteException
	 */
	String getCamera() throws RemoteException;
	
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
	 * Gets the offset of the focus device for the first image.
	 * 
	 * @return The offset, in muM.
	 * @throws RemoteException
	 */
	double getOffset1() throws RemoteException;

	/**
	 * Sets the focus offset in which the first image should be taken.
	 * 
	 * @param offset1 The focus offset, in muM.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setOffset1(double offset1) throws RemoteException, ComponentRunningException;
	
	/**
	 * Gets the offset of the focus device for the second image.
	 * 
	 * @return The offset, in muM.
	 * @throws RemoteException
	 */
	double getOffset2() throws RemoteException;

	/**
	 * Sets the focus offset in which the second image should be taken.
	 * 
	 * @param offset2 The focus offset, in muM.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setOffset2(double offset2) throws RemoteException, ComponentRunningException;

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
	
	/**
	 * Sets a short string describing the image made by this job.
	 * @param description The description which should be returned for the images produced by this job, or null, to switch to the default description.
	 * @throws RemoteException
	 * @throws ComponentRunningException 
	 */
	void setImageDescription(String description) throws RemoteException, ComponentRunningException;
}
