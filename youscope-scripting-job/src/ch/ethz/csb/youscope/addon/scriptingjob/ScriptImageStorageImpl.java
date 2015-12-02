/**
 * 
 */
package ch.ethz.csb.youscope.addon.scriptingjob;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import ch.ethz.csb.youscope.shared.ImageEvent;
import ch.ethz.csb.youscope.shared.ImageListener;
import ch.ethz.csb.youscope.shared.measurement.ImageProducer;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.resource.scripting.ScriptImageStorage;

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

    private ImageEvent lastImage = null;

    private boolean newImageArrived = false;

    ScriptImageStorageImpl(ImageProducer job) throws RemoteException, MeasurementRunningException
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
    public void imageMade(ImageEvent e)
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
