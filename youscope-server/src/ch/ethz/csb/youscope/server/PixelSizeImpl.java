/**
 * 
 */
package ch.ethz.csb.youscope.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import ch.ethz.csb.youscope.server.microscopeaccess.MicroscopeInternal;
import ch.ethz.csb.youscope.shared.microscope.DeviceSettingDTO;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeConfigurationListener;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;
import ch.ethz.csb.youscope.shared.microscope.PixelSize;
import ch.ethz.csb.youscope.shared.microscope.SettingException;

/**
 * For a given set of device settings, this class stores information about the corresponding pixel size of the camera.
 * @author Moritz Lang
 * 
 */
class PixelSizeImpl implements MicroscopeConfigurationListener, Comparable<PixelSizeImpl>
{
	private final String					pixelSizeID;
	private final Vector<DeviceSettingDTO>	pixelSizeSettings	= new Vector<DeviceSettingDTO>();
	private double							pixelSize			= 6.45;
	private final MicroscopeInternal		microscope;

	public PixelSizeImpl(String pixelSizeID, MicroscopeInternal microscope)
	{
		this.pixelSizeID = new String(pixelSizeID);
		this.microscope = microscope;
	}

	public String getPixelSizeID()
	{
		return pixelSizeID;
	}

	public DeviceSettingDTO[] getPixelSizeSettings()
	{
		// Clone every element...
		DeviceSettingDTO[] newArray = new DeviceSettingDTO[pixelSizeSettings.size()];
		for(int i = 0; i < newArray.length; i++)
		{
			newArray[i] = new DeviceSettingDTO(pixelSizeSettings.elementAt(i));
		}

		return newArray;
	}

	public double getPixelSize()
	{
		return pixelSize;
	}

	@Override
	public void deviceRemoved(String deviceID)
	{
		for(int i = 0; i < pixelSizeSettings.size(); i++)
		{
			DeviceSettingDTO setting = pixelSizeSettings.elementAt(i);
			if(setting.getDevice().equals(deviceID))
			{
				pixelSizeSettings.remove(setting);
				i--;
			}
		}
	}

	public void setPixelSizeSettings(DeviceSettingDTO[] newSettings, int accessID) throws MicroscopeLockedException, SettingException
	{
		microscope.lockExclusiveWrite(accessID);
		try
		{

			SettingsValidator.areSettingsValid(newSettings, true, microscope, accessID);
			pixelSizeSettings.clear();
			for(DeviceSettingDTO setting : newSettings)
			{
				pixelSizeSettings.add(new DeviceSettingDTO(setting));
			}
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}

		ServerSystem.out.println("Settings of pixel size configuration " + pixelSizeID + " changed.");
	}

	public void addPixelSizeSetting(DeviceSettingDTO setting, int accessID) throws MicroscopeLockedException, SettingException
	{
		microscope.lockExclusiveWrite(accessID);
		try
		{
			SettingsValidator.isSettingValid(setting, true, microscope, accessID);
			pixelSizeSettings.add(setting);
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}

		ServerSystem.out.println("Added settings \"" + setting.toString() + "\" to pixel size configuration " + pixelSizeID + ".");
	}

	public void setPixelSize(double pixelSize, int accessID) throws MicroscopeLockedException, SettingException
	{
		microscope.lockExclusiveWrite(accessID);
		try
		{
			this.pixelSize = pixelSize;
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}

		ServerSystem.out.println("Pixel size of " + pixelSizeID + " set to " + Double.toString(pixelSize) + " microns.");
	}

	@Override
	public void microscopeUninitialized()
	{
		pixelSizeSettings.clear();
		pixelSize = 6.45;
	}

	@Override
	public void labelChanged(DeviceSettingDTO oldLabel, DeviceSettingDTO newLabel)
	{
		for(int i = 0; i < pixelSizeSettings.size(); i++)
		{
			if(pixelSizeSettings.elementAt(i).equals(oldLabel))
				pixelSizeSettings.setElementAt(newLabel, i);
		}
	}

	@Override
	public int compareTo(PixelSizeImpl o)
	{
		if(o == null)
			return -1;
		return getPixelSizeID().compareToIgnoreCase(o.getPixelSizeID());
	}

	private class RMIInterface extends UnicastRemoteObject implements PixelSize
	{
		/**
		 * Serial Version UID.
		 */
		private static final long	serialVersionUID	= -297042545568227183L;
		private final int			accessID;

		/**
		 * Constructor.
		 * @param accessID The microscope access ID used by this RMI interface.
		 * @throws RemoteException
		 */
		RMIInterface(int accessID) throws RemoteException
		{
			super();
			this.accessID = accessID;
		}

		@Override
		public String getPixelSizeID() throws RemoteException
		{
			return PixelSizeImpl.this.getPixelSizeID();
		}

		@Override
		public DeviceSettingDTO[] getPixelSizeSettings() throws RemoteException
		{
			return PixelSizeImpl.this.getPixelSizeSettings();
		}

		@Override
		public void setPixelSizeSettings(DeviceSettingDTO[] newSettings) throws MicroscopeLockedException, SettingException, RemoteException
		{
			PixelSizeImpl.this.setPixelSizeSettings(newSettings, accessID);
		}

		@Override
		public void addPixelSizeSetting(DeviceSettingDTO setting) throws MicroscopeLockedException, SettingException, RemoteException
		{
			PixelSizeImpl.this.addPixelSizeSetting(setting, accessID);
		}

		@Override
		public double getPixelSize() throws RemoteException
		{
			return PixelSizeImpl.this.getPixelSize();
		}

		@Override
		public void setPixelSize(double pixelSize) throws MicroscopeLockedException, SettingException, RemoteException
		{
			PixelSizeImpl.this.setPixelSize(pixelSize, accessID);
		}
	}

	/**
	 * Returns the RMI interface of this class used by client applications.
	 * @param accessID The access ID to the microscope used by the RMI interface.
	 * @throws RemoteException
	 */
	PixelSize getRMIInterface(int accessID) throws RemoteException
	{
		return new RMIInterface(accessID);
	}
}
