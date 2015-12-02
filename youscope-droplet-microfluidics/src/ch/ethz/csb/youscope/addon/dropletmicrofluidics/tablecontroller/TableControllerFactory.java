/**
 * 
 */
package ch.ethz.csb.youscope.addon.dropletmicrofluidics.tablecontroller;

import ch.ethz.csb.youscope.addon.adapters.AddonFactoryAdapter;

/**
 * Controller for droplet-based microfluidics based on a syringe table.
 * @author Moritz Lang
 */
public class TableControllerFactory extends AddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public TableControllerFactory()
	{
		super(TableControllerUI.class, TableController.class, TableControllerUI.getMetadata());
	}
}
