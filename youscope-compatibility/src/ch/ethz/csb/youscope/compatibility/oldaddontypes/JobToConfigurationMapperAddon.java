package ch.ethz.csb.youscope.compatibility.oldaddontypes;

import java.awt.Component;
import java.util.ArrayList;


import ch.ethz.csb.youscope.client.addon.ConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.ConfigurationAddonListener;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.YouScopeFrame;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonFactory;
import ch.ethz.csb.youscope.client.addon.job.JobConfigurationAddonListener;
import ch.ethz.csb.youscope.compatibility.tools.InlineFrame;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.Configuration;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationMetadata;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;

@SuppressWarnings("deprecation")
class JobToConfigurationMapperAddon<C extends JobConfiguration> implements ConfigurationAddon<C>
{
	private final JobConfigurationAddon nativeAddon;
	private final String typeIdentifier;
	private final JobConfigurationAddonFactory nativeFactory;
	private final Class<C> configurationClass;
	private final YouScopeClient client;
	private JobToConfigurationMapperAddon(String typeIdentifier, Class<C> configurationClass, YouScopeClient client, JobConfigurationAddon nativeAddon, JobConfigurationAddonFactory nativeFactory) 
	{
		this.nativeAddon = nativeAddon;
		this.typeIdentifier = typeIdentifier;
		this.nativeFactory = nativeFactory;
		this.configurationClass = configurationClass;
		this.client = client;
	}
	static <C extends JobConfiguration> JobToConfigurationMapperAddon<C> getMapperAddon(String typeIdentifier, Class<C> configurationClass, YouScopeClient client, JobConfigurationAddon nativeAddon, JobConfigurationAddonFactory nativeFactory)
	{
		return new JobToConfigurationMapperAddon<C>(typeIdentifier, configurationClass, client, nativeAddon, nativeFactory);
	}
	@Override
	public YouScopeFrame toFrame() throws AddonException 
	{
		YouScopeFrame frame = client.createFrame();
		nativeAddon.createUI(frame);
		return frame;
	}
	@Override
	public Component toPanel(YouScopeFrame parentFrame) throws AddonException 
	{
		InlineFrame frame = new InlineFrame(parentFrame);
		nativeAddon.createUI(frame.getYouScopeFrame());
		return frame;
	}
	@Override
	public void setConfiguration(Configuration configuration)
			throws AddonException, ConfigurationException 
	{
		if(configuration == null)
			throw new AddonException("Configuration which should be loaded is null.");
		else if(!(configuration instanceof JobConfiguration))
			throw new AddonException("Configuration which should be loaded must be a JobConfigurationDTO, however, is of class " + configuration.getClass().getName() + ", however, is of class " + configuration.getClass().getName());
		nativeAddon.setConfigurationData((JobConfiguration) configuration);
	}
	@Override
	public C getConfiguration()
	{
		@SuppressWarnings("unchecked")
		C result = (C) nativeAddon.getConfigurationData();
		return result;
	}
	
	private final ArrayList<ConfigurationListenerMapper> configurationListenerMappers = new ArrayList<ConfigurationListenerMapper>(10);
	private class ConfigurationListenerMapper implements JobConfigurationAddonListener
	{
		final ConfigurationAddonListener<? super C> listener;
		public ConfigurationListenerMapper(ConfigurationAddonListener<? super C> listener) 
		{
			this.listener = listener;
		}
		@Override
		public void jobConfigurationFinished(JobConfiguration configuration) 
		{
			@SuppressWarnings("unchecked")
			C configurationTrans = (C)configuration;
			listener.configurationFinished(configurationTrans);
		}
		
	}
	
	@Override
	public void addConfigurationListener(ConfigurationAddonListener<? super C> listener) 
	{
		ConfigurationListenerMapper mapper = new ConfigurationListenerMapper(listener);
		configurationListenerMappers.add(mapper);
		nativeAddon.addConfigurationListener(mapper);
	}
	@Override
	public void removeConfigurationListener(ConfigurationAddonListener<? super C> listener) 
	{
		ConfigurationListenerMapper found = null;
		for(ConfigurationListenerMapper mapper : configurationListenerMappers)
		{
			if(mapper.listener == listener)
			{
				found = mapper;
				break;
			}
		}
		if(found != null)
		{
			configurationListenerMappers.remove(found);
			nativeAddon.removeConfigurationListener(found);
		}
	}
	@Override
	public ConfigurationMetadata<C> getConfigurationMetadata() 
	{
		return JobToConfigurationMapperAddonMetadata.getMetadata(typeIdentifier, configurationClass, nativeFactory);
	}
}
