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
package org.youscope.client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.youscope.addon.AddonException;
import org.youscope.addon.AddonMetadata;
import org.youscope.addon.AddonUI;
import org.youscope.addon.component.ComponentAddonFactory;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.addon.measurement.MeasurementAddonFactory;
import org.youscope.addon.postprocessing.PostProcessorAddonFactory;
import org.youscope.addon.skin.Skin;
import org.youscope.addon.skin.SkinFactory;
import org.youscope.addon.tool.ToolAddonFactory;
import org.youscope.addon.tool.ToolAddonUI;
import org.youscope.addon.tool.ToolMetadata;
import org.youscope.clientinterfaces.ClientAddonProvider;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.saving.MeasurementFileLocations;

class ClientAddonProviderImpl implements ClientAddonProvider
{
	private final List<ComponentAddonFactory> componentAddonFactories = new ArrayList<ComponentAddonFactory>(30);
	private final List<MeasurementAddonFactory> measurementAddonFactories = new ArrayList<MeasurementAddonFactory>(30);
	private final List<ToolAddonFactory> toolAddonFactories = new ArrayList<ToolAddonFactory>(30);
	private final List<SkinFactory> skinFactories = new ArrayList<SkinFactory>(5);
	private final List<PostProcessorAddonFactory> postProcessorAddonFactories = new ArrayList<PostProcessorAddonFactory>(30);
	private final List<ScriptEngineFactory> scriptEngineFactories = new ArrayList<ScriptEngineFactory>(5); 
	private static ClientAddonProviderImpl singleton = null;
	/**
	 * Use {@value ClientAddonProviderImpl#getProvider()}.
	 * @throws RemoteException
	 */
	private ClientAddonProviderImpl()
	{
		// Singleton. Use getProvider().
	}
	public static synchronized ClientAddonProviderImpl getProvider()
	{
		if(singleton != null)
			return singleton;
		singleton = new ClientAddonProviderImpl();
		
		// components
		Iterable<ComponentAddonFactory> componentAddonFactories = ServiceLoader.load(ComponentAddonFactory.class,
				ClientAddonProviderImpl.class.getClassLoader());
		for(ComponentAddonFactory factory : componentAddonFactories)
		{
			singleton.componentAddonFactories.add(factory);
		}
		
		// measurements
		Iterable<MeasurementAddonFactory> measurementAddonFactories = ServiceLoader.load(MeasurementAddonFactory.class,
				ClientAddonProviderImpl.class.getClassLoader());
		for(MeasurementAddonFactory factory : measurementAddonFactories)
		{
			singleton.measurementAddonFactories.add(factory);
		}
		
		// tools
		Iterable<ToolAddonFactory> toolAddonFactories = ServiceLoader.load(ToolAddonFactory.class,
				ClientAddonProviderImpl.class.getClassLoader());
		for(ToolAddonFactory factory : toolAddonFactories)
		{
			singleton.toolAddonFactories.add(factory);
		}
		
		// post processors
		Iterable<PostProcessorAddonFactory> postProcessorAddonFactories = ServiceLoader.load(PostProcessorAddonFactory.class,
				ClientAddonProviderImpl.class.getClassLoader());
		for(PostProcessorAddonFactory factory : postProcessorAddonFactories)
		{
			singleton.postProcessorAddonFactories.add(factory);
		}
		
		// look and feels
		Iterable<SkinFactory> skinFactories = ServiceLoader.load(SkinFactory.class,
				ClientAddonProviderImpl.class.getClassLoader());
		for(SkinFactory factory : skinFactories)
		{
			singleton.skinFactories.add(factory);
		}
		
		// script engines
		ScriptEngineManager mgr = new ScriptEngineManager(ClientAddonProviderImpl.class.getClassLoader());
        singleton.scriptEngineFactories.addAll(mgr.getEngineFactories());
		
		return singleton;
		
	}
	
	private ComponentAddonFactory getComponentAddonFactory(String typeIdentifier)
			throws AddonException {
		for(ComponentAddonFactory factory : componentAddonFactories)
		{
			if(factory.isSupportingTypeIdentifier(typeIdentifier))
				return factory;
		}
		throw new AddonException("No factory to create component addons for type identifer " + typeIdentifier + " exists.");
	}
	
