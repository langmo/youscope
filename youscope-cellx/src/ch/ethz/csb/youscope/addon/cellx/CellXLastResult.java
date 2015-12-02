/**
 * 
 */
package ch.ethz.csb.youscope.addon.cellx;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Moritz Lang
 *
 */
interface CellXLastResult extends Remote
{

	/**
	 * Sets the specific key value pair.
	 * @param key
	 * @param value
	 * @throws RemoteException
	 */
	void setData(String key, Object value) throws RemoteException;

	/**
	 * Returns the value for the specific key, or null.
	 * @param key
	 * @return value for key, or null.
	 * @throws RemoteException
	 */
	Object getData(String key) throws RemoteException;
	
}
