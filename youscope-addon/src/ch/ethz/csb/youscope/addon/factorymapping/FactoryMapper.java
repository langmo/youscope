package ch.ethz.csb.youscope.addon.factorymapping;

import java.util.HashMap;
import java.util.ServiceLoader;

import ch.ethz.csb.youscope.addon.adapters.AddonFactory;
import ch.ethz.csb.youscope.client.addon.ConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.ConfigurationAddonFactory;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.server.addon.ConstructionAddonFactory;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.Configuration;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationMetadata;
import ch.ethz.csb.youscope.shared.measurement.Component;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;

/**
 * This class maps the general AddonFactories to the different specialized factories used by YouScope internally. Thus,
 * it is only necessary to expose an implementation of an AddonFactory to the ServiceLoader mechanism, while this meta-factory takes care such that
 * the corresponding ConfigurationAddonFactory and ConstructionAddonFactory will be found by the different parts of YouScope.
 * @author Moritz Lang
 *
 */
public class FactoryMapper implements ConfigurationAddonFactory, ConstructionAddonFactory {

	private static final HashMap<String, AddonFactory> supportedFactories = new HashMap<String, AddonFactory>(50);
	private static volatile boolean initialized = false;
	
	/**
	 * Constructor.
	 */
	public FactoryMapper() 
	{
		// do nothing.
	}
	
	private static synchronized void initialize()
	{
		if(initialized)
			return;
		ServiceLoader<AddonFactory> nativeFactories =
                ServiceLoader.load(AddonFactory.class,
                		FactoryMapper.class.getClassLoader());
		for(AddonFactory nativeFactory : nativeFactories)
		{
			for(String typeIdentifier : nativeFactory.getSupportedTypeIdentifiers())
			{
				supportedFactories.put(typeIdentifier, nativeFactory);
			}
		}
		initialized = true;
	}

	@Override
	public Component createComponent(PositionInformation positionInformation, Configuration configuration,
			ConstructionContext constructionContext) throws ConfigurationException, AddonException 
	{
		if(configuration == null)
			throw new AddonException("Configuration is null.");
		initialize();
		
		AddonFactory factory = supportedFactories.get(configuration.getTypeIdentifier());
		if(factory == null)
			throw new AddonException("Configuration type identifier " + configuration.getTypeIdentifier() + " not supported by this factory.");
		return factory.createComponent(positionInformation, configuration, constructionContext);
	}

	@Override
	public ConfigurationAddon<?> createConfigurationAddon(String typeIdentifier, YouScopeClient client,
			YouScopeServer server) throws AddonException 
	{
		if(typeIdentifier == null)
			throw new AddonException("Type identifier is null.");
		initialize();
		
		AddonFactory factory = supportedFactories.get(typeIdentifier);
		if(factory == null)
			throw new AddonException("Configuration type identifier " + typeIdentifier + " not supported by this factory.");
		return factory.createConfigurationAddon(typeIdentifier, client, server);
	}

	@Override
	public String[] getSupportedTypeIdentifiers() 
	{
		initialize();
		return supportedFactories.keySet().toArray(new String[0]);
	}

	@Override
	public boolean isSupportingTypeIdentifier(String typeIdentifier) 
	{
		if(typeIdentifier == null)
			return false;
		initialize();
		return supportedFactories.get(typeIdentifier) != null;
	}

	@Override
	public ConfigurationMetadata<?> getConfigurationMetadata(String typeIdentifier) throws AddonException 
	{
		if(typeIdentifier == null)
			throw new AddonException("Type identifier is null.");
		initialize();
		AddonFactory factory = supportedFactories.get(typeIdentifier);
		if(factory == null)
			throw new AddonException("Configuration type identifier " + typeIdentifier + " not supported by this factory.");
		return factory.getConfigurationMetadata(typeIdentifier);
	}
}
