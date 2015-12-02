/**
 * 
 */
package ch.ethz.csb.youscope.addon.multicameracontinuousimaging;

import java.util.ArrayList;
import java.util.List;

import ch.ethz.csb.youscope.shared.addon.XMLConfigurationProvider;
import ch.ethz.csb.youscope.shared.configuration.Configuration;

/**
 * @author Moritz Lang
 *
 */
public class MultiCameraContinousImagingXMLConfigurationProvider implements XMLConfigurationProvider
{

	@Override
	public List<Class<? extends Configuration>> getConfigurationClasses()
	{
		List<Class<? extends Configuration>> classes = new ArrayList<Class<? extends Configuration>>();
		classes.add(MultiCameraContinousImagingConfigurationDTO.class);
		return classes;
	}

}
