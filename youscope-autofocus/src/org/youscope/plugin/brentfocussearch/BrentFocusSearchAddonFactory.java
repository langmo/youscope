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
package org.youscope.plugin.brentfocussearch;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;
 
/**
 * Implementation of the Brent focus search algorithm
 * @author Moritz Lang
 *
 */
public class BrentFocusSearchAddonFactory extends ComponentAddonFactoryAdapter
{

	/**
	 * Constructor.
	 */
	public BrentFocusSearchAddonFactory()
	{
		super(BrentFocusSearchConfiguration.CONFIGURATION_ID, BrentFocusSearchConfiguration.class, BrentFocusSearchAddon.class);
	}
}
