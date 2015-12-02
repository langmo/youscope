/**
 * 
 */
package ch.ethz.csb.youscope.addon.simplefocusscores;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.ImageEvent;
import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.resource.ResourceAdapter;
import ch.ethz.csb.youscope.shared.measurement.resource.ResourceException;
import ch.ethz.csb.youscope.shared.resource.focusscore.FocusScoreResource;

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
	public double calculateScore(ImageEvent imageEvent) throws ResourceException, RemoteException
	{
		assertInitialized();
		if(imageEvent == null)
			throw new ResourceException("Image for which focus score should be calculated is null.");
		
		return new ImageAdapter(imageEvent).getVariance();
	}
}
