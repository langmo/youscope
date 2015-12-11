/**
 * 
 */
package org.youscope.common;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * An image listener which stores the last (and only the last) image it receives and allows
 * to access it.
 * @author Moritz Lang
 * 
 */
public class ImageAdapter extends UnicastRemoteObject implements ImageListener
{
	/**  
	 * SerializableVersion UID.
	 */
	private static final long	serialVersionUID	= 521510353477597919L;
	private volatile ImageEvent<?>	lastImage			= null;

	/**
	 * Constructor.
	 * @throws RemoteException
	 */
	public ImageAdapter() throws RemoteException
	{
		super();
	}

	@Override
	public synchronized void imageMade(ImageEvent<?> e) throws RemoteException
	{
		lastImage = e;
	}

	/**
	 * Returns the last image received by this adapter.
	 * @return Last image received, or null, if yet none received.
	 */
	public synchronized ImageEvent<?> getImage()
	{
		return lastImage;
	}

	/**
	 * Returns the last image received by this adapter and sets the last image to null.
	 * @return Last image received, or null, if yet none received.
	 */
	public synchronized ImageEvent<?> clearImage()
	{
		ImageEvent<?> e = lastImage;
		lastImage = null;
		return e;
	}
}
