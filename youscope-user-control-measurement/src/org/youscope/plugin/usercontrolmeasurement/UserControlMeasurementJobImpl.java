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
package org.youscope.plugin.usercontrolmeasurement;

import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.callback.CallbackException;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.image.ImageProducer;
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobException;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.StageDevice;

/**
 * @author langmo 
 */
class UserControlMeasurementJobImpl extends JobAdapter implements ImageProducer, UserControlMeasurementCallbackListener
{
	/**
	 * SerializableVersion UID.
	 */
	private static final long	serialVersionUID	= 8128119758338178084L;

	private UserControlMeasurementCallback callback;
	private volatile ImageEvent<?> lastImage = null;
	
	private final ArrayList<ImageListener> imageListeners = new ArrayList<ImageListener>();
	
	private String stageDevice = null;
	private double stageTolerance = 10;
	
	private volatile long imageNumber = -1;

	private volatile boolean channelChanged = true;
	
	private volatile String channel = null;
	private volatile String channelGroup = null;
	private volatile double exposure = 10;
		
	private volatile double lastPositionX = 0;
	private volatile double lastPositionY = 0;
	
	private volatile double currentPositionX = 0;
	private volatile double currentPositionY = 0;
	
	private volatile int currentPositionInformation = 0;
	
	private final ImageGatherer imageGatherer = new ImageGatherer();
	
	public UserControlMeasurementJobImpl(PositionInformation positionInformation) throws RemoteException
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
		
		/**
		 * Constructor.
		 * @throws RemoteException
		 */
		ImageGatherer() throws RemoteException
		{
			super();
		}

		@Override
		public void imageMade(ImageEvent<?> image) throws RemoteException
		{
			synchronized(this)
			{
				lastImage = image;
			}
		}
		
