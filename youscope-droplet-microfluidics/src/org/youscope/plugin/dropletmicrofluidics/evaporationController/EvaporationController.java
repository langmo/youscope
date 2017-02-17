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
package org.youscope.plugin.dropletmicrofluidics.evaporationController;

import java.rmi.RemoteException;

import org.youscope.addon.dropletmicrofluidics.DropletControllerResource;
import org.youscope.addon.dropletmicrofluidics.DropletControllerResult;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;


class EvaporationController  extends ResourceAdapter<EvaporationControllerConfiguration> implements DropletControllerResource
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 7466666188440653662L;
	public EvaporationController(PositionInformation positionInformation, EvaporationControllerConfiguration configuration) throws ConfigurationException, RemoteException
	{
		super(positionInformation, configuration, EvaporationControllerConfiguration.TYPE_IDENTIFIER,EvaporationControllerConfiguration.class, "Droplet-based microfluidics controller based on syringe table");
	}

	@Override
	public DropletControllerResult runController(ExecutionInformation executionInformation, MeasurementContext measurementContext, double meanDropletOffset, int microfluidicChipID) throws ResourceException, RemoteException 
	{
		boolean[] useSyringes = getConfiguration().getUseSyringe();
		double f = getConfiguration().getRatioHeightToVolume();
		double intTimeConstant = getConfiguration().getTimeConstantIntegral() / 60 / 1000;//min
		double propTimeConstant = getConfiguration().getTimeConstantProportional() / 60 / 1000; //min
		double maxDeltaFlowRate = getConfiguration().getMaxDeltaFlowRate();
		
		// get and update state
		ControllerState state = loadState(measurementContext, microfluidicChipID);
		long lastExecution = state.getLastExecutionTime();
		long currentExecution = measurementContext.getMeasurementRuntime();
		double intError = state.getIntegralError();
		if(lastExecution >= 0)
			intError += meanDropletOffset * (currentExecution-lastExecution)/60/1000;
		state.setLastExecutionTime(currentExecution);
		state.setIntegralError(intError);
		saveState(state, measurementContext, microfluidicChipID);
		
		// Calculate delta flow
		double deltaFlow = - meanDropletOffset / propTimeConstant / f
				- intError / intTimeConstant / f;
		
		// Check boundaries for flow
		if(deltaFlow > maxDeltaFlowRate)
		{
			deltaFlow = maxDeltaFlowRate;
			sendMessage("Calculated delta flow rate was " + Double.toString(deltaFlow) + "ul/min. Setting to maximal allowed delta flow rate " + Double.toString(maxDeltaFlowRate)+"ul/min.");
		}
		else if(deltaFlow < 0)
		{
			sendMessage("Calculated delta flow rate was " + Double.toString(deltaFlow) + "ul/min. Setting to zero, since delta flow was negative.");
			deltaFlow = 0;
		}
		else
		{
			sendMessage("Calculated delta flow rate is " + Double.toString(deltaFlow) + "ul/min.");
		}
		
		// find active syringe configuration
		int numSyringesCorrect = 0;
		for(boolean useSyringe : useSyringes)
		{
			if(useSyringe)
				numSyringesCorrect++;
		}
		
	
		double[] flows = new double[useSyringes.length];
		for(int i=0; i<useSyringes.length; i++)
		{
			if(useSyringes[i])
			{
				flows[i] = deltaFlow / numSyringesCorrect;
			}
			else
				flows[i] = 0; 
		}
		
		return new DropletControllerResult(flows, deltaFlow);
	}

	@Override
	public void initialize(MeasurementContext measurementContext) throws ResourceException, RemoteException
	{
		super.initialize(measurementContext);
		boolean[] useSyringes = getConfiguration().getUseSyringe();
		if(useSyringes == null || useSyringes.length == 0)
			throw new ResourceException("No syringes selected to control evaporation");
		boolean atLeastOne = false;
		for(boolean syringe : useSyringes)
		{
			atLeastOne = atLeastOne || syringe;
		}
		if(!atLeastOne)
			throw new ResourceException("No syringes selected to control evaporation");
	}
	
	private static String getStateIdentifier(int microfluidicChipID)
	{
		return EvaporationControllerConfiguration.TYPE_IDENTIFIER+".Chip"+Integer.toString(microfluidicChipID);
	}
	private ControllerState loadState(MeasurementContext measurementContext,  int microfluidicChipID) throws RemoteException
	{
		String identifier = getStateIdentifier(microfluidicChipID);
		ControllerState controllerState = measurementContext.getProperty(identifier, ControllerState.class);
		if(controllerState == null)
			controllerState = new ControllerState();
		return controllerState;
	}
	private void saveState(ControllerState state, MeasurementContext measurementContext,  int microfluidicChipID) throws RemoteException
	{
		String identifier = getStateIdentifier(microfluidicChipID);
		measurementContext.setProperty(identifier, state);
	}

}
