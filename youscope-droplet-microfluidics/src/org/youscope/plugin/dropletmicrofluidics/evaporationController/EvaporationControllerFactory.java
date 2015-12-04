/**
 * 
 */
package org.youscope.plugin.dropletmicrofluidics.evaporationController;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;

/**
 * Controller for droplet-based microfluidics based on a syringe table.
 * @author Moritz Lang
 */
public class EvaporationControllerFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public EvaporationControllerFactory()
	{
		super(EvaporationControllerUI.class, EvaporationController.class, EvaporationControllerUI.getMetadata());
	}
}
