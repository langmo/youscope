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
package org.youscope.plugin.continousimaging;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
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
import org.youscope.common.microscope.Microscope;

/**
 * @author Moritz Lang
 */
class ShortContinuousImagingJobImpl extends JobAdapter implements ShortContinuousImagingJob
{
	/**
	 * Serial Version UID.
	 */
	private static final long					serialVersionUID	= -6111800070044720173L;

	private volatile String					channel				= null;
	private volatile String					configGroup			= null;
	
	private ImageGatherer[] imageGatherers = null;
	
	private volatile double[]					exposures			= new double[] {10};
	private volatile boolean burstMode = false;
	private volatile String[]							cameras				= new String[] {null};
	private final Vector<ImageListener>	imageListeners		= new Vector<ImageListener>();

	private volatile int numImages = 10;
	
	private volatile int imagingPeriod = 100;
	
	private volatile String imageDescription = null;
	
	public ShortContinuousImagingJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}
	
	private class ImageGatherer extends UnicastRemoteObject implements ImageListener
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 1767496756465103915L;
		private ImageEvent<?> lastImage = null;
		private final PositionInformation positionInformation;
		ImageGatherer(PositionInformation positionInformation) throws RemoteException
		{
			this.positionInformation = positionInformation;
		}
		@Override
		public void imageMade(ImageEvent<?> image) throws RemoteException
		{
			image.setPositionInformation(positionInformation);
			
			synchronized(this)
			{
				lastImage = image;
				this.notifyAll();
			}
		}
		
		private synchronized ImageEvent<?> getLastImage()
		{
			ImageEvent<?> temp = lastImage;
			lastImage = null;
			return temp;
		}
		
		private ImageEvent<?> waitForImage() throws InterruptedException
		{
			ImageEvent<?> image;
			while(true)
			{
				synchronized(this)
				{
					image = getLastImage();
					if(image != null)
						return image;
					this.wait();
				}
			}
		}
	}

	@Override
	public void setImageDescription(String description) throws ComponentRunningException
	{
		assertRunning();
		imageDescription = description;
	}
	
	@Override
	protected String getDefaultName()
	{
		String text = "Short Continuously imaging channel " + configGroup + "." + channel;
		for(int i = 0; i < cameras.length && i < exposures.length; i++)
		{
			text += ", ";
			if(cameras[i] == null)
				text += "default camera, ";
			else
				text += cameras[i] + ", ";
			text += "exposure " + Double.toString(exposures[i]) + "ms";
		}

		if(burstMode)
			text += "(burst mode)";
		return text;
	}

	@Override
	public void runJob(final ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		// Initialize
		String[] cameraIDs = getCameras();
		imageGatherers = new ImageGatherer[cameraIDs.length];
		for(int i = 0; i < cameraIDs.length; i++)
		{
			PositionInformation positionInformation = getPositionInformation();
			if(cameraIDs.length > 1)
				positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_CAMERA, i);
			imageGatherers[i] = new ImageGatherer(positionInformation);
			
			CameraDevice camera;
			try
			{
				if(cameraIDs[i] != null && cameraIDs[i].length() > 0)
				{
					camera = microscope.getCameraDevice(cameraIDs[i]);
				}
				else
				{
					camera = microscope.getCameraDevice();
				}
			}
			catch(Exception e)
			{
				throw new JobException("Could not obtain camera device from microscope.", e);
			}
			
			try
			{
				if(i == 0)
					camera.startContinuousSequenceAcquisition(ShortContinuousImagingJobImpl.this.configGroup, ShortContinuousImagingJobImpl.this.channel, ShortContinuousImagingJobImpl.this.exposures[i], imageGatherers[i]);
				else
					camera.startContinuousSequenceAcquisition(null, null, ShortContinuousImagingJobImpl.this.exposures[i], imageGatherers[i]);
			}
			catch(Exception e)
			{
				throw new JobException("Could not start continuous sequence acquisition on camera.", e);
			}
		}
		
		// run
		for(int n=0; n<numImages; n++)
		{
			for(int i = 0; i < cameraIDs.length; i++)
			{
				ImageEvent<?> image = imageGatherers[i].waitForImage();
				if(image == null)
					continue;
				ExecutionInformation exInfo = new ExecutionInformation(executionInformation, n);
				image.setExecutionInformation(exInfo);
				image.setCreationRuntime(measurementContext.getMeasurementRuntime());
				sendImageToListeners(image);
			}
			if(!burstMode)
				Thread.sleep(imagingPeriod);
		}
		
		// uninitialize
		for(int i = 0; i < cameraIDs.length; i++)
		{
			CameraDevice camera;
			try
			{
				if(cameraIDs[i] != null && cameraIDs[i].length() > 0)
				{
					camera = microscope.getCameraDevice(cameraIDs[i]);
				}
				else
				{
					camera = microscope.getCameraDevice();
				}
			}
			catch(Exception e)
			{
				throw new JobException("Could not obtain camera device from microscope.", e);
			}
			
			try
			{
				camera.stopContinuousSequenceAcquisition();
			}
			catch(Exception e)
			{
				throw new JobException("Could not stop continuous sequence acquisition on camera.", e);
			}
		}
		imageGatherers = null;
	}
	
	private void sendImageToListeners(ImageEvent<?> image)
	{
		synchronized(imageListeners)
		{
			for(int k = 0; k < imageListeners.size(); k++)
			{
				ImageListener listener = imageListeners.elementAt(k);
				try
				{
					listener.imageMade(image);
				}
				catch(@SuppressWarnings("unused") RemoteException e1)
				{
					// Connection probably broken down...
					imageListeners.removeElementAt(k);
					k--;
				}
			}
		}
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
		return configGroup;
	}

	@Override
	public String[] getCameras() throws RemoteException
	{
		return cameras;
	}
	
	@Override
	public String getCamera()
	{
		return cameras[0];
	}

	@Override
	public synchronized void setChannel(String deviceGroup, String channel) throws ComponentRunningException
	{
		assertRunning();
		this.configGroup = deviceGroup;
		this.channel = channel;
	}

	@Override
	public String getChannel()
	{
		return channel;
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
				// Fill with default
				newExposures[i] = 10;
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

	@Override
	public String getImageDescription() 
	{
		if(imageDescription == null)
			return getDefaultImageDescription();
		return imageDescription;
	}
	
	private String getDefaultImageDescription()
	{
		String retVal = "Continuously imaging channel \"" + channel + "\": ";
		for(int i = 0; i < cameras.length; i++)
		{
			if(i > 0)
				retVal += ", ";
			if(cameras[i] != null)
				retVal += cameras[i];
			else
				retVal += "default camera";
			if(exposures[i] > 0.0)
				retVal += " (exposure = " + Double.toString(exposures[i]) + "ms)";
		}
		return retVal;
	}

	@Override
	public int getNumberOfImages() throws RemoteException
	{
		return 1;
	}

	@Override
	public boolean isBurstImaging() throws RemoteException
	{
		return burstMode;
	}

	@Override
	public synchronized void setBurstImaging(boolean burstMode) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		this.burstMode = burstMode;
	}

	@Override
	public void setNumImages(int numImages) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		this.numImages = numImages;
	}

	@Override
	public int getNumImages() throws RemoteException
	{
		return numImages;
	}

	@Override
	public void setImagingPeriod(int imagingPeriod) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		this.imagingPeriod = imagingPeriod;
	}

	@Override
	public int getImagingPeriod() throws RemoteException
	{
		return imagingPeriod;
	}
}
