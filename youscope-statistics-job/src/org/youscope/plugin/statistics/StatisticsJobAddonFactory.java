/**
 * 
 */
package org.youscope.plugin.statistics;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.basicjobs.StatisticsJob;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * A factory for jobs gathering statistics about their sub-jobs runtime.
 * @author Moritz Lang
 */
public class StatisticsJobAddonFactory extends ComponentAddonFactoryAdapter 
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
			StatisticsJob statisticsJob;
			
			// Add all child jobs
			try
			{
				statisticsJob = new StatisticsJobImpl(positionInformation);
				if(configuration.getFileName() != null)
					statisticsJob.addTableListener(constructionContext.getMeasurementSaver().getSaveTableListener(configuration.getFileName()));
				for(JobConfiguration childJobConfig : configuration.getJobs())
				{
					Job childJob;
					try {
						childJob = constructionContext.getComponentProvider().createJob(positionInformation, childJobConfig);
						statisticsJob.addJob(childJob);
					} catch (ComponentCreationException e) {
						throw new AddonException("Could not create child job.", e);
					} 
					
				}
			}
			catch(MeasurementRunningException e)
			{
				throw new AddonException("Could not create statistics job, since newly created job is already running.", e);
			} catch (RemoteException e1) {
				throw new AddonException("Could not create statistics job, due to remote exception.", e1);
			}
			
			return statisticsJob;
		}

		@Override
		public Class<StatisticsJob> getComponentInterface() {
			return StatisticsJob.class;
		}
	};
}
