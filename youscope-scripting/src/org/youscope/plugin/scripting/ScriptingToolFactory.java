/**
 * 
 */
package org.youscope.plugin.scripting;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class ScriptingToolFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public ScriptingToolFactory()
	{
		super(ScriptingTool.class, ScriptingTool.getMetadata());
	}
}
