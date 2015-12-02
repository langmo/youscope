/**
 * 
 */
package ch.ethz.csb.youscope.addon.continousimaging;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.csb.youscope.shared.addon.XMLConfigurationProvider;
import ch.ethz.csb.youscope.shared.configuration.Configuration;

/**
 * @author Moritz Lang
 *
 */
public class AllXMLConfigurationProvider implements XMLConfigurationProvider
{

	@Override
	public List<Class<? extends Configuration>> getConfigurationClasses()
	{
		List<Class<? extends Configuration>> classes = new ArrayList<Class<? extends Configuration>>();
		classes.add(ContinousImagingMeasurementConfiguration.class);
		classes.add(ShortContinuousImagingJobConfiguration.class);
		return classes;
	}

}
