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
 * Abstract class all controllers for droplet based microfluidics should implement. 
 * @author Moritz Lang
 *
 */
public interface DropletControllerResource  extends Resource 
{
	/**
	 * Runs the controller and returns the flow rates of the flow units (i.e. the controller's output).
	 * @param executionInformation Current execution information.
	 * @param meanDropletOffset Mean height offset of the droplet (as obtained by observer).
	 * @param measurementContext The Measurement context.
	 * @param microfluidicChipID The id of the microfluidic chip whose droplets are controlled by this controller. Controllers for different chips have to have independent states, i.e. not influence one another.
	 * @return Flow rate of the flow units.
	 * @throws ResourceException
	 * @throws RemoteException
	 */
	public DropletControllerResult runController(ExecutionInformation executionInformation, MeasurementContext measurementContext, double meanDropletOffset, int microfluidicChipID) throws ResourceException, RemoteException;	
}
