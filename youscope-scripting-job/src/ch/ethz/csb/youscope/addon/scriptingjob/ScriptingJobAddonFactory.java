/**
 * 
 */
package ch.ethz.csb.youscope.addon.scriptingjob;

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
import ch.ethz.csb.youscope.shared.measurement.callback.CallbackCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.ScriptingJob;
import ch.ethz.csb.youscope.shared.measurement.resource.scripting.RemoteScriptEngine;

/**
 * @author Moritz Lang
 */
public class ScriptingJobAddonFactory extends AddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public ScriptingJobAddonFactory()
	{
		super(ScriptingJobConfigurationAddon.class, CREATOR, ScriptingJobConfigurationAddon.getMetadata());
	}
	
	private static final CustomAddonCreator<ScriptingJobConfiguration, ScriptingJob> CREATOR = new CustomAddonCreator<ScriptingJobConfiguration,ScriptingJob>()
	{
		@Override
		public ScriptingJob createCustom(PositionInformation positionInformation, ScriptingJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			if(configuration.getScriptEngine() == null)
				throw new ConfigurationException("No script engine defined.");
			ScriptingJob job;
			try
			{
				job = new ScriptingJobImpl(positionInformation);
				job.setScriptFile(configuration.getScriptFile());		
				job.addMessageListener(constructionContext.getLogger());
				
				if(configuration.isUseClientScriptEngine())
				{
					RemoteScriptEngine engine;
					try {
						engine = constructionContext.getCallbackProvider().createCallback(configuration.getScriptEngine(), RemoteScriptEngine.class);
					} catch (CallbackCreationException e) {
						throw new AddonException("Could not create remote script engine with name " + configuration.getScriptEngine()+".", e);
					}
					job.setRemoteScriptEngine(engine);
				}
				else
				{
					job.setScriptEngine(configuration.getScriptEngine());
				}
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
				throw new AddonException("Could not create scripting job since newly created job already running.", e);
			} catch (RemoteException e) {
				throw new AddonException("Could not create scripting job due to remote exception.", e);
			}
			return job;
		}

		@Override
		public Class<ScriptingJob> getComponentInterface() {
			return ScriptingJob.class;
		}
	};
}
