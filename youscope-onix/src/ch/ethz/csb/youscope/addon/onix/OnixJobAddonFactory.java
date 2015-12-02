/**
 * 
 */
package ch.ethz.csb.youscope.addon.onix;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;
import ch.ethz.csb.youscope.addon.adapters.CustomAddonCreator;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;

/**
 * @author Moritz Lang
 */
public class OnixJobAddonFactory extends AddonFactoryAdapter 
{
	private static final CustomAddonCreator<OnixJobConfiguration, OnixJob> CREATOR = new CustomAddonCreator<OnixJobConfiguration, OnixJob>()
	{

		@Override
		public OnixJob createCustom(PositionInformation positionInformation,
				OnixJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				OnixJobImpl job = new OnixJobImpl(positionInformation);
				job.addMessageListener(constructionContext.getLogger());
				job.setOnixProtocol(configuration.getOnixProtocol());
				job.setWaitUntilFinished(configuration.isWaitUntilFinished());
				
				return job;
				
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create job due to remote exception.", e);
			} catch (MeasurementRunningException e) {
				throw new AddonException("Could not initialize newly created job since job is already running.", e);
			}
		}

		@Override
		public Class<OnixJob> getComponentInterface() {
			return OnixJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public OnixJobAddonFactory()
	{
		super(OnixJobConfigurationAddon.class, CREATOR, OnixJobConfigurationAddon.getMetadata());
	}
}
