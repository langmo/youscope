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
package org.youscope.plugin.systemskin;

import org.youscope.addon.skin.SkinFactoryAdapter;

/**
 * Look and feel adjusting to the operating system default look and feel.
 * @author Moritz Lang
 *
 */
public class SystemSkinFactory extends SkinFactoryAdapter 
{
	/**
	 * Constructor.
	 */
	public SystemSkinFactory()
	{
		super(SystemSkin.class, SystemSkin.createMetadata());
	}
}
