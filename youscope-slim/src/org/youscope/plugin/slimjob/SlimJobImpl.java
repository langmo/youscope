/**
 * 
 */
package org.youscope.plugin.slimjob;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.youscope.common.ImageAdapter;
import org.youscope.common.ImageEvent;
import org.youscope.common.ImageListener;
import org.youscope.common.measurement.ExecutionInformation;
import org.youscope.common.measurement.ImageProducer;
import org.youscope.common.measurement.MeasurementContext;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.Job;
import org.youscope.common.measurement.job.EditableJobContainerAdapter;
import org.youscope.common.measurement.job.JobException;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.Microscope;

/**
 * @author Moritz Lang
 */
class SlimJobImpl extends EditableJobContainerAdapter implements SlimJob
{
	
	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID	= -3768311144003480111L;
	private final ArrayList<ImageListener>	imageListeners		= new ArrayList<ImageListener>();
	
	private volatile String imageDescription = null;
	
	private volatile String				reflector			= null;
	
	private volatile int maskX = 1000;
	private volatile int maskY = 500;
	private volatile int innerRadius = 150;
	private volatile int outerRadius = 300;
	private volatile int phaseShiftOutside = 0;
	private volatile int slimDelayMs = 0;
	private final int[] phaseShiftsMask = {0,64,128,192};
	private volatile String maskFileName = null;
	private ImageAdapter[] imageAdapters = null;
	
