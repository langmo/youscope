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
package org.youscope.plugin.exhaustivefocussearch;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;

/**
 * Factory for the creation of exhaustive search focus search.
 * @author Moritz Lang
 *
 */
public class ExhaustiveFocusSearchAddonFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public ExhaustiveFocusSearchAddonFactory()
	{
		super(ExhaustiveFocusSearchConfiguration.CONFIGURATION_ID, ExhaustiveFocusSearchConfiguration.class, ExhaustiveFocusSearchAddon.class);
	}
}
