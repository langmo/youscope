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
package org.youscope.plugin.dropletmicrofluidics.defaultobserver;

import org.youscope.addon.component.ComponentAddonFactoryAdapter;
 
/**
 * Implementation of the an observer based on the discrete Fourier transformation. The observer separately learns the individual and the mean
 * droplet heights. 
 * @author Moritz Lang
 *
 */
public class DefaultObserverFactory extends ComponentAddonFactoryAdapter
{

	/**
	 * Constructor.
	 */
	public DefaultObserverFactory()
	{
		super(DefaultObserverConfiguration.TYPE_IDENTIFIER, DefaultObserverConfiguration.class, DefaultObserver.class);
	}
}
