/**
 * 
 */
package org.youscope.common.image;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * Listener which gets notified about produced images.
 * 
 * @author Moritz Lang
 */
public interface ImageListener extends Remote, EventListener
{
    /**
     * Method gets invoked if new image is available.
     * 
     * @param image Object containing the pixel data, as well as the image metadata.
     * @throws RemoteException
     */
    void imageMade(ImageEvent<?> image) throws RemoteException;
}
