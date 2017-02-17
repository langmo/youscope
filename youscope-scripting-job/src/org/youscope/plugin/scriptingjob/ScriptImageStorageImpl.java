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
package org.youscope.plugin.scriptingjob;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.image.ImageEvent;
import org.youscope.common.image.ImageListener;
import org.youscope.common.image.ImageProducer;
import org.youscope.common.scripting.ScriptImageStorage;

/**
 * @author langmo
 */
class ScriptImageStorageImpl extends UnicastRemoteObject implements ScriptImageStorage,
        ImageListener
{
    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 8303485384981886687L;

    private ImageEvent<?> lastImage = null;

    private boolean newImageArrived = false;

    ScriptImageStorageImpl(ImageProducer job) throws RemoteException, ComponentRunningException
    {
        super();
        job.addImageListener(this);
    }

    @Override
    public int getLastImageBitDepth()
    {
    	if(lastImage == null)
    		return -1;
		return lastImage.getBitDepth();
    }

    @Override
    public int getLastImageBytesPerPixel()
    {
    	if(lastImage == null)
    		return -1;
		return lastImage.getBytesPerPixel();
    }

    @Override
    public int getLastImageHeight()
    {
    	if(lastImage == null)
    		return -1;
		return lastImage.getHeight();
    }

    @Override
    public Object getLastImagePixels()
    {
    	if(lastImage == null)
    		return -1;
		return lastImage.getImageData();
    }

    @Override
    public int getLastImageWidth()
    {
    	if(lastImage == null)
    		return -1;
		return lastImage.getWidth();
    }

    @Override
    public void imageMade(ImageEvent<?> e)
    {
        synchronized (this)
        {
            this.lastImage = e;
            notifyAll();
        }
    }

    @Override
    public String getLastImageCoding() throws RemoteException
    {
        return "uint" + Integer.toString(8 * getLastImageBytesPerPixel());
    }

    @Override
    public Object getNewImagePixels(int timeout) throws RemoteException
    {
        // Wait until new image arrives
        synchronized (this)
        {
            if (!newImageArrived)
            {
                try
                {
                    wait(timeout);
                    if (Thread.interrupted())
                    {
                        // User wants to quit
                        throw new InterruptedException();
                    }
                } 
                catch (@SuppressWarnings("unused") InterruptedException e)
                {
                    return null;
                }
            }
            if (!newImageArrived)
            {
                return null;
            }
            // New image is there....
            newImageArrived = false;
            return getLastImagePixels();
        }
    }

}
