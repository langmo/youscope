/**
 * 
 */
package org.youscope.plugin.onix;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class OnixJobAddonFactory extends ComponentAddonFactoryAdapter 
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
