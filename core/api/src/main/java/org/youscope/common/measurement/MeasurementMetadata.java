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
package org.youscope.common.measurement;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

import org.youscope.common.MetadataProperty;
import org.youscope.common.configuration.ConfigurationException;

/**
 * Interface providing functions to query or set the metadata associated to a {@link Measurement}.
 * @author Moritz Lang
 *
 */
public interface MeasurementMetadata extends Remote 
{
	/**
	 * Returns the configuration of the measurement, or null if the configuration is unknown (e.g. because the measurement was constructed directly).
	 * @return Configuration of the measurement.
	 * @throws RemoteException
	 * @throws ConfigurationException Thrown if configuration could not be deep cloned, probably because it or a sub-configuration do not correctly implement {@link Serializable}.
	 */
	MeasurementConfiguration getConfiguration() throws RemoteException, ConfigurationException;

	/**
	 * Sets the configuration of the measurement. The configuration should be such, that a new measurement should be possible to be created
	 * with it which has the same properties as the current measurement.
	 * @param configuration The configuration of the measurement, or null if the configuration should be set to unknown.
	 * @throws RemoteException
	 * @throws ConfigurationException Thrown if configuration could not be deep cloned, probably because it or a sub-configuration do not correctly implement {@link Serializable}.
	 */
	void setConfiguration(MeasurementConfiguration configuration) throws RemoteException, ConfigurationException;
	
	/**
	 * Returns the value of the metadata property with the given name, or null if this property does not exist.
	 * @param propertyName The name of the metadata property.
	 * @return Value of the property, or null.
	 * @throws IllegalArgumentException Thrown if propertyName is null or empty.
	 * @throws RemoteException 
	 */
	public String getMetadataPropertyValue(String propertyName) throws IllegalArgumentException, RemoteException;
	
	/**
	 * Returns a human readable free-form description of the measurement, typically set by the user.
	 * Initially empty string.
	 * @return Non-null description of the measurement.
	 * @throws RemoteException 
	 */
	public String getDescription() throws RemoteException;
	/**
	 * Sets a human readable free-form description of the measurement typically provided by the user.
	 * Set to null to delete description.
	 * @param description Description of measurement, or null.
	 * @throws RemoteException 
	 */
	public void setDescription(String description) throws RemoteException;
	
	/**
	 * Returns the metadata property with the given name, or null if this property does not exist.
	 * @param propertyName The name of the metadata property.
	 * @return Metadata property with given name, or null.
	 * @throws IllegalArgumentException Thrown if propertyName is null or empty.
	 * @throws RemoteException 
	 */
	public MetadataProperty getMetadataProperty(String propertyName) throws IllegalArgumentException, RemoteException;
	
	
	/**
	 * Returns all defined metadata properties.
	 * @return List of defined metadata properties.
	 * @throws RemoteException 
	 */
	public MetadataProperty[] getMetadataProperties() throws RemoteException;
	
	/**
	 * Sets the metadata properties of this measurement. Removes all metadata properties defined before.
	 * @param properties Properties to set.
	 * @throws IllegalArgumentException thrown if properties or one element of properties is null.
	 * @throws RemoteException 
	 * @see #clearMetadataProperties()
	 */
	public void setMetadataProperties(MetadataProperty[] properties) throws IllegalArgumentException, RemoteException;
	
	/**
	 * Sets the metadata property to the provided property. If property with the same name already exists, it is overwritten.
	 * @param property Property to set.
	 * @throws IllegalArgumentException thrown if property is null
	 * @throws RemoteException 
	 */
	public void setMetadataProperty(MetadataProperty property) throws IllegalArgumentException, RemoteException;
	
	/**
	 * Sets the value of the metadata property with the given name.
	 * @param propertyName The name of the metadata property.
	 * @param propertyValue The value of the metadata property.
	 * @throws IllegalArgumentException thrown if either propertyName or propertyValue are null, or if propertyName is empty.
	 * @throws RemoteException 
	 */
	public void setMetadataProperty(String propertyName, String propertyValue) throws IllegalArgumentException, RemoteException;
	/**
	 * Removes all metadata properties.
	 * @throws RemoteException 
	 */
	public void clearMetadataProperties() throws RemoteException;
	
	/**
	 * Deletes the metadata property with the given name. Does nothing if the property does not exist.
	 * @param propertyName The name of the metadata property.
	 * @return True if property was deleted, false if property did not exist.
	 * @throws IllegalArgumentException Thrown if propertyName is null or empty.
	 * @throws RemoteException 
	 */
	public boolean deleteMetadataProperty(String propertyName) throws IllegalArgumentException, RemoteException;
}
