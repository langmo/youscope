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
package org.youscope.plugin.multicolorstream;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import javax.swing.JPanel;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.PositionInformation;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.job.basicjobs.ImagingJob;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.task.Task;
import org.youscope.common.util.ImageConvertException;
import org.youscope.common.util.ImageTools;
import org.youscope.serverinterfaces.MeasurementProvider;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 *
 */
public class MultiStreamPanel extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 2713829811044542455L;

	private int imagingPeriod;

	private String[] channels = {null, null, null, null};
	private String[] configGroups = {null, null, null, null};

    private double[] exposures = {20, 20, 20, 20};
    
    private Measurement measurement = null;
    
    private final YouScopeServer server;
	private final YouScopeClient client;    
	
	private volatile BufferedImage[] singleImages = new BufferedImage[4];

    private static AffineTransform transform = new AffineTransform();

    private float[] lowerCutoffs = {0.0F, 0.0F, 0.0F, 0.0F};

    private float[] upperCutoffs = {1.0F, 1.0F, 1.0F, 1.0F};
    
    private double brightFieldOpacity = 0.5;
    
    private boolean[] increaseContrasts = {true, true, true, true};
    
    private volatile BufferedImage composedImage = null;
    
    MultiStreamPanel(YouScopeClient client, YouScopeServer server)
	{
		this.server = server;
		this.client = client;
	}
    /**
     * Stops the measurement. Does nothing if measurement is not running.
     * The call immediately returns, even if the stopping of the measurement takes longer.
     */
    public void stopMeasurement()
    {
        synchronized (this)
        {
            if (measurement != null)
            {
                try
                {
                    measurement.stopMeasurement(false);
                } catch (RemoteException | MeasurementException e)
                {
                    client.sendError("Could not stop measurement.", e);
                }
            }
        }
    }
    
    /**
     * Sets the channel in which it is imaged.
     * @param channelNo The channel for which the exposure time should be set.
     * @param configGroup The config groups in which the channel is defined.
     * @param channel The channels to image.
     */
    public void setChannel(int channelNo, String configGroup, String channel)
    {
    	if(channelNo < 0 || channelNo >= exposures.length)
    		throw new ArrayIndexOutOfBoundsException("ChannelNo must be larger or equal to 0 and smaller than" + exposures.length + ".");
        this.channels[channelNo] = channel;
        this.configGroups[channelNo] = configGroup;
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
     */
	public synchronized void startMeasurement()
    {
        
        try
        {
        	// Create measurement on server
            MeasurementProvider measurementFactory =server.getMeasurementProvider();
            measurement = measurementFactory.createMeasurement();
            measurement.setName("Multi-Color Stream");
            Task task = measurement.addTask(imagingPeriod, false, 0);
            ImagingJob[] jobs = new ImagingJob[channels.length];
            for(int i=0; i<jobs.length; i++)
            {
            	singleImages[i] = null;
            	if(configGroups[i] == null || channels[i] == null)
            		continue;
            	jobs[i] = server.getComponentProvider(null).createJob(new PositionInformation(), ImagingJob.DEFAULT_TYPE_IDENTIFIER, ImagingJob.class);
            	jobs[i].setChannel(configGroups[i], channels[i]);
            	jobs[i].setExposure(exposures[i]);
            	jobs[i].addImageListener(new ImageListenerImpl(i));
            	task.addJob(jobs[i]);
            }
            
            measurement.setLockMicroscopeWhileRunning(false);
            
            // Start measurement
            measurement.startMeasurement();
        } 
        catch (Exception e)
        {
        	client.sendError("Could not create/start measurement", e);
            measurement = null;
            return;
        }
    }
    
    private class ImageListenerImpl extends UnicastRemoteObject implements ImageListener
    {
        /**
         * Serial Version UID
         */
        private static final long serialVersionUID = -2641367149643651724L;
        private final int imageNo;
        private ImageEvent<?> imageEvent = null;
        /**
         * @throws RemoteException
         */
        private ImageListenerImpl(int imageNo) throws RemoteException
        {
            super();
            this.imageNo = imageNo;            
        }

        @Override
        public void imageMade(ImageEvent<?> e)
        {
        	imageEvent = e;
            // Start new thread to process image.
        	Thread thread = new Thread(new Runnable()
            {
				@Override
				public void run()
				{
					newImage(imageNo, imageEvent);
				}
            }, "Image processor");
            thread.start();
        }
    }
    protected void newImage(int imageNo, ImageEvent<?> imageEvent)
	{
    	try
		{
			if (increaseContrasts[imageNo])
        	{
				singleImages[imageNo] = ImageTools.getScaledMicroscopeImage(imageEvent, lowerCutoffs[imageNo], upperCutoffs[imageNo]);
            } 
			else
            {
				singleImages[imageNo] = ImageTools.getScaledMicroscopeImage(imageEvent);
            }
			composeImage();
		}
		catch(Exception e)
		{
			client.sendError("Could not compose colors of images.", e);
		}
	}
    
    private void composeImage() throws ImageConvertException
	{
    	// Make local copy of image pointers.
    	BufferedImage[] images = new BufferedImage[4];
    	for(int i=0; i<4; i++)
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
    		throw new ImageConvertException("All images are null pointers.");
    	
    	BufferedImage compImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    	
		// Adjust colors.
		WritableRaster compRaster = compImage.getRaster();
		
		// First set bright field level
		double localPercentageBright;
		if(images[0] != null)
		{
			localPercentageBright = brightFieldOpacity;
			double multiplier;
			switch(images[0].getType())
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
			WritableRaster brightRaster = images[0].getRaster();
			
			for(int i = 0; i < width; i++)
			{
				for(int j = 0; j < height; j++)
				{
					int value = (int)(brightRaster.getSample(i, j, 0) * multiplier * localPercentageBright);
					compRaster.setSample(i, j, 0, value);
					compRaster.setSample(i, j, 1, value);
					compRaster.setSample(i, j, 2, value);
				}
			}
		}
		else
		{
			localPercentageBright = 0;
		}
		
		// Colors
		for(int color= 0; color<3; color++)
		{
			if(images[color+1] != null && configGroups[color+1] != null && channels[color+1] != null)
			{
				double multiplier;
				switch(images[color+1].getType())
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
				WritableRaster colorRaster = images[color+1].getRaster();
				for(int i = 0; i < width; i++)
				{
					for(int j = 0; j < height; j++)
					{
						int value = (int)(colorRaster.getSample(i, j, 0) * multiplier * (1.0 - localPercentageBright));
						compRaster.setSample(i, j, color, value + compRaster.getSample(i, j, color));
					}
				}
			}
		}
							
		composedImage = compImage;
		repaint();
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
     * Rotates the image clockwise.
     */
    public void rotateClockwise()
    {
        transform.rotate(-Math.PI / 2);
    }

    /**
     * Rotates the image counter clockwise.
     */
    public void rotateCounterClockwise()
    {
        transform.rotate(Math.PI / 2);
    }

    /**
     * Flips the image horizontally.
     */
    public void flipHorizontal()
    {
        transform.concatenate(new AffineTransform(-1, 0, 0, 1, 0, 0));
    }

    /**
     * Flips the image vertically.
     */
    public void flipVertical()
    {
        transform.concatenate(new AffineTransform(1, 0, 0, -1, 0, 0));
    }
	/**
	 * @return opacity of the bright field channel (0-1).
	 */
	public double getBrightFieldOpacity()
	{
		return brightFieldOpacity;
	}
	/**
	 * @param brightFieldOpacity opacity of the bright field channel (0-1).
	 */
	public void setBrightFieldOpacity(double brightFieldOpacity)
	{
		this.brightFieldOpacity = brightFieldOpacity;
	}
}
