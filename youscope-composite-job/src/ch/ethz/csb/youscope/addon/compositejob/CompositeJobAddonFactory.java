/**
 * 
 */
package ch.ethz.csb.youscope.addon.compositejob;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;
import ch.ethz.csb.youscope.addon.adapters.CustomAddonCreator;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.CompositeJob;

/**
 * @author Moritz Lang
 */
public class CompositeJobAddonFactory extends AddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public CompositeJobAddonFactory()
	{
		super(CompositeJobConfigurationAddon.class, CREATOR, CompositeJobConfigurationAddon.getMetadata());
	}
	
	private static final CustomAddonCreator<CompositeJobConfiguration, CompositeJob> CREATOR = new CustomAddonCreator<CompositeJobConfiguration,CompositeJob>()
	{
		@Override
		public CompositeJob createCustom(PositionInformation positionInformation, CompositeJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			CompositeJob job;
			try {
				job = new CompositeJobImpl(positionInformation);
			}
			catch (RemoteException e1) 
			{
				throw new AddonException("Could not create composite job due to remote error.", e1);
			}
			for(JobConfiguration childJobConfig : configuration.getJobs())
			{
				try
				{
					Job childJob = constructionContext.getComponentProvider().createJob(positionInformation, childJobConfig);
					job.addJob(childJob);
				}
				catch(Exception e)
				{
					throw new AddonException("Could not create child jobs of composite job.", e);
				}
			}
			return job;
		}

		@Override
		public Class<CompositeJob> getComponentInterface() {
			return CompositeJob.class;
		}
	};
}
