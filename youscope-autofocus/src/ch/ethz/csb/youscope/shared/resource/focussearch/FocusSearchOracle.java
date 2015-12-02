package ch.ethz.csb.youscope.shared.resource.focussearch;

import java.rmi.Remote;
import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.measurement.resource.ResourceException;

/**
 * Oracle for a focus score algorithm to get the focus score for a given focal plane.
 * @author langmo
 *
 */
public interface FocusSearchOracle extends Remote {
	/**
	 * Returns the focal score for a given focal plane. Higher scores indicate better focal planes.
	 * @param relativeFocusPosition The focal position (z-position) for which the focus score should be querried.
	 * @return The focus score of the given focal position.
	 * @throws ResourceException
	 * @throws RemoteException
	 */
	public double getFocusScore(double relativeFocusPosition) throws ResourceException, RemoteException;
}
