/**
 * 
 */
package org.youscope.plugin.microplatejob;

import java.rmi.RemoteException;
import java.util.ServiceLoader;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.addon.pathoptimizer.PathOptimizer;
import org.youscope.addon.pathoptimizer.PathOptimizerPosition;
import org.youscope.common.Well;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.measurement.ComponentCreationException;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.Job;
import org.youscope.common.measurement.job.basicjobs.ChangePositionJob;
import org.youscope.common.measurement.job.basicjobs.CompositeJob;
import org.youscope.common.measurement.job.basicjobs.FocusingJob;
import org.youscope.plugin.microplatemeasurement.MicroplatePositionConfigurationDTO;

/**
 * @author Moritz Lang
 */
public class MicroplateJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	private static final CustomAddonCreator<MicroplateJobConfigurationDTO, CompositeJob> CREATOR = new CustomAddonCreator<MicroplateJobConfigurationDTO, CompositeJob>()
	{

		@Override
		public CompositeJob createCustom(PositionInformation mainPositionInformation,
				MicroplateJobConfigurationDTO configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{
				String stageDeviceID = configuration.getStageDevice();		
				
				CompositeJob microplateJob;
				try {
					microplateJob = constructionContext.getComponentProvider().createJob(mainPositionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
				} catch (ComponentCreationException e3) {
					throw new AddonException("Microplate measurements need the composite job plugin.", e3);
				}
				

				// Iterate over all wells and positions
				MicroplatePositionConfigurationDTO posConf = configuration.getMicroplatePositions();
				boolean isMultiPos = posConf.getWellNumPositionsX() > 1 || posConf.getWellNumPositionsY() > 1;
				boolean isWellMeasurement = !posConf.isAliasMicroplate();
				
				String pathOptimizerID = configuration.getPathOptimizerID();
				if(pathOptimizerID == null)
					pathOptimizerID = "CSB::NonOptimizedOptimizer";
				PathOptimizer pathOptimizer = getPathOptimizer(pathOptimizerID);
				if(pathOptimizer == null)
					throw new ConfigurationException("Path optimizer with ID \"" + pathOptimizerID + "\" not known.");
				if(!pathOptimizer.isApplicable(posConf))
					throw new ConfigurationException("Path optimizer with ID \"" + pathOptimizerID + "\" is not applicable for given position configuration.");
				Iterable<PathOptimizerPosition> path = pathOptimizer.getPath(posConf);
				if(path == null)
					throw new ConfigurationException("Path created by path optimizer \"" + pathOptimizerID + "\" is null. Try another optimizer.");
				
				for(PathOptimizerPosition position : path)
				{
				
						PositionInformation positionInformation;
						String locationString;
						if(isWellMeasurement && isMultiPos)
						{
							Well well = new Well(position.getWellY(), position.getWellX());
							positionInformation = new PositionInformation(well);
							positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_YTILE, position.getPositionY());
							positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_XTILE, position.getPositionX());
							
							locationString = "well " + well.getWellName() + ", tile [" + Integer.toString(position.getPositionY() + 1) + ", " + Integer.toString(position.getPositionX() + 1) + "]";
						}
						else if(isWellMeasurement)
						{
							Well well = new Well(position.getWellY(), position.getWellX());
							positionInformation = new PositionInformation(well);
							
							locationString = "well " + well.getWellName();
						}
						else if(isMultiPos)
						{
							positionInformation = mainPositionInformation;
							positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_MAIN_POSITION, position.getWellX());
							positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_YTILE, position.getPositionY());
							positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_XTILE, position.getPositionX());
							
							locationString = "position " + Integer.toString(position.getWellX()+1) + ", tile [" + Integer.toString(position.getPositionY() + 1) + ", " + Integer.toString(position.getPositionX() + 1) + "]";
						}
						else
						{
							positionInformation = mainPositionInformation;
							positionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_MAIN_POSITION, position.getWellX());
							
							locationString = "position " + Integer.toString(position.getWellX()+1);
						}

						
						// Create a job container in which all jobs at the given position are put into.
						CompositeJob jobContainer;
						try {
							jobContainer = constructionContext.getComponentProvider().createJob(positionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
						} catch (ComponentCreationException e2) {
							throw new AddonException("Microplate measurements need the composite job plugin.",e2);
						}
						
						jobContainer.setName("Job container for " + locationString);
						microplateJob.addJob(jobContainer);
						
						// Set position to well
						ChangePositionJob positionJob;
						try {
							positionJob = constructionContext.getComponentProvider().createJob(positionInformation, ChangePositionJob.DEFAULT_TYPE_IDENTIFIER, ChangePositionJob.class);
						} catch (ComponentCreationException e1) {
							throw new AddonException("Microplate measurements need the change position job plugin.", e1);
						}
						positionJob.setPosition(position.getX(), position.getY());
						positionJob.setStageDevice(stageDeviceID);
						positionJob.setName("Moving stage to " + locationString);
						jobContainer.addJob(positionJob);

						// Set Focus
						if(configuration.getFocusConfiguration() != null)
						{
							FocusingJob focusingJob;
							try {
								focusingJob = constructionContext.getComponentProvider().createJob(positionInformation, FocusingJob.DEFAULT_TYPE_IDENTIFIER, FocusingJob.class);
							} catch (ComponentCreationException e) {
								throw new AddonException("Microplate measurements need the focussing job plugin.", e);
							}
							focusingJob.setFocusAdjustmentTime(configuration.getFocusConfiguration().getAdjustmentTime());
							focusingJob.setPosition(position.getFocus(), false);
							focusingJob.setFocusDevice(configuration.getFocusConfiguration().getFocusDevice());
							focusingJob.setName("Setting focus for " + locationString);
							jobContainer.addJob(focusingJob);
						}

						// Add all other configured jobs
						JobConfiguration[] jobConfigurations = configuration.getJobs();
						for(JobConfiguration jobConfiguration : jobConfigurations)
						{
							Job job;
							try {
								job = constructionContext.getComponentProvider().createJob(positionInformation, jobConfiguration);
							} catch (ComponentCreationException e) {
								throw new AddonException("Could not create child job.", e);
							}
							jobContainer.addJob(job);
						}
						
							
				}
				return microplateJob;
				
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create job due to remote exception.", e);
			} catch (MeasurementRunningException e) {
				throw new AddonException("Could not initialize newly created job since job is already running.", e);
			}
		}

		@Override
		public Class<CompositeJob> getComponentInterface() {
			return CompositeJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public MicroplateJobAddonFactory()
	{
		super(MicroplateJobConfigurationAddon.class, CREATOR, MicroplateJobConfigurationAddon.getMetadata());
	}
	
	private static Iterable<PathOptimizer> getPathOptimizers()
    {
        ServiceLoader<PathOptimizer> pathOptimizers =
                ServiceLoader.load(PathOptimizer.class,
                		MicroplateJobAddonFactory.class.getClassLoader());
        return pathOptimizers;
    }
	
	private static PathOptimizer getPathOptimizer(String pathOptimizerID)
	{
		for (PathOptimizer addon : getPathOptimizers())
        {
        	if(addon.getOptimizerID().equals(pathOptimizerID))
                return addon;
        }
        return null;
	}
}
