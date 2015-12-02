/**
 * 
 */
package ch.ethz.csb.youscope.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import ch.ethz.csb.youscope.server.microscopeaccess.DeviceInternal;
import ch.ethz.csb.youscope.server.microscopeaccess.FloatPropertyInternal;
import ch.ethz.csb.youscope.server.microscopeaccess.IntegerPropertyInternal;
import ch.ethz.csb.youscope.server.microscopeaccess.PropertyInternal;
import ch.ethz.csb.youscope.server.microscopeaccess.ReadOnlyPropertyInternal;
import ch.ethz.csb.youscope.server.microscopeaccess.SelectablePropertyInternal;
import ch.ethz.csb.youscope.server.microscopeaccess.StringPropertyInternal;
import ch.ethz.csb.youscope.shared.microscope.Device;
import ch.ethz.csb.youscope.shared.microscope.DeviceException;
import ch.ethz.csb.youscope.shared.microscope.DeviceType;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;
import ch.ethz.csb.youscope.shared.microscope.Property;

/**
 * @author langmo
 */
class DeviceRMI extends UnicastRemoteObject implements Device
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 8821464564099326373L;

	protected DeviceInternal	device;

	protected int				accessID;

	/**
	 * Constructor.
	 * 
	 * @throws RemoteException
	 */
	DeviceRMI(DeviceInternal device, int accessID) throws RemoteException
	{
		super();
		this.accessID = accessID;
		this.device = device;
	}

	@Override
	public String getDeviceID()
	{
		return device.getDeviceID();
	}

	@Override
	public DeviceType getType()
	{
		return device.getType();
	}

	@Override
	public Property[] getProperties() throws RemoteException
	{
		PropertyInternal[] orgProperties = device.getProperties();
		Property[] newProperties = new Property[orgProperties.length];
		for(int i = 0; i < orgProperties.length; i++)
		{
			newProperties[i] = toProperty(orgProperties[i]);
		}
		return newProperties;
	}

	private Property toProperty(PropertyInternal property) throws RemoteException
	{
		if(property instanceof StringPropertyInternal)
			return new StringPropertyRMI((StringPropertyInternal)property, accessID);
		else if(property instanceof IntegerPropertyInternal)
			return new IntegerPropertyRMI((IntegerPropertyInternal)property, accessID);
		else if(property instanceof FloatPropertyInternal)
			return new FloatPropertyRMI((FloatPropertyInternal)property, accessID);
		else if(property instanceof SelectablePropertyInternal)
			return new SelectablePropertyRMI((SelectablePropertyInternal)property, accessID);
		else if(property instanceof ReadOnlyPropertyInternal)
			return new ReadOnlyPropertyRMI((ReadOnlyPropertyInternal)property, accessID);
		else
			throw new RemoteException("Property type of remote property invalid.");
	}

	@Override
	public Property[] getEditableProperties() throws RemoteException
	{
		PropertyInternal[] orgProperties = device.getEditableProperties();
		Property[] newProperties = new Property[orgProperties.length];
		for(int i = 0; i < orgProperties.length; i++)
		{
			newProperties[i] = toProperty(orgProperties[i]);
		}
		return newProperties;
	}

	@Override
	public Property getProperty(String name) throws DeviceException, RemoteException
	{
		return toProperty(device.getProperty(name));
	}

	@Override
	public String getLibraryID()
	{
		return device.getLibraryID();
	}

	@Override
	public String getDriverID()
	{
		return device.getDriverID();
	}

	@Override
	public double getExplicitDelay()
	{
		return device.getExplicitDelay();

	}

	@Override
	public void setExplicitDelay(double delay) throws MicroscopeException, MicroscopeLockedException
	{
		device.setExplicitDelay(delay, accessID);
	}

	@Override
	public void waitForDevice() throws MicroscopeException, RemoteException, InterruptedException
	{
		device.waitForDevice();
	}
}
