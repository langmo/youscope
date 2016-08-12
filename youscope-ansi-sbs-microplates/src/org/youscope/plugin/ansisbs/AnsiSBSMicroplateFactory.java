/**
 * 
 */
package org.youscope.plugin.ansisbs;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;
/**
 * Factory for ANSI SBS microplates.
 * @author Moritz Lang
 */
public class AnsiSBSMicroplateFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public AnsiSBSMicroplateFactory()
	{
		addAddon(AnsiSBS96MicroplateConfiguration.TYPE_IDENTIFIER, AnsiSBS96MicroplateConfiguration.class, AnsiSBS96MicroplateResource.class);
		addAddon(AnsiSBS384MicroplateConfiguration.TYPE_IDENTIFIER, AnsiSBS384MicroplateConfiguration.class, AnsiSBS384MicroplateResource.class);
		addAddon(AnsiSBS1536MicroplateConfiguration.TYPE_IDENTIFIER, AnsiSBS1536MicroplateConfiguration.class, AnsiSBS1536MicroplateResource.class);
	}
}
