/**
 * 
 */
package ch.ethz.csb.youscope.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import ch.ethz.csb.youscope.server.microscopeaccess.PreInitDevicePropertyInternal;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeDriverException;
import ch.ethz.csb.youscope.shared.microscope.PreInitDeviceProperty;
import ch.ethz.csb.youscope.shared.microscope.PropertyType;

/**
 * @author langmo
 */
class PreInitDevicePropertyRMI extends UnicastRemoteObject implements PreInitDeviceProperty
{

	/**
	 * Serial Version UID.
	 */
	private static final long					serialVersionUID	= -2204579569726358661L;

	private final PreInitDevicePropertyInternal	deviceProperty;

	/**
	 * Constructor.
	 * @param deviceProperty 
	 * @param accessID  
	 * @throws RemoteException 
	 */
	public PreInitDevicePropertyRMI(PreInitDevicePropertyInternal deviceProperty, int accessID) throws RemoteException
	{
		super();
		this.deviceProperty = deviceProperty;
	}

	@Override
	public String getPropertyID() throws MicroscopeDriverException
	{
		return deviceProperty.getPropertyID();
	}

	@Override
	public PropertyType getType() throws MicroscopeDriverException
	{
		return deviceProperty.getType();
	}

	@Override
	public String[] getAllowedPropertyValues() throws MicroscopeDriverException
	{
		return deviceProperty.getAllowedPropertyValues();
	}

	@Override
	public String getDefaultValue() throws MicroscopeDriverException
	{
		return deviceProperty.getDefaultValue();
	}
}
