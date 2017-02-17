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
package org.youscope.plugin.simplefocusscores;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;

/**
 * @author Moritz Lang
 */ 
public class SimpleScoresAddonFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public SimpleScoresAddonFactory()
	{
		addAddon(AutocorrelationFocusScoreConfiguration.CONFIGURATION_ID, AutocorrelationFocusScoreConfiguration.class, AutocorrelationFocusScoreAddon.class);
		addAddon(VarianceFocusScoreConfiguration.CONFIGURATION_ID, VarianceFocusScoreConfiguration.class, VarianceFocusScoreAddon.class);
		
	}
}
