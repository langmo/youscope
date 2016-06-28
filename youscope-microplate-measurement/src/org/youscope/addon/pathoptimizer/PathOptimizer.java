/**
 * 
 */
package org.youscope.addon.pathoptimizer;

import org.youscope.common.resource.ResourceException;
import org.youscope.plugin.microplatemeasurement.MicroplatePositionConfiguration;

/**
 * A path optimizer is a method to optimize the path of a microplate measurement, such that the distances between the measured
 * wells/ positions is minimal.
 * @author Moritz Lang
 *
 */
public interface PathOptimizer
{
	/**
	 * Returns the optimized path for a given position configuration, or zero if not applicable.
	 * @param posConf Configuration of which positions should be in the path.
	 * @return optimized path, or null.
	 * @throws ResourceException 
	 */
	public Iterable<PathOptimizerPosition> getPath(MicroplatePositionConfiguration posConf) throws ResourceException;
	
	/**
	 * Returns true if this optimizer can be used to optimize the path for the positions in the respective configuration.
	 * @param posConf Configuration of the positions which should be in the path.
	 * @return TRUE if this optimizer can be used for the given path.
	 */
	public boolean isApplicable(MicroplatePositionConfiguration posConf);
	
	/**
	 * Returns a value in between 0 and 1 indicating how specific the given optimizer is, i.e. how "optimal" the calculated path can be expected to be.
	 * A value of 0 indicates an unspecific (i.e. probably bad path), a value of 1 a very specific (i.e. the optimal path). In general, for a given problem
	 * the optimizer which returns the highest specificity should be preferred, if measurement speed is important.
	 * In general, calculating the optimal path is NP hard. Most optimizers therefore either do not return the global optimal path, but only
	 * a path which is expected to be good, or, impose certain requirements on the selected wells and positions.
	 * @param posConf The position configuration for which the optimal path should be calculated.
	 * @return The specificity (0-1) of the optimizer for the given position configuration, or -1, if the optimizer can not be used to calculate the path for the given position configuration.
	 */
	public double getSpecificity(MicroplatePositionConfiguration posConf);
	
	/**
	 * Returns a human readable short name of the optimizer.
	 * @return Human readable short name.
	 */
	public String getName();
	
	/**
	 * Returns the ID of this optimizer. The ID should be unique.
	 * @return ID of the optimizer.
	 */
	public String getOptimizerID();
}
