package ch.ethz.csb.youscope.addon.dropletmicrofluidics.tablecontroller;

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


class TableController  extends ResourceAdapter<TableControllerConfiguration> implements DropletControllerResource
{
	private static final String CONTEXT_PROPERTY_INTEGRATED_ERROR = "CSB::TableController::IntegratedError";
	private static final String CONTEXT_PROPERTY_LAST_EXECUTION = "CSB::TableController::LastExecution";
	public TableController(PositionInformation positionInformation, ResourceConfiguration configuration) throws ConfigurationException
	{
		super(positionInformation, configuration, TableControllerConfiguration.TYPE_IDENTIFIER,TableControllerConfiguration.class, "Droplet-based microfluidics controller based on syringe table");
	}

	@Override
	public DropletControllerResult runController(ExecutionInformation executionInformation, MeasurementContext measurementContext, double meanDropletOffset) throws ResourceException, RemoteException 
	{
		double targetFlowRate = getConfiguration().getTargetFlowRate();
		double f = getConfiguration().getRatioHeightToVolume();
		double intTimeConstant = getConfiguration().getTimeConstantIntegral() / 60 / 1000;//min
		double propTimeConstant = getConfiguration().getTimeConstantProportional() / 60 / 1000; //min
		double maxDeltaFlowRate = getConfiguration().getMaxDeltaFlowRate();
		boolean correctByOutflow = getConfiguration().isCorrectByOutflow();
		
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
			if(rows[i].getStartTimeMS() <= executionInformation.getMeasurementRuntime())
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
