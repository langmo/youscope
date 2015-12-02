package ch.ethz.csb.youscope.addon.dropletmicrofluidics.flexiblecontroller;

import java.io.Serializable;
import java.rmi.RemoteException;

import ch.ethz.csb.youscope.shared.configuration.ConfigurationException;
import ch.ethz.csb.youscope.shared.configuration.ResourceConfiguration;
import ch.ethz.csb.youscope.shared.measurement.ExecutionInformation;
import ch.ethz.csb.youscope.shared.measurement.MeasurementContext;
import ch.ethz.csb.youscope.shared.measurement.PositionInformation;
import ch.ethz.csb.youscope.shared.measurement.resource.ResourceAdapter;
import ch.ethz.csb.youscope.shared.measurement.resource.ResourceException;
import ch.ethz.csb.youscope.shared.resource.dropletmicrofluidics.DropletControllerResource;
import ch.ethz.csb.youscope.shared.resource.dropletmicrofluidics.DropletControllerResult;

 
class FlexibleController  extends ResourceAdapter<FlexibleControllerConfiguration> implements DropletControllerResource
{
	private static final String CONTEXT_PROPERTY_INTEGRATED_ERROR = "CSB::FlexibleController::IntegratedError";
	private static final String CONTEXT_PROPERTY_LAST_EXECUTION = "CSB::FlexibleController::LastExecution";
	public FlexibleController(PositionInformation positionInformation, ResourceConfiguration configuration) throws ConfigurationException
	{
		super(positionInformation, configuration, FlexibleControllerConfiguration.TYPE_IDENTIFIER,FlexibleControllerConfiguration.class, "Droplet-based microfluidics controller based on syringe table");
	}

