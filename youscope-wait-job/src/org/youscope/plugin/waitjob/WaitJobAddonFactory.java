/**
 * 
 */
package org.youscope.plugin.waitjob;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.Job;
import org.youscope.common.measurement.job.basicjobs.WaitJob;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class WaitJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public WaitJobAddonFactory()
	{
		addAddon(WaitJobConfigurationAddon.class, CREATOR_WAIT, WaitJobConfigurationAddon.getMetadata());
		addAddon(ExecuteAndWaitJobConfigurationAddon.class, CREATOR_EXECUTE_WAIT, ExecuteAndWaitJobConfigurationAddon.getMetadata());
	}
	
	private static final CustomAddonCreator<WaitJobConfiguration, WaitJob> CREATOR_WAIT = new CustomAddonCreator<WaitJobConfiguration,WaitJob>()
	{
		@Override
		public WaitJob createCustom(PositionInformation positionInformation, WaitJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			WaitJob job;
			try
			{
				job = new WaitJobImpl(positionInformation);
				job.setWaitTime(configuration.getWaitTime());
			}
			catch(MeasurementRunningException e)
			{
				throw new AddonException("Could not create wait job since newly created job already running.", e);
			} catch (RemoteException e) {
				throw new AddonException("Could not create wait job due to remote exception.", e);
			}
			return job;
		}

		@Override
		public Class<WaitJob> getComponentInterface() {
			return WaitJob.class;
		}
	};
	
	private static final CustomAddonCreator<ExecuteAndWaitJobConfiguration, WaitJob> CREATOR_EXECUTE_WAIT = new CustomAddonCreator<ExecuteAndWaitJobConfiguration,WaitJob>()
	{
		@Override
		public WaitJob createCustom(PositionInformation positionInformation, ExecuteAndWaitJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			WaitJob job;
			try
			{
				job = new WaitJobImpl(positionInformation);
				job.setWaitTime(configuration.getWaitTime());
				for(JobConfiguration childJobConfig : configuration.getJobs())
				{
					Job childJob;
					try {
						childJob = constructionContext.getComponentProvider().createJob(positionInformation, childJobConfig);
					} catch (ComponentCreationException e) {
						throw new AddonException("Could not create child job.", e);
					}
					job.addJob(childJob);
				}
			}
			catch(MeasurementRunningException e)
			{
				throw new AddonException("Could not create wait job since newly created job already running.", e);
			} catch (RemoteException e) {
				throw new AddonException("Could not create wait job due to remote exception.", e);
			}
			return job;
		}

		@Override
		public Class<WaitJob> getComponentInterface() {
			return WaitJob.class;
		}
	};
}
