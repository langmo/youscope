/**
 * 
 */
package ch.ethz.csb.youscope.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;



/**
 * Listener which detects the availability of new pictures of the microscope.
 * 
 * @author Moritz Lang
 */
public interface ImageListener extends Remote, EventListener
{
    /**
     * Method gets invoked if new picture is available and makes picture and its properties available.
     * 
     * @param e Object containing picture and picture properties data.
     * @throws RemoteException
     */
    void imageMade(ImageEvent e) throws RemoteException;
}
