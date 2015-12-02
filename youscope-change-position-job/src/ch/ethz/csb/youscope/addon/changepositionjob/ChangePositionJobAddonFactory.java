/**
 * 
 */
package ch.ethz.csb.youscope.addon.changepositionjob;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;
import ch.ethz.csb.youscope.addon.adapters.CustomAddonCreator;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.ChangePositionJob;

/**
 * @author Moritz Lang
 */
public class ChangePositionJobAddonFactory extends AddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public ChangePositionJobAddonFactory()
	{
		super(ChangePositionJobConfigurationAddon.class, CREATOR, ChangePositionJobConfigurationAddon.getMetadata());
	}
	
	private static final CustomAddonCreator<ChangePositionJobConfiguration, ChangePositionJob> CREATOR = new CustomAddonCreator<ChangePositionJobConfiguration,ChangePositionJob>()
	{
		@Override
		public ChangePositionJob createCustom(PositionInformation positionInformation, ChangePositionJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			ChangePositionJob job;
			try
			{
				job = new ChangePositionJobImpl(positionInformation);
				if(configuration.isAbsolute())
					job.setPosition(configuration.getX(), configuration.getY());
				else
					job.setRelativePosition(configuration.getX(), configuration.getY());
			}
			catch(MeasurementRunningException e)
			{
				throw new AddonException("Could not create change position job since newly created job is already running.", e);
			} catch (RemoteException e) {
				throw new AddonException("Could not create change position job due to remote exception.", e);
			}
			return job;
		}

		@Override
		public Class<ChangePositionJob> getComponentInterface() {
			return ChangePositionJob.class;
		}
	};
}
