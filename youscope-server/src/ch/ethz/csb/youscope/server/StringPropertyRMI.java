/**
 * 
 */
package ch.ethz.csb.youscope.server;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.server.microscopeaccess.StringPropertyInternal;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;
import ch.ethz.csb.youscope.shared.microscope.StringProperty;

/**
 * @author Moritz Lang
 * 
 */
class StringPropertyRMI extends PropertyRMI implements StringProperty
{
	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID	= 2756352088850819009L;
	private final StringPropertyInternal	stringProperty;

	/**
	 * Constructor.
	 * @throws RemoteException
	 */
	StringPropertyRMI(StringPropertyInternal stringProperty, int accessID) throws RemoteException
	{
		super(stringProperty, accessID);
		this.stringProperty = stringProperty;
	}

	@Override
	public void setValue(String value) throws RemoteException, MicroscopeException, MicroscopeLockedException, InterruptedException
	{
		stringProperty.setValue(value, accessID);
	}
}
