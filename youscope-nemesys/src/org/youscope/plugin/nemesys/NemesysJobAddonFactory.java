/**
 * 
 */
package org.youscope.plugin.nemesys;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.plugin.nemesys.NemesysJob;
import org.youscope.plugin.nemesys.NemesysJobConfiguration;

/**
 * @author Moritz Lang
 */
public class NemesysJobAddonFactory extends ComponentAddonFactoryAdapter 
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
