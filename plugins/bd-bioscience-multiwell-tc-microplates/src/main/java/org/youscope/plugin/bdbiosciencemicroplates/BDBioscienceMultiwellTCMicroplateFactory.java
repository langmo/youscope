/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
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
