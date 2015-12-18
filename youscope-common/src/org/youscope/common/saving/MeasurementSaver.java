/**
 * 
 */
package org.youscope.common.saving;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.youscope.common.image.ImageListener;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.table.TableListener;

/**
 * Interface providing functionality to set and determine the way images and meta information of a measurement are saved
 * to the file system.
 * @author Moritz Lang
 * 
 */
public interface MeasurementSaver extends Remote
{
	/**
	 * Returns an image listener which can be added to an image producing job to save all produced images to the disk.
	 * @param imageSaveName The name of the imaging job which is producing the images. Should not contain a file type.
	 * @return An image listener which can be added to the job created by the respective configuration to save the images, or null, if the listener could not be constructed.
	 * @throws RemoteException
	 */
	ImageListener getSaveImageListener(String imageSaveName) throws RemoteException;

	/**
	 * Returns a table listener which can be added to an table data producing job to save all produced data to the disk.
	 * @param tableSaveName The name of the table generating job. Should not contain a file type.
	 * @return A table data listener which can be added to the job created by the respective configuration to save table data, or null, if the listener could not be constructed.
	 * @throws RemoteException
	 */
	TableListener getSaveTableListener(String tableSaveName) throws RemoteException;

	/**
	 * Sets the settings how the measurement should be saved to the disk (e.g. in which folder, ...).
	 * Set to null if measurement should not be saved.
	 * @param saveSettings Settings how the measurement should be saved.
	 * @throws MeasurementRunningException
	 * @throws RemoteException
	 */
	void setSaveSettings(SaveSettings saveSettings) throws MeasurementRunningException, RemoteException;

	/**
	 * Returns the current settings how the measurement should be saved to the disk (e.g. in which folder, ...), or null if
	 * measurement should not be saved.
	 * @return Current measurement save settings.
	 * @throws RemoteException
	 */
	SaveSettings getSaveSettings() throws RemoteException;

	/**
	 * Returns the path to the last run of this measurement. This path is composed of the path in
	 * the MeasurementSaveSettings, with an additional subfolder added which indicates the time of the
	 * last execution of this measurement. Be aware that when running the server and the client on different
	 * computers, this path is relative to the server's file system.
	 * @return The path of the measurement on the server side. 
	 * @throws RemoteException
	 */
	String getLastMeasurementFolder() throws RemoteException;

	/**
	 * Returns the configuration of the measurement, or null if the configuration is unknown.
	 * @return Configuration of the measurement.
	 * @throws RemoteException
	 */
	MeasurementConfiguration getConfiguration() throws RemoteException;

	/**
	 * Sets the configuration of the measurement. The configuration should be such, that a new measurement should be possible to be created
	 * with it which has the same properties as the current measurement.
	 * @param configuration The configuration of the measurement, or null if the configuration should be set to unknown.
	 * @throws MeasurementRunningException
	 * @throws RemoteException
	 */
	void setConfiguration(MeasurementConfiguration configuration) throws MeasurementRunningException, RemoteException;
}
