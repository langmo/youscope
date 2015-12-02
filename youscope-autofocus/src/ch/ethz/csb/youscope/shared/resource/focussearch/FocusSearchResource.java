/**
 * 
 */
package ch.ethz.csb.youscope.shared.resource.focussearch;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.measurement.resource.Resource;
import ch.ethz.csb.youscope.shared.measurement.resource.ResourceException;

/**
 * A focus search algorithm addon is a class defining an algorithm to autofocus based on a given focus score.
 * @author Moritz Lang
 *
 */
public interface FocusSearchResource extends Resource
{
	/**
	 * Runs the focus score algorithm using the given oracle to determine the focus scores.
	 * @param oracle An oracle to determine the focus score at a given (relative) focal plane.
	 * @return The focus position of the best focal plane.
	 * @throws ResourceException 
	 * @throws RemoteException 
	 */
	public double runAutofocus(FocusSearchOracle oracle) throws ResourceException, RemoteException;
}
