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
package org.youscope.plugin.imagingjob;

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
import org.youscope.common.job.basicjobs.ImagingJob;
import org.youscope.common.microscope.CameraDevice;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.Microscope;

/**
 * Implementation of the basic imaging job.
 * @author Moritz Lang
 */
class ImagingJobImpl extends JobAdapter implements ImagingJob
{
	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID	= -3768352144003480453L;
	private volatile String					channelID				= null;
	private volatile String					channelGroupID			= null;
	private volatile double[]				exposures			= new double[] {10};
	private String[]						cameras				= new String[] {null};
	private volatile Vector<ImageListener>	imageListeners		= new Vector<ImageListener>();

	private volatile String imageDescription = null;
	
	public ImagingJobImpl(PositionInformation positionInformation) throws RemoteException
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
		String retVal = "";
		if(channelID != null && channelID.length() > 0)
			retVal += "channel = " + channelID;
		if(cameras.length == 1 && cameras[0] == null)
		{
			if(retVal.length() > 0)
				retVal += ", ";
			retVal += "exposure = " + Double.toString(exposures[0]) + "ms";
		}
		else
		{
			for(int i = 0; i < cameras.length; i++)
			{
				if(retVal.length() > 0)
					retVal += ", ";
				if(cameras[i] != null)
					retVal += cameras[i];
				else
					retVal += "default camera";
				if(exposures[i] > 0.0)
					retVal += " (exposure = " + Double.toString(exposures[i]) + "ms)";
			}
		}
		return retVal;
	}

	@Override
	public synchronized void setExposure(double exposure) throws ComponentRunningException
	{
		assertRunning();
		exposures = new double[cameras.length];
		for(int i = 0; i < exposures.length; i++)
		{
			exposures[i] = exposure;
		}
		setExposures(exposures);
	}

	@Override
	public double getExposure()
	{
		return getExposures()[0];
	}

	@Override
	public String getChannelGroup() throws RemoteException
	{
		return channelGroupID;
	}

	@Override
	public String getCamera()
	{
		if(cameras == null || cameras.length < 1)
			return null;
		return cameras[1];
	}
	
	@Override
	public String[] getCameras() throws RemoteException
	{
		return cameras;
	}

	@Override
	public synchronized void setChannel(String deviceGroup, String channel) throws ComponentRunningException
	{
		assertRunning();
		this.channelGroupID = deviceGroup;
		this.channelID = channel;
	}

	@Override
	public String getChannel()
	{
		return channelID;
	}

	@Override
	public void setCamera(String camera) throws ComponentRunningException
	{
		setCameras(new String[]{camera});
	}
	
	@Override
	public synchronized void setCameras(String[] cameras) throws ComponentRunningException
	{
		assertRunning();

		if(cameras == null)
			cameras = new String[] {null};

		this.cameras = cameras;

		// Adjust length of exposures array
		if(exposures.length > this.cameras.length)
		{
			double[] newExposures = new double[cameras.length];
			System.arraycopy(exposures, 0, newExposures, 0, newExposures.length);
			exposures = newExposures;
		}
		else if(exposures.length < this.cameras.length)
		{
			double[] newExposures = new double[cameras.length];
			System.arraycopy(exposures, 0, newExposures, 0, exposures.length);
			for(int i = exposures.length; i < newExposures.length; i++)
			{
				// Fill with "do not set".
				newExposures[i] = -1.0;
			}
			exposures = newExposures;
		}
	}

	@Override
	public synchronized void setExposures(double[] exposures) throws ComponentRunningException
	{
		assertRunning();
		this.exposures = exposures;
	}

	@Override
	public double[] getExposures()
	{
		return exposures;
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

	@SuppressWarnings("deprecation")
	@Override
	public void runJob(final ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		if(cameras.length == 1)
		{
			// Simple case: use only one camera.
			// This function produces less overhead.
			CameraDevice cameraDevice;
			if(cameras[0] == null || cameras[0].length() < 1)
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
					cameraDevice = microscope.getCameraDevice(cameras[0]);
				}
				catch(DeviceException e1)
				{
					throw new JobException("Could not find camera with device ID \"" + cameras[0] + "\".", e1);
				}
			}
			
			ImageEvent<?> e;
			try
			{
				e = cameraDevice.makeImage(channelGroupID, channelID, exposures[0]);
			}
			catch(Exception e1)
			{
				throw new JobException("Could not take image.", e1);
			}
			// Set execution information
			e.setExecutionInformation(executionInformation);
			e.setCreationRuntime(measurementContext.getMeasurementRuntime());
			
			sendImageToListeners(e);
		}
		else
		{
			ImageEvent<?>[] images;
			try
			{
				images = microscope.getCameraDevice().makeParallelImages(channelGroupID, channelID, cameras, exposures);
			}
			catch(Exception e1)
			{
				throw new JobException("Could not take images.", e1);
			}
			for(ImageEvent<?> image : images)
			{
				// Set execution information
				image.setExecutionInformation(executionInformation);
				image.setCreationRuntime(measurementContext.getMeasurementRuntime());
				sendImageToListeners(image);
			}
		}
	}

	@Override
	protected String getDefaultName()
	{
		String text = "Imaging channel " + channelGroupID + "." + channelID;
		for(int i = 0; i < cameras.length && i < exposures.length; i++)
		{
			text += ", ";
			if(cameras[i] == null)
				text += "default camera, ";
			else
				text += cameras[i] + ", ";
			text += "exposure " + Double.toString(exposures[i]) + "ms";
		}

		return text;
	}

	private void sendImageToListeners(ImageEvent<?> e)
	{
		// Set position information
		if(cameras.length > 1)
		{
			String camera = e.getCamera();
			boolean found = false;
			for(int i = 0; i < cameras.length; i++)
			{
				if((camera == null && cameras[i] != null) || (camera != null && !camera.equals(cameras[i])))
					continue;
				found = true;
				e.setPositionInformation(new PositionInformation(getPositionInformation(), PositionInformation.POSITION_TYPE_CAMERA, i));
				break;
			}
			if(!found)
				e.setPositionInformation(new PositionInformation(getPositionInformation(), PositionInformation.POSITION_TYPE_CAMERA, 0));
		}
		else
		{
			e.setPositionInformation(getPositionInformation());
		}

		// send images to listeners.
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

	@Override
	public void setImageDescription(String description) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		imageDescription = description;
	}

	@Override
	public int getNumberOfImages() throws RemoteException
	{
		return cameras.length;
	}
}
