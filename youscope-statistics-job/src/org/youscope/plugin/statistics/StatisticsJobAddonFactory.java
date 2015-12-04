/**
 * 
 */
package org.youscope.plugin.statistics;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.measurement.ComponentCreationException;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.Job;
import org.youscope.common.measurement.job.basicjobs.StatisticsJob;

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
