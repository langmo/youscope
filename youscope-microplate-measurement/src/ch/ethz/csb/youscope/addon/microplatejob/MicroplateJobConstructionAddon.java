/**
 * 
 */
package ch.ethz.csb.youscope.addon.microplatejob;

import java.rmi.RemoteException;
import java.util.ServiceLoader;

import ch.ethz.csb.youscope.addon.microplatemeasurement.MicroplatePositionConfigurationDTO;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.server.addon.job.JobConstructionAddon;
import ch.ethz.csb.youscope.shared.Well;
import ch.ethz.csb.youscope.shared.addon.pathoptimizer.PathOptimizer;
import ch.ethz.csb.youscope.shared.addon.pathoptimizer.PathOptimizerPosition;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.measurement.ComponentCreationException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;
import ch.ethz.csb.youscope.shared.measurement.job.JobCreationException;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.ChangePositionJob;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.CompositeJob;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.FocusingJob;

/**
 * @author Moritz Lang
 * 
 */
public class MicroplateJobConstructionAddon implements JobConstructionAddon
{

	@Override
	public Job createJob(JobConfiguration generalJobConfiguration, ConstructionContext jobInitializer, PositionInformation mainPositionInformation) throws RemoteException, ConfigurationException, JobCreationException
	{
		if(!(generalJobConfiguration instanceof MicroplateJobConfigurationDTO))
		{
			throw new ConfigurationException("Job configuration is not a microplate job.");
		}
		MicroplateJobConfigurationDTO configuration = (MicroplateJobConfigurationDTO)generalJobConfiguration;

		String stageDeviceID = configuration.getStageDevice();		
		
		CompositeJob microplateJob;
		try {
			microplateJob = jobInitializer.getComponentProvider().createJob(mainPositionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
		} catch (ComponentCreationException e3) {
			throw new JobCreationException("Microplate measurements need the composite job plugin.", e3);
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

				try
				{
					// Create a job container in which all jobs at the given position are put into.
					CompositeJob jobContainer;
					try {
						jobContainer = jobInitializer.getComponentProvider().createJob(positionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
					} catch (ComponentCreationException e2) {
						throw new JobCreationException("Microplate measurements need the composite job plugin.",e2);
					}
					
					jobContainer.setName("Job container for " + locationString);
					microplateJob.addJob(jobContainer);
					
					// Set position to well
					ChangePositionJob positionJob;
					try {
						positionJob = jobInitializer.getComponentProvider().createJob(positionInformation, ChangePositionJob.DEFAULT_TYPE_IDENTIFIER, ChangePositionJob.class);
					} catch (ComponentCreationException e1) {
						throw new JobCreationException("Microplate measurements need the change position job plugin.", e1);
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
							focusingJob = jobInitializer.getComponentProvider().createJob(positionInformation, FocusingJob.DEFAULT_TYPE_IDENTIFIER, FocusingJob.class);
						} catch (ComponentCreationException e) {
							throw new JobCreationException("Microplate measurements need the focussing job plugin.", e);
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
							job = jobInitializer.getComponentProvider().createJob(positionInformation, jobConfiguration);
						} catch (ComponentCreationException e) {
							throw new JobCreationException("Could not create child job.", e);
						}
						jobContainer.addJob(job);
					}
				}
				catch(MeasurementRunningException e)
				{
					throw new ConfigurationException("Could not create measurement since it is already running.", e);
				}
					
		}
		return microplateJob;
	}
	
	private static Iterable<PathOptimizer> getPathOptimizers()
    {
        ServiceLoader<PathOptimizer> pathOptimizers =
                ServiceLoader.load(PathOptimizer.class,
                		MicroplateJobConstructionAddon.class.getClassLoader());
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
