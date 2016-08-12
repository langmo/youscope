/**
 * 
 */
package org.youscope.plugin.bdbiosciencemicroplates;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;
/**
 * Factory for ANSI SBS microplates.
 * @author Moritz Lang
 */
public class BDBioscienceMultiwellTCMicroplateFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public BDBioscienceMultiwellTCMicroplateFactory()
	{
		addAddon(BDBioscienceMultiwellTC6MicroplateConfiguration.TYPE_IDENTIFIER, BDBioscienceMultiwellTC6MicroplateConfiguration.class, BDBioscienceMultiwellTC6MicroplateResource.class);
		addAddon(BDBioscienceMultiwellTC24MicroplateConfiguration.TYPE_IDENTIFIER, BDBioscienceMultiwellTC24MicroplateConfiguration.class, BDBioscienceMultiwellTC24MicroplateResource.class);
		addAddon(BDBioscienceMultiwellTC12MicroplateConfiguration.TYPE_IDENTIFIER, BDBioscienceMultiwellTC12MicroplateConfiguration.class, BDBioscienceMultiwellTC12MicroplateResource.class);
	}
}
