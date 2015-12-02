/**
 * 
 */
package ch.ethz.csb.youscope.shared.microscope;

import java.rmi.RemoteException;

/**
 * @author Moritz Lang
 * 
 */
public interface SelectableProperty extends Property
{
	/**
	 * Returns a list of all allowed property values. If all possible values are allowed, the allowed values are not known, or the allowed values are not discrete, returns null.
	 * @return List of all allowed values or null.
	 * @throws RemoteException
	 */
	public String[] getAllowedPropertyValues() throws RemoteException;
}
