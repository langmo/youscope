/**
 * 
 */
package org.youscope.plugin.composedimaging;

import java.awt.Dimension;
import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ComponentCreationException;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.basicjobs.CompositeJob;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
public class PlateScanningJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public PlateScanningJobAddonFactory()
	{
		super(PlateScanningJobConfigurationAddon.class, CREATOR, PlateScanningJobConfigurationAddon.getMetadata());
	}
	
	private static final CustomAddonCreator<PlateScanningJobConfiguration, PlateScanningJob> CREATOR = new CustomAddonCreator<PlateScanningJobConfiguration,PlateScanningJob>()
	{
		@Override
		public PlateScanningJob createCustom(PositionInformation positionInformation, PlateScanningJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			PlateScanningJobImpl job;
			try
			{
				job = new PlateScanningJobImpl(positionInformation);
				job.setDeltaX(configuration.getDeltaX());
				job.setDeltaY(configuration.getDeltaY());
				job.setNumTiles(new Dimension(configuration.getNumTilesX(), configuration.getNumTilesY()));
				
				for(int x = 0; x < configuration.getNumTilesX(); x++)
				{
					PositionInformation xPositionInformation = new PositionInformation(positionInformation, PositionInformation.POSITION_TYPE_XTILE, x);
					for(int y = 0; y < configuration.getNumTilesY(); y++)
					{
						PositionInformation yPositionInformation = new PositionInformation(xPositionInformation, PositionInformation.POSITION_TYPE_YTILE, y);
						
						CompositeJob jobContainer;
						try {
							jobContainer = constructionContext.getComponentProvider().createJob(yPositionInformation, CompositeJob.DEFAULT_TYPE_IDENTIFIER, CompositeJob.class);
						} catch (ComponentCreationException e) {
							throw new AddonException("Plate scanning jobs need the composite job plugin.", e);
						}
				
						
						// Add all child jobs
						for(JobConfiguration childJobConfig : configuration.getJobs())
						{
							Job childJob;
							try {
								childJob = constructionContext.getComponentProvider().createJob(yPositionInformation, childJobConfig);
							} catch (ComponentCreationException e) {
								throw new AddonException("Could not create child job.", e);
							}
							jobContainer.addJob(childJob);
						}
						
						job.addJob(jobContainer);
					}
				}
				
				
			}
			catch(MeasurementRunningException e)
			{
				throw new AddonException("Could not create job, since newly created job is already running.", e);
			}
			catch(RemoteException e)
			{
				throw new AddonException("Could not create job due to remote error.", e);
			}
			return job;
		}

		@Override
		public Class<PlateScanningJob> getComponentInterface() {
			return PlateScanningJob.class;
		}
	};
}
