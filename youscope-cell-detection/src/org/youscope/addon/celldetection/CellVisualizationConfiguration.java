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
package org.youscope.addon.celldetection;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.image.ImageProducerConfiguration;
import org.youscope.common.resource.ResourceConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Subclasses of this abstract class represent the configuration for a cell visualization addon.
 * These subclasses should extend this class such that all necessary configuration information is stored therein.
 * It should be given that when starting the cell visualization algorithm of two objects with the same configuration and the same images,
 * the same result should be returned, independent of any prior state modification of an addon.  
 * @author Moritz Lang
 *
 */
@XStreamAlias("cell-visualization-configuration")
public abstract class CellVisualizationConfiguration extends ResourceConfiguration implements ImageProducerConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 1123512978102625712L;

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// do nothing.
		
	}
}
