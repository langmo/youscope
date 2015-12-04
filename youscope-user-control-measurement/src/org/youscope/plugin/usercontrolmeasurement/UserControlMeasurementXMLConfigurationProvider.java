/**
 * 
 */
package org.youscope.plugin.usercontrolmeasurement;

import java.util.ArrayList;
import java.util.List;

import org.youscope.addon.XMLConfigurationProvider;
import org.youscope.common.configuration.Configuration;

/**
 * @author Moritz Lang
 *
 */
public class UserControlMeasurementXMLConfigurationProvider implements XMLConfigurationProvider
{

	@Override
	public List<Class<? extends Configuration>> getConfigurationClasses()
	{
		List<Class<? extends Configuration>> classes = new ArrayList<Class<? extends Configuration>>();
		classes.add(UserControlMeasurementConfiguration.class);
		return classes;
	}

}