	private MeasurementAddonFactory getMeasurementAddonFactory(String typeIdentifier)
			throws AddonException {
		for(MeasurementAddonFactory factory : measurementAddonFactories)
		{
			if(factory.isSupportingTypeIdentifier(typeIdentifier))
				return factory;
		}
		throw new AddonException("No factory to create component addons for type identifer " + typeIdentifier + " exists.");
	}
	@Override
	public ComponentAddonUI<?> createComponentUI(String typeIdentifier) throws AddonException 
	{
		return createComponentUI(typeIdentifier, Configuration.class);
	}
	@Override
	public <T extends Configuration> ComponentAddonUI<? extends T> createComponentUI(String typeIdentifier,
			Class<T> configurationClass)
					throws AddonException 
	{
		// try first a component
		ComponentAddonFactory factory;
		try
		{
			factory = getComponentAddonFactory(typeIdentifier);
		}
		catch(@SuppressWarnings("unused") AddonException e)
		{
			factory = null;
		}
		if(factory != null)
		{
			ComponentMetadata<?> metadata = factory.getComponentMetadata(typeIdentifier);
			if(metadata == null)
			{
				throw new AddonException("Metadata for configuration addon with configuration type " + typeIdentifier + " is null.");
			}
			Class<? extends Configuration> addonClass = metadata.getConfigurationClass();
			if(!configurationClass.isAssignableFrom(addonClass))
				throw new AddonException("Configuration addon with type identifier " + typeIdentifier + " creates configurations of class " + addonClass.getName()+" which are not subclasses of " + configurationClass.getName() + ".");
			@SuppressWarnings("unchecked")
			ComponentAddonUI<? extends T> result = (ComponentAddonUI<? extends T>) factory.createComponentUI(typeIdentifier, new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer());
			return result;
		}
		// Now, we try a measurement
		MeasurementAddonFactory measurementFactory;
		try
		{
			measurementFactory = getMeasurementAddonFactory(typeIdentifier);
		}
		catch(@SuppressWarnings("unused") AddonException e)
		{
			measurementFactory = null;
		}
		if(measurementFactory != null)
		{
			ComponentMetadata<?> metadata = measurementFactory.getComponentMetadata(typeIdentifier);
			if(metadata == null)
			{
				throw new AddonException("Metadata for configuration addon with configuration type " + typeIdentifier + " is null.");
			}
			Class<? extends Configuration> addonClass = metadata.getConfigurationClass();
			if(!configurationClass.isAssignableFrom(addonClass))
				throw new AddonException("Configuration addon with type identifier " + typeIdentifier + " creates configurations of class " + addonClass.getName()+" which are not subclasses of " + configurationClass.getName() + ".");
			@SuppressWarnings("unchecked")
			ComponentAddonUI<? extends T> result = (ComponentAddonUI<? extends T>) measurementFactory.createMeasurementUI(typeIdentifier, new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer());
			return result;
		}
		
		// Neither measurement nor component --> Error
		throw new AddonException("No factory to create component addons for type identifer " + typeIdentifier + " exists.");
	}
	@Override
	public List<String> getComponentTypeIdentifiers(Class<? extends Configuration> configurationClass) 
	{
		ArrayList<String> returnVal = new ArrayList<String>();
		for(ComponentAddonFactory addonFactory : componentAddonFactories) 
		{
			for(String algorithmID : addonFactory.getSupportedTypeIdentifiers())
			{
				ComponentMetadata<?> metadata;
				try {
					metadata = addonFactory.getComponentMetadata(algorithmID);
				} catch (@SuppressWarnings("unused") AddonException e) {
					continue;
				}
				if(metadata == null)
					continue;
				if(!configurationClass.isAssignableFrom(metadata.getConfigurationClass()))
						continue;
				returnVal.add(algorithmID);
			}
		}
		for(MeasurementAddonFactory addonFactory : measurementAddonFactories) 
		{
			for(String algorithmID : addonFactory.getSupportedTypeIdentifiers())
			{
				ComponentMetadata<?> metadata;
				try {
					metadata = addonFactory.getComponentMetadata(algorithmID);
				} catch (@SuppressWarnings("unused") AddonException e) {
					continue;
				}
				if(metadata == null)
					continue;
				if(!configurationClass.isAssignableFrom(metadata.getConfigurationClass()))
						continue;
				returnVal.add(algorithmID);
			}
		}
		return returnVal;
	}
	@Override
	public <T extends Configuration> List<ComponentMetadata<? extends T>> getComponentMetadata(
			Class<T> configurationClass) {
		ArrayList<ComponentMetadata<? extends T>> returnVal = new ArrayList<ComponentMetadata<? extends T>>();
		for(ComponentAddonFactory addonFactory : componentAddonFactories) 
		{
			for(String algorithmID : addonFactory.getSupportedTypeIdentifiers())
			{
				ComponentMetadata<?> metadata;
				try {
					metadata = addonFactory.getComponentMetadata(algorithmID);
				} catch (@SuppressWarnings("unused") AddonException e) {
					continue;
				}
				if(metadata == null)
					continue;
				if(!configurationClass.isAssignableFrom(metadata.getConfigurationClass()))
						continue;
				@SuppressWarnings("unchecked")
				ComponentMetadata<? extends T> temp = (ComponentMetadata<? extends T>) metadata;
				
				returnVal.add(temp);
			}
		}
		for(MeasurementAddonFactory addonFactory : measurementAddonFactories) 
		{
			for(String algorithmID : addonFactory.getSupportedTypeIdentifiers())
			{
				ComponentMetadata<?> metadata;
				try {
					metadata = addonFactory.getComponentMetadata(algorithmID);
				} catch (@SuppressWarnings("unused") AddonException e) {
					continue;
				}
				if(metadata == null)
					continue;
				if(!configurationClass.isAssignableFrom(metadata.getConfigurationClass()))
						continue;
				@SuppressWarnings("unchecked")
				ComponentMetadata<? extends T> temp = (ComponentMetadata<? extends T>) metadata;
				
				returnVal.add(temp);
			}
		}
		return returnVal;
	}
	@Override
	public <T extends Configuration> ComponentAddonUI<? extends T> createComponentUI(T configuration)
			throws AddonException, ConfigurationException {
		if(configuration == null)
			throw new NullPointerException();
		ComponentAddonUI<?> addonTemp = createComponentUI(configuration.getTypeIdentifier(), configuration.getClass());
		@SuppressWarnings("unchecked")
		ComponentAddonUI<? extends T> addon = (ComponentAddonUI<? extends T>) addonTemp;
		addonTemp.setConfiguration(configuration);
		return addon;
	}
	@Override
	public List<ComponentMetadata<?>> getComponentMetadata() {
		ArrayList<ComponentMetadata<?>> returnVal = new ArrayList<ComponentMetadata<?>>();
		for(ComponentAddonFactory addonFactory : componentAddonFactories) 
		{
			for(String algorithmID : addonFactory.getSupportedTypeIdentifiers())
			{
				ComponentMetadata<?> metadata;
				try {
					metadata = addonFactory.getComponentMetadata(algorithmID);
				} catch (@SuppressWarnings("unused") AddonException e) {
					continue;
				}
				if(metadata == null)
					continue;
				returnVal.add(metadata);
			}
		}
		for(MeasurementAddonFactory addonFactory : measurementAddonFactories) 
		{
			for(String algorithmID : addonFactory.getSupportedTypeIdentifiers())
			{
				ComponentMetadata<?> metadata;
				try {
					metadata = addonFactory.getComponentMetadata(algorithmID);
				} catch (@SuppressWarnings("unused") AddonException e) {
					continue;
				}
				if(metadata == null)
					continue;
				returnVal.add(metadata);
			}
		}
		return returnVal;
	}
	@Override
	public <T extends Configuration> ComponentAddonUI<T> createComponentUI(ComponentMetadata<T> metadata)
			throws AddonException {
		ComponentAddonUI<?> addon = createComponentUI(metadata.getTypeIdentifier());
		@SuppressWarnings("unchecked")
		ComponentAddonUI<T> returnVal = (ComponentAddonUI<T>) addon;
		return returnVal;
	}
	@Override
	public ComponentMetadata<?> getComponentMetadata(String typeIdentifier) throws AddonException {
		for(ComponentAddonFactory addonFactory : componentAddonFactories) 
		{
			if(addonFactory.isSupportingTypeIdentifier(typeIdentifier))
				return addonFactory.getComponentMetadata(typeIdentifier);
		}
		for(MeasurementAddonFactory addonFactory : measurementAddonFactories) 
		{
			if(addonFactory.isSupportingTypeIdentifier(typeIdentifier))
				return addonFactory.getComponentMetadata(typeIdentifier);
		}
		throw new AddonException("Component type identifier " + typeIdentifier +" no supported by any component addon factory.");
	}
	@Override
	public <T extends Configuration> ComponentMetadata<? extends T> getComponentMetadata(String typeIdentifier,
			Class<T> configurationClass) throws AddonException {
		ComponentMetadata<?> metadataRaw = getComponentMetadata(typeIdentifier);
		if(!configurationClass.isAssignableFrom(metadataRaw.getConfigurationClass()))
			throw new AddonException("Configuration with type identifier " + typeIdentifier + " is of class " + metadataRaw.getConfigurationClass().getName()+", which is not a subclass of " + configurationClass.getName()+".");
		@SuppressWarnings("unchecked")
		ComponentMetadata<? extends T> returnVal = (ComponentMetadata<? extends T>) metadataRaw;
		return returnVal;
	}
	@Override
	public List<String> getComponentTypeIdentifiers() {
		ArrayList<String> returnVal = new ArrayList<String>();
		for(ComponentAddonFactory addonFactory : componentAddonFactories) 
		{
			for(String algorithmID : addonFactory.getSupportedTypeIdentifiers())
			{
				returnVal.add(algorithmID);
			}
		}
		for(MeasurementAddonFactory addonFactory : measurementAddonFactories) 
		{
			for(String algorithmID : addonFactory.getSupportedTypeIdentifiers())
			{
				returnVal.add(algorithmID);
			}
		}
		return returnVal;
	}
	@Override
	public AddonUI<? extends AddonMetadata> createPostProcessorUI(String typeIdentifier, MeasurementFileLocations measurementFileLocations) throws AddonException {
		for(PostProcessorAddonFactory addonFactory : postProcessorAddonFactories) 
		{
			if(addonFactory.isSupportingTypeIdentifier(typeIdentifier))
				return addonFactory.createPostProcessorUI(typeIdentifier, new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer(), measurementFileLocations);
		}
		throw new AddonException("Microplate type with type identifier " + typeIdentifier+" is unknown.");
	}
	@Override
	public <T extends AddonMetadata> AddonUI<T> createPostProcessorUI(T metadata, MeasurementFileLocations measurementFileLocations) throws AddonException {
		
		@SuppressWarnings("unchecked")
		AddonUI<T> temp = (AddonUI<T>) createPostProcessorUI(metadata.getTypeIdentifier(), measurementFileLocations);
		return temp;
	}
	@Override
	public List<String> getPostProcessorTypeIdentifiers() {
		ArrayList<String> returnVal = new ArrayList<String>();
		for(PostProcessorAddonFactory addonFactory : postProcessorAddonFactories) 
		{
			for(String algorithmID : addonFactory.getSupportedTypeIdentifiers())
			{
				returnVal.add(algorithmID);
			}
		}
		return returnVal;
	}
	@Override
	public List<AddonMetadata> getPostProcessorMetadata() {
		ArrayList<AddonMetadata> returnVal = new ArrayList<AddonMetadata>();
		for(PostProcessorAddonFactory addonFactory : postProcessorAddonFactories) 
		{
			for(String algorithmID : addonFactory.getSupportedTypeIdentifiers())
			{
				try {
					returnVal.add(addonFactory.getPostProcessorMetadata(algorithmID));
				} catch (AddonException e) {
					ClientSystem.err.println("Metadata for post processor with type identifier "+algorithmID+" cannot be constructed. Skipping this post processor.", e);
				}
			}
		}
		return returnVal;
	}
	public List<AddonMetadata> getSkinMetadata() {
		ArrayList<AddonMetadata> returnVal = new ArrayList<AddonMetadata>();
		for(SkinFactory addonFactory : skinFactories) 
		{
			for(String algorithmID : addonFactory.getSupportedTypeIdentifiers())
			{
				try {
					returnVal.add(addonFactory.getMetadata(algorithmID));
				} catch (AddonException e) {
					ClientSystem.err.println("Metadata for look and feel with type identifier "+algorithmID+" cannot be constructed. Skipping this post processor.", e);
				}
			}
		}
		return returnVal;
	}
	
