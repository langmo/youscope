package org.youscope.plugin.nonoptimizedpath;

import org.youscope.addon.pathoptimizer.PathOptimizerConfiguration;
import org.youscope.common.configuration.YSConfigAlias;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Non-optimized path through microplate
 * @author Moritz Lang
 *
 */
@YSConfigAlias("Non-Optimized Path")
@XStreamAlias("non-optimized-path")
public class NonOptimizedPathConfiguration extends PathOptimizerConfiguration 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 3041118882205729335L;
	/**
	 * Type identifier.
	 */
	public static final String TYPE_IDENTIFIER = "YouScope.path.NonOptimized";

	@Override
	public String getTypeIdentifier() {
		return TYPE_IDENTIFIER;
	}

	

}
