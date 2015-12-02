package ch.ethz.csb.youscope.compatibility.oldaddontypes;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ServiceLoader;

import ch.ethz.csb.youscope.server.addon.ConstructionAddonFactory;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddonFactory;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.measurement.Component;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;

/**
 * Mapper factory to translate ConstructionAddonFactory into JobConstructionAddonFactory.
 * @author Moritz Lang
 *
 */
@SuppressWarnings("deprecation")
public class ConstructionToJobMapperAddonFactory implements JobConstructionAddonFactory
{
	private static final HashMap<String, ConstructionAddonFactory> supportedFactories = new HashMap<String, ConstructionAddonFactory>(50);
	private static volatile boolean initialized = false;
	
	/**
	 * Constructor.
	 */
	public ConstructionToJobMapperAddonFactory() 
	{
		// do nothing.
	}
	
	private static synchronized void initialize()
	{
		if(initialized)
			return;
		ServiceLoader<ConstructionAddonFactory> nativeFactories =
                ServiceLoader.load(ConstructionAddonFactory.class,
                		ConstructionToJobMapperAddonFactory.class.getClassLoader());
		for(ConstructionAddonFactory nativeFactory : nativeFactories)
		{
			if(nativeFactory instanceof JobToConstructionMapperAddonFactory)
				continue;
			for(String typeIdentifier : nativeFactory.getSupportedTypeIdentifiers())
			{
				supportedFactories.put(typeIdentifier, nativeFactory);
			}
		}
		initialized = true;
	}
	private class NativeJobConstructionAddon implements JobConstructionAddon
	{
		private final ConstructionAddonFactory factory;
		public NativeJobConstructionAddon(ConstructionAddonFactory factory) 
		{
			this.factory = factory;
		}
		@Override
		public Job createJob(JobConfiguration jobConfiguration,
				ConstructionContext initializer,
				PositionInformation positionInformation)
				throws RemoteException, ConfigurationException,
				JobCreationException 
		{
			Component component;
			try {
				component = factory.createComponent(positionInformation, jobConfiguration, initializer);
			} catch (AddonException e) {
				throw new JobCreationException("Error while creating job", e);
			}
			if(component instanceof Job)
				return (Job)component;
			throw new JobCreationException("Create measurement component does not implement the interface job.");
		}
	}
	@Override
	public JobConstructionAddon createJobConstructionAddon(String ID) 
	{
		initialize();
		ConstructionAddonFactory factory = supportedFactories.get(ID);
		if(factory == null)
			return null;
		return new NativeJobConstructionAddon(factory);
	}

	@Override
	public String[] getSupportedConfigurationIDs() 
	{
		initialize();
		return supportedFactories.keySet().toArray(new String[0]);
	}

	@Override
	public boolean supportsConfigurationID(String ID) 
	{
		initialize();
		return supportedFactories.containsKey(ID);
	}

	@Override
	public Iterable<Class<? extends Job>> getJobImplementations() 
	{
		return new ArrayList<Class<? extends Job>>(0);
	}

}
