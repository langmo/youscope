/**
 * 
 */
package ch.ethz.csb.youscope.addon.onoffdevicejob;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;
import ch.ethz.csb.youscope.addon.adapters.CustomAddonCreator;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.CompositeJob;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.DeviceSettingJob;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.WaitJob;

/**
 * @author Moritz Lang
 */
public class OnOffDeviceJobAddonFactory extends AddonFactoryAdapter 
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
				throw new JobCreationException("Could not create job.", e);
			}
			return compositeJob;
		}

		@Override
		public Class<CompositeJob> getComponentInterface() {
			return CompositeJob.class;
		}
	};
}
