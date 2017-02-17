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
package org.youscope.common.resource;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.UUID;

import org.youscope.common.MeasurementContext;
import org.youscope.common.MessageListener;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;

/**
 * Adapter to simplify resource creation.
 * @author Moritz Lang
 *
 * @param <C>
 */
public abstract class ResourceAdapter<C extends ResourceConfiguration> extends UnicastRemoteObject implements Resource
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 5664301234426356788L;
	private final C configuration;
	private final String typeIdentifier;
	private final ArrayList<MessageListener> messageWriters = new ArrayList<MessageListener>();
	private boolean initialized = false;
	private final PositionInformation positionInformation;
	private String name = null;
	private final String defaultName;
	private final UUID uniqueID = UUID.randomUUID();
	/**
	 * Constructor.
	 * @param positionInformation logical position in measurement.
	 * @param configuration Configuration of resource.
	 * @param typeIdentifier type identifier of resource.
	 * @param configurationClass
	 * @param defaultName
	 * @throws RemoteException 
	 * @throws ConfigurationException
	 */
	public ResourceAdapter(PositionInformation positionInformation, C configuration, String typeIdentifier, Class<C> configurationClass, String defaultName) throws RemoteException, ConfigurationException
	{
		if(defaultName == null || defaultName.length() < 3)
			throw new ConfigurationException("The default name must be at least three characters long.");
		if(positionInformation == null)
			throw new ConfigurationException("Position information is null.");
		if(configuration == null)
			throw new ConfigurationException("Configuration is null.");
		if(!configurationClass.isAssignableFrom(configuration.getClass()))
			throw new ConfigurationException("Configuration is of class "+configuration.getClass().getName()+", while required class is "+configurationClass.getName()+".");
		this.configuration = configurationClass.cast(configuration);
		this.typeIdentifier = configuration.getTypeIdentifier();
		this.positionInformation = positionInformation;
		this.defaultName = defaultName;
	}
    @Override	
	public void addMessageListener(MessageListener writer) throws RemoteException
	{
		synchronized(messageWriters)
		{
			messageWriters.add(writer);
		}
	}

    @Override
	public void removeMessageListener(MessageListener writer) throws RemoteException
	{
		synchronized(messageWriters)
		{
			messageWriters.remove(writer);
		}
	}
	
    /**
     * Sends a message to all registered listeners.
     * @param message Message to send.
     */
	protected void sendMessage(String message)
	{
		synchronized(messageWriters)
		{
			for(int i=0; i<messageWriters.size(); i++)
			{
				try {
					messageWriters.get(i).sendMessage(message);
				} 
				catch (@SuppressWarnings("unused") RemoteException e) 
				{
					messageWriters.remove(i);
					i--;
				}
			}
		}
	}

	/**
     * Sends an error message to all registered listeners.
     * @param message Error message to send.
     * @param error The error which occurred.
     */
	protected void sendErrorMessage(String message, Throwable error)
	{
		synchronized(messageWriters)
		{
			for(int i=0; i<messageWriters.size(); i++)
			{
				try {
					messageWriters.get(i).sendErrorMessage(message, error);
				} 
				catch (@SuppressWarnings("unused") RemoteException e) 
				{
					messageWriters.remove(i);
					i--;
				}
			}
		}
	}

	@Override
	public String getTypeIdentifier()
	{
		return typeIdentifier;
	}


	@Override
	public void initialize(MeasurementContext measurementContext) throws ResourceException, RemoteException
	{
		if(isInitialized())
			throw new ResourceException("Resource "+getName()+" must not be initialized twice. Call uninitialize before initializing a second time.");
		initialized = true;
	}


	@Override
	public void uninitialize(MeasurementContext measurementContext) throws ResourceException, RemoteException
	{
		initialized = false;
	}


	@Override
	public boolean isInitialized() throws RemoteException
	{
		return initialized;
	}
	
	protected C getConfiguration()
	{
		return configuration;
	}
	
	protected void assertInitialized() throws ResourceException
	{
		if(!initialized)
			throw new ResourceException("Resource is not initialized, yet.");
	}
	@Override
	public PositionInformation getPositionInformation() throws RemoteException
	{
		return positionInformation;
	}
	
	private  String getDefaultName()
	{
		return defaultName;
	}
	
	@Override
	public String getName() throws RemoteException
	{
		if(name == null)
			return getDefaultName();
		return name;
	}
	@Override
	public void setName(String name) throws RemoteException
	{
		this.name = name;
	}
	@Override
	public UUID getUUID() throws RemoteException {
		return uniqueID;
	}
}
