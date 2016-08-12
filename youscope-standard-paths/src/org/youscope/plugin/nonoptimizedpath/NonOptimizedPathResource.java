/**
 * 
 */
package org.youscope.plugin.nonoptimizedpath;

import java.awt.geom.Point2D.Double;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.youscope.addon.pathoptimizer.PathOptimizerResource;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;

/**
 * This optimizer actually does not really optimize the path in any way, but just iterates through all wells from left to right and from top to bottom.
 * The same for the positions in a well.
 * @author Moritz Lang
 *
 */
public class NonOptimizedPathResource extends ResourceAdapter<NonOptimizedPathConfiguration> implements PathOptimizerResource
{

	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -7269306932049313657L;

	/**
	 * Constructor.
	 * @param positionInformation logical position in measurement.
	 * @param configuration Configuration of resource.
	 * @throws RemoteException 
	 * @throws ConfigurationException
	 */
	public NonOptimizedPathResource(PositionInformation positionInformation, NonOptimizedPathConfiguration configuration) throws RemoteException, ConfigurationException {
		super(positionInformation, configuration, NonOptimizedPathConfiguration.TYPE_IDENTIFIER, NonOptimizedPathConfiguration.class, "Non-optimized path");
		
	}

	@Override
	public List<PositionInformation> getPath(Map<PositionInformation, ? extends Double> positions)
			throws ResourceException, RemoteException {
		ArrayList<PositionInformation> path = new ArrayList<>(positions.keySet());
		Collections.sort(path);
		return path;
	}
}
