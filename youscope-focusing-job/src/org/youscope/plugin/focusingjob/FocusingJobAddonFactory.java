/**
 * 
 */
package org.youscope.plugin.focusingjob;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.basicjobs.FocusingJob;

/**
 * @author Moritz Lang
 */
public class FocusingJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public FocusingJobAddonFactory()
	{
		super(FocusingJob.DEFAULT_TYPE_IDENTIFIER, FocusingJobConfiguration.class, CREATOR); 
	}
	 
	private static final CustomAddonCreator<FocusingJobConfiguration, FocusingJob> CREATOR = new CustomAddonCreator<FocusingJobConfiguration,FocusingJob>()
	{
		@Override
		public FocusingJob createCustom(PositionInformation positionInformation, FocusingJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			FocusingJob job;
			try
			{
				job = new FocusingJobImpl(positionInformation);
				job.setPosition(configuration.getPosition(), configuration.isRelative());
				if(configuration.getFocusConfiguration() != null)
				{
					
					job.setFocusDevice(configuration.getFocusConfiguration().getFocusDevice());
					job.setFocusAdjustmentTime(configuration.getFocusConfiguration().getAdjustmentTime());
				}
				else
				{
					job.setFocusDevice(null);
					job.setFocusAdjustmentTime(0); 
				}
			}
			catch(MeasurementRunningException e)
			{
				throw new AddonException("Could not create focusing job, since newly created job is already running.", e);
			} catch (RemoteException e1) {
				throw new AddonException("Could not create focusing job, due to remote exception.", e1);
			}
			return job;
		}

		@Override
		public Class<FocusingJob> getComponentInterface() {
			return FocusingJob.class;
		}
	};
}