	public AddonMetadata getSkinMetadata(String typeIdentifier) throws AddonException {
		for(SkinFactory addonFactory : skinFactories) 
		{
			if(addonFactory.isSupportingTypeIdentifier(typeIdentifier))
				return addonFactory.getMetadata(typeIdentifier);
		}
		throw new AddonException("Look and feel with type identifier " + typeIdentifier+" is unknown.");
	}
	@Override
	public AddonMetadata getPostProcessorMetadata(String typeIdentifier) throws AddonException {
		for(PostProcessorAddonFactory addonFactory : postProcessorAddonFactories) 
		{
			if(addonFactory.isSupportingTypeIdentifier(typeIdentifier))
				return addonFactory.getPostProcessorMetadata(typeIdentifier);
		}
		throw new AddonException("Post processor with type identifier " + typeIdentifier+" is unknown.");
	}
	@Override
	public ToolAddonUI createToolUI(String typeIdentifier) throws AddonException {
		for(ToolAddonFactory addonFactory : toolAddonFactories) 
		{
			if(addonFactory.isSupportingTypeIdentifier(typeIdentifier))
				return addonFactory.createToolUI(typeIdentifier, new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer());
		}
		throw new AddonException("Tool with type identifier " + typeIdentifier+" is unknown.");
	}
	@Override
	public ToolAddonUI createToolUI(ToolMetadata metadata) throws AddonException {
		return createToolUI(metadata.getTypeIdentifier());
	}
	
