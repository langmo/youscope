/**
 * 
 */
package org.youscope.plugin.simplefocusscores;

import java.rmi.RemoteException;

import org.youscope.addon.focusscore.FocusScoreResource;
import org.youscope.common.ImageEvent;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.ResourceConfiguration;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.resource.ResourceAdapter;
import org.youscope.common.measurement.resource.ResourceException;

/**
 * @author Moritz Lang
 *
 */
class AutocorrelationFocusScoreAddon extends ResourceAdapter<AutocorrelationFocusScoreConfiguration> implements FocusScoreResource
{
	AutocorrelationFocusScoreAddon(PositionInformation positionInformation, ResourceConfiguration configuration) throws ConfigurationException
	{
		super(positionInformation, configuration, AutocorrelationFocusScoreConfiguration.CONFIGURATION_ID, AutocorrelationFocusScoreConfiguration.class, "Autocorrelation score");
	}
	
	@Override
	public double calculateScore(ImageEvent<?> imageEvent) throws ResourceException, RemoteException
	{
		assertInitialized();
		if(imageEvent == null)
			throw new ResourceException("Image for which focus score should be calculated is null.");
		
		ImageAdapter imageAdapter = new ImageAdapter(imageEvent);
		double variance = imageAdapter.getVariance();
		double mean = imageAdapter.getMean();
		double[][] image = imageAdapter.getScaledImage();
		int width = imageAdapter.getWidth();
		int height = imageAdapter.getHeight();
		int lag = getConfiguration().getLag();
		if(lag <= 0)
			throw new ResourceException("Lag must be positive.");
		double score = (height - lag)*width*variance;
		for(int j=0;j<height-lag;j++)
		{
			for(int i=0;i<width;i++)
			{
				score-=(image[j][i]-mean)*(image[j+lag][i]-mean);
			}
		}
		return score;
	}
}
