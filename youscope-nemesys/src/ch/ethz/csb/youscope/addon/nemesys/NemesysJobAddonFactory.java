/**
 * 
 */
package ch.ethz.csb.youscope.addon.nemesys;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;
import ch.ethz.csb.youscope.addon.adapters.CustomAddonCreator;
import ch.ethz.csb.youscope.addon.nemesys.NemesysJob;
import ch.ethz.csb.youscope.addon.nemesys.NemesysJobConfiguration;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;

/**
 * @author Moritz Lang
 */
public class NemesysJobAddonFactory extends AddonFactoryAdapter 
{
	private static final CustomAddonCreator<NemesysJobConfiguration, NemesysJob> CREATOR = new CustomAddonCreator<NemesysJobConfiguration, NemesysJob>()
	{

		@Override
		public NemesysJob createCustom(PositionInformation positionInformation,
				NemesysJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				NemesysJobImpl job = new NemesysJobImpl(positionInformation);

				job.addMessageListener(constructionContext.getLogger());
				job.setScript(configuration.getScript());
				job.setScriptEngine(configuration.getScriptEngine());
				job.setNemesysDeviceName(configuration.getNemesysDevice());
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
		public Class<NemesysJob> getComponentInterface() {
			return NemesysJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public NemesysJobAddonFactory()
	{
		super(NemesysJobConfigurationAddon.class, CREATOR, NemesysJobConfigurationAddon.getMetadata());
	}
}
