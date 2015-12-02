/**
 * 
 */
package ch.ethz.csb.youscope.addon.lifecelldetection;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;
import ch.ethz.csb.youscope.addon.adapters.CustomAddonCreator;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.addon.celldetection.CellDetectionAddon;
import ch.ethz.csb.youscope.shared.addon.celldetection.CellDetectionConfiguration;
import ch.ethz.csb.youscope.shared.addon.celldetection.CellVisualizationAddon;
import ch.ethz.csb.youscope.shared.addon.celldetection.CellVisualizationConfiguration;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.measurement.ComponentCreationException;
import ch.ethz.csb.youscope.shared.measurement.ImageProducer;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.Job;

/**
 * @author Moritz Lang
 */
public class CellDetectionJobAddonFactory extends AddonFactoryAdapter 
{
	private static final CustomAddonCreator<CellDetectionJobConfiguration, CellDetectionJob> CREATOR = new CustomAddonCreator<CellDetectionJobConfiguration, CellDetectionJob>()
	{

		@Override
		public CellDetectionJob createCustom(PositionInformation positionInformation,
				CellDetectionJobConfiguration configuration, ConstructionContext constructionContext)
						throws ConfigurationException, AddonException 
		{
			try
			{			
				// Get detection & visualization algorithms.
				CellDetectionConfiguration detectionAlgorithmConfiguration = configuration.getDetectionAlgorithmConfiguration();
				CellVisualizationConfiguration visualizationAlgorithmConfiguration = configuration.getVisualizationAlgorithmConfiguration();
				if(detectionAlgorithmConfiguration == null)
				{
					throw new ConfigurationException("Configuration of cell detection algorithm is null.");
				}
				CellDetectionAddon cellDetectionAddon = constructionContext.getComponentProvider().createComponent(positionInformation, detectionAlgorithmConfiguration, CellDetectionAddon.class);
				CellVisualizationAddon cellVisualizationAddon;
				if(visualizationAlgorithmConfiguration != null)
				{
					cellVisualizationAddon = constructionContext.getComponentProvider().createComponent(positionInformation, visualizationAlgorithmConfiguration, CellVisualizationAddon.class);
					
				}
				else
					cellVisualizationAddon = null;
				 
				CellDetectionJobImpl cellDetectionJob = new CellDetectionJobImpl(positionInformation);
				cellDetectionJob.addMessageListener(constructionContext.getLogger());
				cellDetectionJob.setDetectionAlgorithm(cellDetectionAddon);
				cellDetectionJob.setVisualizationAlgorithm(cellVisualizationAddon);
				cellDetectionJob.setMinimalTimeMS(configuration.getMinimalTimeMS());
							
				// Detection Image producing job
				JobConfiguration detectionImageProducerConfig = configuration.getDetectionJob();
				if(detectionImageProducerConfig == null)
					throw new ConfigurationException("No image producing job defined to take the image used for detection.");
				Job detectionImageProducer = constructionContext.getComponentProvider().createJob(positionInformation, configuration.getDetectionJob());
				if(detectionImageProducer == null)
					throw new ConfigurationException("Type of job defined to take the image used for detection (\"" + detectionImageProducerConfig.getTypeIdentifier() + "\") unknown.");
				if(!(detectionImageProducer instanceof ImageProducer))
						throw new ConfigurationException("Job defined to take the image used for detection (\"" + detectionImageProducerConfig.getTypeIdentifier() + "\") does not implement interface ImageProducer.");
				
				// Add image save listeners
				if(configuration.getSegmentationImageSaveName() != null)
				{
					cellDetectionJob.addSegmentationImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getSegmentationImageSaveName()));
				}
				if(configuration.getControlImageSaveName() != null)
				{
					cellDetectionJob.addControlImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getControlImageSaveName()));
				}
				
				// Change image name for display
				if(configuration.getControlImageSaveName() != null)
				{
					cellDetectionJob.setImageDescription(configuration.getControlImageSaveName() + " (cell-detection control image)");
				}
				else if(configuration.getVisualizationAlgorithmConfiguration() != null)
				{
					cellDetectionJob.setImageDescription("cell-detection control image");
				}
				else if(configuration.getSegmentationImageSaveName() != null)
				{
					cellDetectionJob.setImageDescription(configuration.getSegmentationImageSaveName() + " (cell-detection segmentation image)");
				}
				else
				{
					cellDetectionJob.setImageDescription("cell-detection segmentation image");
				}
				
				if(configuration.getCellTableSaveName() != null)
				{
					cellDetectionJob.addTableListener(constructionContext.getMeasurementSaver().getSaveTableDataListener(configuration.getCellTableSaveName()));
				}
				cellDetectionJob.addJob(detectionImageProducer);
				
				// other image producing jobs
				for(JobConfiguration childJobConfig : configuration.getJobs())
				{
					Job childJob = constructionContext.getComponentProvider().createJob(positionInformation, childJobConfig);
					if(!(childJob instanceof ImageProducer))
						throw new ConfigurationException("Job defined to take the image used for quantification (\"" + childJobConfig.getTypeIdentifier() + "\") does not implement interface ImageProducer.");
				
					cellDetectionJob.addJob(childJob);
				}

				return cellDetectionJob;
				
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create job due to remote exception.", e);
			} catch (ComponentCreationException e) {
				throw new AddonException("Could not create all components of job.", e);
			} catch (MeasurementRunningException e) {
				throw new AddonException("Could not initialize newly created job since job is already running.", e);
			}
		}

		@Override
		public Class<CellDetectionJob> getComponentInterface() {
			return CellDetectionJob.class;
		}
		
	};

	/**
	 * Constructor.
	 */
	public CellDetectionJobAddonFactory()
	{
		super(CellDetectionJobConfigurationAddon.class, CREATOR, CellDetectionJobConfigurationAddon.getMetadata());
	}
}
