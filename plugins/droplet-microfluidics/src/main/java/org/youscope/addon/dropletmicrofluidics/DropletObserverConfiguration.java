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
package org.youscope.addon.dropletmicrofluidics;

import org.youscope.common.resource.ResourceConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Configuration of an observer for droplet based microfluidics.
 * @author Moritz Lang
 *
 */
@XStreamAlias("droplet-observer-configuration")
public abstract class DropletObserverConfiguration  extends ResourceConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 6268977101312936654L;

	

}
