/**
 * 
 */
package org.youscope.plugin.simplecompositejob;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.basicjobs.SimpleCompositeJob;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class SimpleCompositeJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public SimpleCompositeJobAddonFactory()
	{
		super(SimpleCompositeJobConfigurationAddon.class, CREATOR, SimpleCompositeJobConfigurationAddon.getMetadata()); 
	}
	
	private static final CustomAddonCreator<SimpleCompositeJobConfiguration, SimpleCompositeJob> CREATOR = new CustomAddonCreator<SimpleCompositeJobConfiguration,SimpleCompositeJob>()
	{
		@Override
		public SimpleCompositeJob createCustom(PositionInformation positionInformation, SimpleCompositeJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			SimpleCompositeJob job;
			try {
				job = new SimpleCompositeJobImpl(positionInformation);
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
		public Class<SimpleCompositeJob> getComponentInterface() {
			return SimpleCompositeJob.class;
		}
	};
}
