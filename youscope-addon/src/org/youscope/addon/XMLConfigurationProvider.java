/**
 * 
 */
package org.youscope.addon;

import java.util.List;

import org.youscope.common.configuration.Configuration;

/**
 * This class is used by YouScope's XML import/export mechanism to obtain information about all classes which can be exported to XML.
 * Each plugin which provides data transfer objects should also expose this service provider.
 * @author Moritz Lang
 * 
 */
public interface XMLConfigurationProvider
{
	/**
	 * Returns an list of all classes which can be saved to XML.
	 * @return List of classes which can be saved to XML.
	 */
	public List<Class<? extends Configuration>> getConfigurationClasses();
}
