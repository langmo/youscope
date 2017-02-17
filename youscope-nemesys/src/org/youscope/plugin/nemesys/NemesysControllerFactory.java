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
package org.youscope.plugin.nemesys;

import org.youscope.addon.tool.ToolAddonFactoryAdapter;

/**
 * Tool to control the Nemesys Syringe system.
 * @author Moritz Lang
 *
 */
public class NemesysControllerFactory extends ToolAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public NemesysControllerFactory()
	{
		super(NemesysController.class, NemesysController.getMetadata());
	}
}
