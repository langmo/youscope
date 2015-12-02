/**
 * 
 */
package ch.ethz.csb.youscope.addon.statistics;

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
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.StatisticsJob;

/**
 * A factory for jobs gathering statistics about their sub-jobs runtime.
 * @author Moritz Lang
 */
public class StatisticsJobAddonFactory extends AddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public StatisticsJobAddonFactory()
	{
		super(StatisticsJobConfigurationAddon.class, CREATOR, StatisticsJobConfigurationAddon.getMetadata());
	}
	
	private static final CustomAddonCreator<StatisticsJobConfiguration, StatisticsJob> CREATOR = new CustomAddonCreator<StatisticsJobConfiguration,StatisticsJob>()
	{
		@Override
		public StatisticsJob createCustom(PositionInformation positionInformation, StatisticsJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			if(configuration.getFileName() == null)
				throw new ConfigurationException("Name of statistics file must not be null.");
			StatisticsJob statisticsJob;
			
			// Add all child jobs
			try
			{
				statisticsJob = new StatisticsJobImpl(positionInformation);
				statisticsJob.addTableListener(constructionContext.getMeasurementSaver().getSaveTableDataListener(configuration.getFileName()));
				for(JobConfiguration childJobConfig : configuration.getJobs())
				{
					Job childJob;
					try {
						childJob = constructionContext.getComponentProvider().createJob(positionInformation, childJobConfig);
						statisticsJob.addJob(childJob);
					} catch (ComponentCreationException e) {
						throw new JobCreationException("Could not create child job.", e);
					} catch (RemoteException e) {
						throw new JobCreationException("Could not create child job.", e);
					}
					
				}
			}
			catch(MeasurementRunningException e)
			{
				throw new JobCreationException("Could not create statistics job, since newly created job is already running.", e);
			} catch (RemoteException e1) {
				throw new JobCreationException("Could not create statistics job, due to remote exception.", e1);
			}
			
			return statisticsJob;
		}

		@Override
		public Class<StatisticsJob> getComponentInterface() {
			return StatisticsJob.class;
		}
	};
}
