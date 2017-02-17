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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.rmi.RemoteException;
import java.util.ArrayList;

import org.youscope.addon.microscopeaccess.CameraDeviceInternal;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.microscope.MicroscopeException;

import mmcorej.CMMCore;
import mmcorej.TaggedImage;

/**
 * Object taking care of distributing images arriving at the microscope.
 * @author Moritz Lang
 *
 */
class ImageGatherer extends Thread
{
	private static ImageGatherer gatherer = null;
	private volatile boolean shouldRun = true;
	private final CMMCore core;
	private final MicroscopeImpl microscope;
	private final ArrayList<ImageWaiter> imageWaiters = new ArrayList<ImageWaiter>(4);
	private final ArrayList<CameraDeviceInternal> imageWaitersToBeRemoved = new ArrayList<CameraDeviceInternal>();
	
	private static final int POP_TIMEOUT_MS = 20;
	
	private ImageGatherer(MicroscopeImpl microscope, CMMCore core)
	{
		this.core = core;
		this.microscope = microscope;
	}
	
	private class ImageWaiter
	{
		public final ImageListener listener;
		public final String cameraID;
		public final CameraDeviceInternal cameraObj;
		ImageWaiter(ImageListener listener, CameraDeviceInternal camera)
		{
			this.listener = listener;
			this.cameraID = camera.getDeviceID();
			this.cameraObj = camera;
		}
	}
	
	/**
	 * Starts the image gatherer, if yet not started.
	 * @param microscope the microscope object.
	 * @param core microManager core. 
	 */
	public static synchronized void initialize(MicroscopeImpl microscope, CMMCore core)
	{
		if(gatherer == null)
		{
			gatherer = new ImageGatherer(microscope, core);
			gatherer.start();
		}
	}
	public static synchronized void uninitialize()
	{
		if(gatherer != null)
		{
			gatherer.shouldRun = false;
			gatherer = null;
		}
	}
	public static synchronized void startReceiveImages(CameraDeviceInternal camera, ImageListener listener)
	{
		// replace if somebody is already waiting for images form this camera.
		if(gatherer != null)
		{
			String cameraID = camera.getDeviceID();
			synchronized(gatherer.imageWaitersToBeRemoved)
			{
				for(int i=0; i<gatherer.imageWaitersToBeRemoved.size(); i++)
				{
					if(gatherer.imageWaitersToBeRemoved.get(i).getDeviceID().equals(cameraID))
					{
						gatherer.imageWaitersToBeRemoved.remove(i);
						i--;
					}
				}
			}
			gatherer.addImageWaiter(gatherer.new ImageWaiter(listener, camera));
		}
	}
	public static synchronized void stopReceiveImages(CameraDeviceInternal camera)
	{
		// do not remove directly, but only when circular buffer became empty!
		if(gatherer != null)
		{
			synchronized(gatherer.imageWaitersToBeRemoved)
			{
				gatherer.imageWaitersToBeRemoved.add(camera);
			}
		}
	}
	
	private void addImageWaiter(ImageWaiter waiter)
	{
		synchronized(imageWaiters)
		{
			// remove any previous listener
			removeImageWaiter(waiter.cameraObj);
			imageWaiters.add(waiter);
		}
	}
	
