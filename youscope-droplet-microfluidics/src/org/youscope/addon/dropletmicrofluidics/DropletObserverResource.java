/**
 * 
 */
package org.youscope.addon.dropletmicrofluidics;

import java.rmi.RemoteException;

import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.resource.Resource;
import org.youscope.common.resource.ResourceException;

/**
 * Abstract class all observers for droplet based microfluidics should implement. 
 * @author Moritz Lang
 *
 */
public interface DropletObserverResource  extends Resource 
{
	/**
	 * Runs the observer and returns the estimated droplet heights.
	 * @param executionInformation Current execution information.
	 * @param measurementContext The Measurement context.
	 * @param dropletOffset The measured droplet offset.
	 * @return Estimated offsets of all droplets.
	 * @throws ResourceException
	 * @throws RemoteException
	 */
	public DropletObserverResult runObserver(ExecutionInformation executionInformation, MeasurementContext measurementContext, double dropletOffset) throws ResourceException, RemoteException;	
}
