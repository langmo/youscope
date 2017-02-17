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
package org.youscope.plugin.dropletmicrofluidics.tablecontroller;

import java.rmi.RemoteException;

import org.youscope.addon.dropletmicrofluidics.DropletControllerResource;
import org.youscope.addon.dropletmicrofluidics.DropletControllerResult;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceException;


class TableController  extends ResourceAdapter<TableControllerConfiguration> implements DropletControllerResource
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -6644190588089271841L;
	public TableController(PositionInformation positionInformation, TableControllerConfiguration configuration) throws ConfigurationException, RemoteException
	{
		super(positionInformation, configuration, TableControllerConfiguration.TYPE_IDENTIFIER,TableControllerConfiguration.class, "Droplet-based microfluidics controller based on syringe table");
	}

	@Override
	public DropletControllerResult runController(ExecutionInformation executionInformation, MeasurementContext measurementContext, double meanDropletOffset, int microfluidicChipID) throws ResourceException, RemoteException 
	{
		double targetFlowRate = getConfiguration().getTargetFlowRate();
		double f = getConfiguration().getRatioHeightToVolume();
		double intTimeConstant = getConfiguration().getTimeConstantIntegral() / 60 / 1000;//min
		double propTimeConstant = getConfiguration().getTimeConstantProportional() / 60 / 1000; //min
		double maxDeltaFlowRate = getConfiguration().getMaxDeltaFlowRate();
		boolean correctByOutflow = getConfiguration().isCorrectByOutflow();
		
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
		else if(deltaFlow < -maxDeltaFlowRate && maxDeltaFlowRate<targetFlowRate)
		{
			deltaFlow = -maxDeltaFlowRate;
			sendMessage("Calculated delta flow rate was " + Double.toString(deltaFlow) + "ul/min. Setting to minimal allowed delta flow rate " + Double.toString(-maxDeltaFlowRate)+"ul/min.");
		}
		else if(deltaFlow < - targetFlowRate && maxDeltaFlowRate>targetFlowRate)
		{
			deltaFlow = -targetFlowRate;
			sendMessage("Calculated delta flow rate was " + Double.toString(deltaFlow) + "ul/min. Setting to negative of target flow rate " + Double.toString(-targetFlowRate)+"ul/min.");
		}
		else
		{
			sendMessage("Calculated delta flow rate is " + Double.toString(deltaFlow) + "ul/min.");
		}
		
		// find active syringe configuration
		SyringeTableRow[] rows = getConfiguration().getSyringeTableRows();
		if(rows == null || rows.length == 0)
			throw new ResourceException("Syringe table is empty.");
		
		SyringeTableRow activeRow = rows[0];
		for(int i=1; i<rows.length; i++)
		{
			if(rows[i].getStartTimeMS() <= measurementContext.getMeasurementRuntime())
				activeRow = rows[i];
			else
				break;
		}
		SyringeState[] states = activeRow.getSyringeStates();
		if(states == null || states.length == 0)
			throw new ResourceException("Syringe table definitions empty for time " + activeRow.getStartTimeMS() + "ms.");
		
		int numSyringesIn = 0;
		int numSyringesOut = 0;
		for(int i=0; i<states.length; i++)
		{
			if(states[i] == SyringeState.INFLOW)
				numSyringesIn++;
			else if(states[i] == SyringeState.OUTFLOW)
				numSyringesOut++;
		}
		// set flows
		double totalInflow;
		double totalOutflow;
		if(correctByOutflow)
		{
			totalInflow = targetFlowRate;
			totalOutflow = targetFlowRate + deltaFlow;
		}
		else
		{
			totalInflow = targetFlowRate + deltaFlow;
			totalOutflow = targetFlowRate;
		}
		double[] flows = new double[states.length];
		for(int i=0; i<states.length; i++)
		{
			if(states[i] == SyringeState.INFLOW)
			{
				flows[i] = totalInflow / numSyringesIn;
			}
			else if(states[i] == SyringeState.OUTFLOW)
			{
				flows[i] = -totalOutflow / numSyringesOut;
			}
			else
				flows[i] = 0;
		}
		return new DropletControllerResult(flows, deltaFlow);
	}
	
	private static String getStateIdentifier(int microfluidicChipID)
	{
		return TableControllerConfiguration.TYPE_IDENTIFIER+".Chip"+Integer.toString(microfluidicChipID);
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
