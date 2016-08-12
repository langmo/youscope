/**
 * 
 */
package org.youscope.plugin.screeningpath;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;
/**
 * Factory for screening paths.
 * @author Moritz Lang
 */
public class ScreeningPathFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public ScreeningPathFactory()
	{
		super(ScreeningPathConfiguration.TYPE_IDENTIFIER, ScreeningPathConfiguration.class, ScreeningPathResource.class);
		
	}
}
