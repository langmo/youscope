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
import java.util.HashMap;
import java.util.ServiceLoader;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactory;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.common.Component;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;
import org.youscope.serverinterfaces.ComponentProvider;
import org.youscope.serverinterfaces.ConstructionContext;

class ComponentProviderImpl extends UnicastRemoteObject implements ComponentProvider {

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -7178023950885081129L;

	private static final HashMap<String, ComponentAddonFactory> supportedFactories = new HashMap<String, ComponentAddonFactory>(50);
	private static volatile boolean initialized = false;
	
	private static synchronized void initialize(boolean force)
	{
		if(initialized && !force)
			return;
		supportedFactories.clear();
		ServiceLoader<ComponentAddonFactory> nativeFactories =
                ServiceLoader.load(ComponentAddonFactory.class,
                		ComponentProviderImpl.class.getClassLoader());
		for(ComponentAddonFactory nativeFactory : nativeFactories)
		{
			for(String typeIdentifier : nativeFactory.getSupportedTypeIdentifiers())
			{
				supportedFactories.put(typeIdentifier, nativeFactory);
			}
		}
		initialized = true; 
	}
	
	private final ConstructionContext constructionContext;
	/**
	 * Constructor.
	 * The construction context used to create the components.
	 * @param constructionContext 
	 * @throws RemoteException
	 */
	public ComponentProviderImpl(ConstructionContext constructionContext) throws RemoteException 
	{
		this.constructionContext = constructionContext;
	}
	@Override
	public Component createComponent(PositionInformation positionInformation, Configuration configuration) throws ComponentCreationException, ConfigurationException
	{
		if(configuration == null)
			throw new ComponentCreationException("Configuration is null.");
		if(positionInformation == null)
			throw new ComponentCreationException("Position information is null.");

		initialize(false);
		ComponentAddonFactory factory = supportedFactories.get(configuration.getTypeIdentifier());
		if(factory == null)
		{
			// try to reload all factories...
			initialize(true);
			factory = supportedFactories.get(configuration.getTypeIdentifier());
			if(factory == null)
				throw new ComponentCreationException("Configuration type identifier " + configuration.getTypeIdentifier() + " unknown.");
		}
		try {
			return factory.createComponent(positionInformation, configuration, constructionContext);
		} 
		catch (AddonException e) 
		{
			throw new ComponentCreationException("Addon providing the component with type identifier " + configuration.getTypeIdentifier() + " signaled an error during component construction.", e);
		}
	}
	
	@Override
	public <T extends Component> T createComponent(PositionInformation positionInformation, Configuration configuration, Class<T> componentKind) throws ComponentCreationException, ConfigurationException
	{
		if(configuration == null)
			throw new ComponentCreationException("Configuration is null.");
		if(positionInformation == null)
			throw new ComponentCreationException("Position information is null.");
		if(componentKind == null)
			throw new ComponentCreationException("Component kind is null.");
		Component component = createComponent(positionInformation, configuration);
		try
		{
			return componentKind.cast(component);
		}
		catch(ClassCastException e)
		{
			throw new ComponentCreationException("Created component with type identifier " + configuration.getTypeIdentifier() + " is of class " + component.getClass().getName()+", which is not a sub-class of "+componentKind.getName() + ".", e);
		}
	}
	@Override
	public Job createJob(PositionInformation positionInformation, JobConfiguration jobConfiguration)
			throws ComponentCreationException, ConfigurationException 
	{
		return createComponent(positionInformation, jobConfiguration, Job.class);
	}
	
	@Override
	public <T extends Component> T createComponent(PositionInformation positionInformation, String typeIdentifier,
			Class<T> componentInterface) throws ComponentCreationException {
		if(componentInterface == null)
			throw new ComponentCreationException("Component interface is null.");
		Component component = createComponent(positionInformation, typeIdentifier);
		if(!componentInterface.isInstance(component))
			throw new ComponentCreationException("Component with type identifier " + typeIdentifier + " has interface " + component.getClass().getName() + ", which is not a sub-class of " + componentInterface.getName()+".");
		return componentInterface.cast(component);
	}
	@Override
	public Component createComponent(PositionInformation positionInformation, String typeIdentifier)
			throws ComponentCreationException {
		if(positionInformation == null)
			throw new ComponentCreationException("Position information is null.");
		if(typeIdentifier == null)
			throw new ComponentCreationException("Type identifier is null.");
		initialize(false);
		ComponentAddonFactory factory = supportedFactories.get(typeIdentifier);
		if(factory == null)
		{
			// try to reload all factories...
			initialize(true);
			factory = supportedFactories.get(typeIdentifier);
			if(factory == null)
				throw new ComponentCreationException("Configuration type identifier " + typeIdentifier + " unknown.");
		}
		ComponentMetadata<?> metadata;
		try
		{
			metadata = factory.getComponentMetadata(typeIdentifier);
		}
		catch(AddonException e)
		{
			throw new ComponentCreationException("Factory "+factory.getClass().getName()+" does not provide metadata for configuration with type identifier " + typeIdentifier + ".", e);
		}					
		
		Configuration configuration;
		try {
			configuration = metadata.getConfigurationClass().getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			throw new ComponentCreationException("Could not create default configuration for component with type identifier " + typeIdentifier + ".", e);
		}
		Component component;
		try {
			component = factory.createComponent(positionInformation, configuration, constructionContext);
		} 
		catch (ConfigurationException e) 
		{
			throw new ComponentCreationException("Default configuration for component with type identifier " + typeIdentifier + " is not a valid configuration.", e);
		} 
		catch (AddonException e) 
		{
			throw new ComponentCreationException("Error while creating component with type identifier " + typeIdentifier + " by initializing it with default configuration.", e);
		}
		return component;
	}
	@Override
	public <T extends Job> T createJob(PositionInformation positionInformation, String typeIdentifier,
			Class<T> jobInterface) throws ComponentCreationException 
	{
		return createComponent(positionInformation, typeIdentifier, jobInterface);
	}
	@Override
	public Job createJob(PositionInformation positionInformation, String typeIdentifier)
			throws ComponentCreationException, RemoteException {
		return createComponent(positionInformation, typeIdentifier, Job.class);
	}
	@Override
	public <T extends Job> T createJob(PositionInformation positionInformation, JobConfiguration jobConfiguration,
			Class<T> jobInterface) throws ComponentCreationException, ConfigurationException, RemoteException {
		return createComponent(positionInformation, jobConfiguration, jobInterface);
	}
	
}
