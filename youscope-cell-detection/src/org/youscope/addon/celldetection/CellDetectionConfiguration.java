/**
 * 
 */
package org.youscope.addon.celldetection;

import org.youscope.common.configuration.ResourceConfiguration;
import org.youscope.common.configuration.TableProducerConfiguration;

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

	@Override
	public CellDetectionConfiguration clone()
	{
		try {
			return (CellDetectionConfiguration) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("Clone not supported.", e);// will not happen.
		}
	}
	
	/**
	 * Returns true if the detection algorithm with the current configuration generates a label image.
	 * @return True if a detection image is generated with this algorithm, false otherwise.
	 */
	public abstract boolean isGenerateLabelImage();
}
