package ch.ethz.csb.youscope.compatibility.oldaddontypes;

import java.util.ArrayList;
import java.util.ServiceLoader;

import javax.swing.ImageIcon;

import ch.ethz.csb.youscope.client.addon.ConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.ConfigurationAddonFactory;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonFactory;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationMetadata;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

/**
 * Factory to map from ConfigurationAddonFactory to JobConfigurationAddonFactory.
 * @author Moritz Lang
 *
 */
@SuppressWarnings("deprecation")
public class ConfigurationToJobMapperAddonFactory implements JobConfigurationAddonFactory 
{
	
	private static ConfigurationAddonFactory[] factories = null;
	private static String[] typeIdentifiers = null;
	/**
	 * Constructor.
	 */
	public ConfigurationToJobMapperAddonFactory() 
	{
	}
	private void initialize()
	{
		synchronized(JobToConfigurationMapperAddonFactory.class)
		{
			if(ConfigurationToJobMapperAddonFactory.factories != null && ConfigurationToJobMapperAddonFactory.typeIdentifiers!=null)
				return;
			ServiceLoader<ConfigurationAddonFactory> nativeAddons =
                ServiceLoader.load(ConfigurationAddonFactory.class,
                		ConfigurationToJobMapperAddonFactory.class.getClassLoader());
			ArrayList<ConfigurationAddonFactory> factories = new ArrayList<ConfigurationAddonFactory>(10);
			ArrayList<String> typeIdentifiers = new ArrayList<String>(10);
			
			for(ConfigurationAddonFactory nativeAddon : nativeAddons)
			{
				if(nativeAddon instanceof JobToConfigurationMapperAddonFactory)
					continue;
				factories.add(nativeAddon);
				for(String typeIdentifier : nativeAddon.getSupportedTypeIdentifiers())
				{
					
					try 
					{
						if(JobConfiguration.class.isAssignableFrom(nativeAddon.getConfigurationMetadata(typeIdentifier).getConfigurationClass()))
								typeIdentifiers.add(typeIdentifier);
					} 
					catch (@SuppressWarnings("unused") AddonException e) 
					{
						// invalid ID or error in addon. Ignore.
						continue;
					}
				}
			}
			ConfigurationToJobMapperAddonFactory.factories = factories.toArray(new ConfigurationAddonFactory[factories.size()]);
			ConfigurationToJobMapperAddonFactory.typeIdentifiers =typeIdentifiers.toArray(new String[typeIdentifiers.size()]);
		}
	}

	@Override
	public JobConfigurationAddon createJobConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server) 
	{
		initialize();
		for(ConfigurationAddonFactory factory : factories)
		{
			if(factory.isSupportingTypeIdentifier(ID))
			{
				ConfigurationAddon<?> addon;
				try 
				{
					addon = factory.createConfigurationAddon(ID, client, server);
				} 
				catch (AddonException e) 
				{
					client.sendError("Could not create native addon for mapping.", e);
					return null;
				}
				if(!JobConfiguration.class.isAssignableFrom(addon.getConfigurationMetadata().getConfigurationClass()))
						client.sendError("Class " + addon.getConfigurationMetadata().getClass().getName() + " is not a JobConfigurationDTO.");
				@SuppressWarnings("unchecked")
				ConfigurationAddon<? extends JobConfiguration> temp = (ConfigurationAddon<? extends JobConfiguration>) addon;
				return new ConfigurationToJobMapperAddon(ID, temp, client);
			}
		}
		
		return null;
	}

	@Override
	public Class<? extends JobConfiguration> getConfigurationClass(String ID) 
	{
		initialize();
		for(ConfigurationAddonFactory factory:factories)
		{
			if(factory.isSupportingTypeIdentifier(ID))
			{
				try
				{
					return factory.getConfigurationMetadata(ID).getConfigurationClass().asSubclass(JobConfiguration.class);
				}
				catch(@SuppressWarnings("unused") Exception e)
				{
					// Error in addon. Old logic is to just ignore that and to return null
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs() 
	{
		initialize();
		return typeIdentifiers;
	}

	@Override
	public boolean supportsConfigurationID(String ID) 
	{
		initialize();
		for(String ident:typeIdentifiers)
		{
			if(ident.equals(ID))
				return true;
		}
		return false;
	}

	@Override
	public String getJobName(String ID) 
	{
		initialize();
		for(ConfigurationAddonFactory factory : factories)
		{
			if(factory.isSupportingTypeIdentifier(ID))
			{
				ConfigurationMetadata<?> metadata;
				try {
					metadata = factory.getConfigurationMetadata(ID);
				} catch (@SuppressWarnings("unused") AddonException e) {
					// error in addon. Old logic is to just return null.
					return null;
				}
				if(metadata == null)
					return null;
				String name = metadata.getTypeName();
				String[] classification = metadata.getConfigurationClassification();
				if(classification.length == 0)
					return name;
				for(int i=classification.length-1; i>= 0; i--)
				{
					name = classification[i] + "/" + name;
				}
				return name;
			}
		}
		
		return null;
	}

	@Override
	public ImageIcon getJobIcon(String ID) 
	{
		initialize();
		for(ConfigurationAddonFactory factory : factories)
		{
			if(factory.isSupportingTypeIdentifier(ID))
			{
				try {
					return factory.getConfigurationMetadata(ID).getIcon();
				} catch (@SuppressWarnings("unused") AddonException e) {
					// error in addon. Old logic is to just return null.
					return null;
				}
			}
		}
		
		return null;
	}

}
