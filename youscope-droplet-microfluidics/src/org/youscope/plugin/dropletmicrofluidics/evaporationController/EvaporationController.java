package org.youscope.plugin.dropletmicrofluidics.evaporationController;

import java.io.Serializable;
import java.rmi.RemoteException;

import org.youscope.addon.dropletmicrofluidics.DropletControllerResource;
import org.youscope.addon.dropletmicrofluidics.DropletControllerResult;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.resource.ResourceAdapter;
import org.youscope.common.resource.ResourceConfiguration;
import org.youscope.common.resource.ResourceException;


class EvaporationController  extends ResourceAdapter<EvaporationControllerConfiguration> implements DropletControllerResource
{
	private static final String CONTEXT_PROPERTY_INTEGRATED_ERROR = "YouScope.EvaporationController.IntegratedError";
	private static final String CONTEXT_PROPERTY_LAST_EXECUTION = "YouScope.EvaporationController.LastExecution";
	public EvaporationController(PositionInformation positionInformation, ResourceConfiguration configuration) throws ConfigurationException
	{
		super(positionInformation, configuration, EvaporationControllerConfiguration.TYPE_IDENTIFIER,EvaporationControllerConfiguration.class, "Droplet-based microfluidics controller based on syringe table");
	}

	@Override
	public DropletControllerResult runController(ExecutionInformation executionInformation, MeasurementContext measurementContext, double meanDropletOffset) throws ResourceException, RemoteException 
	{
		boolean[] useSyringes = getConfiguration().getUseSyringe();
		double f = getConfiguration().getRatioHeightToVolume();
		double intTimeConstant = getConfiguration().getTimeConstantIntegral() / 60 / 1000;//min
		double propTimeConstant = getConfiguration().getTimeConstantProportional() / 60 / 1000; //min
		double maxDeltaFlowRate = getConfiguration().getMaxDeltaFlowRate();
		
		// get last execution
		long lastExecution;
		Serializable lastExecutionProperty = measurementContext.getProperty(CONTEXT_PROPERTY_LAST_EXECUTION);
		if(lastExecutionProperty != null && lastExecutionProperty instanceof Long)
		{
			lastExecution = ((Long)lastExecutionProperty).longValue();
		}
		else
		{
			lastExecution = -1;
		}
		long currentExecution = executionInformation.getMeasurementRuntime();
		measurementContext.setProperty(CONTEXT_PROPERTY_LAST_EXECUTION, new Long(currentExecution));
		
		// update integral error.
		double intError;
		Serializable intErrorProperty = measurementContext.getProperty(CONTEXT_PROPERTY_INTEGRATED_ERROR);
		if(intErrorProperty != null && intErrorProperty instanceof Double)
		{
			intError = ((Double)intErrorProperty).doubleValue();
		}
		else
		{
			intError = 0;
		}
		if(lastExecution >= 0)
			intError += meanDropletOffset * (currentExecution-lastExecution)/60/1000;
		measurementContext.setProperty(CONTEXT_PROPERTY_INTEGRATED_ERROR, new Double(intError));
		
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
		measurementContext.setProperty(CONTEXT_PROPERTY_INTEGRATED_ERROR, new Double(0));
	}

	@Override
	public void uninitialize(MeasurementContext measurementContext) throws ResourceException, RemoteException 
	{
		measurementContext.setProperty(CONTEXT_PROPERTY_INTEGRATED_ERROR, new Double(0));
		super.uninitialize(measurementContext);
	}

}
