/**
 * 
 */
package org.youscope.plugin.zigzagpath;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;
/**
 * Factory for zig-zag paths.
 * @author Moritz Lang
 */
public class ZigZagPathFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public ZigZagPathFactory()
	{
		super(ZigZagPathConfiguration.TYPE_IDENTIFIER, ZigZagPathConfiguration.class, ZigZagPathResource.class);
		
	}
}
