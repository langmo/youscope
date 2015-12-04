/**
 * 
 */
package org.youscope.server;

import java.rmi.RemoteException;

import org.youscope.addon.microscopeaccess.ReadOnlyPropertyInternal;
import org.youscope.common.microscope.ReadOnlyProperty;

/**
 * @author Moritz Lang
 * 
 */
class ReadOnlyPropertyRMI extends PropertyRMI implements ReadOnlyProperty
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 3130755396734654776L;

	/**
	 * Constructor.
	 * @throws RemoteException
	 */
	ReadOnlyPropertyRMI(ReadOnlyPropertyInternal readOnlyProperty, int accessID) throws RemoteException
	{
		super(readOnlyProperty, accessID);
	}
}
