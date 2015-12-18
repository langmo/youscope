/**
 * 
 */
package org.youscope.plugin.dropletmicrofluidics;

import java.rmi.RemoteException;

import org.youscope.addon.dropletmicrofluidics.DropletControllerResource;
import org.youscope.addon.dropletmicrofluidics.DropletObserverResource;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobContainer;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.table.TableProducer;
import org.youscope.plugin.autofocus.AutoFocusJob;
import org.youscope.plugin.nemesys.NemesysJob;

/**
 * Job which implements the droplet based microfluidic controller. 
 * @author Moritz Lang
 *
 */
public interface DropletMicrofluidicJob extends Job, JobContainer, TableProducer
{
	/**
	 * Returns the job which is used to produce the input for the controller, i.e. the autofocus job.
	 * @return input job.
	 * @throws RemoteException 
	 */
	public AutoFocusJob getInputJob() throws RemoteException;

	/**
	 * Sets the input job.
	 * @param inputJob input job.
	 * @throws RemoteException 
	 * @throws MeasurementRunningException 
	 */
	public void setInputJob(AutoFocusJob inputJob) throws RemoteException, MeasurementRunningException;
	
	/**
	 * Returns the nemesys job which is used as the output.
	 * @return Output job
	 * @throws RemoteException 
	 */
	public NemesysJob getOutputJob() throws RemoteException;

	/**
	 * Sets the nemesys job which is used as the output
	 * @param outputJob output job.
	 * @throws RemoteException 
	 * @throws MeasurementRunningException 
	 * @throws IllegalArgumentException If outputJob does not implement OutputConsumerConfiguration.
	 */
	public void setOutputJob(NemesysJob outputJob) throws RemoteException, MeasurementRunningException, IllegalArgumentException;
	
	/**
	 * Returns the control algorithm used to correct droplet heights by changing inflow or outflow.
	 * @return controller algorithm.
	 * @throws RemoteException
	 */
	public DropletControllerResource getController() throws RemoteException;
	
	/**
	 * Sets the control algorithm used to correct droplet heights by changing inflow or outflow.
	 * @param controller controller algorithm.
	 * @throws RemoteException
	 * @throws MeasurementRunningException 
	 */
	public void setController(DropletControllerResource controller) throws RemoteException, MeasurementRunningException;
	
	/**
	 * Returns the observer algorithm used to estimate the droplet heights from periodical autofocus measurements.
	 * @return observer algorithm.
	 * @throws RemoteException
	 */
	public DropletObserverResource getObserver() throws RemoteException;
	
	/**
	 * Sets the observer algorithm used to estimate the droplet heights from periodical autofocus measurements.
	 * @param observer observer algorithm.
	 * @throws RemoteException
	 * @throws MeasurementRunningException 
	 */
	public void setObserver(DropletObserverResource observer) throws RemoteException, MeasurementRunningException;
}