	private void removeImageWaiter(CameraDeviceInternal camera)
	{
		String cameraID = camera.getDeviceID();
		synchronized(imageWaiters)
		{
			for(int i=0; i<imageWaiters.size(); i++)
			{
				if(imageWaiters.get(i).cameraID.equals(cameraID))
				{
					imageWaiters.remove(i);
					i--;
				}
			}
		}
	}
	
	
	@Override
	public void run()
	{
		try
		{
			while(shouldRun)
			{
				if(!shouldRun)
					return;
				int numImagesPopped;
				try
				{
					numImagesPopped = popImages();
				}
				catch(MicroscopeException e)
				{
					microscope.errorOccured("Error occured while getting images. Restart microscope.", e);
					throw new InterruptedException("Image querying interrupted due to error.");
				}
				if(!shouldRun)
					return;
				Thread.sleep(POP_TIMEOUT_MS);
				if(numImagesPopped <= 0)
				{
					// if image buffer was empty, remove the image waiters which do not want to be notified anymore.
					synchronized(imageWaitersToBeRemoved)
					{
						for(CameraDeviceInternal camera : imageWaitersToBeRemoved)
						{
							removeImageWaiter(camera);
						}
						imageWaitersToBeRemoved.clear();
					}
				}
			}
		}
		catch(InterruptedException e)
		{
			// stop gatherer
			uninitialize();
			microscope.errorOccured("Stoped getting images from cameras.", e);
			return;
		}
	}
	
	private int popImages() throws MicroscopeException, InterruptedException
	{
		int bytesPerPixel = -1;
		int imageWidth =  -1;
		int imageHeight =  -1;
		int bitDepth =  -1;
		int bands =  -1;
		
		int imgNum = 0;
		while(true)
		{
			if(Thread.interrupted())
				throw new InterruptedException();
			
			TaggedImage taggedImage = null;
			try
			{
				taggedImage = core.popNextTaggedImage();
				if(taggedImage == null)
					break;
			}
			catch(@SuppressWarnings("unused") Exception e)
			{
				// no image available...
				break;
			}
			imgNum++;
			
			// we can assume that all images in the image buffer are essentially the same size and data type.
			// Thus, we query the size only once per iteration...
			if(bytesPerPixel < 0)
			{
				// get metadata
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
			}
			
			// find camera
			String imgCamera = cameraNameFromMetadata(taggedImage);
			ImageWaiter waiter = null;
			if(imgCamera != null)
			{
				synchronized(imageWaiters)
				{
					for(ImageWaiter aWaiter : imageWaiters)
					{
						if(aWaiter.cameraID.equals(imgCamera))
						{
							waiter = aWaiter;
							break;
						}
					}
				}
				if(waiter == null)
				{
					// nobody wants the specific image...
					microscope.stateChanged("Obtained image from camera " + imgCamera + ", albeit nobody is waiting for it. Skipping.");
					continue;
				}
			}
			else
			{
				synchronized(imageWaiters)
				{
					int numWaiters = imageWaiters.size();
					switch(numWaiters)
					{
						case 0:
							microscope.stateChanged("Obtained image from camera, albeit nobody is waiting for it. Skipping.");
							continue;
						case 1:
							waiter = imageWaiters.get(0);
							break;
						default:
							microscope.stateChanged("Image was produced which did not indicate from which camera it is from. Since more than one camera is possible, skipping.");
							continue;
							
					}
				}
			}
			
			
			// Create event object
			Object imageDataRaw = convertToRightType(taggedImage.pix, bytesPerPixel);
			ImageEvent<?> event;
			try
			{
				event = ImageEvent.createImage(imageDataRaw, imageWidth, imageHeight, bitDepth);
			}
			catch(Exception e)
			{
				microscope.errorOccured("Error creating image from data received from camera.", e);
				continue;
			}
			event.setCamera(imgCamera);
			event.setBands(bands);
			event.setTransposeX(waiter.cameraObj.isTransposeX());
			event.setTransposeY(waiter.cameraObj.isTransposeY());
			event.setSwitchXY(waiter.cameraObj.isSwitchXY());
			
			// send image to listener
			try
			{
				waiter.listener.imageMade(event);
			}
			catch(@SuppressWarnings("unused") RemoteException e)
			{
				stopReceiveImages(waiter.cameraObj);
			}
		}
		
		return imgNum;
	}
	
	private static String cameraNameFromMetadata(TaggedImage taggedImage)
	{
		try
		{
			return taggedImage.tags.getString("Camera");
		}
		catch(@SuppressWarnings("unused") Exception e)
		{
			return null;
		}
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
}
