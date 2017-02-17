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
package org.youscope.plugin.multicameraandcolorstream;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import javax.swing.JPanel;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.PositionInformation;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.job.basicjobs.ContinuousImagingJob;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.task.Task;
import org.youscope.common.util.ImageConvertException;
import org.youscope.common.util.ImageTools;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
class MultiStreamPanel extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 2713829811044542455L;

	private static final int NUM_COLORS = 3;
	
	private String channel = null;
	private String configGroup = null;

    private double[] exposures = {20, 20, 20};
    
    private String[] cameras = {null, null, null};
    
    private Measurement measurement = null;
    private volatile ImageHandler imageHandler = null;
    
    private final YouScopeServer server;
	private final YouScopeClient client;    
	
	private volatile BufferedImage[] singleImages = new BufferedImage[NUM_COLORS];

	//private AffineTransform[] imageTransformations = new AffineTransform[NUM_COLORS];
	
	private static double[] deltaXs = {0,0,0};
	private static double[] deltaYs = {0,0,0};
	private static double[] deltaPhis = {0,0,0};
	
    private float[] lowerCutoffs = {0.0F, 0.0F, 0.0F};

    private float[] upperCutoffs = {1.0F, 1.0F, 1.0F};
    
    private int imagingPeriod = 100;
    
    private boolean[] increaseContrasts = {true, true, true};
    
    private volatile BufferedImage composedImage = null;
    
    
    
    
    
    private class ImageHandler extends Thread
    {
    	private volatile boolean shouldRun = true;
    	
    	private final ImageEvent<?>[] nextImages;
    	ImageHandler()
    	{
    		nextImages = new ImageEvent[cameras.length];
    	}
    	
    	private class ImageListenerImpl extends UnicastRemoteObject implements ImageListener
    	{
    		/**
			 * Serial Version UID.
			 */
			private static final long	serialVersionUID	= -4935351939442377119L;

			/**
    		 * Constructor
			 * @throws RemoteException
			 */
			protected ImageListenerImpl() throws RemoteException
			{
				super();
			}

			@Override
    		public void imageMade(ImageEvent<?> e) throws RemoteException
    		{
				String camera = e.getCamera();
	        	if(camera == null || camera.compareToIgnoreCase("unknown") == 0)
	        	{
	        		try
					{
						stopMeasurement();
					}
					catch(RemoteException | MeasurementException e1)
					{
						client.sendError("Measurement could not be stopped.", e1);
					}
					client.sendError("Stopped measurement since it was not detectable by which camera an image was taken. Check the camera drivers and ensure, that they set the \"Camera\" metadata tag correctly.");
					return;
	        	}
	        	for(int i=0; i < cameras.length; i++)
	        	{
	        		if(cameras[i] != null && camera.compareTo(cameras[i]) == 0)
	    			{
	        			synchronized(ImageHandler.this)
	        			{
	        				nextImages[i] = e;
	        				ImageHandler.this.notifyAll();
	        			}
	        			
	    				return;
	    			}
	        	}
	        	
	        	// When we are here, we received an image from a camera which shouldn't be imaging.
	        	// This is OK, since it might be an image from a camera which was just deactivated, i.e. which should have imaged
	        	// a second ago...
	        	client.sendMessage("Received an image from camera \"" + camera + "\", although it shouldn't be imaging. Check if all cameras which are not taking part in the parallel imaging stopped imaging, or if the device driver of the respective camera sets the \"Camera\" metadata tag correctly.");
    		}
    	}
    	private volatile ImageListenerImpl listener = null;
		
		
		synchronized void stopListening()
		{
			shouldRun = false;
			this.notifyAll();
		}
		
		ImageListener startListening() throws RemoteException
		{
			start();
			if(listener == null)
				listener = new ImageListenerImpl();
			return listener;
		}
    	
		@Override
		public void run()
		{
			while(shouldRun)
			{
				// update images if available
				for(int i=0; i<nextImages.length; i++)
				{
					ImageEvent<?> image = null;
					synchronized(this)
					{
						image = nextImages[i];
						nextImages[i] = null;
					}
					if(image != null)
					{
						processImage(i, image);
					}
				}
				try
				{
					if(composeImage())
						MultiStreamPanel.this.repaint();
				}
				catch(ImageConvertException e1)
				{
					client.sendError("Could not overlay images. Stoping.", e1);
					return;
				}
				
				// wait that at least one image arrives.
				outer: while(shouldRun)
				{
					for(ImageEvent<?> e : nextImages)
					{
						if(e != null)
							break outer;
					}
					synchronized(this)
					{
						try
						{
							this.wait();
						}
						catch(InterruptedException e)
						{
							client.sendError("Microscope image processor was interrupted.", e);
							return;
						}
					}
				}
				
			}
			
		}
		
    }
    
    MultiStreamPanel(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
		
	}
    
    /**
     * Stops the measurement. Does nothing if measurement is not running.
     * The call immediately returns, even if the stopping of the measurement takes longer.
     * @throws RemoteException 
     * @throws MeasurementException 
     */
    public synchronized void stopMeasurement() throws RemoteException, MeasurementException
	{
		if(measurement != null)
		{
			measurement.stopMeasurement(false);			
			measurement = null;
		}
		if(imageHandler != null)
		{
			imageHandler.stopListening();
			imageHandler = null;
		}
	}
    
    /**
     * Sets the channel in which it is imaged.
     * @param configGroup The config groups in which the channel is defined.
     * @param channel The channels to image.
     */
    public void setChannel(String configGroup, String channel)
    {
    	this.channel = channel;
        this.configGroup = configGroup;
    }
    /**
     * Sets the camera with which should be imaged.
     * @param channelNo The channel for which the exposure time should be set.
     * @param cameraID ID of the camera.
     */
    public void setCamera(int channelNo, String cameraID)
    {
    	this.cameras[channelNo] = cameraID;
    }
    
    /**
     * Sets if the contrast should be automatically increased.
     * @param channelNo The channel for which the exposure time should be set.
     * @param increaseContrast True if the contrast should be increased.
     */
    public void setIncreaseContrast(int channelNo, boolean increaseContrast)
    {
        this.increaseContrasts[channelNo] = increaseContrast;
    }

    /**
     * Sets the exposure time for the continuous imaging.
     * @param channelNo The channel for which the exposure time should be set.
     * @param exposure The exposure time.
     */
    public void setExposure(int channelNo, double exposure)
    {
    	if(channelNo < 0 || channelNo >= exposures.length)
    		throw new ArrayIndexOutOfBoundsException("ChannelNo must be larger or equal to 0 and smaller than" + exposures.length + ".");
        this.exposures[channelNo] = exposure;
    }

    /**
     * Sets the lower and the upper cutoff, if the contrast is increased.
     * @param channelNo The channel for which the exposure time should be set.
     * @param lowerCutoff The lower cutoff.
     * @param upperCutoff The upper cutoff.
     */
    public void setCutoff(int channelNo, float lowerCutoff, float upperCutoff)
    {
    	if(channelNo < 0 || channelNo >= exposures.length)
    		throw new ArrayIndexOutOfBoundsException("ChannelNo must be larger or equal to 0 and smaller than" + exposures.length + ".");
        this.lowerCutoffs[channelNo] = lowerCutoff;
        this.upperCutoffs[channelNo] = upperCutoff;
    }
    
    /**
     * Sets the period length between two successive images.
     * @param imagingPeriod The period time.
     */
    public void setImagingPeriod(int imagingPeriod)
    {
        this.imagingPeriod = imagingPeriod;
    }
    
    /**
     * Starts the continuous measurement.
     * @throws RemoteException 
     */
    public synchronized void startMeasurement() throws RemoteException
    {
    	try
		{
    		stopMeasurement();
		}
		catch(Exception e)
		{
			client.sendError("Could not initialize multi-camera and -color measurement.", e);
			return;
		}
    	Vector<Integer> used = new Vector<Integer>();
		for(int i=0; i<cameras.length; i++)
		{
			if(cameras[i] != null)
			{
				used.addElement(i);
			}
		}
		if(used.size() <= 0)
			return;
		
		String[] usedCameras = new String[used.size()];
		double[] usedExposures = new double[used.size()];
		for(int i=0; i< used.size(); i++)
		{
			int u = used.elementAt(i);
			usedCameras[i] = cameras[u];
			usedExposures[i] = exposures[u];
		}
		measurement = server.getMeasurementProvider().createMeasurement();
		imageHandler = new ImageHandler();
		try
		{
			measurement.setName("Multi-Camera and -Color Stream");
			measurement.setLockMicroscopeWhileRunning(false);

			// Create continuous pulling job.
			ContinuousImagingJob job = server.getComponentProvider(null).createJob(new PositionInformation(null), ContinuousImagingJob.DEFAULT_TYPE_IDENTIFIER, ContinuousImagingJob.class);
			job.setCameras(usedCameras);
			job.setExposures(usedExposures);
			job.setBurstImaging(false);
			job.addImageListener(imageHandler.startListening());
			job.setChannel(configGroup, channel);

			// Add a task for the continuous pulling job.
			Task task = measurement.addTask(imagingPeriod, false, 0);
			task.addJob(job);
			
			measurement.startMeasurement();
		}
		catch(Exception e)
		{
			client.sendError("Could not initialize multi-camera and -color measurement.", e);
			return;
		}
		
		
    }
    
    void processImage(int imageNo, ImageEvent<?> imageEvent)
	{
    	try
		{
    		BufferedImage image;
			if (increaseContrasts[imageNo])
        	{
				image = ImageTools.getScaledMicroscopeImage(imageEvent, lowerCutoffs[imageNo], upperCutoffs[imageNo]);
            } 
			else
            {
				image = ImageTools.getScaledMicroscopeImage(imageEvent);
            }
			
			AffineTransform transform = new AffineTransform();
			transform.translate(deltaXs[imageNo], deltaYs[imageNo]);
			transform.rotate(deltaPhis[imageNo] * Math.PI / 180, image.getWidth() / 2, image.getHeight() / 2);
						
			BufferedImage transformedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
	        Graphics2D g = (Graphics2D) transformedImage.getGraphics();
	        g.setTransform(transform);
	        g.drawImage(image, 0, 0, null);

	        singleImages[imageNo] = transformedImage;
		}
		catch(Exception e)
		{
			client.sendError("Could not compose colors of images.", e);
		}
	}
    
    private boolean composeImage() throws ImageConvertException
	{
    	// Make local copy of image pointers.
    	BufferedImage[] images = new BufferedImage[NUM_COLORS];
    	for(int i=0; i<NUM_COLORS; i++)
    	{
    		images[i] = singleImages[i];
    	}
    	
    	
    	
    	// Get width and height from the first non null image.
    	int width = -1;
    	int height = -1;
    	for(BufferedImage image : images)
    	{
    		if(image == null)
    			continue;
    		width = image.getWidth();
    		height = image.getHeight();
    		break;
    	}
    	if(width < 0 || height < 0)
    		return false;
    	
    	BufferedImage compImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    	
		// Adjust colors.
		WritableRaster compRaster = compImage.getRaster();
		
		// Colors
		for(int color= 0; color<NUM_COLORS; color++)
		{
			if(images[color] != null && cameras[color] != null)
			{
				double multiplier;
				switch(images[color].getType())
				{
					case BufferedImage.TYPE_BYTE_GRAY:
						multiplier = 1.0;
						break;
					case BufferedImage.TYPE_USHORT_GRAY:
						multiplier = ((double)Byte.MAX_VALUE) / ((double)Short.MAX_VALUE);
						break;
					default:
						throw new ImageConvertException("Only byte or ushort grayscale non-indexed images can be composed.");
				}
				WritableRaster colorRaster = images[color].getRaster();
				for(int i = 0; i < width; i++)
				{
					for(int j = 0; j < height; j++)
					{
						int value = (int)(colorRaster.getSample(i, j, 0) * multiplier);
						compRaster.setSample(i, j, color, value + compRaster.getSample(i, j, color));
					}
				}
			}
		}
							
		composedImage = compImage;
		return true;
	}
    
    @Override
    public synchronized void paintComponent(Graphics grp)
    {
        // super.paintComponent(grp);

        // Make local copy of image pointer
        BufferedImage image;
        synchronized (this)
        {
            image = this.composedImage;
        }

        Graphics2D g2D = (Graphics2D) grp;

        // set the background color to white
        g2D.setColor(getBackground());
        // fill the rect
        g2D.fillRect(0, 0, getWidth(), getHeight());

        if (image == null)
            return;

        double imageWidth = image.getWidth(null);
        double imageHeight = image.getHeight(null);
        double componentWidth = getWidth();
        double componentHeight = getHeight();
        if (componentWidth / imageWidth > componentHeight / imageHeight)
        {
            imageWidth = imageWidth * componentHeight / imageHeight;
            imageHeight = componentHeight;
        } else
        {
            imageHeight = imageHeight * componentWidth / imageWidth;
            imageWidth = componentWidth;
        }

        // draw the image
        g2D.drawImage(image, (int) (componentWidth - imageWidth) / 2,
                (int) (componentHeight - imageHeight) / 2, (int) imageWidth, (int) imageHeight,
                null);
    }
    
    /**
     * Rotates and moves an image.
     * @param imageNo The number of the image.
     * @param deltaX move in x-direction
     * @param deltaY move in y-direction
     * @param deltaPhi rotate in angles (degree)
     */
    public void setRotationAndTranslation(int imageNo, double deltaX, double deltaY, double deltaPhi)
    {
    	deltaXs[imageNo] = deltaX;
    	deltaYs[imageNo] = deltaY;
    	deltaPhis[imageNo] = deltaPhi;
		
    }
}
