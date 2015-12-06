/**
 * 
 */
package org.youscope.plugin.positioncontrol;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class PositionControlFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public PositionControlFactory()
	{
		super(PositionControl.class, PositionControl.getMetadata());
	}
}
