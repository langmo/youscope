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
package org.youscope.clientinterfaces;

import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementConfiguration;


/**
 * Interface through which an addon can access the client.
 * @author langmo
 *
 */
public interface YouScopeClient
{
	
	/** 
	 * Returns an interface through which an addon can save properties as well as access the properties of the YouScope client.
	 * @return Interface to save and load properties.
	 */
	PropertyProvider getPropertyProvider();
	
	/** 
	 * Returns an interface through which an addon can querry and set default and mandatory metadata for measurements
	 * @return Interface to get and set measurement metadata.
	 */
	MetadataDefinitionManager getMeasurementMetadataProvider();
	/**
	 * Creates a new content window. The content window is usually
	 * either a frame or an internal frame (default).  
	 * @return New Frame.
	 */
	YouScopeFrame createFrame();
	
	/**
     * Returns a list of the last saved mesurements.
     * 
     * @return The list of measurements.
     */
    MeasurementConfiguration[] getLastSavedMeasurements();
    
	/**
	 * Notifies the client that an error occurred, such that the client can notify the user in the
	 * client specific way.
	 * @param message Description of the error.
	 * @param error The error.
	 */
	void sendError(String message, Throwable error);
	
	/**
	 * Notifies the client that an error occurred, such that the client can notify the user in the
	 * client specific way.
	 * @param message Description of the error.
	 */
	void sendError(String message);
	
	/**
	 * Sends a human readable message to the client, which (may) be displayed to the user in the
	 * client specific way.
	 * @param message The message to send to the client.
	 */
	void sendMessage(String message);
	
	/**
	 * Returns true if the microscope is connected to this computer, and false, if the microscope is on a different computer.
	 * @return True if the microscope is connected to this computer, and false, if the microscope is on a different computer.
	 */
	boolean isLocalServer();
	
	/**
	 * Opens the default editor to manipulate the measurement configuration.
	 * @param configuration The configuration of the measurement to edit.
	 * @return True if editor could be opened, wrong otherwise.
	 */
	boolean editMeasurement(MeasurementConfiguration configuration);
	
	/**
	 * Initializes the measurement and shows the measurement control frame.
	 * @param configuration The configuration of the measurement.
	 * @return The initialized measurement, or null if not successful.
	 */
	Measurement initializeMeasurement(MeasurementConfiguration configuration);
	
	/**
	 * Opens the measurement control frame for a measurement.
	 * @param measurement The measurement to control.
	 * @return True if successful, false otherwise.
	 */
	boolean initializeMeasurement(Measurement measurement);
	
	/**
	 * Returns a class with which client addons can be constructed.
	 * @return client addon provider.
	 */
	ClientAddonProvider getAddonProvider();
}
