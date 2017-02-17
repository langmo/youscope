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
package org.youscope.plugin.livestream;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.util.ImageConvertException;
import org.youscope.common.util.ImageTools;
import org.youscope.serverinterfaces.MeasurementProvider;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;

/**
 * A panel showing the images of a continuous measurement.
 * @author Moritz Lang
 */
class ContinousMeasurementPanel extends JPanel
{
    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 8773818230193224289L;

    private volatile Measurement measurement = null;
    private volatile ImageHandler imageHandler = null;

    private int imagingPeriod;

    private String channel;
    private String channelGroup;

    private String camera;
    
    private double exposure;

    private volatile BufferedImage image = null;
    private volatile ImageEvent<?> orgImage = null;
    private volatile int imageNo = 0;

    private static AffineTransform transform = new AffineTransform();

    private boolean increaseContrast = true;

    private float lowerCutoff = 0.0F;

    private float upperCutoff = 1.0F;
    
    private YouScopeClient client;
	private YouScopeServer server;
	
	private final LowerPanel lowerPanel = new LowerPanel();
	
	private static final String LAST_IMAGE_FILE_PROPERTY = "YouScope.LAST_IMAGE_FILE";
	
    /**
     * Constructor.
     * @param client Interface to the client.
     * @param server Interface to the server.
     * @param camera The ID of the camera with which it should be imaged, or null to use the default camera.
     * @param imagingPeriod The period between two images.
     * @param configGroup The config group in which the channel is defined.
     * @param channel The channel which should be imaged.
     * @param exposure The exposure time for the imaging.
     * @param increaseContrast True if the contrast of the image should be automatically increased.
     */
    public ContinousMeasurementPanel(YouScopeClient client, YouScopeServer server, String camera, int imagingPeriod, String configGroup, String channel, double exposure,
            boolean increaseContrast)
    {
    	super(new BorderLayout());
    	add(lowerPanel, BorderLayout.SOUTH);
    	addMouseListener(lowerPanel);
    	addMouseMotionListener(lowerPanel);
        setDoubleBuffered(true);
        setOpaque(true);
        this.channel = channel;
        this.channelGroup = configGroup;
        this.imagingPeriod = imagingPeriod;
        this.exposure = exposure;
        this.increaseContrast = increaseContrast;
        this.client = client;
		this.server = server;
		this.camera = camera;
    }

    /**
     * Sets if the contrast should be automatically increased.
     * @param increaseContrast True if the contrast should be increased.
     */
    public void setIncreaseContrast(boolean increaseContrast)
    {
        this.increaseContrast = increaseContrast;
    }

    /**
     * Sets the camera with which it should be imaged. Set to null or empty string to image with default camera.
     * @param camera Camera with which it should be imaged.
     */
    public void setCamera(String camera)
    {
    	this.camera = camera;
    }
    
    /**
     * Sets the channel in which it is imaged.
     * @param configGroup The config group in which the channel is defined.
     * @param channel The channel to image.
     */
    public void setChannel(String configGroup, String channel)
    {
        this.channel = channel;
        this.channelGroup = configGroup;
    }

