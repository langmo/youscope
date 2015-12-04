/**
 * 
 */
package org.youscope.plugin.continousimaging;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Date;
import java.util.Vector;

import org.youscope.common.ImageEvent;
import org.youscope.common.ImageListener;
import org.youscope.common.measurement.ExecutionInformation;
import org.youscope.common.measurement.MeasurementContext;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.JobAdapter;
import org.youscope.common.measurement.job.JobException;
import org.youscope.common.measurement.job.basicjobs.ContinuousImagingJob;
import org.youscope.common.microscope.CameraDevice;
import org.youscope.common.microscope.Microscope;

/**
 * @author Moritz Lang
 */
class ContinuousImagingJobImpl extends JobAdapter implements ContinuousImagingJob
{
	/**
	 * Serial Version UID.
	 */
	private static final long					serialVersionUID	= -6452800070044720173L;

	private volatile String					channel				= null;
	private volatile String					configGroup			= null;
	
	private ImageGatherer[] imageGatherers = null;
	
	private volatile double[]					exposures			= new double[] {10};
	private volatile boolean burstMode = false;
	private volatile String[]							cameras				= new String[] {null};
	private final Vector<ImageListener>	imageListeners		= new Vector<ImageListener>();
	private volatile String imageDescription = null;
	public ContinuousImagingJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}
	
	private class ImageGatherer extends UnicastRemoteObject implements ImageListener
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= 1767496756465103915L;
		private ImageEvent lastImage = null;
		private volatile int evaluationNumber = 0;
		private final PositionInformation positionInformation;
		ImageGatherer(PositionInformation positionInformation) throws RemoteException
		{
			this.positionInformation = positionInformation;
		}
		@Override
		public void imageMade(ImageEvent image) throws RemoteException
		{
			image.setPositionInformation(positionInformation);
			
			if(burstMode)
			{
				image.setExecutionInformation(new ExecutionInformation(new Date().getTime(), evaluationNumber++));
				sendImageToListeners(image);
			}
			else
			{
				synchronized(this)
				{
					lastImage = image;
				}
			}
		}
		
		private synchronized ImageEvent getLastImage()
		{
			ImageEvent temp = lastImage;
			lastImage = null;
			return temp;
		}
	}

	@Override
	public String getDefaultName()
	{
		String text = "Continuously imaging channel " + configGroup + "." + channel;
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
		if(!burstMode)
		{
			String[] cameraIDs = getCameras();
			for(int i = 0; i < cameraIDs.length; i++)
			{
				ImageEvent image = imageGatherers[i].getLastImage();
				if(image == null)
					continue;
				image.setExecutionInformation(executionInformation);
				sendImageToListeners(image);
			}
		}
	}
	
	private void sendImageToListeners(ImageEvent image)
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
	public synchronized void setExposure(double exposure) throws MeasurementRunningException
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
	public String getChannelGroup()
	{
		return configGroup;
	}

	@Override
	public String[] getCameras()
	{
		return cameras;
	}
	
	@Override
	public String getCamera()
	{
		return cameras[0];
	}

	@Override
	public synchronized void setChannel(String deviceGroup, String channel) throws MeasurementRunningException
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
	public void setCamera(String camera) throws MeasurementRunningException
	{
		setCameras(new String[]{camera});
	}
	
	@Override
	public synchronized void setCameras(String[] cameras) throws MeasurementRunningException
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
	public synchronized void setExposures(double[] exposures) throws MeasurementRunningException
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
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.initializeJob(microscope, measurementContext);
		String[] cameraIDs = getCameras();
		//evaluationNumbers = new int[cameraIDs.length];
		imageGatherers = new ImageGatherer[cameraIDs.length];
		for(int i = 0; i < cameraIDs.length; i++)
		{
			//evaluationNumbers[i] = 0;
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
					camera.startContinuousSequenceAcquisition(ContinuousImagingJobImpl.this.configGroup, ContinuousImagingJobImpl.this.channel, ContinuousImagingJobImpl.this.exposures[i], imageGatherers[i]);
				else
					camera.startContinuousSequenceAcquisition(null, null, ContinuousImagingJobImpl.this.exposures[i], imageGatherers[i]);
			}
			catch(Exception e)
			{
				throw new JobException("Could not start continuous sequence acquisition on camera.", e);
			}
		}
	}

	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		String[] cameraIDs = getCameras();
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
		
		super.uninitializeJob(microscope, measurementContext);
	}

	@Override
	public int getNumberOfImages()
	{
		return 1;
	}

	@Override
	public boolean isBurstImaging()
	{
		return burstMode;
	}

	@Override
	public synchronized void setBurstImaging(boolean burstMode) throws MeasurementRunningException
	{
		assertRunning();
		this.burstMode = burstMode;
	}
	
	@Override
	public void setImageDescription(String description) throws MeasurementRunningException
	{
		assertRunning();
		imageDescription = description;
	}
}
