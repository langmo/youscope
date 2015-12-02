/**
 * 
 */
package ch.ethz.csb.youscope.shared.resource.focusscore;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import ch.ethz.csb.youscope.shared.configuration.ResourceConfiguration;

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

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
