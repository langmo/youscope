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
package org.youscope.plugin.composedimaging;

import java.awt.Dimension;
import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.image.ImageProducer;
import org.youscope.common.job.Job;


/**
 * @author langmo
 */
public interface ComposedImagingJob extends Job, ImageProducer
{

	/**
	 * Sets the channel.
	 * 
	 * @param deviceGroup The device group where the channel is defined.
	 * @param channel The channel.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setChannel(String deviceGroup, String channel) throws RemoteException, ComponentRunningException;

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
	void setExposure(double exposure) throws RemoteException, ComponentRunningException;

	/**
	 * Gets the exposure. If more than one camera is initialized, returns the exposure of the first camera.
	 * 
	 * @return The exposure.
	 * @throws RemoteException
	 */
	double getExposure() throws RemoteException;

	/**
	 * Sets the exposures of the cameras.
	 * Should have the same size as cameras, otherwise an exception is thrown.
	 * Each element can be set to -1 to not set the exposure for the given camera.
	 * 
	 * @param exposures The exposures.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 * @throws IllegalArgumentException Thrown if number of elements in exposure array is unequal to the number of cameras.
	 */
	void setExposures(double[] exposures) throws RemoteException, ComponentRunningException, IllegalArgumentException;

	/**
	 * Gets the exposures of the initialized cameras.
	 * 
	 * @return The exposures.
	 * @throws RemoteException
	 */
	double[] getExposures() throws RemoteException;

	/**
	 * Sets the cameras with which it should be imaged. If set to null, the default (currently initialized) camera is used.
	 * Each camera should only appear ones in the array. One element of the array can be NULL, such that the default camera is used.
	 * 
	 * @param cameras Device names of the cameras
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setCameras(String[] cameras) throws RemoteException, ComponentRunningException;

	/**
	 * Gets the cameras.
	 * 
	 * @return The cameras.
	 * @throws RemoteException
	 */
	String[] getCameras() throws RemoteException;
	
	/**
	 * Returns the distance between two horizontally attached sub-images.
	 * 
	 * @return distance in micro meter.
	 * @throws RemoteException
	 */
	double getDeltaX() throws RemoteException;

	/**
	 * Sets the distance between two horizontally attached sub-images.
	 * 
	 * @param deltaX
	 *            Distance in micro meter.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setDeltaX(double deltaX) throws RemoteException,
	ComponentRunningException;

	/**
	 * Returns the distance between two vertically attached sub-images.
	 * 
	 * @return distance in micro meter.
	 * @throws RemoteException
	 */
	double getDeltaY() throws RemoteException;

	/**
	 * Sets the distance between two vertically attached sub-images.
	 * 
	 * @param deltaY
	 *            Distance in micro meter.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setDeltaY(double deltaY) throws RemoteException,
	ComponentRunningException;

	/**
	 * Returns the number of sub-images.
	 * 
	 * @return Number of sub-images
	 * @throws RemoteException
	 */
	Dimension getSubImageNumber() throws RemoteException;

	/**
	 * Sets the number of sub-images
	 * 
	 * @param imageNumbers
	 *            Number of sub-images.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setSubImageNumber(Dimension imageNumbers) throws RemoteException,
	ComponentRunningException;
}
