/**
 * 
 */
package org.youscope.plugin.usercontrolmeasurement;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.EventListener;

/**
 * A listener which informs the measurement if in the visual callback the user either changed the channel settings or pressed "snap image".
 * @author Moritz Lang
 */
public interface UserControlMeasurementCallbackListener extends Remote, EventListener
{
	/**
	 * Invoked by the callback if the channel to image in was changed.
	 * @throws RemoteException
	 */
	void channelSettingsChanged() throws RemoteException;
	
	/**
	 * Invoked by the callback if the measurement should save the currently displayed image.
	 * @throws RemoteException
	 */
	void snapImage() throws RemoteException;
	
	/**
	 * Called when the user closed the callback UI. The measurement should quit then.
	 * @throws RemoteException
	 */
	void callbackClosed() throws RemoteException;
}