		private synchronized ImageEvent<?> getLastImage()
		{
			ImageEvent<?> temp = lastImage;
			lastImage = null;
			return temp;
		}
	}
	
	@Override
	public void runJob(final ExecutionInformation executionInformation,  Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		// Start or change imaging
		if(channelChanged)
		{
			channelChanged = false;
			channel = callback.getCurrentChannel();
			channelGroup = callback.getCurrentChannelGroup();
			exposure = callback.getCurrentExposure();
			
			try
			{
				microscope.getCameraDevice().stopContinuousSequenceAcquisition();
				microscope.getCameraDevice().startContinuousSequenceAcquisition(channelGroup, channel, exposure, imageGatherer);
			}
			catch(Exception e)
			{
				throw new JobException("Could not change channel or exposure in which the camera is continuously imaging.", e);
			}
		}
		
		// detect if position is changed, and if, increase position information and reset image number to 0;
		if(getStageTolerance() >= 0)
		{
			try
			{
				StageDevice stage;
				if(getStageDevice() == null)
					stage = microscope.getStageDevice();
				else
					stage = microscope.getStageDevice(getStageDevice());
				Point2D.Double currentPosition = stage.getPosition();
				
				currentPositionX = currentPosition.x;
				currentPositionY = currentPosition.y;
			}
			catch(Exception e)
			{
				throw new JobException("Could not get current stage position to automatically detect stage position changes.", e);
			}
		}
		
		// get image
		try
		{
			ImageEvent<?> image = imageGatherer.getLastImage();
			if(image != null)
			{
				image.setPositionInformation(getPositionInformation());
				image.setExecutionInformation(executionInformation);
				image.setCreationRuntime(measurementContext.getMeasurementRuntime());
				synchronized(this)
				{
					lastImage = image;
				}
				callback.newImage(image);
			}
		}
		catch(Exception e)
		{
			throw new JobException("Could not get next image from continuous image series.", e);
		}
	}
	
	@Override
	protected String getDefaultName()
	{
		return "User Control Measurement";
	}

	public void setMeasurementCallback(UserControlMeasurementCallback callback) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		this.callback = callback;
	}

	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.initializeJob(microscope, measurementContext);
		
		imageNumber = -1;
		
		// Check if callback defined.
		if(callback == null)
			throw new JobException("Measurement callback is null, thus cannot let the user control the measurement.");
		// Initialize callback.
		try
		{
			callback.initializeCallback();
		}
		catch(CallbackException e)
		{
			throw new JobException("Measurement callback did throw an error while initialization.", e);
		}
		
		callback.addCallbackListener(this);
	}

	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.uninitializeJob(microscope, measurementContext);
		
		if(callback != null)
		{
			callback.removeCallbackListener(this);
			try
			{
				callback.uninitializeCallback();
			}
			catch(CallbackException e)
			{
				throw new JobException("Measurement callback did throw an error while uninitialization.", e);
			}
		}	
		
		try
		{
			microscope.getCameraDevice().stopContinuousSequenceAcquisition();
		}
		catch(Exception e)
		{
			throw new JobException("Could not stop continuous imaging.", e);
		}
	}

	@Override
	public void addImageListener(ImageListener listener) throws RemoteException
	{
		if(listener == null)
			return;
		synchronized(imageListeners)
		{
			imageListeners.add(listener);
		}
	}

	@Override
	public void removeImageListener(ImageListener listener) throws RemoteException
	{
		if(listener == null)
			return;
		synchronized(imageListeners)
		{
			imageListeners.remove(listener);
		}
	}

	@Override
	public String getImageDescription() throws RemoteException
	{
		return "User activated imaging";
	}

	@Override
	public int getNumberOfImages() throws RemoteException
	{
		return 1;
	}

	@Override
	public void channelSettingsChanged() throws RemoteException
	{
		channelChanged = true;
	}

	@Override
	public void snapImage() throws RemoteException
	{
		ImageEvent<?> image;
		synchronized(this)
		{
			image = lastImage;
		}
		if(image == null)
			return;
		
		if((currentPositionInformation == 0 && getStageTolerance() >= 0) 
				|| Math.abs(currentPositionX - lastPositionX) > getStageTolerance()
				|| Math.abs(currentPositionY - lastPositionY) > getStageTolerance())
		{
			currentPositionInformation++;
			imageNumber = -1;
			lastPositionX = currentPositionX;
			lastPositionY = currentPositionY;
		}
		
		// update image information
		imageNumber++;
		image.setChannel(channel);
		image.setChannelGroup(channelGroup);
		image.setExecutionInformation(new ExecutionInformation(imageNumber));
		image.setPositionInformation(new PositionInformation(getPositionInformation(), PositionInformation.POSITION_TYPE_MAIN_POSITION, currentPositionInformation));
		
		// save image
		synchronized(imageListeners)
		{
			for(ImageListener listener : imageListeners)
			{
				listener.imageMade(image);
			}
		}
		
		// tell callback that image was saved.
		if(callback != null)
			callback.snappedImage();
	}

	@Override
	public void callbackClosed() throws RemoteException
	{
		callback = null;
	}
	
	/**
	 * Sets the stage device which should be monitored.
	 * If the stage device moves less than the tolerance between two successive images, the images are assumed to be made
	 * at the same position. Set to null to use the default stage.
	 * @param stageDevice ID of the stage to monitor, or null to use the default stage.
	 * @throws ComponentRunningException 
	 */
	public synchronized void setStageDevice(String stageDevice) throws ComponentRunningException
	{
		assertRunning();
		this.stageDevice = stageDevice;
	}

	/**
	 * Returns the stage device which should be monitored.
	 * If the stage device moves less than the tolerance between two successive images, the images are assumed to be made
	 * at the same position. Returns null if the default stage should be used.
	 * @return ID of the stage to monitor, or null to use the default stage.
	 */
	public String getStageDevice()
	{
		return stageDevice;
	}

	/**
	 * Sets the tolerance for the stage monitoring (in muM).
	 * If the stage device moves less than the tolerance between two successive images, the images are assumed to be made
	 * at the same position. Set to a negative value to not monitor the stage.
	 * @param stageTolerance Tolerance for stage in muM, or negative number to not monitor stage.
	 * @throws ComponentRunningException 
	 */
	public synchronized void setStageTolerance(double stageTolerance) throws ComponentRunningException
	{
		assertRunning();
		this.stageTolerance = stageTolerance;
	}

	/**
	 * Returns the tolerance for the stage monitoring (in muM).
	 * If the stage device moves less than the tolerance between two successive images, the images are assumed to be made
	 * at the same position. Returns a negative value if the stage is not monitored.
	 * @return Tolerance for stage in muM, or negative number if stage is not monitored.
	 */
	public double getStageTolerance()
	{
		return stageTolerance;
	}
}
