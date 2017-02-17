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