    /**
     * Sets the exposure time for the continuous imaging.
     * @param exposure The exposure time.
     */
    public void setExposure(double exposure)
    {
        this.exposure = exposure;
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
     * Returns if measurement is currently running or not.
     * @return TRUE if measurement is running, false otherwise.
     */
    public boolean isMeasurementRunning()
    {
    	return measurement != null;
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
                } 
                catch (RemoteException | MeasurementException e)
                {
                    client.sendError("Could not stop measurement.", e);
                }
                measurement = null;
            }
            if(imageHandler != null)
            {
            	imageHandler.stopListening();
                imageHandler = null;
            }
        }
    }

    /**
     * Starts the continuous measurement.
     */
    public void startMeasurement()
    {
    	// stop any previous measurement
    	stopMeasurement();
    	
        // Create measurement on server
        try
        {
            MeasurementProvider measurementFactory =server.getMeasurementProvider();
            synchronized (this)
            {
                // Create measurement
            	imageHandler = new ImageHandler();
                measurement = measurementFactory.createContinuousMeasurement((camera == null || camera.length() < 1) ? null : camera, channelGroup, channel, imagingPeriod, exposure, imageHandler.startListening());
                
                // Start measurement
                measurement.startMeasurement();
            }
        } 
        catch (Exception e)
        {
        	client.sendError("Could not create/start measurement", e);
            measurement = null;
            return;
        }
    }

    /**
     * Displays the new image.
     * @param event image to be displayed
     */
    public void newImage(ImageEvent<?> event)
    {
    	// Create image.
        BufferedImage bufferedImage;
        try
		{
        	if (increaseContrast)
        	{
				bufferedImage =
				        ImageTools.getScaledMicroscopeImage(event, lowerCutoff, upperCutoff);
            } else
            {
                bufferedImage =
                        ImageTools.getMicroscopeImage(event);
            }
		}
		catch (ImageConvertException e)
		{
			client.sendError("Could not process image!", e);
			return;
		}

        AffineTransformOp transformOp =
                new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        Rectangle2D transformRect = transformOp.getBounds2D(bufferedImage);

        AffineTransform imageTransform = new AffineTransform();
        imageTransform.translate(-transformRect.getX(), -transformRect.getY());
        imageTransform.concatenate(transform);

        transformOp =
                new AffineTransformOp(imageTransform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        bufferedImage = transformOp.filter(bufferedImage, null);
        
        synchronized (this)
        {
            this.image = bufferedImage;
            this.orgImage = event;
            imageNo++;
        }
        if (isVisible())
            repaint();
    }
    
    private class ImageHandler extends Thread
    {
    	private volatile ImageEvent<?> nextImage;
    	private volatile boolean shouldRun = true;
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
    			synchronized(ImageHandler.this)
    			{
    				nextImage = e;
    				ImageHandler.this.notifyAll();
    			}
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
				// get image.
				ImageEvent<?> image = null;
				synchronized(this)
				{
					while(shouldRun && nextImage == null)
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
					if(!shouldRun)
						return;
					image = nextImage;
					nextImage = null;
				}
				
				// process image
				newImage(image);
				
			}
			
		}
		
    }

    @Override
    public synchronized void paintComponent(Graphics grp)
    {
        // Make local copy of image pointer
        BufferedImage image;
        synchronized (this)
        {
            image = this.image;
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
     * Sets the lower and the upper cutoff, if the contrast is increased.
     * @param lowerCutoff The lower cutoff.
     * @param upperCutoff The upper cutoff.
     */
    public void setCutoff(float lowerCutoff, float upperCutoff)
    {
        this.lowerCutoff = lowerCutoff;
        this.upperCutoff = upperCutoff;
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
     * Panel providing basic functionality like saving an image.
     * @author Moritz Lang
     *
     */
    private class LowerPanel extends JPanel implements MouseListener, MouseMotionListener
    {
    	/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -4553168530016767129L;

		private final Color BACKGROUND = new Color(0.5f, 0.5f, 0.5f, 0.5f);
		private final Font FONT = new Font("SansSerif", Font.PLAIN, 12);
		
		int currentX = -1;
		int currentY = -1;
		
		@Override
	    public synchronized void paintComponent(Graphics g)
	    {
			g.setColor(BACKGROUND);
			g.fillRect(0, 0, getWidth(), getHeight());
			g.setColor(Color.BLACK);
			g.drawLine(0, 0, getWidth(), 0);
			g.setFont(FONT);
			String message = "Image #" + Integer.toString(imageNo);
			if(currentX >= 0 && currentY >= 0)
			{
				message += ", X: " + Integer.toString(currentX + 1) + ", Y: " + Integer.toString(currentY + 1);
				ImageEvent<?> orgImage = ContinousMeasurementPanel.this.orgImage;
				Point orgCoord = ImageTools.backTransformCoordinate(orgImage, new Point(currentX, currentY));
				if(orgCoord != null)
				{
					long pixelValue = ImageTools.getPixelValue(orgImage, orgCoord.x, orgCoord.y);
					if(pixelValue >= 0)
						message += ", I: " + Long.toString(pixelValue);
				}
			}
			g.drawString(message, 8, (getHeight()-12)/2 + 12);
	    }
		
		LowerPanel()
    	{
    		super(new FlowLayout(FlowLayout.RIGHT));
    		setOpaque(false);
    		setVisible(false);
    		addMouseListener(this);
    		
    		Icon saveIcon = ImageLoadingTools.getResourceIcon("bonus/icons-shadowless-24/disk-black.png", "Save Image");
    		
    		JButton saveButton;
    		if(saveIcon == null)
    			saveButton = new JButton("Save image");
    		else
    			saveButton = new JButton(saveIcon);
    		saveButton.setMargin(new Insets(1, 1, 1, 1));
    		saveButton.addMouseListener(this);
    		saveButton.setOpaque(false);
    		saveButton.setBorder(null);
    		saveButton.setToolTipText("Save current image to file system.");
    		add(saveButton);
    		
    		saveButton.addActionListener(new ActionListener()
    		{
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					// Make local copy of image pointer
			        BufferedImage image;
			        synchronized (this)
			        {
			            image = ContinousMeasurementPanel.this.image;
			        }
			        if(image == null)
			        	return;
			        
			        // Let user select file to save to
			        String lastFile = client.getPropertyProvider().getProperty(LAST_IMAGE_FILE_PROPERTY, "image.tif");
                    JFileChooser fileChooser = new JFileChooser(lastFile);
                    //Thread.currentThread().setContextClassLoader(ContinousMeasurementPanel.class.getClassLoader());
                    String[] imageFormats = ImageIO.getWriterFileSuffixes();
                    FileFilter tifFilter = null;
                    fileChooser.setAcceptAllFileFilterUsed(false);
                    for(int i=0; i<imageFormats.length; i++)
                    {
                    	if(imageFormats[i] == null || imageFormats[i].length() <= 0)
                    		continue;
                    	
                    	FileNameExtensionFilter filter = new FileNameExtensionFilter(imageFormats[i].toUpperCase() + " Images (."+imageFormats[i].toLowerCase()+")", new String[]{imageFormats[i]});
                    	fileChooser.addChoosableFileFilter(filter);
                    	if(imageFormats[i].compareToIgnoreCase("tif") == 0)
                    		tifFilter = filter;
                    }
                    if(tifFilter != null)
                    {
                    	fileChooser.setFileFilter(tifFilter);
                    	fileChooser.setSelectedFile(new File("Image_" + Integer.toString(imageNo) + ".tif"));
                    }
                                                           
                    File file;
                    ImageWriter imageWriter;
                    while(true)
                    {
                    	int returnVal = fileChooser.showDialog(null, "Save");
                    	if (returnVal != JFileChooser.APPROVE_OPTION)
                    	{
                    		return;
                    	}
                    	file = fileChooser.getSelectedFile().getAbsoluteFile();
                    	if(file.exists())
                    	{
                    		returnVal = JOptionPane.showConfirmDialog(null, "File " + file.toString() + " does already exist.\nOverwrite?", "File does already exist", JOptionPane.YES_NO_OPTION);
                    		if(returnVal != JOptionPane.YES_OPTION)
                    			continue;
                    	}
                    	
                    	FileFilter selectedFilter = fileChooser.getFileFilter();
                    	String fileSuffix;
                    	if(selectedFilter == null || !(selectedFilter instanceof FileNameExtensionFilter) || ((FileNameExtensionFilter)selectedFilter).getExtensions().length != 1)
                    	{
	                    	String fileName = file.getPath();
	                        int idx = fileName.lastIndexOf('.');
	                        if(idx < 0)
	                        {
	                        	JOptionPane.showMessageDialog(null, "File " + fileName + " does not have a valid file type ending.", "File has invalid file type", JOptionPane.ERROR_MESSAGE);
	                    		continue;
	                        }
	                        fileSuffix = fileName.substring(idx+1);
                    	}
                    	else
                    	{
                    		fileSuffix = ((FileNameExtensionFilter)selectedFilter).getExtensions()[0]; 
                    	}
                        Iterator<ImageWriter> imageIterator = ImageIO.getImageWritersBySuffix(fileSuffix);
                        if(!imageIterator.hasNext())
                        {
                        	JOptionPane.showMessageDialog(null, "YouScope does not have a plug-in installed to support saving images with file type \"" + fileSuffix + "\".", "Image file type not supported", JOptionPane.ERROR_MESSAGE);
                    		continue;
                        }
                        imageWriter = imageIterator.next();
                        break;
                    }
                    
                    client.getPropertyProvider().setProperty(LAST_IMAGE_FILE_PROPERTY, file.toString());
			        
                    ImageOutputStream ios = null;
                    try
					{
                    	ios = ImageIO.createImageOutputStream(file);
	                    imageWriter.setOutput(ios);
	                    imageWriter.write(image);
					}
					catch(IOException e)
					{
						client.sendError("New image " + file.getPath() + " could not be saved.", e);
					}
                    finally
                    {
                    	imageWriter.dispose();
                    	if(ios != null)
                    	{
	                    	try
							{
	                    		ios.flush();
								ios.close();
							}
							catch(IOException e)
							{
								client.sendError("Image " + file.getPath() + " saved, but file handle could not be released. Check validity of image!", e);
							}
                    	}
                    }
				}
    		});
    	}

		@Override
		public void mouseClicked(MouseEvent arg0)
		{
			// do nothing.
		}

		@Override
		public void mouseEntered(MouseEvent arg0)
		{
			if(image != null)
				setVisible(true);
		}

		@Override
		public void mouseExited(MouseEvent arg0)
		{
			setVisible(false);
		}

		@Override
		public void mousePressed(MouseEvent arg0)
		{
			// do nothing.
		}

		@Override
		public void mouseReleased(MouseEvent arg0)
		{
			// do nothing.
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			// do nothing yet. Maybe later: measure distances?
			
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
			Point mousePos = e.getPoint();		
			BufferedImage image = ContinousMeasurementPanel.this.image;
			if(image == null)
			{
				currentX = -1;
		        currentY = -1;
		        return;
			}
			
			int imageOrgWidth = image.getWidth(null);
			int imageOrgHeight = image.getHeight(null);
			double componentWidth = ContinousMeasurementPanel.this.getWidth();
	        double componentHeight = ContinousMeasurementPanel.this.getHeight();
	        double imageWidth;
	        double imageHeight;
	        if (componentWidth / imageOrgWidth > componentHeight / imageOrgHeight)
	        {
	            imageWidth = imageOrgWidth * componentHeight / imageOrgHeight;
	            imageHeight = componentHeight;
	        } 
	        else
	        {
	            imageHeight = imageOrgHeight * componentWidth / imageOrgWidth;
	            imageWidth = componentWidth;
	        }

	        double relX = ((double)(mousePos.x - (int)((componentWidth - imageWidth) / 2))) / (int)imageWidth;
	        double relY = ((double)(mousePos.y - (int)((componentHeight - imageHeight) / 2))) / (int)imageHeight;
	        if(relX < 0 || relX > 1 || relY < 0 || relY > 1)
	        {
	        	relX = -1;
	        	relY = -1;
	        }
	        currentX = (int)Math.round(relX * (imageOrgWidth-1));
	        currentY = (int)Math.round(relY * (imageOrgHeight-1));
	        repaint();
		}
    }
}
