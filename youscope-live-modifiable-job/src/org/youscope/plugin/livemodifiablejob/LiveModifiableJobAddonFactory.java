/**
 * 
 */
package org.youscope.plugin.livemodifiablejob;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.callback.CallbackCreationException;
import org.youscope.common.measurement.job.Job;


/**
 * @author Moritz Lang
 */
public class LiveModifiableJobAddonFactory extends ComponentAddonFactoryAdapter  
{
	private static final CustomAddonCreator<LiveModifiableJobConfiguration, LiveModifiableJob> CREATOR = new CustomAddonCreator<LiveModifiableJobConfiguration, LiveModifiableJob>()
	{

		@Override
		public LiveModifiableJob createCustom(PositionInformation positionInformation,
				LiveModifiableJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				LiveModifiableJobImpl job = new LiveModifiableJobImpl(positionInformation);
				
				// Get callback.
	            LiveModifiableJobCallback callback;
	            try
	            {
	                callback = constructionContext.getCallbackProvider().createCallback(LiveModifiableJobCallback.TYPE_IDENTIFIER, LiveModifiableJobCallback.class);
	            } catch (CallbackCreationException e1)
	            {
	            	throw new AddonException("Could not obtain measurement callback for live modifiable job.", e1);
	            }

	            // ping callback
	            try
	            {
	                callback.pingCallback();
	            } catch (RemoteException e)
	            {
	                throw new AddonException("Callback for live modifiable job is not responding.", e);
	            }
	            
	            job.setData(constructionContext, callback);
	            job.setEnabled(configuration.isEnabledAtStartup());
	            try
	            {
	            	job.setChildJobConfigurations(configuration.getJobs());
	            } catch (CloneNotSupportedException e1)
	            {
	                throw new AddonException("Child jobs do not allow for clone().", e1);
	            }

	            // Add all child jobs
                for (JobConfiguration childJobConfig : configuration.getJobs())
                {
                    Job childJob =
                    		constructionContext.getComponentProvider().createJob(positionInformation, childJobConfig);
                    job.addJob(childJob);
                }
				return job;
				
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create job due to remote exception.", e);
			} catch (ComponentCreationException e) {
				throw new AddonException("Could not create all components of job.", e);
			} catch (MeasurementRunningException e) {
				throw new AddonException("Could not initialize newly created job since job is already running.", e);
			}
		}

		@Override
		public Class<LiveModifiableJob> getComponentInterface() {
			return LiveModifiableJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public LiveModifiableJobAddonFactory()
	{
		super(LiveModifiableJobConfigurationAddon.class, CREATOR, LiveModifiableJobConfigurationAddon.getMetadata());
	}
}
