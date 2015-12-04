/**
 * 
 */
package org.youscope.plugin.dropletmicrofluidics.flexiblecontroller;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;

/**
 * Controller for droplet-based microfluidics based on a syringe table.
 * @author Moritz Lang
 */
public class FlexibleControllerFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public FlexibleControllerFactory()
	{
		super(FlexibleControllerUI.class, FlexibleController.class, FlexibleControllerUI.getMetadata());
	}
}
