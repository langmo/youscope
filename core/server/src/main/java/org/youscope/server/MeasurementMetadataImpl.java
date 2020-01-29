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
package org.youscope.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

import org.youscope.common.MetadataProperty;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.measurement.MeasurementMetadata;
import org.youscope.common.util.ConfigurationTools;


class MeasurementMetadataImpl extends UnicastRemoteObject implements MeasurementMetadata 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -8325879898948849196L;
	/**
	 * Metadata of the measurement, like temperature, species and similar.
	 */
	private final HashMap<String, String> metadataProperties = new HashMap<>();
	/**
	 * Human readable description of the measurement.
	 */
	private String description = "";
	
	private MeasurementConfiguration measurementConfiguration = null;
	
	public MeasurementMetadataImpl() throws RemoteException 
	{
		// do nothing.
	}
	/**
	 * Returns the value of the metadata property with the given name, or null if this property does not exist.
	 * @param propertyName The name of the metadata property.
	 * @return Value of the property, or null.
	 * @throws IllegalArgumentException Thrown if propertyName is null or empty.
	 */
	@Override
	public String getMetadataPropertyValue(String propertyName) throws IllegalArgumentException
	{
		if(propertyName == null || propertyName.isEmpty())
			throw new IllegalArgumentException();
		synchronized(metadataProperties)
		{
			return metadataProperties.get(propertyName);
		}
	}
	
	/**
	 * Returns a human readable free-form description of the measurement, typically set by the user.
	 * Initially empty string.
	 * @return Non-null description of the measurement.
	 */
	@Override
	public String getDescription()
	{
		return description;
	}
	/**
	 * Sets a human readable free-form description of the measurement typically provided by the user.
	 * Set to null to delete description.
	 * @param description Description of measurement, or null.
	 */
	@Override
	public void setDescription(String description)
	{
		this.description = description == null ? "" : description;
	}
	
	/**
	 * Returns the metadata property with the given name, or null if this property does not exist.
	 * @param propertyName The name of the metadata property.
	 * @return Metadata property with given name, or null.
	 * @throws IllegalArgumentException Thrown if propertyName is null or empty.
	 */
	@Override
	public MetadataProperty getMetadataProperty(String propertyName) throws IllegalArgumentException
	{
		String value = getMetadataPropertyValue(propertyName);
		return value == null ? null : new MetadataProperty(propertyName, value);
	}
	
	
	/**
	 * Returns all defined metadata properties.
	 * @return List of defined metadata properties.
	 */
	@Override
	public MetadataProperty[] getMetadataProperties()
	{
		synchronized(metadataProperties)
		{
			ArrayList<MetadataProperty> result = new ArrayList<>(metadataProperties.size());
			for(Entry<String, String> entry : metadataProperties.entrySet())
			{
				result.add(new MetadataProperty(entry.getKey(), entry.getValue()));
			}
			Collections.sort(result);
			return result.toArray(new MetadataProperty[result.size()]);
		}
	}
	
	/**
	 * Sets the metadata properties of this measurement. Removes all metadata properties defined before.
	 * @param properties Properties to set.
	 * @throws IllegalArgumentException thrown if properties or one element of properties is null.
	 */
	@Override
	public void setMetadataProperties(MetadataProperty[] properties) throws IllegalArgumentException
	{
		if(properties == null)
			throw new IllegalArgumentException();
		for(MetadataProperty property : properties)
		{
			if(property == null)
				throw new IllegalArgumentException();
		}
		synchronized(metadataProperties)
		{
			metadataProperties.clear();
			for(MetadataProperty property : properties)
			{
				metadataProperties.put(property.getName(), property.getValue());
			}
		}
	}
	
	/**
	 * Sets the metadata property to the provided property. If property with the same name already exists, it is overwritten.
	 * @param property Property to set.
	 * @throws IllegalArgumentException thrown if property is null
	 */
	@Override
	public void setMetadataProperty(MetadataProperty property) throws IllegalArgumentException
	{
		if(property == null)
			throw new IllegalArgumentException();
		synchronized(metadataProperties)
		{
			metadataProperties.put(property.getName(), property.getValue());
		}
	}
	
	/**
	 * Sets the value of the metadata property with the given name.
	 * @param propertyName The name of the metadata property.
	 * @param propertyValue The value of the metadata property.
	 * @throws IllegalArgumentException thrown if either propertyName or propertyValue are null, or if propertyName is empty.
	 */
	@Override
	public void setMetadataProperty(String propertyName, String propertyValue) throws IllegalArgumentException
	{
		if(propertyName == null || propertyName.isEmpty() || propertyValue == null)
			throw new IllegalArgumentException();
		synchronized(metadataProperties)
		{
			metadataProperties.put(propertyName, propertyValue);
		}
	}
	/**
	 * Removes all metadata properties.
	 */
	@Override
	public void clearMetadataProperties()
	{
		synchronized(metadataProperties)
		{
			metadataProperties.clear();
		}
	}
	
	/**
	 * Deletes the metadata property with the given name. Does nothing if the property does not exist.
	 * @param propertyName The name of the metadata property.
	 * @return True if property was deleted, false if property did not exist.
	 * @throws IllegalArgumentException Thrown if propertyName is null or empty.
	 */
	@Override
	public boolean deleteMetadataProperty(String propertyName) throws IllegalArgumentException
	{
		if(propertyName == null || propertyName.isEmpty())
			throw new IllegalArgumentException();
		synchronized(metadataProperties)
		{
			return metadataProperties.remove(propertyName) != null;
		}
	}
	@Override
	public MeasurementConfiguration getConfiguration() throws ConfigurationException
	{
		if(measurementConfiguration == null)
			return null;
		return ConfigurationTools.deepCopy(measurementConfiguration, MeasurementConfiguration.class);
	}
	@Override
	public void setConfiguration(MeasurementConfiguration configuration)
			throws ConfigurationException{
		this.measurementConfiguration = configuration == null ? null : ConfigurationTools.deepCopy(configuration, MeasurementConfiguration.class);
	}
}
