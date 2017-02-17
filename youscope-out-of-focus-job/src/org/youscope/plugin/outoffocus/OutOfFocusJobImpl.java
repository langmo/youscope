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
package org.youscope.plugin.outoffocus;

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
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;

/**
 * @author Moritz Lang
 */
class OutOfFocusJobImpl extends JobAdapter implements OutOfFocusJob
{
	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID	= -3768352144003480111L;
	private volatile String					channel				= null;
	private volatile String					channelGroup			= null;
	private volatile double					exposure			= 10;
	private volatile Vector<ImageListener>	imageListeners		= new Vector<ImageListener>();
	
	private volatile double			offset			= 0;
	private volatile int				adjustmentTime		= 0;
	private volatile String			focusDevice			= null;
	
	private volatile String imageDescription = null;

	public OutOfFocusJobImpl(PositionInformation positionInformation) throws RemoteException
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
		String retVal = "OOF, ";
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
		try
		{
			goFocus(microscope, offset);
			ImageEvent<?> e = microscope.getCameraDevice().makeImage(channelGroup, channel, exposure);
			e.setPositionInformation(getPositionInformation());
			e.setExecutionInformation(executionInformation);
			e.setCreationRuntime(measurementContext.getMeasurementRuntime());
			sendImageToListeners(e);
			goFocus(microscope, -offset);
		}
		catch(Exception e)
		{
			throw new JobException("Could not take image in out-of-focus plane.", e);
		}
	}

	@Override
	protected String getDefaultName()
	{
		return "Out of focus (offset "+Double.toString(getOffset())+"um) imaging channel " + getChannelGroup() + "." + getChannel() + ", exposure " + Double.toString(getExposure()) + "ms";
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
	public double getOffset()
	{
		return offset;
	}

	@Override
	public void setOffset(double offset) throws ComponentRunningException
	{
		assertRunning();
		this.offset = offset;
	}

	@Override
	public void setImageDescription(String description) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		imageDescription = description;
	}

	@Override
	public int getNumberOfImages() throws RemoteException
	{
		return 1;
	}
}
