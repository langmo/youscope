package ch.ethz.csb.youscope.client;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import ch.ethz.csb.youscope.client.addon.ClientAddonProvider;
import ch.ethz.csb.youscope.client.addon.ConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.ConfigurationAddonFactory;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.Configuration;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationMetadata;

class ClientAddonProviderImpl implements ClientAddonProvider
{
	private final List<ConfigurationAddonFactory> configurationAddonFactories = new ArrayList<ConfigurationAddonFactory>(30);
	private static ClientAddonProviderImpl singleton = null;
	/**
	 * Singleton. Use getAddonLoader().
	 * @throws RemoteException
	 */
	private ClientAddonProviderImpl()
	{
		// Singleton. Use getAddonLoader().
	}
	public static synchronized ClientAddonProviderImpl getProvider()
	{
		if(singleton != null)
			return singleton;
		singleton = new ClientAddonProviderImpl();
		Iterable<ConfigurationAddonFactory> factories = ServiceLoader.load(ConfigurationAddonFactory.class,
                ClientSystem.class.getClassLoader());
		for(ConfigurationAddonFactory factory : factories)
		{
			singleton.configurationAddonFactories.add(factory);
		}
		return singleton;
		
	}
	@Override
	public List<ConfigurationAddonFactory> getConfigurationAddonFactories()
	{
		return new ArrayList<ConfigurationAddonFactory>(configurationAddonFactories);
	}
	
	@Override
	public ConfigurationAddonFactory getConfigurationAddonFactory(String typeIdentifier)
			throws AddonException {
		for(ConfigurationAddonFactory factory : configurationAddonFactories)
		{
			if(factory.isSupportingTypeIdentifier(typeIdentifier))
				return factory;
		}
		throw new AddonException("No factory to create configuration addons for configuration types " + typeIdentifier + " exists.");
	}
	@Override
	public ConfigurationAddon<?> createConfigurationAddon(String typeIdentifier) throws AddonException 
	{
		return getConfigurationAddonFactory(typeIdentifier).createConfigurationAddon(typeIdentifier, new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer());
	}
	@Override
	public <T extends Configuration> ConfigurationAddon<? extends T> createConfigurationAddon(String typeIdentifier,
			Class<T> configurationClass)
					throws AddonException 
	{
		ConfigurationAddonFactory factory = getConfigurationAddonFactory(typeIdentifier);
		ConfigurationMetadata<?> metadata = factory.getConfigurationMetadata(typeIdentifier);
		if(metadata == null)
		{
			throw new AddonException("Metadata for configuration addon with configuration type " + typeIdentifier + " is null.");
		}
		Class<? extends Configuration> addonClass = metadata.getConfigurationClass();
		if(!configurationClass.isAssignableFrom(addonClass))
			throw new AddonException("Configuration addon with type identifier " + typeIdentifier + " creates configurations of class " + addonClass.getName()+" which are not subclasses of " + configurationClass.getName() + ".");
		@SuppressWarnings("unchecked")
		ConfigurationAddon<? extends T> result = (ConfigurationAddon<? extends T>) factory.createConfigurationAddon(typeIdentifier, new YouScopeClientConnectionImpl(), YouScopeClientImpl.getServer());
		return result;
		
	}
	@Override
	public List<String> getConformingTypeIdentifiers(Class<? extends Configuration> configurationClass) 
	{
		ArrayList<String> returnVal = new ArrayList<String>();
		for(ConfigurationAddonFactory addonFactory : getConfigurationAddonFactories()) 
		{
			for(String algorithmID : addonFactory.getSupportedTypeIdentifiers())
			{
				ConfigurationMetadata<?> metadata;
				try {
					metadata = addonFactory.getConfigurationMetadata(algorithmID);
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
	public <T extends Configuration> List<ConfigurationMetadata<? extends T>> getConformingConfigurationMetadata(
			Class<T> configurationClass) {
		ArrayList<ConfigurationMetadata<? extends T>> returnVal = new ArrayList<ConfigurationMetadata<? extends T>>();
		for(ConfigurationAddonFactory addonFactory : getConfigurationAddonFactories()) 
		{
			for(String algorithmID : addonFactory.getSupportedTypeIdentifiers())
			{
				ConfigurationMetadata<?> metadata;
				try {
					metadata = addonFactory.getConfigurationMetadata(algorithmID);
				} catch (@SuppressWarnings("unused") AddonException e) {
					continue;
				}
				if(metadata == null)
					continue;
				if(!configurationClass.isAssignableFrom(metadata.getConfigurationClass()))
						continue;
				@SuppressWarnings("unchecked")
				ConfigurationMetadata<? extends T> temp = (ConfigurationMetadata<? extends T>) metadata;
				
				returnVal.add(temp);
			}
		}
		return returnVal;
	}
	@Override
	public <T extends Configuration> ConfigurationAddon<? extends T> createConfigurationAddon(T configuration)
			throws AddonException, ConfigurationException {
		if(configuration == null)
			throw new NullPointerException();
		ConfigurationAddon<?> addonTemp = createConfigurationAddon(configuration.getTypeIdentifier(), configuration.getClass());
		@SuppressWarnings("unchecked")
		ConfigurationAddon<? extends T> addon = (ConfigurationAddon<? extends T>) addonTemp;
		addonTemp.setConfiguration(configuration);
		return addon;
	}
	@Override
	public List<ConfigurationMetadata<?>> getConfigurationMetadata() {
		ArrayList<ConfigurationMetadata<?>> returnVal = new ArrayList<ConfigurationMetadata<?>>();
		for(ConfigurationAddonFactory addonFactory : getConfigurationAddonFactories()) 
		{
			for(String algorithmID : addonFactory.getSupportedTypeIdentifiers())
			{
				ConfigurationMetadata<?> metadata;
				try {
					metadata = addonFactory.getConfigurationMetadata(algorithmID);
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
	public <T extends Configuration> ConfigurationAddon<T> createConfigurationAddon(ConfigurationMetadata<T> metadata)
			throws AddonException {
		ConfigurationAddon<?> addon = createConfigurationAddon(metadata.getTypeIdentifier());
		@SuppressWarnings("unchecked")
		ConfigurationAddon<T> returnVal = (ConfigurationAddon<T>) addon;
		return returnVal;
	}
}
