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

import org.youscope.common.resource.ResourceConfiguration;
import org.youscope.common.table.TableProducerConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Subclasses of this abstract class represent the configuration for a cell detection addon.
 * These subclasses should extend this class such that all necessary configuration information is stored therein.
 * It should be given that when starting the cell detection algorithm of two objects with the same configuration and the same images,
 * the same result should be returned, independent of any prior state modification of an addon.  
 * @author Moritz Lang
 *
 */
@XStreamAlias("cell-detection-configuration")
public abstract class CellDetectionConfiguration extends ResourceConfiguration implements TableProducerConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 1623512978102625712L;
	
	/**
	 * Returns true if the detection algorithm with the current configuration generates a label image.
	 * @return True if a detection image is generated with this algorithm, false otherwise.
	 */
	public abstract boolean isGenerateLabelImage();
}
