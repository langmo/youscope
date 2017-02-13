/**
 * 
 */
package org.youscope.plugin.custommetadata;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class CustomMetadataToolFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public CustomMetadataToolFactory()
	{
		super(CustomMetadataTool.class, CustomMetadataTool.getMetadata());
	}
} 