	public Skin createSkin(String typeIdentifier) throws AddonException {
		for(SkinFactory addonFactory : skinFactories) 
		{
			if(addonFactory.isSupportingTypeIdentifier(typeIdentifier))
				return addonFactory.createSkin(typeIdentifier);
		}
		throw new AddonException("Look and feel with type identifier " + typeIdentifier+" is unknown.");
	}
	public Skin createSkin(AddonMetadata metadata) throws AddonException {
		return createSkin(metadata.getTypeIdentifier());
	}
	
	@Override
	public List<String> getToolTypeIdentifiers() {
		ArrayList<String> returnVal = new ArrayList<String>();
		for(ToolAddonFactory addonFactory : toolAddonFactories) 
		{
			for(String algorithmID : addonFactory.getSupportedTypeIdentifiers())
			{
				returnVal.add(algorithmID);
			}
		}
		return returnVal;
	}
	
	public List<String> getSkinTypeIdentifiers() {
		ArrayList<String> returnVal = new ArrayList<String>();
		for(SkinFactory addonFactory : skinFactories) 
		{
			for(String algorithmID : addonFactory.getSupportedTypeIdentifiers())
			{
				returnVal.add(algorithmID);
			}
		}
		return returnVal;
	}
	@Override
	public List<ToolMetadata> getToolMetadata() {
		ArrayList<ToolMetadata> returnVal = new ArrayList<ToolMetadata>();
		for(ToolAddonFactory addonFactory : toolAddonFactories) 
		{
			for(String typeIdentifier : addonFactory.getSupportedTypeIdentifiers())
			{
				try {
					returnVal.add(addonFactory.getToolMetadata(typeIdentifier));
				} catch (AddonException e) {
					ClientSystem.err.println("Metadata for tool with type identifier "+typeIdentifier+" cannot be constructed. Skipping this tool.", e);
				}
			}
		}
		return returnVal;
	}
	@Override
	public ToolMetadata getToolMetadata(String typeIdentifier) throws AddonException {
		for(ToolAddonFactory addonFactory : toolAddonFactories) 
		{
			if(addonFactory.isSupportingTypeIdentifier(typeIdentifier))
				return addonFactory.getToolMetadata(typeIdentifier);
		}
		throw new AddonException("Tool with type identifier " + typeIdentifier+" is unknown.");
	}
	@Override
	public List<ScriptEngineFactory> getScriptEngineFactories() {
		ArrayList<ScriptEngineFactory> copy = new ArrayList<ScriptEngineFactory>(scriptEngineFactories.size());
		copy.addAll(scriptEngineFactories);
		return copy;
	}
	@Override
	public ScriptEngineFactory getScriptEngineFactory(String typeIdentifier) throws AddonException {
		for(ScriptEngineFactory factory : scriptEngineFactories)
    	{
    		if(factory.getEngineName().equals(typeIdentifier))
    			return factory;
    	}
		throw new AddonException("Script engine with type identifier " + typeIdentifier+" is unknown.");
	}
}
