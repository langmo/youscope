package org.youscope.client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactory;
import org.youscope.addon.component.ComponentAddonUI;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.clientinterfaces.ClientAddonProvider;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;

class ClientAddonProviderImpl implements ClientAddonProvider
{
	private final List<ComponentAddonFactory> configurationAddonFactories = new ArrayList<ComponentAddonFactory>(30);
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
		Iterable<ComponentAddonFactory> factories = ServiceLoader.load(ComponentAddonFactory.class,
				ClientAddonProviderImpl.class.getClassLoader());
		for(ComponentAddonFactory factory : factories)
		{
			singleton.configurationAddonFactories.add(factory);
		}
		return singleton;
		
	}
	private List<ComponentAddonFactory> getConfigurationAddonFactories()
	{
		return new ArrayList<ComponentAddonFactory>(configurationAddonFactories);
	}
	
	private ComponentAddonFactory getConfigurationAddonFactory(String typeIdentifier)
			throws AddonException {
		for(ComponentAddonFactory factory : configurationAddonFactories)
		{
			if(factory.isSupportingTypeIdentifier(typeIdentifier))
				return factory;
		}
		throw new AddonException("No factory to create configuration addons for configuration types " + typeIdentifier + " exists.");
	}
	@Override
	public ComponentAddonUI<?> createComponentAddonUI(String typeIdentifier) throws AddonException 
	{
		return getConfigurationAddonFactory(typeIdentifier).createComponentUI(typeIdentifier, new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer());
	}
	@Override
	public <T extends Configuration> ComponentAddonUI<? extends T> createComponentAddonUI(String typeIdentifier,
			Class<T> configurationClass)
					throws AddonException 
	{
		ComponentAddonFactory factory = getConfigurationAddonFactory(typeIdentifier);
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
	@Override
	public List<String> getComponentTypeIdentifiers(Class<? extends Configuration> configurationClass) 
	{
		ArrayList<String> returnVal = new ArrayList<String>();
		for(ComponentAddonFactory addonFactory : getConfigurationAddonFactories()) 
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
		for(ComponentAddonFactory addonFactory : getConfigurationAddonFactories()) 
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
	public <T extends Configuration> ComponentAddonUI<? extends T> createComponentAddonUI(T configuration)
			throws AddonException, ConfigurationException {
		if(configuration == null)
			throw new NullPointerException();
		ComponentAddonUI<?> addonTemp = createComponentAddonUI(configuration.getTypeIdentifier(), configuration.getClass());
		@SuppressWarnings("unchecked")
		ComponentAddonUI<? extends T> addon = (ComponentAddonUI<? extends T>) addonTemp;
		addonTemp.setConfiguration(configuration);
		return addon;
	}
	@Override
	public List<ComponentMetadata<?>> getComponentMetadata() {
		ArrayList<ComponentMetadata<?>> returnVal = new ArrayList<ComponentMetadata<?>>();
		for(ComponentAddonFactory addonFactory : getConfigurationAddonFactories()) 
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
	public <T extends Configuration> ComponentAddonUI<T> createComponentAddonUI(ComponentMetadata<T> metadata)
			throws AddonException {
		ComponentAddonUI<?> addon = createComponentAddonUI(metadata.getTypeIdentifier());
		@SuppressWarnings("unchecked")
		ComponentAddonUI<T> returnVal = (ComponentAddonUI<T>) addon;
		return returnVal;
	}
	@Override
	public ComponentMetadata<?> getComponentMetadata(String typeIdentifier) throws AddonException {
		for(ComponentAddonFactory addonFactory : getConfigurationAddonFactories()) 
		{
			if(addonFactory.isSupportingTypeIdentifier(typeIdentifier))
				return addonFactory.getComponentMetadata(typeIdentifier);
		}
		throw new AddonException("Configuration type identifier " + typeIdentifier +" no supported by any component addon factory.");
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
}
