/**
 * 
 */
package org.youscope.addon.focussearch;

import org.youscope.common.configuration.ResourceConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Subclasses of this abstract class represent the configuration for a focus search algorithm addon.
 * These subclasses should extend this class such that all necessary configuration information is stored therein.
 * @author Moritz Lang
 *
 */
@XStreamAlias("focus-search-configuration")
public abstract class FocusSearchConfiguration extends ResourceConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 1623512978102625788L;

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
