/**
 * 
 */
package org.youscope.plugin.outoffocus;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;

/**
 * @author Moritz Lang
 */
public class OutOfFocusJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	private static final CustomAddonCreator<OutOfFocusJobConfiguration, OutOfFocusJob> CREATOR = new CustomAddonCreator<OutOfFocusJobConfiguration, OutOfFocusJob>()
	{

		@Override
		public OutOfFocusJob createCustom(PositionInformation positionInformation,
				OutOfFocusJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				OutOfFocusJobImpl job = new OutOfFocusJobImpl(positionInformation);
				
				if(configuration.getFocusConfiguration()==null)
				{
					job.setFocusDevice(null);
					job.setFocusAdjustmentTime(0);
				}
				else
				{
					job.setFocusDevice(configuration.getFocusConfiguration().getFocusDevice());
					job.setFocusAdjustmentTime(configuration.getFocusConfiguration().getAdjustmentTime());
				}
				job.setOffset(configuration.getPosition());
				
				job.setChannel(configuration.getChannelGroup(), configuration.getChannel());
				job.setExposure(configuration.getExposure());
				if(configuration.isSaveImages())
				{
					job.addImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()));
					job.setImageDescription(configuration.getImageSaveName() + " (" + job.getImageDescription() + ")");
				}
				
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
		public Class<OutOfFocusJob> getComponentInterface() {
			return OutOfFocusJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public OutOfFocusJobAddonFactory()
	{
		super(OutOfFocusJobConfigurationAddon.class, CREATOR, OutOfFocusJobConfigurationAddon.getMetadata());
	}
}
