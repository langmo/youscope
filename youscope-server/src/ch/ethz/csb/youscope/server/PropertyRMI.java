/**
 * 
 */
package ch.ethz.csb.youscope.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import ch.ethz.csb.youscope.server.microscopeaccess.PropertyInternal;
import ch.ethz.csb.youscope.shared.microscope.DeviceException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;
import ch.ethz.csb.youscope.shared.microscope.Property;
import ch.ethz.csb.youscope.shared.microscope.PropertyType;

/**
 * @author langmo
 */
abstract class PropertyRMI extends UnicastRemoteObject implements Property
{
	/**
	 * Serial Version UID.
	 */
	private static final long		serialVersionUID	= 5401100467596480263L;

	private final PropertyInternal	property;

	protected final int				accessID;

	/**
	 * Constructor.
	 * 
	 * @throws RemoteException
	 */
	PropertyRMI(PropertyInternal property, int accessID) throws RemoteException
	{
		super();
		this.property = property;
		this.accessID = accessID;
	}

	@Override
	public String getPropertyID()
	{
		return property.getPropertyID();
	}

	@Override
	public PropertyType getType()
	{
		return property.getType();
	}

	@Override
	public String getValue() throws MicroscopeException, InterruptedException
	{
		return property.getValue();
	}

	@Override
	public boolean isEditable()
	{
		return property.isEditable();
	}

	@Override
	public String getDeviceID() throws RemoteException
	{
		return property.getDeviceID();
	}

	@Override
	public void setValue(String value) throws RemoteException, MicroscopeException, MicroscopeLockedException, InterruptedException, DeviceException
	{
		property.setValue(value, accessID);
	}
}
