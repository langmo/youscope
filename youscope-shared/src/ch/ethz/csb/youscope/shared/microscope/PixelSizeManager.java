/**
 * 
 */
package ch.ethz.csb.youscope.shared.microscope;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Allows to access the pixel size settings.
 * @author langmo
 * 
 */
public interface PixelSizeManager extends Remote
{
	/**
	 * Returns a list of all available pixel sizes.
	 * 
	 * @return List of all pixel size settings.
	 * @throws RemoteException
	 */
	PixelSize[] getPixelSizes() throws RemoteException;

	/**
	 * Returns the pixel size with the given ID.
	 * @param pixelSizeID The ID of the pixel size setting.
	 * @return The pixel size with the given ID.
	 * @throws SettingException
	 * @throws RemoteException
	 */
	PixelSize getPixelSize(String pixelSizeID) throws SettingException, RemoteException;

	/**
	 * Adds a new pixel size setting with the given ID. If the setting already exists, the existing setting is returned.
	 * @param pixelSizeID The ID of the pixel size setting which should be defined/re-defined.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @return The newly created or the already existing pixel size setting.
	 * @throws MicroscopeLockedException
	 * @throws SettingException
	 * @throws RemoteException
	 */
	PixelSize addPixelSize(String pixelSizeID) throws MicroscopeLockedException, SettingException, RemoteException;

	/**
	 * Removes the pixel size setting with the given ID. If the ID does not exist, nothing happens.
	 * @param pixelSizeID The ID of the pixel size setting which should be removed.
	 * @param accessID The access ID of the current microscope object. If the microscope is locked with a different accessID, a MicroscopeLockedException is thrown.
	 * @throws MicroscopeLockedException
	 * @throws SettingException
	 * @throws RemoteException
	 */
	void removePixelSize(String pixelSizeID) throws MicroscopeLockedException, SettingException, RemoteException;
}
