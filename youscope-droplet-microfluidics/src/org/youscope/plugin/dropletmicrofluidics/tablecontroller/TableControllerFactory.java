/**
 * 
 */
package org.youscope.plugin.dropletmicrofluidics.tablecontroller;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;

/**
 * Controller for droplet-based microfluidics based on a syringe table.
 * @author Moritz Lang
 */
public class TableControllerFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public TableControllerFactory()
	{
		super(TableControllerUI.class, TableController.class, TableControllerUI.getMetadata());
	}
}