	@Override
	public DropletControllerResult runController(ExecutionInformation executionInformation, MeasurementContext measurementContext, double meanDropletOffset) throws ResourceException, RemoteException 
	{
		double f = getConfiguration().getRatioHeightToVolume();
		double intTimeConstant = getConfiguration().getTimeConstantIntegral() / 60 / 1000;//min
		double propTimeConstant = getConfiguration().getTimeConstantProportional() / 60 / 1000; //min
		
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
		
		// Calculate delta flow
		double deltaFlow = - meanDropletOffset / propTimeConstant / f
				- intError / intTimeConstant / f;
		
		// find active syringe configuration
		FlexibleSyringeTableRow[] rows = getConfiguration().getSyringeTableRows();
		if(rows == null || rows.length == 0)
			throw new ResourceException("Syringe table is empty.");
		
		FlexibleSyringeTableRow activeRow = rows[0];
		for(int i=1; i<rows.length; i++)
		{
			if(rows[i].getStartTimeMS() <= executionInformation.getMeasurementRuntime())
				activeRow = rows[i];
			else
				break;
		}
		
		// Check boundaries for flow
		if(activeRow.getMaxDeltaFlowRate() == 0)
		{
			deltaFlow = 0;
			sendMessage("No delta flow rate allowed. Setting delta flow to zero.");
		}
		else if(deltaFlow > activeRow.getMaxDeltaFlowRate())
		{
			deltaFlow = activeRow.getMaxDeltaFlowRate();
			sendMessage("Calculated delta flow rate was " + Double.toString(deltaFlow) + "ul/min. Setting to maximal allowed delta flow rate " + Double.toString(activeRow.getMaxDeltaFlowRate())+"ul/min.");
		}
		else if(deltaFlow < -activeRow.getMaxDeltaFlowRate())
		{
			deltaFlow = -activeRow.getMaxDeltaFlowRate();
			sendMessage("Calculated delta flow rate was " + Double.toString(deltaFlow) + "ul/min. Setting to minimally allowed delta flow rate " + Double.toString(activeRow.getMaxDeltaFlowRate())+"ul/min.");
		}
		else
		{
			sendMessage("Calculated delta flow rate is " + Double.toString(deltaFlow) + "ul/min.");
		}
		
		
		double[] targetFlows = activeRow.getTargetFlowRates();
		SyringeControlState[] states = activeRow.getSyringeControlStates();
		if(states.length != targetFlows.length)
			throw new ResourceException("Number of control states and target flows not equal.");
		double[] flows = new double[states.length];
		boolean[] changeable = new boolean[states.length];
		for(int i=0; i<states.length; i++)
		{
			flows[i] = targetFlows[i];
			changeable[i] = states[i] != SyringeControlState.FIXED;
		}
		
		if(deltaFlow != 0)
		{
			double flowToDistribute = deltaFlow;
			double totalChangableFlow = 0;
			for(int i=0; i<states.length ; i++)
			{
				if(changeable[i])
					totalChangableFlow+= Math.abs(flows[i]);
			}
			if(totalChangableFlow > 0)
			{
				// first, try to increase or decrease all flows by equal factor
				double calcFactor = flowToDistribute / totalChangableFlow;
				double realFactor = calcFactor < -1 ? -1 : calcFactor > 1 ? 1 : calcFactor;
				for(int i=0; i<states.length ; i++)
				{
					if(!changeable[i])
						continue;
					flows[i] *= (1+Math.signum(flows[i]) * realFactor);
					if((flows[i] < 0 && realFactor > 0) || (flows[i] > 0 && realFactor < 0))
						changeable[i] = false;
				}
				if(calcFactor == realFactor)
					flowToDistribute = 0;
				else
					flowToDistribute *= (1-Math.abs(realFactor/calcFactor)) ;
			}
			if(flowToDistribute != 0)
			{
				// OK, now change everything which is still changeable. The change will be in the right direction...
				totalChangableFlow = 0;
				for(int i=0; i<states.length ; i++)
				{
					if(changeable[i])
						totalChangableFlow+= Math.abs(flows[i]);
				}
				if(totalChangableFlow>0)
				{
					double factor = flowToDistribute / totalChangableFlow;
					for(int i=0; i<states.length ; i++)
					{
						if(!changeable[i])
							continue;
						flows[i] *= (1+Math.signum(flows[i]) * factor);
					}
					flowToDistribute = 0;
				}
			}
			if(flowToDistribute != 0)
			{
				// increase/decrease all flows for which this is still possible
				int numChangeable = 0;
				for(int i=0; i<states.length ; i++)
				{
					if((states[i] == SyringeControlState.NEGATIVE && flowToDistribute < 0) || (states[i] == SyringeControlState.POSITIVE && flowToDistribute > 0))
						numChangeable++;
				}
				if(numChangeable > 0)
				{
					for(int i=0; i<states.length ; i++)
					{
						if(states[i] == SyringeControlState.NEGATIVE && flowToDistribute < 0) 
						{
							flows[i] += flowToDistribute / numChangeable;
						}
						else if(states[i] == SyringeControlState.POSITIVE && flowToDistribute > 0)
						{
							flows[i] += flowToDistribute / numChangeable;
						}
					}
					flowToDistribute = 0;
				}
			}
			if(flowToDistribute != 0)
			{
				// if we are here, target cannot be fulfilled. Write a message, adjust deltaflow, and continue.
				
				sendMessage("Could only distribute "+Double.toString(deltaFlow-flowToDistribute)+" ul/min of the calculated delta flow rate of " + Double.toString(activeRow.getMaxDeltaFlowRate())+"ul/min.");
				deltaFlow -= flowToDistribute;
			}
			else
			{
				// we only save integrated error if we could distribute anything. Otherwise, integrator would go quickly to inf.
				measurementContext.setProperty(CONTEXT_PROPERTY_INTEGRATED_ERROR, new Double(intError));
			}
		}
		
		return new DropletControllerResult(flows, deltaFlow);
	}

	@Override
	public void initialize(MeasurementContext measurementContext) throws ResourceException, RemoteException
	{
		super.initialize(measurementContext);
		measurementContext.setProperty(CONTEXT_PROPERTY_INTEGRATED_ERROR, new Double(0));
	}

	@Override
	public void uninitialize(MeasurementContext measurementContext) throws ResourceException, RemoteException 
	{
		measurementContext.setProperty(CONTEXT_PROPERTY_INTEGRATED_ERROR, new Double(0));
		super.uninitialize(measurementContext);
	}

}
