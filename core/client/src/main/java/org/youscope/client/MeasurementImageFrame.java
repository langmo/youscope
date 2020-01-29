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
package org.youscope.client;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.youscope.clientinterfaces.YouScopeFrame;
import org.youscope.clientinterfaces.YouScopeFrameListener;
import org.youscope.common.PositionInformation;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.image.ImageProducer;
import org.youscope.uielements.ImagePanel;

/**
 * Frame to display the images currently made by the microscope.
 * @author Moritz Lang
 */
class MeasurementImageFrame extends ImagePanel
{
    /**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -5826503501231432101L;

	private final ImageFrameListener[] imageFrameListener;

    private volatile ImageFrameListener currentImageProducer = null;
    
    private class ImageFrameListener extends UnicastRemoteObject implements ImageListener
		{
		/**
		 * Serial Version UID.
		 */
		private static final long serialVersionUID = 8782739907507416541L;
		private final ImageProducer imageProducer;
		private final PositionInformation positionInformation;
		ImageFrameListener(ImageProducer imageProducer, PositionInformation positionInformation) throws RemoteException
		{
		    super();
		    this.imageProducer = imageProducer;
		    this.positionInformation = positionInformation;
		}
		
		public PositionInformation getPositionInformation()
		{
			return positionInformation;
		}
		
		@Override
		public void imageMade(final ImageEvent<?> event)
		{
		    // Start new thread to process image.
		    Thread thread = new Thread(new Runnable()
		    {
		    	@Override
		        public void run()
		        {
		            setImage(event, ImageFrameListener.this);
		        }
		    }, "Image processor");
		    thread.start();
		}
		
		public void removeImageListener() throws RemoteException
		{
			if(imageProducer != null)
				imageProducer.removeImageListener(this);
		}
		
		public void addImageListener() throws RemoteException
		{
			if(imageProducer != null)
				imageProducer.addImageListener(this);
		}
		
		public String getImageDescription() throws RemoteException
		{
			return imageProducer.getImageDescription();
		}
	}
    
    MeasurementImageFrame(ImageProducer imageProducer, PositionInformation positionInformation) throws Exception
    {
    	this(new ImageProducer[]{imageProducer}, new PositionInformation[]{positionInformation});
    }
    MeasurementImageFrame(ImageProducer[] imageProducers, PositionInformation[] positionInformation) throws Exception
    {
    	super(new YouScopeClientConnectionImpl());
    	setUserChoosesAutoAdjustContrast(true);
    	this.imageFrameListener = new ImageFrameListener[imageProducers.length];
    	for(int i=0; i<imageProducers.length; i++)
		{
			imageFrameListener[i] = new ImageFrameListener(imageProducers[i], positionInformation[i]);
		}
        if(imageFrameListener.length > 0)
        	setImage(null, imageFrameListener[0]);
        if(imageFrameListener.length > 2)
        	setUserChoosesAutoAdjustContrast(true);
    }

    private synchronized void setImage(ImageEvent<?> image, ImageFrameListener imageProducer)
    {
    	if(imageProducer != currentImageProducer)
    	{
	    	currentImageProducer = imageProducer;
	    	
	    	// Set title
			String frameTitel;
			if(imageFrameListener.length > 1)
			{
				frameTitel = "Last image";
			}
			else
				frameTitel = "";
			
			String positionString = imageProducer.getPositionInformation().toString();
			if(positionString.length() > 0)
			{
				if(frameTitel.length() > 0)
					frameTitel+=", ";
				frameTitel+=positionString;
			}
			if(frameTitel.length() > 0)
	    		frameTitel+=": ";
	        try
	        {
	        	frameTitel += imageProducer.getImageDescription();
	        } 
	        catch (RemoteException e1)
	        {
	            ClientSystem.err
	                    .println("Could not obtain image description from image producer. Substituting default string.", e1);
	            frameTitel+="Unknown Image";
	        }
			
			setTitle(frameTitel);
    	}
    	if(image != null)
    		setImage(image);
    }
    
	@Override
	public YouScopeFrame toFrame() {
		YouScopeFrame frame = super.toFrame();
		frame.addFrameListener(new YouScopeFrameListener()
        {

            @Override
            public void frameClosed()
            {
                for(ImageFrameListener imageProducer : MeasurementImageFrame.this.imageFrameListener)
                {
                	if(imageProducer == null)
                		continue;
                    try
                    {
                    	
                    	imageProducer.removeImageListener();
                    } 
                    catch (RemoteException e)
                    {
                        ClientSystem.err.println("Could not remove image listener for visualization due to network problems.", e);
                    }
                }
            }

            @Override
            public void frameOpened()
            {
                for(ImageFrameListener imageProducer : MeasurementImageFrame.this.imageFrameListener)
                {
                	if(imageProducer == null)
                		continue;
                    try
                    {
                    	imageProducer.addImageListener();
                    } 
                    catch (RemoteException e)
                    {
                        ClientSystem.err.println("Could not add image listener for visualization due to network problems.", e);
                    }
                }
            }
        });
		return frame;
	}
}
