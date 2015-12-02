/**
 * 
 */
package ch.ethz.csb.youscope.addon.controller;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;
import ch.ethz.csb.youscope.addon.adapters.CustomAddonCreator;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.measurement.ComponentCreationException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;

/**
 * @author Moritz Lang
 */
public class ControllerJobAddonFactory extends AddonFactoryAdapter 
{
	private static final CustomAddonCreator<ControllerJobConfiguration, ControllerJob> CREATOR = new CustomAddonCreator<ControllerJobConfiguration, ControllerJob>()
	{

		@Override
		public ControllerJob createCustom(PositionInformation positionInformation,
				ControllerJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				ControllerJobImpl job = new ControllerJobImpl(positionInformation);
				
				job.addMessageListener(constructionContext.getLogger());
				job.setControllerScript(configuration.getControllerScript());
				job.setControllerScriptEngine(configuration.getControllerScriptEngine());
				
				if(configuration.getInputJob() != null)
				{
					Job inputJob = constructionContext.getComponentProvider().createJob(positionInformation, configuration.getInputJob());
					job.setInputJob(inputJob);
				}
				
				if(configuration.getOutputJob() != null)
				{
					Job outputJob = constructionContext.getComponentProvider().createJob(positionInformation, configuration.getOutputJob());
					job.setOutputJob(outputJob);
				}
				
				if(configuration.getControllerTableSaveName() != null)
				{
					job.addTableListener(constructionContext.getMeasurementSaver().getSaveTableDataListener(configuration.getControllerTableSaveName()));
				}

				return job;
				
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create controller job due to remote exception.", e);
			} catch (ComponentCreationException e) {
				throw new AddonException("Could not create all components of controller job.", e);
			} catch (MeasurementRunningException e) {
				throw new AddonException("Could not initialize newly created controller job since job is already running.", e);
			}
		}

		@Override
		public Class<ControllerJob> getComponentInterface() {
			return ControllerJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public ControllerJobAddonFactory()
	{
		super(ControllerJobConfigurationAddon.class, CREATOR, ControllerJobConfigurationAddon.getMetadata());
	}
}
