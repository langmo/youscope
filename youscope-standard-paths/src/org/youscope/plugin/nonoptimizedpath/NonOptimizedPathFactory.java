/**
 * 
 */
package org.youscope.plugin.nonoptimizedpath;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;
/**
 * Factory for Non-optimized paths.
 * @author Moritz Lang
 */
public class NonOptimizedPathFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public NonOptimizedPathFactory()
	{
		super(NonOptimizedPathConfiguration.TYPE_IDENTIFIER, NonOptimizedPathConfiguration.class, NonOptimizedPathResource.class);
		
	}
}
