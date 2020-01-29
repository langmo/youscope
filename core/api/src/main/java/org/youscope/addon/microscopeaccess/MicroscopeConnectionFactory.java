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
package org.youscope.addon.microscopeaccess;

/**
 * This interface is intended to be implemented by plug-ins to establish a connection to the microscope drivers.
 * @author langmo
 * 
 */
public interface MicroscopeConnectionFactory
{
	/**
	 * Returns a new interface through which the drivers of the microscope can be accessed.
	 * @param microscopeConnectionID The identifier of the microscope connection type.
	 * @param driverPath An URL to the local folder where the drivers are stored, or null if the respective connection type does not need such a folder.
	 * 
	 * @return New microscope connection.
	 * @throws MicroscopeConnectionException Thrown if error in connecting to microManager occured.
	 */
	MicroscopeInternal createMicroscopeConnection(String microscopeConnectionID, String driverPath) throws MicroscopeConnectionException;

	/**
	 * Returns a list of all types of microscope connections the plug-in supports.
	 * 
	 * @return List of supported microscope connection types.
	 */
	String[] getSupportedMicroscopeConnectionIDs();

	/**
	 * Returns true if the plug-in supports the given microscope connection type, false otherwise.
	 * @param microscopeConnectionID The identifier of the microscope connection type.
	 * @return True if the plug-in supports the given connection type, false otherwise.
	 */
	boolean supportsMicroscopeConnectionID(String microscopeConnectionID);
	
	/**
	 * Returns true if the given connection type needs an URL to the folder where the microscope drivers are, false if such a folder is not needed.
	 * @param microscopeConnectionID The identifier of the microscope connection type.
	 * @return True if a folder location is needed.
	 */
	boolean needsDriverPath(String microscopeConnectionID);
	
	/**
	 * Returns a human readable description of the plug-in. If a driver path is needed by this plug-in, instructions should be given
	 * to the user of how to precicely define this location. 
	 * @param microscopeConnectionID The identifier of the microscope connection type.
	 * @return Human readable description.
	 */
	String getMicroscopeConnectionDescription(String microscopeConnectionID);
	
	/**
	 * Returns a short (one line) human readable description of the plug-in.
	 * @param microscopeConnectionID The identifier of the microscope connection type.
	 * @return Human readable description.
	 */
	String getShortMicroscopeConnectionDescription(String microscopeConnectionID);
}
