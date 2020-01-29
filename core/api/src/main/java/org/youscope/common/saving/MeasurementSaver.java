/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.common.saving;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.image.ImageListener;
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
	 * @throws ComponentRunningException
	 * @throws RemoteException
	 */
	void setSaveSettings(SaveSettings saveSettings) throws ComponentRunningException, RemoteException;

	/**
	 * Returns the current settings how the measurement should be saved to the disk (e.g. in which folder, ...), or null if
	 * measurement should not be saved.
	 * @return Current measurement save settings.
	 * @throws RemoteException
	 */
	SaveSettings getSaveSettings() throws RemoteException;

	/**
	 * Returns an object containing the paths to the meta files of the last run of this measurement. 
	 * Be aware that when running the server and the client on different
	 * computers, these paths are relative to the server's file system.
	 * @return The paths of the measurement meta data on the server side. 
	 * @throws RemoteException
	 */
	MeasurementFileLocations getLastMeasurementFileLocations() throws RemoteException;
}
