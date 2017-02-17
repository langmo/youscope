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
package org.youscope.plugin.microscopeaccess;

import java.awt.Dimension;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Formatter;

import org.youscope.addon.microscopeaccess.CameraDeviceInternal;
import org.youscope.addon.microscopeaccess.ChannelInternal;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.SettingException;

import mmcorej.CMMCore;
import mmcorej.Metadata;
import mmcorej.MetadataArrayTag;
import mmcorej.MetadataSingleTag;
import mmcorej.StrVector;

/**
 * @author Moritz Lang
 */
class CameraDeviceImpl extends DeviceImpl implements CameraDeviceInternal
{
	/**
	 * There are some problems with some cameras, when a continuous sequence acquisition is stooped and another one is immediately afterwards
	 * started. We then simply wait a given time and try to revover by simply repeating the respective command n times.
	 */
	private static final int SEQUENCE_AQUISITION_NUM_RECOVERIES = 3;
	private static final int SEQUENCE_AQUISITION_SLEEP_RECOVERIES = 1000; // ms
	private static final int PARALLEL_IMAGING_MAX_WAIT = 10000; // ms
	
	/**
	 * Timeout for pulling microManager for new images.
	 */
	private final static int	MICROSCOPE_PULLING_TIMEOUT	= 10;

	/**
	 * Name of the property corresponding to the exposure time of a camera.
	 */
	private final static String	EXPOSURE_PROPERTY_NAME		= "Exposure";
	private final static String PROPERTY_SEQUENCE_RUNNING = "Sequence Running";
	
	private boolean transposeX = false;
	private boolean transposeY = false;
	private boolean transposeXY = false;
		
	private volatile ChannelInternal continousAcquisitionChannel = null;

	CameraDeviceImpl(MicroscopeImpl microscope, String deviceName, String libraryID, String driverID)
	{
		super(microscope, deviceName, libraryID, driverID, DeviceType.CameraDevice, new String[]{"TransposeMirrorX", "TransposeMirrorY", "TransposeXY"});
	}

	@Override
	protected void initializeDevice(int accessID) throws MicroscopeException
	{
		super.initializeDevice(accessID);
		
		// Add some additional properties...
		properties.put(PROPERTY_SEQUENCE_RUNNING, new SelectablePropertyImpl(microscope, getDeviceID(), PROPERTY_SEQUENCE_RUNNING, new String[]{"Running", "Stopped"}, false, this)
		{
			@Override
			public String getValue() throws MicroscopeException, InterruptedException
			{
				if(isContinuousSequenceAcquisitionRunning())
					return "Running";
				return "Stopped";
			}
			
			@Override
			protected void setStringValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
			{
				if(value.equals("Running"))
				{
					try
					{
						startContinuousSequenceAcquisition(null, getExposure(), new ImageListener()
						{
							@Override
							public void imageMade(ImageEvent<?> e) throws RemoteException
							{
								// do nothing with the images.	
							}
						}, accessID);
					}
					catch(SettingException e)
					{
						throw new MicroscopeException("Could not start continuous image acquisition.", e);
					}
					catch(DeviceException e)
					{
						throw new MicroscopeException("Could not start continuous image acquisition.", e);
					}
				}
				else
				{
					try
					{
						stopContinuousSequenceAcquisition(accessID);
					}
					catch(SettingException e)
					{
						throw new MicroscopeException("Could not stop continuous image acquisition.", e);
					}
					catch(DeviceException e)
					{
						throw new MicroscopeException("Could not stop continuous image acquisition.", e);
					}
				}
			}
		});
	}
	
	private long takeImage(ChannelInternal channel, double exposure, int accessID) throws MicroscopeException, SettingException, MicroscopeLockedException, InterruptedException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		
		if(exposure <= 0)
			throw new SettingException("Exposure time must be greater than zero. Provided exposure time " + Double.toString(exposure) + ".");
		
