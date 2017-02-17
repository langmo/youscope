/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.plugin.dropletmicrofluidics;

import java.rmi.RemoteException;

import org.youscope.addon.dropletmicrofluidics.DropletControllerResource;
import org.youscope.addon.dropletmicrofluidics.DropletObserverResource;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.job.CompositeJob;
import org.youscope.common.table.TableProducer;
import org.youscope.plugin.autofocus.AutoFocusJob;
import org.youscope.plugin.nemesys.NemesysJob;

/**
 * Job which implements the droplet based microfluidic controller. 
 * @author Moritz Lang
 *
 */
public interface DropletMicrofluidicJob extends CompositeJob, TableProducer
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
	 * @throws ComponentRunningException 
	 */
	public void setInputJob(AutoFocusJob inputJob) throws RemoteException, ComponentRunningException;
	
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
	 * @throws ComponentRunningException 
	 * @throws IllegalArgumentException If outputJob does not implement OutputConsumerConfiguration.
	 */
	public void setOutputJob(NemesysJob outputJob) throws RemoteException, ComponentRunningException, IllegalArgumentException;
	
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
	 * @throws ComponentRunningException 
	 */
	public void setController(DropletControllerResource controller) throws RemoteException, ComponentRunningException;
	
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
	 * @throws ComponentRunningException 
	 */
	public void setObserver(DropletObserverResource observer) throws RemoteException, ComponentRunningException;
	
	/**
	 * Returns the ID of the droplet microfluidic chip/part of the chip which is controlled by this job.
	 * The controllers and observers of different chips are independent.
	 * All jobs having the same chip ID have to be called sequentially and equally often, since they considered to observe and control the height of droplets on the same chip.
	 * Thus, it is possible to control the droplet heights of several chips by having different IDs.
	 * @return microfluidic chip id.
	 * @throws RemoteException 
	 */
	public int getMicrofluidicChipID() throws RemoteException;

	/**
	 * Sets the ID of the droplet microfluidic chip/part of the chip which is controlled by this job.
	 * The controllers and observers of different chips are independent.
	 * All jobs having the same chip ID have to be called sequentially and equally often, since they considered to observe and control the height of droplets on the same chip.
	 * Thus, it is possible to control the droplet heights of several chips by having different IDs.
	 * @param microfluidicChipID  chip id.
	 * @throws RemoteException 
	 * @throws ComponentRunningException 
	 */
	public void setMicrofluidicChipID(int microfluidicChipID) throws RemoteException, ComponentRunningException;
	
	/**
	 * Sets the IDs of the syringes of the nemesys device connected to the chip/part of the chip.
	 * @param connectedSyringes IDs (zero based) of nemesys syringes, or null if not yet configured.
	 * @throws RemoteException 
	 * @throws ComponentRunningException 
	 */
	public void setConnectedSyringes(int[] connectedSyringes) throws RemoteException, ComponentRunningException;
	
	/**
	 * Returns the IDs of the syringes of the nemesys device connected to the chip/part of the chip.
	 * @return Ds (zero based) of nemesys syringes, or null if not yet configured.
	 * @throws RemoteException 
	 */
	public int[]  getConnectedSyringes() throws RemoteException;
}
