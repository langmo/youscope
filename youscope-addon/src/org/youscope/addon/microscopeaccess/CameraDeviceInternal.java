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
package org.youscope.addon.microscopeaccess;

import java.awt.Dimension;

import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.SettingException;

/**
 * Internal interface used to control a camera.
 * @author Moritz Lang
 */
public interface CameraDeviceInternal extends DeviceInternal
{
	/** 
	 * Sets the exposure for imaging.
	 * 
	 * @param exposure The exposure in ms.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	void setExposure(double exposure, int accessID) throws MicroscopeLockedException, MicroscopeException, InterruptedException;

	/**
	 * Makes an image with the given settings.
	 * 
	 * @param channel The channel in which it should be imaged, or null.
	 * @param exposure The exposure time in ms.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @return The image which was taken.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws InterruptedException
	 * @throws SettingException 
	 */
	ImageEvent<?> makeImage(ChannelInternal channel, double exposure, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException;

	/**
	 * Starts a continuous sequence acquisition.
	 * Due to implementation reasons, don't use this method if sequence acquisition should be started on multiple cameras in parallel, but use startParallelContinuousSequenceAcquisition.
	 * @param channel The channel in which it should be imaged, or null.
	 * @param exposure The exposure time in ms.
	 * @param listener Listener which receives all new images.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws InterruptedException
	 * @throws SettingException 
	 * @throws DeviceException 
	 */
	void startContinuousSequenceAcquisition(ChannelInternal channel, double exposure, ImageListener listener, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException, DeviceException;

	/**
	 * Stops the previously started continuous acquisition.
	 * 
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws InterruptedException 
	 * @throws SettingException 
	 * @throws DeviceException 
	 */
	void stopContinuousSequenceAcquisition(int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException, DeviceException;

	/**
	 * Sets if the x-direction of images made by this camera should be transposed.
	 * @param transpose True, if the x-direction should be transposed.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException 
	 */
	void setTransposeX(boolean transpose, int accessID) throws MicroscopeLockedException;

	/**
	 * Sets if the y-direction of images made by this camera should be transposed.
	 * @param transpose True, if the y-direction should be transposed.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException
	 */
	void setTransposeY(boolean transpose, int accessID) throws MicroscopeLockedException;

	/**
	 * Sets if the x and the y direction should be switched for images made by this camera.
	 * @param switchXY True, if the x and y direction should be switched.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException
	 */
	void setSwitchXY(boolean switchXY, int accessID) throws MicroscopeLockedException;

	/**
	 * Returns if the x-direction of images made by this camera should be transposed.
	 * @return True, if the x-direction is transposed.
	 */
	boolean isTransposeX();

	/**
	 * Returns if the if the y-direction of images made by this camera should be transposed.
	 * @return True, if the y-direction is transposed.
	 */
	boolean isTransposeY();

	/**
	 * Returns if the x and the y direction should be switched for images made by this camera.
	 * @return True, if the x and y direction should be switched.
	 */
	boolean isSwitchXY();
	
	/**
	 * Returns the size (in pixels) of the images this camera produces.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @return Size of images in pixels.
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 */
	Dimension getImageSize(int accessID) throws MicroscopeLockedException, MicroscopeException;

	/**
	 * Makes images with the given cameras in parallel.
	 * @param channel The channel in which it should be imaged, or null.
	 * @param cameraIDs Array of IDs of the cameras.
	 * @param exposures The exposure times of the cameras in ms.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @return The images made with the cameras, in the same order as the camera IDs. 
	 * @throws MicroscopeException
	 * @throws MicroscopeLockedException
	 * @throws InterruptedException
	 * @throws SettingException 
	 * @throws DeviceException
	 * 
	 */
	@Deprecated
	ImageEvent<?>[] makeParallelImages(ChannelInternal channel, String[] cameraIDs, double[] exposures, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException, DeviceException;
}
