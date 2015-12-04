/**
 * 
 */
package org.youscope.addon.focusscore;

import java.rmi.RemoteException;

import org.youscope.common.ImageEvent;
import org.youscope.common.measurement.resource.Resource;
import org.youscope.common.measurement.resource.ResourceException;

/**
 * A cell focus score resource is a class supporting scoring the quality of the focal plane by an image taken in the plane.
 * @author Moritz Lang
 *
 */
public interface FocusScoreResource  extends Resource 
{
		
	/**
	 * Calculates the focus score for the given image. The higher the score, the better the focus.
	 * The focus score must be larger or equal zero. The upper bound-if one exists-is implementation dependent.
	 * It can be assumed that the focus score is approximately continuous (except noise in imaging) for different focus values.
	 * Depending on the algorithm, there might exist more than one (local) maximum for different focus values.
	 * @param e The image which should be analyzed.
	 * @return The focus score of the image, given the algorithm of this addon.
	 * @throws ResourceException
	 * @throws RemoteException
	 */
	public double calculateScore(ImageEvent e) throws ResourceException, RemoteException;
}
