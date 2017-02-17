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
