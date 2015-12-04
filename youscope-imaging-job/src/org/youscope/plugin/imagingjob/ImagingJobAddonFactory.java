/**
 * 
 */
package org.youscope.plugin.imagingjob;

import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.job.basicjobs.ImagingJob;

/**
 * @author Moritz Lang
 */
public class ImagingJobAddonFactory extends ComponentAddonFactoryAdapter 
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
