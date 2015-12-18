/**
 * 
 */
package org.youscope.plugin.repeatjob;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class RepeatJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	private static final CustomAddonCreator<RepeatJobConfiguration, RepeatJob> CREATOR = new CustomAddonCreator<RepeatJobConfiguration, RepeatJob>()
	{

		@Override
		public RepeatJob createCustom(PositionInformation positionInformation,
				RepeatJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				RepeatJob job = new RepeatJobImpl(positionInformation);
				try
				{
					job.setNumRepeats(configuration.getNumRepeats());
				}
				catch(MeasurementRunningException e1)
				{
					throw new AddonException("Newly created job already running.", e1);
				}
				for(JobConfiguration childJobConfig : configuration.getJobs())
				{
					Job childJob;
					try {
						childJob = constructionContext.getComponentProvider().createJob(positionInformation, childJobConfig);
					} catch (ComponentCreationException e1) {
						throw new AddonException("Could not create child job.", e1);
					}
					job.addJob(childJob);
				}
				return job;
				
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create job due to remote exception.", e);
			} catch (MeasurementRunningException e) {
				throw new AddonException("Could not initialize newly created job since job is already running.", e);
			}
		}

		@Override
		public Class<RepeatJob> getComponentInterface() {
			return RepeatJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public RepeatJobAddonFactory()
	{
		super(RepeatJobConfigurationAddon.class, CREATOR, RepeatJobConfigurationAddon.getMetadata());
	}
}
