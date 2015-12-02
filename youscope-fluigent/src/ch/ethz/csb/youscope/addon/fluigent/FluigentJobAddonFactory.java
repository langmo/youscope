/**
 * 
 */
package ch.ethz.csb.youscope.addon.fluigent;

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
public class FluigentJobAddonFactory extends AddonFactoryAdapter 
{
	private static final CustomAddonCreator<FluigentJobConfiguration, FluigentJob> CREATOR = new CustomAddonCreator<FluigentJobConfiguration, FluigentJob>()
	{

		@Override
		public FluigentJob createCustom(PositionInformation positionInformation,
				FluigentJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				FluigentJobImpl job = new FluigentJobImpl(positionInformation);

				job.addMessageListener(constructionContext.getLogger());
				job.setScript(configuration.getScript());
				job.setScriptEngine(configuration.getScriptEngine());
				job.setFluigentDeviceName(configuration.getFluigentDevice());
				if(configuration.getTableSaveName() != null)
				{
					job.addTableListener(constructionContext.getMeasurementSaver().getSaveTableDataListener(configuration.getTableSaveName()));
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
		public Class<FluigentJob> getComponentInterface() {
			return FluigentJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public FluigentJobAddonFactory()
	{
		super(FluigentJobConfigurationAddon.class, CREATOR, FluigentJobConfigurationAddon.getMetadata());
	}
}
