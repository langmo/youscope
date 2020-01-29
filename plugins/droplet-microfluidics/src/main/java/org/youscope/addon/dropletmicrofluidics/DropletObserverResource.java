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
	 * @param microfluidicChipID The id of the microfluidic chip whose droplets are controlled by this observer. Observers for different chips have to have independent states, i.e. not influence one another.
	 * @return Estimated offsets of all droplets.
	 * @throws ResourceException
	 * @throws RemoteException
	 */
	public DropletObserverResult runObserver(ExecutionInformation executionInformation, MeasurementContext measurementContext, double dropletOffset, int microfluidicChipID) throws ResourceException, RemoteException;	

	/**
	 * Registers the droplet this observer observes for the microfluidic chip with the given ID. Must be called directly after {@link Resource#initialize(MeasurementContext)}.
	 * @param measurementContext The measurement context.
	 * @param microfluidicChipID The microfluidic chip ID with which this observer should be associated.
	 * @throws ResourceException
	 * @throws RemoteException
	 */
	public void registerDroplet( MeasurementContext measurementContext, int microfluidicChipID) throws ResourceException, RemoteException;	
	
	/**
	 * Unregisters the droplet this observer observes for the microfluidic chip with the given ID. Must be called directly before {@link Resource#uninitialize(MeasurementContext)}.
	 * @param measurementContext The measurement context.
	 * @param microfluidicChipID The microfluidic chip ID with which this observer should be unregistered.
	 * @throws ResourceException
	 * @throws RemoteException
	 */
	public void unregisterDroplet( MeasurementContext measurementContext, int microfluidicChipID) throws ResourceException, RemoteException;
}
