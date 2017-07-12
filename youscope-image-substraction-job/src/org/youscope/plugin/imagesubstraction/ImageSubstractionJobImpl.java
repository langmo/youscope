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
import java.util.Vector;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobException;
import org.youscope.common.microscope.CameraDevice;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;

/**
 * @author Moritz Lang
 */
class ImageSubstractionJobImpl extends JobAdapter implements ImageSubstractionJob
{
	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID	= -3768352144003480111L;
	private volatile String					channel				= null;
	private volatile String					channelGroup			= null;
	private volatile double					exposure			= 10;
	private volatile Vector<ImageListener>	imageListeners		= new Vector<ImageListener>();
	
	private volatile double			offset1			= 5;
	private volatile double			offset2			= -5;
	private volatile int				adjustmentTime		= 0;
	private volatile String			focusDevice			= null;
	private volatile String						camera				= null;
	
	private volatile String imageDescription = null;

	public ImageSubstractionJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	@Override
	public String getImageDescription() 
	{
		if(imageDescription == null)
			return getDefaultImageDescription();
		return imageDescription;
	}
	private String getDefaultImageDescription()
	{
		String retVal = "SUB, ";
		if(channel != null && channelGroup != null && channel.length() > 0 && channelGroup.length() > 0)
			retVal += "channel = " + channelGroup + "." + channel + ", ";
		retVal += "exposure = " + Double.toString(exposure) + "ms";
		
		return retVal;
	}

	@Override
	public synchronized void setExposure(double exposure) throws ComponentRunningException
	{
		assertRunning();
		this.exposure = exposure;
	}

	@Override
	public double getExposure()
	{
		return exposure;
	}

	@Override
	public String getChannelGroup()
	{
		return channelGroup;
	}

	@Override
	public synchronized void setChannel(String deviceGroup, String channel) throws ComponentRunningException
	{
		assertRunning();
		this.channelGroup = deviceGroup;
		this.channel = channel;
	}

	@Override
	public String getChannel()
	{
		return channel;
	}

	@Override
	public void addImageListener(ImageListener listener)
	{
		if(listener == null)
			return;
		synchronized(imageListeners)
		{
			imageListeners.add(listener);
		}
	}

	@Override
	public void removeImageListener(ImageListener listener)
	{
		if(listener == null)
			return;
		synchronized(imageListeners)
		{
			imageListeners.remove(listener);
		}
	}

	@Override
	public void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		CameraDevice cameraDevice;
		if(camera == null || camera.length() < 1)
		{
			try
			{
				cameraDevice = microscope.getCameraDevice();
			}
			catch(DeviceException e1)
			{
				throw new JobException("Could not get default camera", e1);
			}
		}
		else
		{
			try
			{
				cameraDevice = microscope.getCameraDevice(camera);
			}
			catch(DeviceException e1)
			{
				throw new JobException("Could not find camera with device ID \"" + camera + "\".", e1);
			}
		}
		ImageEvent<?> image1;
		ImageEvent<?> image2;
		try
		{
			goFocus(microscope, offset1);
			image1 = cameraDevice.makeImage(channelGroup, channel, exposure);
			image1.setPositionInformation(getPositionInformation());
			image1.setExecutionInformation(executionInformation);
			image1.setCreationRuntime(measurementContext.getMeasurementRuntime());
			
			
			goFocus(microscope, -offset1+offset2);
			image2 = cameraDevice.makeImage(channelGroup, channel, exposure);
			image2.setPositionInformation(getPositionInformation());
			image2.setExecutionInformation(executionInformation);
			image2.setCreationRuntime(measurementContext.getMeasurementRuntime());
			
			goFocus(microscope, -offset2);
		}
		catch(Exception e)
		{
			throw new JobException("Could not take images in focus offsets.", e);
		}
		ImageEvent<?> subImage;
		try {
			subImage = ImageSubstractionTools.divideImages(image1, image2);
		} catch (Exception e) {
			throw new JobException("Could not substract offset images.", e);
		}
		subImage.setPositionInformation(getPositionInformation());
		subImage.setExecutionInformation(executionInformation);
		subImage.setCreationRuntime(measurementContext.getMeasurementRuntime());
		
		sendImageToListeners(subImage);
	}

	

	@Override
	protected String getDefaultName()
	{
		return "Substraction (offsets "+Double.toString(getOffset1())+"/"+Double.toString(getOffset2())+"um) imaging channel " + getChannelGroup() + "." + getChannel() + ", exposure " + Double.toString(getExposure()) + "ms";
	}

	private void sendImageToListeners(ImageEvent<?> e)
	{
		synchronized(imageListeners)
		{
			for(int i = 0; i < imageListeners.size(); i++)
			{
				ImageListener listener = imageListeners.elementAt(i);
				try
				{
					listener.imageMade(e);
				}
				catch(@SuppressWarnings("unused") RemoteException e1)
				{
					// Connection probably broken down...
					imageListeners.removeElementAt(i);
					i--;
				}
			}
		}
	}
	
	private void goFocus(Microscope microscope, double offset) throws InterruptedException, RemoteException, MicroscopeLockedException, MicroscopeException, DeviceException
	{
		if(focusDevice == null)
		{
			microscope.getFocusDevice().setRelativeFocusPosition(offset);
		}
		else
		{
			microscope.getFocusDevice(focusDevice).setRelativeFocusPosition(offset);
		}
		if(Thread.interrupted())
			throw new InterruptedException();
		Thread.sleep(adjustmentTime);
	}
	
	@Override
	public String getFocusDevice()
	{
		return focusDevice;
	}

	@Override
	public void setFocusDevice(String focusDevice) throws ComponentRunningException
	{
		assertRunning();
		this.focusDevice = focusDevice;
	}

	@Override
	public int getFocusAdjustmentTime()
	{
		return adjustmentTime;
	}

	@Override
	public void setFocusAdjustmentTime(int adjustmentTime) throws ComponentRunningException
	{
		assertRunning();
		this.adjustmentTime = adjustmentTime;
	}

	@Override
	public void setImageDescription(String description) throws ComponentRunningException
	{
		assertRunning();
		imageDescription = description;
	}

	@Override
	public int getNumberOfImages()
	{
		return 1;
	}

	@Override
	public void setCamera(String camera) throws ComponentRunningException {
		assertRunning();
		this.camera = camera;
	}

	@Override
	public String getCamera() 
	{
		return camera;
	}

	@Override
	public double getOffset1() 
	{
		return offset1;
	}

	@Override
	public void setOffset1(double offset1) throws ComponentRunningException 
	{
		assertRunning();
		this.offset1 = offset1;
	}

	@Override
	public double getOffset2()
	{
		return offset2;
	}

	@Override
	public void setOffset2(double offset2) throws ComponentRunningException {
		assertRunning();
		this.offset2 = offset2;
	}
}
