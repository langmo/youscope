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
package org.youscope.server;

import java.awt.Dimension;
import java.rmi.RemoteException;

import org.youscope.addon.microscopeaccess.CameraDeviceInternal;
import org.youscope.addon.microscopeaccess.ChannelInternal;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.microscope.CameraDevice;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.SettingException;

/**
 * RMI interface for camera access.
 * @author Moritz Lang
 */
class CameraDeviceRMI extends DeviceRMI implements CameraDevice
{
	/**
	 * Serial Version UID.
	 */
	private static final long			serialVersionUID	= -6553028247559261711L;

	private final CameraDeviceInternal	camera;
	private final ChannelManagerImpl	channelManager;

	/**
	 * Constructor.
	 * @throws RemoteException
	 */
	protected CameraDeviceRMI(CameraDeviceInternal camera, ChannelManagerImpl channelManager, int accessID) throws RemoteException
	{
		super(camera, accessID);
		this.camera = camera;
		this.channelManager = channelManager;
	}

	@Override
	public ImageEvent<?> makeImage(String channelGroupID, String channelID, double exposure) throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException
	{
		ChannelInternal channel = channelManager.getChannel(channelGroupID, channelID);
		return camera.makeImage(channel, exposure, accessID);
	}

	@Override
	public void setExposure(double exposure) throws MicroscopeLockedException, MicroscopeException, InterruptedException
	{
		camera.setExposure(exposure, accessID);

	}

	@Override
	public void stopContinuousSequenceAcquisition() throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException, DeviceException
	{
		camera.stopContinuousSequenceAcquisition(accessID);
	}

	@Override
	public void setTransposeX(boolean transpose) throws MicroscopeLockedException
	{
		camera.setTransposeX(transpose, accessID);
	}

	@Override
	public void setTransposeY(boolean transpose) throws MicroscopeLockedException
	{
		camera.setTransposeY(transpose, accessID);
	}

	@Override
	public void setSwitchXY(boolean switchXY) throws MicroscopeLockedException
	{
		camera.setSwitchXY(switchXY, accessID);
	}

	@Override
	public boolean isTransposeX()
	{
		return camera.isTransposeX();
	}

	@Override
	public boolean isTransposeY()
	{
		return camera.isTransposeY();
	}

	@Override
	public boolean isSwitchXY()
	{
		return camera.isSwitchXY();
	}

	@Override
	public Dimension getImageSize() throws MicroscopeLockedException, MicroscopeException, RemoteException
	{
		return camera.getImageSize(accessID);
	}

	@Override
	public void startContinuousSequenceAcquisition(String channelGroupID, String channelID, double exposure, ImageListener listener) throws MicroscopeException, RemoteException, MicroscopeLockedException, InterruptedException, SettingException, DeviceException
	{
		ChannelInternal channel = channelManager.getChannel(channelGroupID, channelID);
		camera.startContinuousSequenceAcquisition(channel, exposure, listener, accessID);
	}

	@Override
	@Deprecated
	public ImageEvent<?>[] makeParallelImages(String channelGroupID, String channelID, String[] cameraIDs, double[] exposures) throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException, DeviceException, RemoteException
	{
		ChannelInternal channel = channelManager.getChannel(channelGroupID, channelID);
		return camera.makeParallelImages(channel, cameraIDs, exposures, accessID);
	}
}
