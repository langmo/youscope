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
package org.youscope.plugin.quickdetect;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;

/**
 * @author Moritz Lang
 */
public class QuickDetectAddonFactory extends ComponentAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public QuickDetectAddonFactory()
	{
		super(QuickDetectConfiguration.TYPE_IDENTIFIER, QuickDetectConfiguration.class, QuickDetectAddon.class);
	}
}
