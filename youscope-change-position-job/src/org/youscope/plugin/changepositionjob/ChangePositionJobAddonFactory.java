/**
 * 
 */
package org.youscope.plugin.changepositionjob;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.basicjobs.ChangePositionJob;

/**
 * @author Moritz Lang
 */
public class ChangePositionJobAddonFactory extends ComponentAddonFactoryAdapter 
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
