/**
 * 
 */
package ch.ethz.csb.youscope.addon.imagingjob;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;
import ch.ethz.csb.youscope.addon.adapters.CustomAddonCreator;
import ch.ethz.csb.youscope.server.addon.ConstructionContext;
import ch.ethz.csb.youscope.shared.addon.AddonException;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.measurement.MeasurementRunningException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.job.basicjobs.ImagingJob;

/**
 * @author Moritz Lang
 */
public class ImagingJobAddonFactory extends AddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public ImagingJobAddonFactory()
	{
		super(ImagingJobConfigurationAddon.class, CREATOR, ImagingJobConfigurationAddon.getMetadata());
	}
	
	private static final CustomAddonCreator<ImagingJobConfiguration, ImagingJob> CREATOR = new CustomAddonCreator<ImagingJobConfiguration,ImagingJob>()
	{
		@Override
		public ImagingJob createCustom(PositionInformation positionInformation, ImagingJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			ImagingJob job;
			try
			{
				job = new ImagingJobImpl(positionInformation);
				job.setChannel(configuration.getChannelGroup(), configuration.getChannel());
				job.setExposure(configuration.getExposure());
				job.setCamera(configuration.getCamera());
				if(configuration.isSaveImages())
				{
					job.addImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()));
					job.setImageDescription(configuration.getImageSaveName() + " (" + job.getImageDescription() + ")");
				}
			}
			catch(MeasurementRunningException e)
			{
				throw new AddonException("Could not create imaging job since newly created job is already running.", e);
			} catch (RemoteException e) {
				throw new AddonException("Could not create imaging job due to remote exception.", e);
			}
			return job;
		}

		@Override
		public Class<ImagingJob> getComponentInterface() {
			return ImagingJob.class;
		}
	};
}
