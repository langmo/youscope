/**
 * 
 */
package ch.ethz.csb.youscope.addon.dropletmicrofluidics;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;
import ch.ethz.csb.youscope.addon.adapters.CustomAddonCreator;
import ch.ethz.csb.youscope.addon.autofocus.AutoFocusJob;
import ch.ethz.csb.youscope.addon.autofocus.AutoFocusJobConfiguration;
import ch.ethz.csb.youscope.addon.nemesys.NemesysJob;
import ch.ethz.csb.youscope.addon.nemesys.NemesysJobConfiguration;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.measurement.ComponentCreationException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.callback.CallbackCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.resource.dropletmicrofluidics.DropletControllerConfiguration;
import ch.ethz.csb.youscope.shared.resource.dropletmicrofluidics.DropletControllerResource;
import ch.ethz.csb.youscope.shared.resource.dropletmicrofluidics.DropletObserverConfiguration;
import ch.ethz.csb.youscope.shared.resource.dropletmicrofluidics.DropletObserverResource;

/**
 * @author Moritz Lang
 */
public class DropletMicrofluidicJobAddonFactory extends AddonFactoryAdapter 
{
	private static final CustomAddonCreator<DropletMicrofluidicJobConfiguration, DropletMicrofluidicJob> CREATOR = new CustomAddonCreator<DropletMicrofluidicJobConfiguration, DropletMicrofluidicJob>()
	{

		@Override
		public DropletMicrofluidicJob createCustom(PositionInformation positionInformation,
				DropletMicrofluidicJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				DropletMicrofluidicJobImpl job = new DropletMicrofluidicJobImpl(positionInformation);
				
				AutoFocusJobConfiguration autofocusConfiguration = configuration.getAutofocusConfiguration();
				if(autofocusConfiguration == null)
					throw new ConfigurationException("No autofocus configuration set.");
				job.setInputJob(constructionContext.getComponentProvider().createJob(positionInformation, autofocusConfiguration, AutoFocusJob.class));				
				
				NemesysJob nemesysJob = constructionContext.getComponentProvider().createJob(positionInformation, NemesysJobConfiguration.TYPE_IDENTIFIER, NemesysJob.class);
				nemesysJob.setNemesysDeviceName(configuration.getNemesysDevice());
				job.setOutputJob(nemesysJob);
				
				DropletControllerConfiguration controllerConfiguration = configuration.getControllerConfiguration();
				if(controllerConfiguration == null)
					throw new ConfigurationException("No controller configuration set.");
				job.setController(constructionContext.getComponentProvider().createComponent(positionInformation, controllerConfiguration, DropletControllerResource.class));
				
				DropletObserverConfiguration observerConfiguration = configuration.getObserverConfiguration();
				if(observerConfiguration == null)
					throw new ConfigurationException("No observer configuration set.");
				job.setObserver(constructionContext.getComponentProvider().createComponent(positionInformation, observerConfiguration, DropletObserverResource.class));
				
				DropletMicrofluidicJobCallback callback;
				try {
					callback = constructionContext.getCallbackProvider().createCallback(DropletMicrofluidicJobCallback.TYPE_IDENTIFIER, DropletMicrofluidicJobCallback.class);
				} catch (CallbackCreationException e) {
					throw new JobCreationException("Could not construct visual callback for job.", e);
				}
				job.setCallback(callback);
				
				return job;
				
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create droplet based microfluidics job due to remote exception.", e);
			} catch (ComponentCreationException e) {
				throw new AddonException("Could not create all components of droplet based microfluidics job.", e);
			} catch (MeasurementRunningException e) {
				throw new AddonException("Could not initialize newly created droplet based microfluidics job since job is already running.", e);
			}
		}

		@Override
		public Class<DropletMicrofluidicJob> getComponentInterface() {
			return DropletMicrofluidicJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public DropletMicrofluidicJobAddonFactory()
	{
		super(DropletMicrofluidicJobConfigurationAddon.class, CREATOR, DropletMicrofluidicJobConfigurationAddon.getMetadata());
	}
}
