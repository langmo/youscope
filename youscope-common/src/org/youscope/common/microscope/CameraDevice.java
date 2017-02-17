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
package org.youscope.common.microscope;

import java.awt.Dimension;
import java.rmi.RemoteException;

import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;

/**
 * @author langmo
 */
public interface CameraDevice extends Device
{
	/**
	 * Sets the exposure for imaging.
	 * 
	 * @param exposure The exposure in ms.
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 * @throws RemoteException
	 */
	void setExposure(double exposure) throws MicroscopeLockedException, MicroscopeException, InterruptedException, RemoteException;

	/**
	 * Makes an image with the given settings.
	 * 
	 * @param channelGroupID The ID of the channel group the channel is in.
	 * @param channelID The ID of the channel.
	 * @param exposure The exposure time in ms.
	 * @return the Image taken.
	 * @throws MicroscopeException
	 * @throws RemoteException
	 * @throws MicroscopeLockedException
	 * @throws InterruptedException
	 * @throws SettingException
	 */
	ImageEvent<?> makeImage(String channelGroupID, String channelID, double exposure) throws MicroscopeException, RemoteException, MicroscopeLockedException, InterruptedException, SettingException;

	/**
	 * Starts a continuous sequence acquisition.
	 * @param channelGroupID The ID of the channel group the channel is in.
	 * @param channelID The ID of the channel.
	 * @param exposure The exposure time in ms.
	 * @param listener Listener which gets notified if a new image is made.
	 * @throws MicroscopeException
	 * @throws RemoteException
	 * @throws MicroscopeLockedException
	 * @throws InterruptedException
	 * @throws SettingException
	 * @throws DeviceException
	 */
	void startContinuousSequenceAcquisition(String channelGroupID, String channelID, double exposure, ImageListener listener) throws MicroscopeException, RemoteException, MicroscopeLockedException, InterruptedException, SettingException, DeviceException;

	/**
	 * Makes images with the given cameras in parallel.
	 * @param channelGroupID The ID of the channel group the channel is in.
	 * @param channelID The ID of the channel.
	 * @param cameraIDs Array of IDs of the cameras.
	 * @param exposures The exposure times of the cameras in ms.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @return The images made with the cameras, in the same order as the camera IDs.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws InterruptedException
	 * @throws SettingException
	 * @throws DeviceException
	 * @throws RemoteException
	 * 
	 */
	@Deprecated
	ImageEvent<?>[] makeParallelImages(String channelGroupID, String channelID, String[] cameraIDs, double[] exposures) throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException, DeviceException, RemoteException;

	/**
	 * Stops the previously started continuous acquisition.
	 * 
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 * @throws InterruptedException
	 * @throws SettingException
	 * @throws DeviceException
	 */
	void stopContinuousSequenceAcquisition() throws MicroscopeException, MicroscopeLockedException, RemoteException, InterruptedException, SettingException, DeviceException;

	/**
	 * Sets if the x-direction of images made by this camera should be transposed.
	 * @param transpose True, if the x-direction should be transposed.
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 */
	void setTransposeX(boolean transpose) throws MicroscopeLockedException, RemoteException;

	/**
	 * Sets if the y-direction of images made by this camera should be transposed.
	 * @param transpose True, if the y-direction should be transposed.
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 */
	void setTransposeY(boolean transpose) throws MicroscopeLockedException, RemoteException;

	/**
	 * Sets if the x and the y direction should be switched for images made by this camera.
	 * @param switchXY True, if the x and y direction should be switched.
	 * @throws MicroscopeLockedException
	 * @throws RemoteException
	 */
	void setSwitchXY(boolean switchXY) throws MicroscopeLockedException, RemoteException;

	/**
	 * Returns if the x-direction of images made by this camera should be transposed.
	 * @return True, if the x-direction is transposed.
	 * @throws RemoteException
	 */
	boolean isTransposeX() throws RemoteException;

	/**
	 * Returns if the if the y-direction of images made by this camera should be transposed.
	 * @return True, if the y-direction is transposed.
	 * @throws RemoteException
	 */
	boolean isTransposeY() throws RemoteException;

	/**
	 * Returns if the x and the y direction should be switched for images made by this camera.
	 * @return True, if the x and y direction should be switched.
	 * @throws RemoteException
	 */
	boolean isSwitchXY() throws RemoteException;

	/**
	 * Returns the size (in pixels) of the images this camera produces.
	 * @return Size of images in pixels.
	 * @throws RemoteException
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 */
	Dimension getImageSize() throws RemoteException, MicroscopeLockedException, MicroscopeException;
}
