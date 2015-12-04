/**
 * 
 */
package org.youscope.plugin.controller;

import java.util.ArrayList;
import java.util.List;

import org.youscope.addon.XMLConfigurationProvider;
import org.youscope.common.configuration.Configuration;

/**
 * @author Moritz Lang
 *
 */
public class ControllerJobXMLConfigurationProvider implements XMLConfigurationProvider
{

	@Override
	public List<Class<? extends Configuration>> getConfigurationClasses()
	{
		List<Class<? extends Configuration>> classes = new ArrayList<Class<? extends Configuration>>();
		classes.add(ControllerJobConfiguration.class);
		return classes;
	}
}
