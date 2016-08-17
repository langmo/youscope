/**
 * 
 */
package org.youscope.plugin.sawpath;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;
/**
 * Factory for Non-optimized paths.
 * @author Moritz Lang
 */
public class SawPathFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public SawPathFactory()
	{
		super(SawPathConfiguration.TYPE_IDENTIFIER, SawPathConfiguration.class, SawPathResource.class);
		
	}
}
