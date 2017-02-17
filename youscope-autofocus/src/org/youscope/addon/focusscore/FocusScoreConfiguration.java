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
package org.youscope.addon.focusscore;

import org.youscope.common.resource.ResourceConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Subclasses of this abstract class represent the configuration for a focus score addon.
 * These subclasses should extend this class such that all necessary configuration information is stored therein.
 * It should be given that when starting the focus score algorithm of two objects with the same configuration and the same images,
 * the same result should be returned, independent of any prior state modification of an addon.  
 * @author Moritz Lang
 *
 */
@XStreamAlias("focus-score-configuration")
public abstract class FocusScoreConfiguration extends ResourceConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 1623512978102625712L;
}
