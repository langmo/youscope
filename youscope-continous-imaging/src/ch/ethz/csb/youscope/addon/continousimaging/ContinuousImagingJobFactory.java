/**
 * 
 */
package ch.ethz.csb.youscope.addon.continousimaging;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;
import ch.ethz.csb.youscope.addon.adapters.CustomAddonCreator;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.ContinuousImagingJob;

/**
 * @author Moritz Lang
 */
public class ContinuousImagingJobFactory extends AddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public ContinuousImagingJobFactory()
	{
		this.addAddon(ContinuousImagingJob.DEFAULT_TYPE_IDENTIFIER, ContinuousImagingJobConfiguration.class, CREATOR_CONTINUOUS_IMAGING);
	}
	
	private static final CustomAddonCreator<ContinuousImagingJobConfiguration, ContinuousImagingJob> CREATOR_CONTINUOUS_IMAGING = new CustomAddonCreator<ContinuousImagingJobConfiguration,ContinuousImagingJob>()
	{
		@Override
		public ContinuousImagingJob createCustom(PositionInformation positionInformation, ContinuousImagingJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			ContinuousImagingJobImpl job;
			try
			{
				job = new ContinuousImagingJobImpl(positionInformation);
				job.setCamera(configuration.getCamera() == null ? null : configuration.getCamera().getCameraDevice());
				if(configuration.getChannel() == null)
					job.setChannel(null, null);
				else
					job.setChannel(configuration.getChannel().getChannelGroup(), configuration.getChannel().getChannel());
				job.setExposure(configuration.getExposure());
				job.setBurstImaging(configuration.isBurstImaging());
				if(configuration.isSaveImages())
				{
					job.addImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()));
					job.setImageDescription(configuration.getImageSaveName() + " (" + job.getImageDescription() + ")");
				}
				
			}
			catch(MeasurementRunningException e)
			{
				throw new AddonException("Could not create continuous imaging job since newly created job is already running.", e);
			} catch (RemoteException e) {
				throw new AddonException("Could not create continuous imaging job due to remote exception.", e);
			}
			return job;
		}

		@Override
		public Class<ContinuousImagingJob> getComponentInterface() {
			return ContinuousImagingJob.class;
		}
	};
}
