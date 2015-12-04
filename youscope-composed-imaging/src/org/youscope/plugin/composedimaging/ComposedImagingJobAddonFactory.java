/**
 * 
 */
package org.youscope.plugin.composedimaging;

import java.awt.Dimension;
import java.rmi.RemoteException;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentAddonFactoryAdapter;
import org.youscope.addon.component.ConstructionContext;
import org.youscope.addon.component.CustomAddonCreator;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.PositionInformation;

/**
 * @author Moritz Lang
 */
public class ComposedImagingJobAddonFactory extends ComponentAddonFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public ComposedImagingJobAddonFactory()
	{
		super(ComposedImagingJobConfigurationAddon.class, CREATOR, ComposedImagingJobConfigurationAddon.getMetadata());
	}
	
	private static final CustomAddonCreator<ComposedImagingJobConfiguration, ComposedImagingJob> CREATOR = new CustomAddonCreator<ComposedImagingJobConfiguration,ComposedImagingJob>()
	{
		@Override
		public ComposedImagingJob createCustom(PositionInformation positionInformation, ComposedImagingJobConfiguration configuration,
				ConstructionContext constructionContext) throws ConfigurationException, AddonException 
		{
			ComposedImagingJobImpl job;
			try
			{
				job = new ComposedImagingJobImpl(positionInformation);
				double dx = configuration.getPixelSize() * configuration.getNumPixels().width * (1 - configuration.getOverlap());
				double dy = configuration.getPixelSize() * configuration.getNumPixels().height * (1 - configuration.getOverlap());
				job.setChannel(configuration.getChannelGroup(), configuration.getChannel());
				job.setExposure(configuration.getExposure());
				job.setDeltaX(dx);
				job.setDeltaY(dy);
				job.setSubImageNumber(new Dimension(configuration.getNx(), configuration.getNy()));
					
				if(configuration.isSaveImages())
					job.addImageListener(constructionContext.getMeasurementSaver().getSaveImageListener(configuration.getImageSaveName()));
				
				
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
		public Class<ComposedImagingJob> getComponentInterface() {
			return ComposedImagingJob.class;
		}
	};
}
