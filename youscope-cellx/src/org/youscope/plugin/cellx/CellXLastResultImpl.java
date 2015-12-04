/**
 * 
 */
package org.youscope.plugin.cellx;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

/**
 * @author Moritz Lang
 *
 */
class CellXLastResultImpl extends UnicastRemoteObject implements CellXLastResult
{
	private final HashMap<String, Object> lastResult = new HashMap<String, Object>();
	
	/**
	 * SerializableVersion UID.
	 */
	private static final long	serialVersionUID	= 1110798742199344228L;

	/**
	 * Constructor.
	 * @throws RemoteException
	 */
	public CellXLastResultImpl() throws RemoteException
	{
		super();
	}

	@Override
	public synchronized void setData(String key, Object value) throws RemoteException
	{
		lastResult.put(key, value);
	}
	
	@Override
	public synchronized Object getData(String key) throws RemoteException
	{
		return lastResult.get(key);
	}
}
