package ch.ethz.csb.youscope.compatibility.oldaddontypes;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.ServiceLoader;

import ch.ethz.csb.youscope.server.addon.ConstructionAddonFactory;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddonFactory;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.Configuration;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationMetadata;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.measurement.Component;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;

/**
 * Mapper factory to map JobConstructionAddonFactory into ConstructionAddonFactory.
 * @author Moritz Lang
 *
 */
@SuppressWarnings("deprecation")
public class JobToConstructionMapperAddonFactory implements ConstructionAddonFactory
{

	private static final HashMap<String, JobConstructionAddonFactory> supportedFactories = new HashMap<String, JobConstructionAddonFactory>(50);
	private static volatile boolean initialized = false;
	private static final JobToConfigurationMapperAddonFactory configurationMapper = new JobToConfigurationMapperAddonFactory();
	
	/**
	 * Constructor.
	 */
	public JobToConstructionMapperAddonFactory() 
	{
		// do nothing.
	}
	
	private static synchronized void initialize()
	{
		if(initialized)
			return;
		ServiceLoader<JobConstructionAddonFactory> nativeFactories =
                ServiceLoader.load(JobConstructionAddonFactory.class,
                		JobToConstructionMapperAddonFactory.class.getClassLoader());
		for(JobConstructionAddonFactory nativeFactory : nativeFactories)
		{
			if(nativeFactory instanceof ConstructionToJobMapperAddonFactory)
				continue;
			for(String typeIdentifier : nativeFactory.getSupportedConfigurationIDs())
			{
				// Only show addons for which we also can get the metadata.
				if(!configurationMapper.isSupportingTypeIdentifier(typeIdentifier))
				{ 
					System.out.println("Found old style factory " + typeIdentifier + ", but not its metadata.");
					continue;
				}
				supportedFactories.put(typeIdentifier, nativeFactory);
			}
		}
		initialized = true;
	}

	@Override
	public Component createComponent(PositionInformation positionInformation, Configuration configuration,
			ConstructionContext constructionContext) throws ConfigurationException, AddonException {
		if(positionInformation == null)
			throw new AddonException("Position information is null.");
		if(configuration == null)
			throw new AddonException("Configuration is null.");
		if(constructionContext == null)
			throw new AddonException("Construction context is null.");
		if(!(configuration instanceof JobConfiguration))
			throw new AddonException("Provided configuration with type identifier " + configuration.getTypeIdentifier() + " is not a subclass of JobConfigurationDTO.");
		initialize();
		JobConstructionAddonFactory factory = supportedFactories.get(configuration.getTypeIdentifier());
		if(factory == null)
			throw new AddonException("Addon factory does not support configurations with type identifier " + configuration.getTypeIdentifier() + ".");
		JobConstructionAddon addon = factory.createJobConstructionAddon(configuration.getTypeIdentifier());
		if(addon == null)
			throw new AddonException("Job construction addon factory states that it supports type identifier " + configuration.getTypeIdentifier() + ", however, does not.");
		try {
			return addon.createJob((JobConfiguration)configuration, constructionContext, positionInformation);
		} 
		catch (RemoteException e) {
			throw new AddonException("An error in the remote communication occured while constructing measurement job.", e);
		}
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
		initialize();
		return supportedFactories.containsKey(typeIdentifier);
	}

	@Override
	public ConfigurationMetadata<?> getConfigurationMetadata(String typeIdentifier) throws AddonException 
	{
		initialize();
		if(!isSupportingTypeIdentifier(typeIdentifier))
			throw new AddonException("Addon factory does not support configurations with type identifier " + typeIdentifier + ".");
		return configurationMapper.getConfigurationMetadata(typeIdentifier);
	}

}
