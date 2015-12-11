/**
 * 
 */
package org.youscope.plugin.simplefocusscores;

import java.rmi.RemoteException;

import org.youscope.addon.focusscore.FocusScoreResource;
import org.youscope.common.ImageEvent;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.PositionInformation;
import org.youscope.common.measurement.resource.ResourceAdapter;
import org.youscope.common.measurement.resource.ResourceException;

/**
 * @author Moritz Lang
 *
 */
class VarianceFocusScoreAddon extends ResourceAdapter<VarianceFocusScoreConfiguration> implements FocusScoreResource
{
	VarianceFocusScoreAddon(PositionInformation positionInformation, VarianceFocusScoreConfiguration configuration) throws ConfigurationException
	{
		super(positionInformation, configuration, VarianceFocusScoreConfiguration.CONFIGURATION_ID, VarianceFocusScoreConfiguration.class,  "Variance focus score");
	}
	
	@Override
	public double calculateScore(ImageEvent<?> imageEvent) throws ResourceException, RemoteException
	{
		assertInitialized();
		if(imageEvent == null)
			throw new ResourceException("Image for which focus score should be calculated is null.");
		
		return new ImageAdapter(imageEvent).getVariance();
	}
}
