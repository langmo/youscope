/**
 * 
 */
package ch.ethz.csb.youscope.shared.resource.dropletmicrofluidics;

import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.measurement.ExecutionInformation;
import ch.ethz.csb.youscope.shared.measurement.MeasurementContext;
import ch.ethz.csb.youscope.shared.measurement.resource.Resource;
import ch.ethz.csb.youscope.shared.measurement.resource.ResourceException;

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
