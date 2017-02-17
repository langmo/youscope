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
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.UUID;

import org.youscope.common.MeasurementContext;

/**
 * A simple implementation of a measurement context. This implementation can be used to initialize and call measurement components
 * without the need to construct an actual measurement, e.g. during a configuration of a component to show the expected outcome given the current configuration.
 * Note, that YouScope uses a different implementation during the execution of a measurement.
 * @author Moritz Lang
 *
 */
public class SimpleMeasurementContext extends UnicastRemoteObject implements MeasurementContext
{
	private final HashMap<String, Serializable> properties = new HashMap<String, Serializable>();
	
	private final UUID uniqueIdentifier = UUID.randomUUID();
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 8098035348098383719L;
	final long startTime;
	/**
	 * Constructor.
	 * @throws RemoteException
	 */
	public SimpleMeasurementContext() throws RemoteException
	{
		super();
		startTime = System.currentTimeMillis();
	}
	
    @Override
    public void setProperty(String identifier, Serializable property)
    {
        properties.put(identifier, property);
    }
    
    @Override
    public String[] getPropertyIdentifiers()
    {
        return properties.keySet().toArray(new String[0]);
    }
    
    @Override
    public Serializable getProperty(String identifier)
    {
        return properties.get(identifier);
    }
    
    @Override
    public <T extends Serializable> T getProperty(String identifier, Class<T> propertyType)
    {
    	Serializable property = getProperty(identifier);
        if (property == null)
            return null;
        if (propertyType.isInstance(property))
            return propertyType.cast(property);
		return null;
    }

    @Override
    public void notifyMeasurementStructureChanged() throws RemoteException
    {
        // do nothing
    }

	@Override
	public UUID getMeasurementUUID() throws RemoteException {
		return uniqueIdentifier;
	}

	@Override
	public long getMeasurementRuntime() throws RemoteException {
		return System.currentTimeMillis() - startTime;
	}

}
