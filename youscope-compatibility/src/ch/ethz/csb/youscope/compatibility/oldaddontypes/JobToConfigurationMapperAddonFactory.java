package ch.ethz.csb.youscope.compatibility.oldaddontypes;

import java.util.ArrayList;
import java.util.ServiceLoader;

import ch.ethz.csb.youscope.client.addon.ConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.ConfigurationAddonFactory;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonFactory;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationMetadata;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

/**
 * Mapper factory to map JobConfigurationAddonFactory into ConfigurationAddonFactory.
 * @author Moritz Lang
 *
 */
@SuppressWarnings("deprecation")
public class JobToConfigurationMapperAddonFactory implements ConfigurationAddonFactory 
{
	private static JobConfigurationAddonFactory[] factories = null;
	private static String[] typeIdentifiers = null;
	/**
	 * Constructor.
	 */
	public JobToConfigurationMapperAddonFactory() 
	{
	}
	private void initialize()
	{
		synchronized(JobToConfigurationMapperAddonFactory.class)
		{
			if(JobToConfigurationMapperAddonFactory.factories != null && JobToConfigurationMapperAddonFactory.typeIdentifiers!=null)
				return;
			ServiceLoader<JobConfigurationAddonFactory> nativeAddons =
                ServiceLoader.load(JobConfigurationAddonFactory.class,
                		JobToConfigurationMapperAddonFactory.class.getClassLoader());
			ArrayList<JobConfigurationAddonFactory> factories = new ArrayList<JobConfigurationAddonFactory>(10);
			ArrayList<String> typeIdentifiers = new ArrayList<String>(10);
			for(JobConfigurationAddonFactory nativeAddon : nativeAddons)
			{
				if(nativeAddon instanceof ConfigurationToJobMapperAddonFactory)
					continue;
				factories.add(nativeAddon);
				for(String typeIdentifier : nativeAddon.getSupportedConfigurationIDs())
				{
					typeIdentifiers.add(typeIdentifier);
				}
			}
			JobToConfigurationMapperAddonFactory.factories = factories.toArray(new JobConfigurationAddonFactory[factories.size()]);
			JobToConfigurationMapperAddonFactory.typeIdentifiers =typeIdentifiers.toArray(new String[typeIdentifiers.size()]);
		}
	}

	@Override
	public ConfigurationAddon<?> createConfigurationAddon(String typeIdentifier, YouScopeClient client,
			YouScopeServer server) throws AddonException 
	{
		initialize();
		for(JobConfigurationAddonFactory factory : factories)
		{
			if(factory.supportsConfigurationID(typeIdentifier))
			{
				Class<? extends JobConfiguration> configurationClass = factory.getConfigurationClass(typeIdentifier);
				return JobToConfigurationMapperAddon.getMapperAddon(typeIdentifier, configurationClass, client, factory.createJobConfigurationAddon(typeIdentifier, client, server), factory);
			}
		}
		
		throw new AddonException("Configuration with type identifer " + typeIdentifier + " not supported by this addon factory.");
	}

	@Override
	public String[] getSupportedTypeIdentifiers() 
	{
		initialize();
		return typeIdentifiers;
	}

	@Override
	public boolean isSupportingTypeIdentifier(String typeIdentifier) 
	{
		initialize();
		for(String ident:typeIdentifiers)
		{
			if(ident.equals(typeIdentifier))
				return true;
		}
		return false;
	}

	@Override
	public ConfigurationMetadata<?> getConfigurationMetadata(String typeIdentifier) throws AddonException 
	{
		initialize();
		for(JobConfigurationAddonFactory factory : factories)
		{
			if(factory.supportsConfigurationID(typeIdentifier))
			{
				Class<? extends JobConfiguration> configurationClass = factory.getConfigurationClass(typeIdentifier);
				return JobToConfigurationMapperAddonMetadata.getMetadata(typeIdentifier, configurationClass, factory);
			}
		}
		
		throw new AddonException("Configuration with type identifer " + typeIdentifier + " not supported by this addon factory.");
	}

}
