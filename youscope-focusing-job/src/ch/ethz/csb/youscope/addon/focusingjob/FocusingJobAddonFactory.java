/**
 * 
 */
package ch.ethz.csb.youscope.addon.focusingjob;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;
import ch.ethz.csb.youscope.addon.adapters.CustomAddonCreator;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.FocusingJob;

/**
 * @author Moritz Lang
 */
public class FocusingJobAddonFactory extends AddonFactoryAdapter 
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
				throw new JobCreationException("Could not create focusing job, since newly created job is already running.", e);
			} catch (RemoteException e1) {
				throw new JobCreationException("Could not create focusing job, due to remote exception.", e1);
			}
			return job;
		}

		@Override
		public Class<FocusingJob> getComponentInterface() {
			return FocusingJob.class;
		}
	};
}
