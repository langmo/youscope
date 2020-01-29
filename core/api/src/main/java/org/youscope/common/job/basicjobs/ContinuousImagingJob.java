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
import org.youscope.common.image.ImageProducer;
import org.youscope.common.job.Job;

/**
 * @author langmo
 * 
 */
public interface ContinuousImagingJob extends Job, ImageProducer
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
	public static final String	DEFAULT_TYPE_IDENTIFIER	= "YouScope.ContinuousImagingJob";
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
	 * Sets a short string describing the images which are made by this job.
	 * @param description The description which should be returned for the images produced by this job, or null, to switch to the default description.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setImageDescription(String description) throws RemoteException, ComponentRunningException;
	
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
	 * Sets the camera with which it should be imaged. If set to null, the default (currently initialized) camera is used.
	 * 
	 * @param camera Device name of the camera, or null to use default camera.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setCamera(String camera) throws RemoteException, ComponentRunningException;

	/**
	 * Gets the cameras.
	 * 
	 * @return The cameras.
	 * @throws RemoteException
	 */
	String[] getCameras() throws RemoteException;

	/**
	 * Returns the name of the camera with which it is imaged. Might return null if the default camera is used. If more than one
	 * camera is used, returns the first camera.
	 * 
	 * @return The cameras.
	 * @throws RemoteException
	 */
	String getCamera() throws RemoteException;

	/**
	 * Returns if the imaging is done in burst mode. This corresponds to that the job queries for all images in the image buffer, not
	 * only for the newest one, every time it is executed.
	 * @return True if in burst mode, false otherwise.
	 * @throws RemoteException
	 */
	boolean isBurstImaging() throws RemoteException;

	/**
	 * Sets if the images are produced in burst mode. This corresponds to that the job queries for all images in the image buffer, not
	 * only for the newest one, every time it is executed. Default is false.
	 * 
	 * @param burst True if burst imaging should be activated, false if not.
	 * @throws RemoteException
	 * @throws ComponentRunningException
	 */
	void setBurstImaging(boolean burst) throws RemoteException, ComponentRunningException;
}
