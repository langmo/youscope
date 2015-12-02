/**
 * 
 */
package ch.ethz.csb.youscope.addon.waitjob;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;
import ch.ethz.csb.youscope.addon.adapters.CustomAddonCreator;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.measurement.ComponentCreationException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.WaitJob;

/**
 * @author Moritz Lang
 */
public class WaitJobAddonFactory extends AddonFactoryAdapter 
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
						throw new JobCreationException("Could not create child job.", e);
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