	public SlimJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}
	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException,
			InterruptedException, RemoteException 
	{
		super.initializeJob(microscope, measurementContext);
		Job[] jobs = getJobs();
		if(jobs==null || jobs.length != 4)
			throw new JobException("Slim job requires exactly 4 image producing child jobs.");
		imageAdapters = new ImageAdapter[jobs.length];
		for(int i=0; i<jobs.length; i++)
		{
			if(jobs[i] == null || !(jobs[i] instanceof ImageProducer))
				throw new JobException("Child job " + Integer.toString(i+1) + " of SlimJob is either null or not an image producer.");
			imageAdapters[i] = new ImageAdapter();
			((ImageProducer)jobs[i]).addImageListener(imageAdapters[i]);
		}
	}
	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		if(imageAdapters != null)
		{
			Job[] jobs = getJobs();
			for(int i=0; i< jobs.length && i<imageAdapters.length; i++)
			{
				if(jobs[i] == null || !(jobs[i] instanceof ImageProducer))
					continue;
				((ImageProducer)jobs[i]).removeImageListener(imageAdapters[i]);
			}
		}
		imageAdapters = null;
		super.uninitializeJob(microscope, measurementContext);
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
		String retVal = "SLIM";
		return retVal;
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
		
		if(reflector == null)
			throw new JobException("Reflector device is NULL.");
		Device reflectorDevice;
		try
		{
			reflectorDevice = microscope.getDevice(reflector);
		}
		catch(DeviceException e1)
		{
			throw new JobException("Could not find reflector with device ID \"" + reflector + "\".", e1);
		}
		if(!reflectorDevice.getDriverID().equals("Reflector"))
		{
			throw new JobException("Device set to serve as the reflector must be of type \"Reflector\". DriverID of device \"" + reflectorDevice.getDeviceID() + "\" is \"" + reflectorDevice.getDriverID()+"\".");
		}
		// make images
		ImageEvent<?>[] images = new ImageEvent<?>[phaseShiftsMask.length];
		try
		{
			if(maskFileName == null)
			{
				reflectorDevice.getProperty("mode").setValue("donut");
				reflectorDevice.getProperty("donut.centerX").setValue(Integer.toString(maskX));
				reflectorDevice.getProperty("donut.centerY").setValue(Integer.toString(maskY));
				reflectorDevice.getProperty("donut.innerRadius").setValue(Integer.toString(innerRadius));
				reflectorDevice.getProperty("donut.outerRadius").setValue(Integer.toString(outerRadius));
			}
			else
			{
				reflectorDevice.getProperty("mode").setValue("mask");
				reflectorDevice.getProperty("mask.file").setValue(maskFileName);
			}
			
			reflectorDevice.getProperty("phaseShiftBackground").setValue(Integer.toString(phaseShiftOutside));
			Job[] jobs = getJobs();
			if(jobs == null || jobs.length != 4)
				throw new JobException("SLIM job requires 4 image producing child jobs.");
			for(int i=0; i<phaseShiftsMask.length; i++)
			{
				reflectorDevice.getProperty("phaseShiftForeground").setValue(Integer.toString(phaseShiftsMask[i]));
				if(slimDelayMs>0)
					Thread.sleep(slimDelayMs);
				imageAdapters[i].clearImage();
				jobs[i].executeJob(executionInformation, microscope, measurementContext);
				images[i] = imageAdapters[i].clearImage();
			}
			reflectorDevice.getProperty("phaseShiftBackground").setValue("0");
			reflectorDevice.getProperty("phaseShiftForeground").setValue("0");
		}
		catch(Exception e)
		{
			throw new JobException("Could not take SLIM images.", e);
		}
		ImageEvent<?> slimImage;
		try {
			slimImage = SlimHelper.calculateSlimImage(images);
		} catch (Exception e) {
			throw new JobException("Could not calclate slim image.", e);
		}
		slimImage.setPositionInformation(getPositionInformation());
		slimImage.setExecutionInformation(executionInformation);
		sendImageToListeners(slimImage);
	}
	
	@Override
	public String getDefaultName()
	{
		return "SLIM job";
	}

	private void sendImageToListeners(ImageEvent<?> e)
	{
		synchronized(imageListeners)
		{
			for(int i = 0; i < imageListeners.size(); i++)
			{
				ImageListener listener = imageListeners.get(i);
				try
				{
					listener.imageMade(e);
				}
				catch(@SuppressWarnings("unused") RemoteException e1)
				{
					// Connection probably broken down...
					imageListeners.remove(i);
					i--;
				}
			}
		}	
	}

	@Override
	public void setImageDescription(String description) throws MeasurementRunningException
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
	public void setMaskX(int maskX) throws MeasurementRunningException
	{
		assertRunning();
		this.maskX = maskX;
	}

	@Override
	public int getMaskX()
	{
		return maskX;
	}

	@Override
	public void setMaskY(int maskY) throws MeasurementRunningException
	{
		assertRunning();
		this.maskY = maskY;
	}

	@Override
	public int getMaskY()
	{
		return maskY;
	}

	@Override
	public void setInnerRadius(int innerRadius) throws MeasurementRunningException
	{
		assertRunning();
		this.innerRadius = innerRadius;
	}

	@Override
	public int getInnerRadius()
	{
		return innerRadius;
	}

	@Override
	public void setOuterRadius(int outerRadius) throws MeasurementRunningException
	{
		assertRunning();
		this.outerRadius = outerRadius;
	}

	@Override
	public int getOuterRadius()
	{
		return outerRadius;
	}

	@Override
	public void setPhaseShiftOutside(int phaseShiftOutside) throws MeasurementRunningException
	{
		assertRunning();
		this.phaseShiftOutside = phaseShiftOutside;
	}

	@Override
	public int getPhaseShiftOutside()
	{
		return phaseShiftOutside;
	}

	@Override
	public int getPhaseShiftMask(int maskID)
	{
		return phaseShiftsMask[maskID];
	}

	@Override
	public void setPhaseShiftMask(int maskID, int phaseShift) throws MeasurementRunningException
	{
		assertRunning();
		phaseShiftsMask[maskID] = phaseShift;
	}

	@Override
	public String getReflectorDevice()
	{
		return reflector;
	}

	@Override
	public void setReflectorDevice(String reflectorDevice) throws MeasurementRunningException
	{
		assertRunning();
		this.reflector = reflectorDevice;
	}

	@Override
	public int getSlimDelayMs()
	{
		return slimDelayMs;
	}

	@Override
	public void setSlimDelayMs(int delayMs) throws MeasurementRunningException
	{
		assertRunning();
		this.slimDelayMs = delayMs;
	}

	@Override
	public void setMaskFileName(String maskFileName) throws MeasurementRunningException
	{
		assertRunning();
		this.maskFileName = maskFileName;
	}

	@Override
	public String getMaskFileName()
	{
		return maskFileName;
	}
}
