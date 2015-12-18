/**
 * 
 */
package org.youscope.plugin.compositejob;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.basicjobs.CompositeJob;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class CompositeJobAddonFactory extends ComponentAddonFactoryAdapter 
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
