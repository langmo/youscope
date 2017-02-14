/**
 * 
 */
package org.youscope.plugin.measurementappender;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;
import org.youscope.addon.tool.ToolMetadata;


/**
 * @author Moritz Lang
 *
 */
public class AppenderToolFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor. Calling the superclass constructor {@link ToolAddonFactoryAdapter#ToolAddonFactoryAdapter(Class, org.youscope.addon.tool.ToolMetadata)}
	 * the adapter provides YouScope with the metadata of your tool (like its name), which is an instance of the interface {@link ToolMetadata}.
	 * Furthermore, the superclass constructor specifies the class of which an instance should be
	 * constructed when the user chooses to use your tool, e.g. the class of your tool itself.
	 * 
	 * We also do not have to bother to implement the functions of {@link ToolMetadata} ourselves. Instead, we call the function {@link AppenderTool#getMetadata()}.
	 * 
	 */
	public AppenderToolFactory()
	{
		super(AppenderTool.class, AppenderTool.getMetadata());
	}
}