		long imageCreationTime;
		try
		{
			CMMCore core = microscope.startWrite(accessID);

			// Get data from device
			try
			{
				// Set to correct camera.
				//core.setCameraDevice(getDeviceID());

				// Set exposure and channel
				setExposure(exposure, accessID);
				
				if(Thread.interrupted())
					throw new InterruptedException();
				
				// open shutter
				if(channel != null)
					channel.openShutter(accessID);
					
				// Make image
				imageCreationTime = new Date().getTime();
				core.snapImage();
				// close shutter
				if(channel != null)
					channel.closeShutter(accessID);
				
				if(Thread.interrupted())
					throw new InterruptedException();
				
				// notify clients that image was taken.
				String message = "Made image";
				if(channel != null)
					message += " in channel " + channel.getChannelGroupID() + "." + channel.getChannelID();
				if(exposure > 0)
				{
					Formatter formatter = new Formatter();
					message += " with exposure of " + formatter.format("%2.2f ms", exposure);
					formatter.close();
				}
				message += ".";
				microscope.stateChanged(message);
				
				
			}
			catch(InterruptedException e)
			{
				throw e;
			}
			catch(Exception e)
			{
				throw new MicroscopeException("Couldn't take image from microscope.", e);
			}
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}
		return imageCreationTime;
	}

	@Override
	public void setExposure(double exposure, int accessID) throws MicroscopeLockedException, MicroscopeException, InterruptedException
	{
		setExposure(getDeviceID(), exposure, accessID);
	}

	private void setExposure(String cameraDevice, double exposure, int accessID) throws MicroscopeLockedException, MicroscopeException, InterruptedException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		if(exposure <= 0.0)
			return;
		try
		{
			CMMCore core = microscope.startWrite(accessID);
			try
			{
				core.setProperty(cameraDevice, EXPOSURE_PROPERTY_NAME, Double.toString(exposure));
			}
			catch(Exception e)
			{
				throw new MicroscopeException("Could not set exposure time to " + Double.toString(exposure) + "ms for camera \"" + cameraDevice + "\".", e);
			}
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}
		Formatter formatter = new Formatter();
		String exposureStr =formatter.format("%2.2f ms.", exposure).toString();
		formatter.close();
		microscope.stateChanged("Exposure of camera " + cameraDevice + " set to " + exposureStr);
	}
	public double getExposure() throws MicroscopeException, InterruptedException
	{
		String value;
		try
		{
			value = getProperty(EXPOSURE_PROPERTY_NAME).getValue();
		}
		catch(DeviceException e1)
		{
			throw new MicroscopeException("Could not get exposure time", e1);
		}
		try
		{
			return Double.parseDouble(value);
		}
		catch(NumberFormatException e)
		{
			throw new MicroscopeException("Exposure time value is not a double value (" + value + ").", e);
		}	
	}

	/**
	 * This function is a workaround:
	 * In microManager 1.3, the authors did a writing mistake and named this core function
	 * "intializeCircularBuffer" instead of "initializeCircularBuffer". In the development they found this writing mistake and renamed
	 * the function, however, therewith broke the interface contract. Since we don't really know how this function is now called in the
	 * microManager version we are using, we have to find it out...
	 * @param core The microManager core.
	 */
	static void initializeCircularBuffer(CMMCore core) throws MicroscopeException
	{
		// First try microManager 1.4 method name.
		Method initializeCircularBuffer = null;
		try
		{
			initializeCircularBuffer = CMMCore.class.getMethod("initializeCircularBuffer", new Class<?>[] {});
		}
		catch(@SuppressWarnings("unused") Exception e)
		{
			// Expected error, probably it's a version 1.3 core.
		}
		
		// Try microManager 1.3 method name.
		if(initializeCircularBuffer == null)
		{
			try
			{
				initializeCircularBuffer = CMMCore.class.getMethod("intializeCircularBuffer", new Class<?>[] {});
			}
			catch(@SuppressWarnings("unused") Exception e)
			{
				// Will throw error later.
			}
		}
		
		// If its still null, throw an error
		if(initializeCircularBuffer == null)
		{
			throw new MicroscopeException("Could not initialize circular buffer, since function does not exist.");
		}
		
		// Now, call the function
		try
		{
			initializeCircularBuffer.setAccessible(true);
			initializeCircularBuffer.invoke(core, new Object[] {});
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not initialize circular buffer.", e);
		}
	}
	
	//PROBLEM: SnapImage waits for image, parallel access not possible.
	/*@Override
	@Deprecated
	public ImageEvent[] makeParallelImages(ChannelInternal channel, String[] cameraIDs, double[] exposures, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException, DeviceException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		
		// Check settings
		for(double exposure : exposures)
		{
			if(exposure <= 0)
				throw new SettingException("Exposure time must be greater than zero. Provided exposure time of " + Double.toString(exposure) + "ms.");
		}
		
		// get all camera objects.
		CameraDeviceInternal[] cameras = new CameraDeviceInternal[cameraIDs.length];
		for(int i = 0; i < cameraIDs.length; i++)
		{
			if(cameraIDs[i] == null)
			{
				try
				{
					cameras[i] = microscope.getCameraDevice();
					cameraIDs[i] = cameras[i].getDeviceID();
				}
				catch(DeviceException e)
				{
					throw new MicroscopeException("Could not obtain default camera handle.", e);
				}
			}
			else
			{
				try
				{
					cameras[i] = microscope.getCameraDevice(cameraIDs[i]);
				}
				catch(DeviceException e)
				{
					throw new MicroscopeException("Could not obtain handle for camera " + cameraIDs[i] + ".", e);
				}
			}
		}
		
		
		
		
		int bytesPerPixel;
		int imageWidth;
		int imageHeight;
		int bitDepth;
		int bands;
		Object imageDataRaw = null;
		Date[] imageCreationTimes = new Date[cameraIDs.length];
		try
		{
			CMMCore core = microscope.startWrite(accessID);
			
			// Prepare microscope
			if(channel != null)
				channel.activateChannel(accessID);
			microscope.getMicroscopeConfiguration().waitForImageSynchro();
			
			// open shutter
			if(channel != null)
				channel.openShutter(accessID);
			
			// Ask each camera for image
			for(int i = 0; i < cameraIDs.length; i++)
			{
				try
				{
					// Set to correct camera.
					core.setCameraDevice(cameraIDs[i]);

					// Set exposure and channel
					setExposure(cameraIDs[i], exposures[i], accessID);
					
						
					// Make image
					if(Thread.interrupted())
						throw new InterruptedException();
					imageCreationTimes[i] = new Date();
					core.snapImage();
					core.sn
					
					// notify clients that image was taken.
					String message = "Made image";
					if(channel != null)
						message += " in channel " + channel.getChannelGroupID() + "." + channel.getChannelID();
					if(exposure > 0)
						message += " with exposure of " + (new Formatter()).format("%2.2f ms", exposure);
					message += ".";
					microscope.stateChanged(message);
					
					// close shutter
					if(channel != null)
						channel.closeShutter(accessID);
					
					// deactivate channel
					if(channel != null)
						channel.deactivateChannel(accessID);
				}
				catch(InterruptedException e)
				{
					throw e;
				}
				catch(Exception e)
				{
					throw new MicroscopeException("Couldn't take image from microscope.", e);
				}
				
				
				
				
				
				
				
			}
			if(Thread.interrupted())
				throw new InterruptedException();
			bytesPerPixel = (int)core.getBytesPerPixel();
			imageWidth = (int)core.getImageWidth();
			imageHeight = (int)core.getImageHeight();
			bitDepth = (int)core.getImageBitDepth();
			bands = (int)core.getNumberOfComponents();
			// Workaround: Some cameras wrongly claim to have 3 bytes per pixel, although 4 is more reasonable...
			if(bytesPerPixel == 3)
				bytesPerPixel = 4;
			// Workaround: Band number seems not to be always correct. Thus correct manually for color images.
			if(bytesPerPixel == 4)
			{
				bands = 4;
			}
			
			try
			{
				imageDataRaw = core.getImage();
			}
			catch(Exception e)
			{
				microscope.errorOccured("Couldn't get image from microscope although image should be available. Trying to recover...", e);
				for(int i = 0; i < 10; i++)
				{
					Thread.sleep(100);
					try
					{
						imageDataRaw = core.getImage();
						if(imageDataRaw == null)
							throw new Exception("Circular buffer empty.");
					}
					catch(Exception e2)
					{
						microscope.stateChanged("Recovery " + Integer.toString(i + 1) + " failed...");
						continue;
					}
					microscope.stateChanged("Recovery " + Integer.toString(i + 1) + " succeeded.");
					break;
				}
				if(imageDataRaw == null)
				{
					microscope.stateChanged("Recovery failed.");
					throw new MicroscopeException("Couldn't get picture from microscope.", e);
				}
			}
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}
		
		// Create event object
		imageDataRaw = convertToRightType(imageDataRaw, bytesPerPixel);
		ImageEvent event = new ImageEvent(imageDataRaw, imageWidth, imageHeight, bytesPerPixel, bitDepth);
		event.setBands(bands);
		event.setCamera(getDeviceID());
		if(channel != null)
		{
			event.setChannel(channel.getChannelID());
			event.setConfigGroup(channel.getChannelGroupID());
		}
		event.setImageCreationTime(imageCreationTime);
		event.setTransposeX(isTransposeX());
		event.setTransposeY(isTransposeY());
		event.setSwitchXY(isSwitchXY());

		return event;
		
	}*/
	
	
	@Override
	@Deprecated
	public ImageEvent<?>[] makeParallelImages(ChannelInternal channel, String[] cameraIDs, double[] exposures, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException, DeviceException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		
		// Check settings
		for(double exposure : exposures)
		{
			if(exposure <= 0)
				throw new SettingException("Exposure time must be greater than zero. Provided exposure time of " + Double.toString(exposure) + "ms.");
		}
		
		// get all camera objects.
		CameraDeviceInternal[] cameras = new CameraDeviceInternal[cameraIDs.length];
		class MyImageListener implements ImageListener
		{
			volatile ImageEvent<?> image = null;
			@Override
			public void imageMade(ImageEvent<?> e)
			{
				if(image != null)
					microscope.stateChanged("Obtained more than one image for one camera. Taking last one...");
				image = e;
			}
		}
		MyImageListener[] cameraImageListeners = new MyImageListener[cameraIDs.length];
		for(int i = 0; i < cameraIDs.length; i++)
		{
			if(cameraIDs[i] == null)
			{
				try
				{
					cameras[i] = microscope.getCameraDevice();
					cameraIDs[i] = cameras[i].getDeviceID();
				}
				catch(DeviceException e)
				{
					throw new MicroscopeException("Could not obtain default camera handle.", e);
				}
			}
			else
			{
				try
				{
					cameras[i] = microscope.getCameraDevice(cameraIDs[i]);
				}
				catch(DeviceException e)
				{
					throw new MicroscopeException("Could not obtain handle for camera " + cameraIDs[i] + ".", e);
				}
			}
			cameraImageListeners[i] = new MyImageListener();
			ImageGatherer.startReceiveImages(cameras[i], cameraImageListeners[i]);
		}
				
		try
		{
			CMMCore core = microscope.startWrite(accessID);

			// Set exposure times for all cameras
			for(int i = 0; i < cameraIDs.length; i++)
			{
				if(exposures[i] <= 0.0)
					continue;
				cameras[i].setExposure(exposures[i], accessID);
			}
			
			// Initialize buffer of microManager where images are stored.
			initializeCircularBuffer(core);
			
			if(Thread.interrupted())
				throw new InterruptedException();
			
			// wait for all cameras to finish last sequence acquisition
			// Bug workaround: Sequence acquisition may not finish directly after last image of
			// sequence is made (-> after making one image), but a little bit delayed, depending on camera.
			if(Thread.interrupted())
				throw new InterruptedException();
			for(String camera : cameraIDs)
			{
				try
				{
					while(core.isSequenceRunning(camera))
					{
						Thread.sleep(MICROSCOPE_PULLING_TIMEOUT);
					}
				}
				catch(InterruptedException e)
				{
					throw e;
				}
				catch(Exception e)
				{
					microscope.errorOccured("Could not detect if sequence aquisition of camera " + camera + " is running. Assuming not running and continuing.", e);
				}
			}
			
			// prepare sequence acquisition
			for(int i = 0; i < cameraIDs.length; i++)
			{
				try
				{
					core.prepareSequenceAcquisition(cameraIDs[i]);
				}
				catch(Exception e)
				{
					throw new MicroscopeException("Could not prepare sequence acquision for camera "+ cameraIDs[i] + ".", e);
				}
			}

			if(Thread.interrupted())
				throw new InterruptedException();
			
			
			// Set channel
			if(channel != null)
				channel.activateChannel(accessID);
			
			if(Thread.interrupted())
				throw new InterruptedException();
			
			// Wait for all devices in imaging-synchronization list to stop "moving" before imaging.
			try
			{
				microscope.getMicroscopeConfiguration().waitForImageSynchro();
			}
			catch(Exception e)
			{
				throw new MicroscopeException("Could not synchronize devices with imaging.", e);
			}
			if(Thread.interrupted())
				throw new InterruptedException();
			
			// open shutter
			if(channel != null)
				channel.openShutter(accessID);
			
			// Asynchrony start the imaging process (=sequence acquisition).
			for(String camera : cameraIDs)
			{
				try
				{
					core.setCameraDevice(camera);
					//core.startSequenceAcquisition(1, 0, false);
					core.startSequenceAcquisition(camera, 1, 0, false);
				}
				catch(Exception e)
				{
					// Try to recover...
					boolean recoverySuccess = false;
					for(int i = 0; i < SEQUENCE_AQUISITION_NUM_RECOVERIES; i++)
					{
						microscope.stateChanged("Could not initialize image making process of camera " + camera + ". Trying to recover ("+Integer.toString(i+1)+" of " + Integer.toString(SEQUENCE_AQUISITION_NUM_RECOVERIES) + ")...");
						Thread.sleep(SEQUENCE_AQUISITION_SLEEP_RECOVERIES);
						try
						{
							core.startSequenceAcquisition(camera, 1, 0, false);
							//core.startSequenceAcquisition(1, 0, false);
							recoverySuccess = true;
							microscope.stateChanged("...recovered!");
							break;
						}
						catch(@SuppressWarnings("unused") Exception e_sub)
						{
							// Do nothing, it's just the recovery...
						}
					}
					
					// Throw original error if all recovery trials failed...
					if(!recoverySuccess)
						throw new MicroscopeException("Could not initialize image making process of camera " + camera + ".", e);
				}
			}
	
			// wait for all images to be done
			ImageEvent<?>[] images = new ImageEvent[cameraIDs.length];
			int waitTimeTotal =0;
			do
			{
				int obtainedImages = 0;
				for(int i=0; i<images.length; i++)
				{
					if(images[i] == null)
					{
						images[i] = cameraImageListeners[i].image;
						if(images[i] != null && channel != null)
						{
							images[i].setChannel(channel.getChannelID());
							images[i].setChannelGroup(channel.getChannelGroupID());
						}
						if(images[i]!=null)
						{
							microscope.stateChanged("Parallel image of camera " + cameraIDs[i] + " arrived.");
						}
					}
					
					if(images[i] != null)
						obtainedImages++;
				}
				if(obtainedImages >= images.length)
					break;
				else if(waitTimeTotal >= PARALLEL_IMAGING_MAX_WAIT)
					throw new MicroscopeException("Parallel images did not all arrive in " + Integer.toString(PARALLEL_IMAGING_MAX_WAIT/1000) + "s.");
				else
				{
					Thread.sleep(MICROSCOPE_PULLING_TIMEOUT);
					waitTimeTotal += MICROSCOPE_PULLING_TIMEOUT;
				}
			}while(true);
			
			
			// Close shutter
			if(channel != null)
				channel.closeShutter(accessID);
			
			// deactivate channel
			if(channel != null)
				channel.deactivateChannel(accessID);
			
			// stop sequence acquisition
			for(String camera : cameraIDs)
			{
				try
				{
					core.setCameraDevice(camera);
					core.stopSequenceAcquisition();
				}
				catch(Exception e)
				{
					
					// Try to recover...
					boolean recoverySuccess = false;
					for(int i = 0; i < SEQUENCE_AQUISITION_NUM_RECOVERIES; i++)
					{
						if(isContinuousSequenceAcquisitionRunning())
						{
							
							microscope.stateChanged("Could not stop image making process of camera " + camera + ". Trying to recover ("+Integer.toString(i+1)+" of " + Integer.toString(SEQUENCE_AQUISITION_NUM_RECOVERIES) + ")...");
							Thread.sleep(SEQUENCE_AQUISITION_SLEEP_RECOVERIES);
							try
							{
								core.setCameraDevice(camera);
								core.stopSequenceAcquisition();
								recoverySuccess = true;
								microscope.stateChanged("...recovered!");
								break;
							}
							catch(@SuppressWarnings("unused") Exception e_sub)
							{
								// Do nothing, it's just the recovery...
							}
						}
						else
						{
							// Maybe stopped automatically? Should not, but who knows how they implemented the drivers...
							recoverySuccess = true;
						}
					}
					
					// Throw original error if all recovery trials failed...
					if(!recoverySuccess)
						throw new MicroscopeException("Could not stop image making process of camera " + camera + ".", e);
				}
			}
			
			return images;
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}
	}
	@SuppressWarnings("unused")
	private void makeParallelImagesVeryOld(ChannelInternal channel, String[] cameras, double[] exposures, ImageListener listener, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException, DeviceException
	{
		// TODO: Review: is it possible to skip this function?
		// How to implement it in an object oriented way, i.e. ask every single camera to do an image,
		// instead of a function getting the names of all cameras.
		if(Thread.interrupted())
			throw new InterruptedException();
		
		// Check settings
		for(double exposure : exposures)
		{
			if(exposure <= 0)
				throw new SettingException("Exposure time must be greater than zero. Provided exposure time of " + Double.toString(exposure) + "ms.");
		}
		
		try
		{
			CMMCore core = microscope.startWrite(accessID);

			// Set channel
			if(channel != null)
				channel.activateChannel(accessID);
				
			// Initialize buffer of microManager where images are stored.
			initializeCircularBuffer(core);

			// Set exposure times for all cameras
			for(int i = 0; i < cameras.length; i++)
			{
				if(cameras[i] == null)
				{
					try
					{
						cameras[i] = microscope.getCameraDevice().getDeviceID();
					}
					catch(DeviceException e)
					{
						throw new MicroscopeException("Could not detect default camera.", e);
					}
				}
				if(exposures[i] <= 0.0)
					continue;
				setExposure(cameras[i], exposures[i], accessID);
			}

			// Wait for all devices in imaging-synchronization list to stop "moving" before imaging.
			if(Thread.interrupted())
				throw new InterruptedException();
			
			try
			{
				microscope.getMicroscopeConfiguration().waitForImageSynchro();
			}
			catch(Exception e)
			{
				throw new MicroscopeException("Could not synchronize devices with imaging.", e);
			}

			// wait for all cameras to finish last sequence acquisition
			// Bug workaround: Sequence acquisition may not finish directly after last image of
			// sequence is made (-> after making one image), but a little bit delayed, depending on camera.
			if(Thread.interrupted())
				throw new InterruptedException();
			for(String camera : cameras)
			{
				try
				{
					while(core.isSequenceRunning(camera))
					{
						Thread.sleep(MICROSCOPE_PULLING_TIMEOUT);
					}
				}
				catch(InterruptedException e)
				{
					throw e;
				}
				catch(Exception e)
				{
					microscope.errorOccured("Could not detect if sequence aquisition of camera " + camera + " is running. Assuming not running and continuing.", e);
				}
			}

			if(channel != null)
				channel.openShutter(accessID);
			
			// Asynchrony start the imaging process (=sequence acquisition).
			long imageCreationTime = new Date().getTime();
			for(String camera : cameras)
			{
				try
				{
					core.setCameraDevice(camera);
					core.startSequenceAcquisition(camera, 1, 0, false);
				}
				catch(Exception e)
				{
					// Try to recover...
					boolean recoverySuccess = false;
					for(int i = 0; i < SEQUENCE_AQUISITION_NUM_RECOVERIES; i++)
					{
						microscope.stateChanged("Could not initialize image making process of camera " + camera + ". Trying to recover ("+Integer.toString(i+1)+" of " + Integer.toString(SEQUENCE_AQUISITION_NUM_RECOVERIES) + ")...");
						Thread.sleep(SEQUENCE_AQUISITION_SLEEP_RECOVERIES);
						try
						{
							core.startSequenceAcquisition(camera, 1, 1, false);
							recoverySuccess = true;
							microscope.stateChanged("...recovered!");
							break;
						}
						catch(Exception e_sub)
						{
							// Do nothing, it's just the recovery...
						}
					}
					
					// Throw original error if all recovery trials failed...
					if(!recoverySuccess)
						throw new MicroscopeException("Could not initialize image making process of camera " + camera + ".", e);
				}
			}

			// Use time to read out image properties
			int bytesPerPixel = (int)core.getBytesPerPixel();
			int imageWidth = (int)core.getImageWidth();
			int imageHeight = (int)core.getImageHeight();
			int bitDepth = (int)core.getImageBitDepth();
			int bands = (int)core.getNumberOfComponents();
			// Workaround: Some cameras wrongly claim to have 3 bytes per pixel, although 4 is more reasonable...
			if(bytesPerPixel == 3)
				bytesPerPixel = 4;
			// Workaround: Band number seems not to be always correct. Thus correct manually for color images.
			if(bytesPerPixel == 4)
			{
				bands = 4;
			}
			
			// Pull microscope for the images. Repeat pulling until all images arrived.
			int obtainedImages = 0;
			while(obtainedImages < cameras.length)
			{
				// If no image is there, pull again after short period
				if(core.getRemainingImageCount() <= 0)
				{
					Thread.sleep(MICROSCOPE_PULLING_TIMEOUT);
					continue;
				}

				// Get image

				Metadata metadata = new Metadata();
				Object imageDataRaw = null;
				String camera = null;
				try
				{
					imageDataRaw = core.popNextImageMD(0, 0, metadata); // core.popNextImage();
					if(imageDataRaw == null)
						throw new Exception("Circular buffer empty.");

					camera = cameraNameFromMetadata(metadata);
					if(camera == null)
						camera = getDeviceID();
				}
				catch(Exception e)
				{
					throw new MicroscopeException("Couldn't get image from microscope although image should be available. Trying to recover...", e);
				}
				obtainedImages++;

				// Create event object
				imageDataRaw = convertToRightType(imageDataRaw, bytesPerPixel);
				ImageEvent<?> event;
				try
				{
					event = ImageEvent.createImage(imageDataRaw, imageWidth, imageHeight, bitDepth);
				}
				catch(Exception e)
				{
					throw new MicroscopeException("Error creating image from data received from camera.", e);
				}
				event.setBands(bands);
				event.setCamera(camera);
				if(channel != null)
				{
					event.setChannel(channel.getChannelID());
					event.setChannelGroup(channel.getChannelGroupID());
				}
				event.setCreationTime(imageCreationTime);
				
				// get camera which imaged
				CameraDeviceInternal activeCamera;
				try
				{
					activeCamera= microscope.getCameraDevice(camera);
				}
				catch(DeviceException e1)
				{
					activeCamera = this;
				}
				event.setTransposeX(activeCamera.isTransposeX());
				event.setTransposeY(activeCamera.isTransposeY());
				event.setSwitchXY(activeCamera.isSwitchXY());

				// Send data to listeners
				if(listener != null)
				{
					try
					{
						listener.imageMade(event);
					}
					catch(RemoteException e)
					{
						throw new MicroscopeException("Couldn't send image to client.", e);
					}
				}
			}
			
			// Close shutter
			if(channel != null)
				channel.closeShutter(accessID);
			
			// stop sequence aquisition
			for(String camera : cameras)
			{
				try
				{
					core.setCameraDevice(camera);
					core.stopSequenceAcquisition();
				}
				catch(Exception e)
				{
					throw new MicroscopeException("Could not stop camera " + camera + ".", e);
				}
			}
			
			// deactivate channel
			if(channel != null)
				channel.deactivateChannel(accessID);
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}
	}

	@Override
	public ImageEvent<?> makeImage(ChannelInternal channel, double exposure, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException
	{
		return makeImage(channel, exposure, accessID, true);
	}
	private ImageEvent<?> makeImage(ChannelInternal channel, double exposure, int accessID, boolean retry) throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		// Get data from device
		int bytesPerPixel;
		int imageWidth;
		int imageHeight;
		int bitDepth;
		int bands;
		Object imageDataRaw = null;
		long imageCreationTime;
		Exception error = null;
		try
		{
			CMMCore core = microscope.startWrite(accessID);
			try
			{
				core.setCameraDevice(getDeviceID());
			}
			catch(Exception e1)
			{
				throw new MicroscopeException("Could not activate camera.", e1);
			}
			
			if(channel != null)
				channel.activateChannel(accessID);
			
			try
			{
				microscope.getMicroscopeConfiguration().waitForImageSynchro();
			}
			catch(DeviceException e1)
			{
				microscope.errorOccured("Synchronization with devices from the image synchronization list failed. Continuing without synchronization.", e1);
			}
						
			imageCreationTime = takeImage(channel, exposure, accessID);
			if(Thread.interrupted())
				throw new InterruptedException();
			bytesPerPixel = (int)core.getBytesPerPixel();
			imageWidth = (int)core.getImageWidth();
			imageHeight = (int)core.getImageHeight();
			bitDepth = (int)core.getImageBitDepth();
			bands = (int)core.getNumberOfComponents();
			// Workaround: Some cameras wrongly claim to have 3 bytes per pixel, although 4 is more reasonable (they might not use the fourth byte, however, still store each pixel as 4 byte...)
			if(bytesPerPixel == 3)
				bytesPerPixel = 4;
			// Workaround: Band number seems not to be always correct. Thus correct manually for color images.
			if(bytesPerPixel == 4)
			{
				bands = 4;
			}
			
			try
			{
				imageDataRaw = core.getImage();
			}
			catch(Exception e)
			{
				error = e;
				microscope.stateChanged("Couldn't get image from microscope although image should be available ("+e.getMessage()+"). Trying to recover...");
				for(int i = 0; i < 10; i++)
				{
					Thread.sleep(100);
					try
					{
						imageDataRaw = core.getImage();
						if(imageDataRaw == null)
							throw new Exception("Circular buffer empty.");
					}
					catch(@SuppressWarnings("unused") Exception e2)
					{
						microscope.stateChanged("Recovery " + Integer.toString(i + 1) + " of 10 failed...");
						continue;
					}
					microscope.stateChanged("Recovery " + Integer.toString(i + 1) + " of 10 succeeded.");
					break;
				}
			}
			// deactivate channel
			if(channel != null)
				channel.deactivateChannel(accessID);
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}
		if(imageDataRaw == null)
		{
			// If we still don't have any image, something went seriously wrong and the image probably will never arrive.
			// To still try to rescue the measurement, we repeat the whole image taking progress from the beginning (but only once).
			
			String message = "Camera signalled that image";
			if(channel != null)
				message += " in channel " + channel.getChannelGroupID() + "." + channel.getChannelID();
			if(exposure > 0)
			{
				Formatter formatter = new Formatter();
				message += " with exposure of " + formatter.format("%2.2f ms", exposure);
				formatter.close();
			}
			message += " was made, but error occured when trying to obtain the image data from the camera and waiting additional time for the image data to show up did not help.";
			
			if(retry) 
			{
				microscope.errorOccured(message+". Trying to restart the image making process from the beginning one last time before finally giving up...", error);
				return makeImage(channel, exposure, accessID, false);
			}
			microscope.errorOccured(message+". Since this happened two times in succession, this indicates a serious error in the hardware or the driver.", error);
			throw new MicroscopeException(message+". Since this happened two times in succession, this indicates a serious error in the hardware or the driver.", error);
		}
		
		// Create event object
		imageDataRaw = convertToRightType(imageDataRaw, bytesPerPixel);
		ImageEvent<?> event;
		try
		{
			event = ImageEvent.createImage(imageDataRaw, imageWidth, imageHeight, bitDepth);
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Error creating image from data received from camera.", e);
		}
		event.setBands(bands);
		event.setCamera(getDeviceID());
		if(channel != null)
		{
			event.setChannel(channel.getChannelID());
			event.setChannelGroup(channel.getChannelGroupID());
		}
		event.setCreationTime(imageCreationTime);
		event.setTransposeX(isTransposeX());
		event.setTransposeY(isTransposeY());
		event.setSwitchXY(isSwitchXY());

		return event;
	}

	private static Object convertToRightType(Object imageDataRaw, int bytesPerPixel) throws MicroscopeException
	{
		if(imageDataRaw == null)
		{
			throw new MicroscopeException("Empty image (null pointer) obtained from microscope.");
		}
		else if(bytesPerPixel < 1 || bytesPerPixel > 4)
		{
			throw new MicroscopeException("Only images with 1-4 bytes per pixel supported. Camera claims to return images with " + Integer.toString(bytesPerPixel) + " bytes per pixel.");
		}
		else if(imageDataRaw instanceof byte[])
		{
			// Some cameras give back byte arrays, when they should give back other arrays. simply convert it...
			if(bytesPerPixel == 1)
			{
				return imageDataRaw;
			}
			else if(bytesPerPixel == 2)
			{
				int size = (((byte[])imageDataRaw).length / 2) + ((((byte[])imageDataRaw).length % 2 == 0) ? 0 : 1);
				short[] result = new short[size]; 


				ByteBuffer.wrap((byte[])imageDataRaw).asShortBuffer().get(result);
				
				return result;
			}
			else // 3 or 4
			{
				int size = (((byte[])imageDataRaw).length / 4) + ((((byte[])imageDataRaw).length % 4 == 0) ? 0 : 1);
				int[] result = new int[size];
				
				ByteBuffer.wrap((byte[])imageDataRaw).order(ByteOrder.LITTLE_ENDIAN) .asIntBuffer().get(result);
				
				return result;
			}
		}
		else if(imageDataRaw instanceof short[])
		{
			if(bytesPerPixel == 2)
			{
				return imageDataRaw;
			}
			// to convert a short array in a byte array somehow don't seem reasonable. If we get something like this, this rather indicates some bigger error...
			throw new MicroscopeException("Obtained 2 byte per pixel image. However, camera claims to return an image with " + Integer.toString(bytesPerPixel) + " bytes per pixel.");
		}
		else if(imageDataRaw instanceof int[])
		{
			if(bytesPerPixel == 3 || bytesPerPixel == 4)
			{
				return imageDataRaw;
			}
			// to convert a short array in a byte array somehow don't seem reasonable. If we get something like this, this rather indicates some bigger error...
			throw new MicroscopeException("Obtained 4 byte per pixel image. However, camera claims to return an image with " + Integer.toString(bytesPerPixel) + " bytes per pixel.");
		}
		else
		{
			// unknown type, currently not processed. Throw error.
			throw new MicroscopeException("Unknown image format: " + imageDataRaw.getClass().getName());
		}
	}

	@Override
	public void startContinuousSequenceAcquisition(ChannelInternal channel, double exposure, ImageListener listener, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException, DeviceException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		ImageGatherer.startReceiveImages(this, listener);
		
		if(exposure <= 0)
			throw new SettingException("Exposure time must be greater than zero. Provided exposure time " + Double.toString(exposure) + ".");
		
		
		try
		{
			CMMCore core = microscope.startWrite(accessID);

			// wait for camera to finish last sequence acquisition
			// Bug workaround: Sequence acquisition may not finish directly after last image of
			// sequence is made (-> after making one image), but a little bit delayed, depending on camera.
			try
			{
				for(int i= 0; i < 1000 / MICROSCOPE_PULLING_TIMEOUT && core.isSequenceRunning(getDeviceID()); i++)
				{
					Thread.sleep(MICROSCOPE_PULLING_TIMEOUT);
				}
			}
			catch(InterruptedException e)
			{
				throw e;
			}
			catch(Exception e)
			{
				microscope.errorOccured("Could not detect if sequence aquisition of camera " + getDeviceID() + " is running. Assuming not running and continuing.", e);
			}
			if(Thread.interrupted())
				throw new InterruptedException();
			
			// set camera
			try
			{
				core.setCameraDevice(getDeviceID());
			}
			catch(Exception e)
			{
				throw new MicroscopeException("Could not set camera.", e);
			}
			
			// Set exposure
			setExposure(getDeviceID(), exposure, accessID);
			
			
			// Set channel
			if(channel != null)
			{
				channel.activateChannel(accessID);
			}
			continousAcquisitionChannel = channel;
			
			//initializeCircularBuffer(core);
			
			// Wait for all devices in imaging-synchronization list to stop "moving" before imaging.
			if(Thread.interrupted())
				throw new InterruptedException();
			try
			{
				microscope.getMicroscopeConfiguration().waitForImageSynchro();
			}
			catch(DeviceException e)
			{
				throw new MicroscopeException("Could not synchronize devices with imaging.", e);
			}

			if(Thread.interrupted())
				throw new InterruptedException();

			// open shutter
			if(channel != null)
				channel.openShutter(accessID);
			
			// Asynchrony start the imaging process (=sequence acquisition).
			try
			{
				//core.startSequenceAcquisition(Integer.MAX_VALUE, 0, true);
				// TODO: Interval = 0 sometimes critical...
				core.setCameraDevice(getDeviceID());
				core.startContinuousSequenceAcquisition(100);
			}
			catch(Exception e)
			{
				// Try to recover...
				boolean recoverySuccess = false;
				for(int j = 0; j < SEQUENCE_AQUISITION_NUM_RECOVERIES; j++)
				{
					microscope.stateChanged("Could not initialize image making process of camera " + getDeviceID() + ". Trying to revover ("+Integer.toString(j+1)+" of " + Integer.toString(SEQUENCE_AQUISITION_NUM_RECOVERIES) + ")...");
					Thread.sleep(SEQUENCE_AQUISITION_SLEEP_RECOVERIES);
					try
					{
						core.setCameraDevice(getDeviceID());
						core.startContinuousSequenceAcquisition(100);
						recoverySuccess = true;
						microscope.stateChanged("...recovered!");
						break;
					}
					catch(@SuppressWarnings("unused") Exception e_sub)
					{
						// Do nothing, it's just the recovery...
					}
				}
				
				// Throw original error if all recovery trials failed...
				if(!recoverySuccess)
					throw new MicroscopeException("Could not initialize continuous imaging of camera " + getDeviceID() + ".", e);
			}
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}

		String message = "Camera " + getDeviceID() + " started continuous imaging ";
		if(channel != null)
			message += " in channel " + channel.getChannelGroupID() + "." + channel.getChannelID();
		if(exposure > 0)
		{
			Formatter formatter = new Formatter();
			message += " with exposure of " + formatter.format("%2.2f ms", exposure);
			formatter.close();
		}
		message += ".";
		microscope.stateChanged(message);
	}

	@Override
	public void stopContinuousSequenceAcquisition(int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException, SettingException, DeviceException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		try
		{
			CMMCore core = microscope.startWrite(accessID);
			
			try
			{
				core.setCameraDevice(getDeviceID());
				core.stopSequenceAcquisition();
				//core.stopSequenceAcquisition(getDeviceID());
			}
			catch(Exception e)
			{
				throw new MicroscopeException("Couldn't stop image acquisition of camera " + getDeviceID() + ".", e);
			}
			
			ImageGatherer.stopReceiveImages(this);
			
			// close shutter
			if(continousAcquisitionChannel != null)
				continousAcquisitionChannel.closeShutter(accessID);
			
			
			// deactivate channel.
			if(continousAcquisitionChannel != null)
				continousAcquisitionChannel.deactivateChannel(accessID);
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}

		String message = "Camera " + getDeviceID() + " stopped continuous imaging.";
		microscope.stateChanged(message);
	}

	/*
	private ImageEvent[] popImages(int numImages, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
	{
		long tempMMImageNumber = lastMMImageNumber;
		int numFound = 0;
		
		ArrayList<ImageEvent> images = new ArrayList<ImageEvent>(numImages > 0 ? numImages : 10);
				
		if(Thread.interrupted())
			throw new InterruptedException();
		try
		{
			CMMCore core = microscope.startWrite(accessID);
			int remainingImages = core.getRemainingImageCount();
			if(remainingImages == 0)
				return new ImageEvent[0];
			for(int imgNum = 0; imgNum < remainingImages; imgNum++)
			{
				if(Thread.interrupted())
					throw new InterruptedException();
				
				TaggedImage taggedImage = null;
				try
				{
					taggedImage = core.getNBeforeLastTaggedImage(imgNum);
					if(taggedImage == null)
						throw new Exception("Image " + Integer.toString(imgNum) + " is empty.");
				}
				catch(Exception e)
				{
					microscope.errorOccured("Couldn't get image from microscope although image should be available. Trying to recover...", e);
					for(int i = 0; i < 10; i++)
					{
						Thread.sleep(100);
						try
						{
							taggedImage = core.getNBeforeLastTaggedImage(imgNum);
							if(taggedImage == null)
								throw new Exception("Image " + Integer.toString(imgNum) + " is empty.");
						}
						catch(Exception e2)
						{
							microscope.stateChanged("Recovery " + Integer.toString(i + 1) + " failed...");
							continue;
						}
						microscope.stateChanged("Recovery " + Integer.toString(i + 1) + " succeeded...");
						break;
					}
					if(taggedImage == null)
					{
						microscope.stateChanged("Recovery failed.");
						throw new MicroscopeException("Couldn't get picture from microscope.", e);
					}
				}
				
				// check if image newer than last image
				lastImageBufferIndex = imageNumberFromMetadata(taggedImage);
				if(lastImageBufferIndex <= lastMMImageNumber)
					break;
				
				// check if correct camera
				String imgCamera = cameraNameFromMetadata(taggedImage);
				if(imgCamera == null)
					throw new MicroscopeException("Camera not set in image metadata.");
				if(!imgCamera.equals(getDeviceID()))
					continue;
				
				// found a correct image!
				tempMMImageNumber = tempMMImageNumber < lastImageBufferIndex ? lastImageBufferIndex : tempMMImageNumber;
				numFound++;
				
				// get metadata
				int bytesPerPixel = (int)core.getBytesPerPixel();
				int imageWidth = (int)core.getImageWidth();
				int imageHeight = (int)core.getImageHeight();
				int bitDepth = (int)core.getImageBitDepth();
				int bands = (int)core.getNumberOfComponents();
				// Workaround: Some cameras wrongly claim to have 3 bytes per pixel, although 4 is more reasonable...
				if(bytesPerPixel == 3)
					bytesPerPixel = 4;
				// Workaround: Band number seems not to be always correct. Thus correct manually for color images.
				if(bytesPerPixel == 4)
				{
					bands = 4;
				}
				

				// Create event object
				Object imageDataRaw = convertToRightType(taggedImage.pix, bytesPerPixel);
				ImageEvent event = new ImageEvent(imageDataRaw, imageWidth, imageHeight, bytesPerPixel, bitDepth);
				event.setCamera(imgCamera);
				event.setBands(bands);
				event.setTransposeX(isTransposeX());
				event.setTransposeY(isTransposeY());
				event.setSwitchXY(isSwitchXY());
				
				images.add(event);
				
				if(numImages > 0 && numFound >= numImages)
					break;
			}			
		}
		finally
		{
			microscope.unlockWrite();
		}
		
		lastMMImageNumber = tempMMImageNumber;
		
		// reverse array of found images
		ImageEvent[] returnVal = new ImageEvent[images.size()];
		for(int i = 0; i < returnVal.length; i++)
		{
			returnVal[i] = images.get(returnVal.length - 1 - i);
		}
		return returnVal;
	}
	
	
	public ImageEvent[] popImages(int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
	{
		return popImages(-1, accessID);
	}
	
	public ImageEvent popLastImage(int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
	{
		ImageEvent[] images = popImages(1, accessID);
		return images.length == 1 ? images[0] : null;
	}
	
	@Deprecated
	public boolean continousSequenceAcquisitionPopNextImage(ImageListener listener, boolean saveOlderImages, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		int bytesPerPixel;
		int imageWidth;
		int imageHeight;
		int bitDepth;
		int bands;
		String camera = null;
		Object imageDataRaw = null;
		Metadata metadata = new Metadata();

		try
		{
			CMMCore core = microscope.startWrite(accessID);
			int bufferNum = core.getRemainingImageCount();
			
			//microscope.stateChanged("Found " + Integer.toString(bufferNum) + " images in buffer.");
			
			for(int bufferI = 0; bufferI < bufferNum; bufferI++)
			{
				try
				{
					//imageDataRaw = core.getLastImageMD(metadata);
					imageDataRaw = core.popNextImageMD(0, 0, metadata);
					
					if(imageDataRaw == null)
						throw new Exception("Circular buffer empty.");
				}
				catch(Exception e)
				{
					microscope.errorOccured("Couldn't get image from microscope although image should be available. Trying to recover...", e);
					for(int i = 0; i < 10; i++)
					{
						Thread.sleep(100);
						try
						{
							//imageDataRaw = core.popNextImageMD(0, 0, metadata);
							imageDataRaw = core.popNextImageMD(metadata);
							if(imageDataRaw == null)
								throw new Exception("Circular buffer empty.");
						}
						catch(Exception e2)
						{
							microscope.stateChanged("Recovery " + Integer.toString(i + 1) + " failed...");
							continue;
						}
						microscope.stateChanged("Recovery " + Integer.toString(i + 1) + " succeeded...");
						break;
					}
					if(imageDataRaw == null)
					{
						microscope.stateChanged("Recovery failed.");
						throw new MicroscopeException("Couldn't get picture from microscope.", e);
					}
				}
				
				if(!saveOlderImages && bufferI + 1 < bufferNum)
					continue;

				
				camera = cameraNameFromMetadata(metadata);
				if(camera == null)
					camera = getDeviceID();
				
				bytesPerPixel = (int)core.getBytesPerPixel();
				imageWidth = (int)core.getImageWidth();
				imageHeight = (int)core.getImageHeight();
				bitDepth = (int)core.getImageBitDepth();
				bands = (int)core.getNumberOfComponents();
				// Workaround: Some cameras wrongly claim to have 3 bytes per pixel, although 4 is more reasonable...
				if(bytesPerPixel == 3)
					bytesPerPixel = 4;
				// Workaround: Band number seems not to be always correct. Thus correct manually for color images.
				if(bytesPerPixel == 4)
				{
					bands = 4;
				}
				
				// Create event object
				imageDataRaw = convertToRightType(imageDataRaw, bytesPerPixel);
				ImageEvent event = new ImageEvent(imageDataRaw, imageWidth, imageHeight, bytesPerPixel, bitDepth);
				event.setCamera(camera);
				event.setBands(bands);
				
				// get camera which imaged
				CameraDeviceInternal activeCamera;
				try
				{
					activeCamera= microscope.getCameraDevice(camera);
				}
				catch(DeviceException e1)
				{
					activeCamera = this;
				}
				event.setTransposeX(activeCamera.isTransposeX());
				event.setTransposeY(activeCamera.isTransposeY());
				event.setSwitchXY(activeCamera.isSwitchXY());

				// Send data to listeners
				if(listener != null)
				{
					try
					{
						listener.imageMade(event);
					}
					catch(RemoteException e)
					{
						throw new MicroscopeException("Couldn't send image to client.", e);
					}
				}
			}

		}
		finally
		{
			microscope.unlockWrite();
		}
		return true;
	}*/

	@Override
	public void setTransposeX(boolean transpose, int accessID) throws MicroscopeLockedException
	{
		try
		{
			microscope.lockWrite(accessID);
			transposeX = transpose;
		}
		finally
		{
			microscope.unlockWrite();
		}
		microscope.stateChanged("Set x-direction of camera " + getDeviceID() + " to " + (transpose? "transposed." : "not transposed."));
	}

	@Override
	public void setTransposeY(boolean transpose, int accessID) throws MicroscopeLockedException
	{
		try
		{
			microscope.lockWrite(accessID);
			transposeY = transpose;
		}
		finally
		{
			microscope.unlockWrite();
		}
		microscope.stateChanged("Set y-direction of camera " + getDeviceID() + " to " + (transpose? "transposed." : "not transposed."));
	}

	@Override
	public void setSwitchXY(boolean switchXY, int accessID) throws MicroscopeLockedException
	{
		try
		{
			microscope.lockWrite(accessID);
			transposeXY = switchXY;
		}
		finally
		{
			microscope.unlockWrite();
		}
		microscope.stateChanged("Set x and y-direction of camera " + getDeviceID() + " to " + (switchXY? "switched." : "not switched."));
	}

	@Override
	public boolean isTransposeX()
	{
		return transposeX;
	}

	@Override
	public boolean isTransposeY()
	{
		return transposeY;
	}

	@Override
	public boolean isSwitchXY()
	{
		return transposeXY;
	}

	public boolean isContinuousSequenceAcquisitionRunning() throws MicroscopeException
	{
		try
		{
			CMMCore core = microscope.startRead();
			return core.isSequenceRunning(getDeviceID());
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not detect camera image size.", e);
		}
		finally
		{
			microscope.unlockRead();
		}
	}
	
	@Override
	public Dimension getImageSize(int accessID) throws MicroscopeLockedException, MicroscopeException
	{
		int height;
		int width;
		try
		{
			CMMCore core = microscope.startRead();
			// Set to correct camera.
			core.setCameraDevice(getDeviceID());
			
			width = (int)core.getImageWidth();
			height = (int)core.getImageHeight();
		}
		catch(MicroscopeLockedException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Could not detect camera image size.", e);
		}
		finally
		{
			microscope.unlockRead();
		}
		return new Dimension(width, height);
	}
	/*
	private static String cameraNameFromMetadata(TaggedImage taggedImage)
	{
		try
		{
			return taggedImage.tags.getString("Camera");
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	private static long imageNumberFromMetadata(TaggedImage taggedImage) throws MicroscopeException
	{
		try
		{
			return taggedImage.tags.getLong("ImageNumber");
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Image does not have tag \"ImageNumber\".", e);
		}
	}
	*/
	private static String cameraNameFromMetadata(Metadata metadata)
	{
		String camera = null;
		try
		{
			camera = metadata.GetSingleTag("Camera").GetValue();
		}
		catch(@SuppressWarnings("unused") Exception e)
		{
			// Do nothing
			camera = null;
		}
		return camera;
	}
	/*
	@SuppressWarnings("unused")
	private static long imageNumberFromMetadata(Metadata metadata) throws MicroscopeException
	{
		String imageNumber;
		try
		{
			imageNumber = metadata.GetSingleTag("ImageNumber").GetValue();
		}
		catch(Exception e)
		{
			throw new MicroscopeException("Image does not have tag \"ImageNumber\".", e);
		}
		try
		{
			return Long.parseLong(imageNumber);
		}
		catch(NumberFormatException e)
		{
			throw new MicroscopeException("Tag \"ImageNumber\" of image is not a number (\"" + imageNumber + "\")", e);
		}
	}*/
	
	@SuppressWarnings("unused")
	private void dumbMetadata(Metadata metadata)
	{
		microscope.stateChanged("==================================");
		
		StrVector keys = metadata.GetKeys();
		if(!keys.isEmpty())
		{
			
			for(String key: keys)
			{
				try
				{
					MetadataSingleTag tag = metadata.GetSingleTag(key);
					if(tag != null)
					{
						microscope.stateChanged(tag.GetName() + " + " + tag.GetDevice() + " + "+tag.GetValue());
					}
					else
					{
						MetadataArrayTag arrayTag = metadata.GetArrayTag(key);
						if(arrayTag != null)
						{
							String message = arrayTag.GetName() + " + " + arrayTag.GetDevice() + " + [[";
							for(int i=0; i< arrayTag.GetSize(); i++)
							{
								if(i > 0)
									message += ", ";
								message += arrayTag.GetValue(i);
							}
							message+="]]";
							microscope.stateChanged(message);
						}
						else
						{
							microscope.stateChanged("INVALID TAG "+key);
						}
					}
				}
				catch(Exception e)
				{
					microscope.stateChanged("Tag " + key + " not there.");
				}
			}
		}
		else
		{
			microscope.stateChanged("Metadata empty.");
		}
		
		// Incompatible with new MM version
		/*keys = metadata.getFrameKeys();
		if(!keys.isEmpty())
		{
			for(String frameKey : keys)
			{
				String value = metadata.getFrameData().get(frameKey);
				microscope.stateChanged(frameKey + " = " + value);
			}
		}
		else
		{
			microscope.stateChanged("Frame data empty.");
		}*/
		microscope.stateChanged("==================================");
	}
}
