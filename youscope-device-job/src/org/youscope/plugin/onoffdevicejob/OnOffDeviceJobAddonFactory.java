/**
 * 
 */
package org.youscope.plugin.onoffdevicejob;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.basicjobs.CompositeJob;
import org.youscope.common.job.basicjobs.DeviceSettingJob;
import org.youscope.common.job.basicjobs.WaitJob;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class OnOffDeviceJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public OnOffDeviceJobAddonFactory()
	{
		super(OnOffDeviceJobConfigurationAddon.class, CREATOR, OnOffDeviceJobConfigurationAddon.getMetadata());
	}
	
	private static final CustomAddonCreator<OnOffDeviceJobConfiguration, CompositeJob> CREATOR = new CustomAddonCreator<OnOffDeviceJobConfiguration,CompositeJob>()
	{
		@Override
		public CompositeJob createCustom(PositionInformation positionInformation, OnOffDeviceJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			CompositeJob compositeJob;
			try
			{
				compositeJob = constructionContext.getComponentProvider().createJob(positionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
			
				if(configuration.getDeviceSettingsOn() != null && configuration.getDeviceSettingsOn().length > 0)
				{
					DeviceSettingJob job = constructionContext.getComponentProvider().createJob(positionInformation,DeviceSettingJob.DEFAULT_TYPE_IDENTIFIER, DeviceSettingJob.class);
					job.setDeviceSettings(configuration.getDeviceSettingsOn());
					compositeJob.addJob(job);
				}
				if(configuration.getExposure() > 0.0)
				{
					WaitJob job = constructionContext.getComponentProvider().createJob(positionInformation, WaitJob.DEFAULT_TYPE_IDENTIFIER, WaitJob.class);
					job.setWaitTime((long)configuration.getExposure());
					compositeJob.addJob(job);
				}
				if(configuration.getDeviceSettingsOff() != null && configuration.getDeviceSettingsOff().length > 0)
				{
					DeviceSettingJob job = constructionContext.getComponentProvider().createJob(positionInformation, DeviceSettingJob.DEFAULT_TYPE_IDENTIFIER, DeviceSettingJob.class);
					job.setDeviceSettings(configuration.getDeviceSettingsOff());
					compositeJob.addJob(job);
				}
			}
			catch(Exception e)
			{
				throw new AddonException("Could not create job.", e);
			}
			return compositeJob;
		}

		@Override
		public Class<CompositeJob> getComponentInterface() {
			return CompositeJob.class;
		}
	};
}
