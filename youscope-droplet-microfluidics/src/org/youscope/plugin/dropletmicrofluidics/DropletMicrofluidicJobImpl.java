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
import java.util.ArrayList;
import java.util.Iterator;

import org.youscope.addon.dropletmicrofluidics.DropletControllerResource;
import org.youscope.addon.dropletmicrofluidics.DropletControllerResult;
import org.youscope.addon.dropletmicrofluidics.DropletObserverResource;
import org.youscope.addon.dropletmicrofluidics.DropletObserverResult;
import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobException;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.resource.ResourceException;
import org.youscope.common.table.ColumnView;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableEntry;
import org.youscope.common.table.TableException;
import org.youscope.common.table.TableListener;
import org.youscope.common.table.TableProducer;
import org.youscope.common.table.TemporaryRow;
import org.youscope.plugin.autofocus.AutoFocusJob;
import org.youscope.plugin.autofocus.AutoFocusTable;
import org.youscope.plugin.nemesys.NemesysControlTable;
import org.youscope.plugin.nemesys.NemesysJob;

/**
 * @author Moritz Lang
 *
 */
class DropletMicrofluidicJobImpl extends JobAdapter implements DropletMicrofluidicJob
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -341165916066788997L;

	private AutoFocusJob inputJob = null;
	
	private NemesysJob outputJob = null;
	
	private volatile Table lastInputTable = null;
	
	private DropletControllerResource controller = null;
	
	private DropletObserverResource observer = null;
	
	private ArrayList<TableListener> tableListeners = new ArrayList<TableListener>();
	
    private DropletMicrofluidicJobCallback callback = null;
    
    private int microfluidicChipID = 1;
    
    private int[] connectedSyringes = new int[0];
	
	private final TableListener inputListener = new TableListener()
	{
		@Override
		public void newTableProduced(Table table)
		{
			synchronized(DropletMicrofluidicJobImpl.this)
			{
				lastInputTable = table;
			}
		}
	};
	
	public DropletMicrofluidicJobImpl(PositionInformation positionInformation) throws RemoteException
	{
		super(positionInformation);
	}

	@Override
	public Job[] getJobs() throws RemoteException
	{
		if(inputJob == null && outputJob == null)
			return new Job[0];
		else if(inputJob == null)
			return new Job[]{outputJob};
		else if(outputJob == null)
			return new Job[]{inputJob};
		else
			return new Job[]{inputJob, outputJob};
	}

	@Override
	public AutoFocusJob getInputJob() throws RemoteException
	{
		return inputJob;
	}

	@Override
	public void setInputJob(AutoFocusJob inputJob) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		this.inputJob = inputJob;
	}

	@Override
	public NemesysJob getOutputJob() throws RemoteException
	{
		return outputJob;
	}

	@Override
	public void setOutputJob(NemesysJob outputJob) throws RemoteException, ComponentRunningException
	{
		assertRunning();
		this.outputJob = outputJob;
	}

	@Override
	public void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		// Execute input job.
		synchronized(this)
		{
			this.lastInputTable = null;
		}
		inputJob.executeJob(executionInformation, microscope, measurementContext);
	
		// get table data from input.
		Table lastInputTable;
		synchronized(this)
		{
			lastInputTable = this.lastInputTable;
			this.lastInputTable = null;
		}
		if(lastInputTable == null)
			throw new JobException("Input job did not produce table data used as input for controller.");	
		
		// get optimal focal plane
		ColumnView<? extends Double> offsetView;
		try
		{
			offsetView = lastInputTable.getColumnView(AutoFocusTable.COLUMN_FOCAL_PLANE_RELATIVE_FOCUS);
		}
		catch(TableException e)
		{
			throw new JobException("Can not find column of optimal relative focus of the autofocus job table.", e);
		}
		double dropletOffset = Double.NaN;
		for(TableEntry<? extends Double> entry : offsetView)
		{
			if(entry.getValue() != null)
			{
				// focus is increasing when droplets get smaller.
				dropletOffset = - entry.getValue().doubleValue();
				break;
			}
		}
		if(Double.isNaN(dropletOffset))
		{
			throw new JobException("Autofocus table does not contain optimal focus offset.");
		}
		
		// call the observer...
		DropletObserverResult observerResult;
		try {
			observerResult = observer.runObserver(executionInformation, measurementContext, dropletOffset, microfluidicChipID);
		} catch (ResourceException e) {
			throw new JobException("Could not run observer for droplet-based microfluidics.", e);
		}
		
		// call the controller...
		DropletControllerResult controllerResult;
		try {
			controllerResult = controller.runController(executionInformation, measurementContext, observerResult.getMeanOffset(), microfluidicChipID);
		} catch (ResourceException e) {
			throw new JobException("Could not run controller for droplet-based microfluidics.", e);
		}
		if(controllerResult.getNumFlowUnits() > connectedSyringes.length)
		{
			throw new JobException("Only "+Integer.toString(connectedSyringes.length) + " syringes connected to chip "+Integer.toString(microfluidicChipID)+", but controller wants to set flow of "+controllerResult.getNumFlowUnits()+" syringes.");
		}

		// Execute the output.
		Table nemesysTable = new Table(NemesysControlTable.getTableDefinition(), measurementContext.getMeasurementRuntime(), getPositionInformation(), executionInformation);
		try
		{
			for(int i=0; i<controllerResult.getNumFlowUnits(); i++)
			{
				TemporaryRow row = nemesysTable.createTemporaryRow();
				row.get(NemesysControlTable.COLUMN_FLOW_UNIT).setValue(new Integer(connectedSyringes[i]));
				row.get(NemesysControlTable.COLUMN_FLOW_RATE).setValue(new Double(controllerResult.getFlowRate(i)));
				nemesysTable.addRow(row);
			}
		}
		catch(TableException e)
		{
			throw new JobException("Could not create control table for Nemesys device.", e);
		}
		try {
			outputJob.consumeTable(nemesysTable);
		} catch (TableException e) {
			throw new JobException("Nemesys control table layout as generated by droplet based microfluidic job is invalid.", e);
		}
		outputJob.executeJob(executionInformation, microscope, measurementContext);
		
		// Create state table
		Table stateTable = new Table(DropletMicrofluidicTable.getTableDefinition(), measurementContext.getMeasurementRuntime(), getPositionInformation(), executionInformation);
		try
		{
			for(int i = 0; i<controllerResult.getNumFlowUnits() || i < observerResult.getNumDroplets() || i < 1; i++)
			{
				TemporaryRow row = stateTable.createTemporaryRow();
				if(i==0)
				{
					row.get(DropletMicrofluidicTable.COLUMN_CURRENT_DROPLET_ESTIMATED_OFFSET).setValue(observerResult.getCurrentOffset());
					row.get(DropletMicrofluidicTable.COLUMN_CURRENT_DROPLET_ID).setValue(observerResult.getCurrentDroplet());
					row.get(DropletMicrofluidicTable.COLUMN_CURRENT_DROPLET_MEASURED_OFFSET).setValue(dropletOffset);
					
					row.get(DropletMicrofluidicTable.COLUMN_DROPLETS_MEAN_OFFSET).setValue(observerResult.getMeanOffset());
					
					row.get(DropletMicrofluidicTable.COLUMN_DELTA_FLOW).setValue(controllerResult.getDeltaFlow());
				}
				if(i<controllerResult.getNumFlowUnits())
				{
					row.get(DropletMicrofluidicTable.COLUMN_FLOW_UNIT_ID).setValue(i);
					row.get(DropletMicrofluidicTable.COLUMN_FLOW_UNIT_FLOW_RATE).setValue(controllerResult.getFlowRate(i));
				}
				if(i<observerResult.getNumDroplets())
				{
					row.get(DropletMicrofluidicTable.COLUMN_DROPLET_ID).setValue(i);
					row.get(DropletMicrofluidicTable.COLUMN_DROPLET_ESTIMATED_OFFSET).setValue(observerResult.getDropletOffset(i));
				}
				stateTable.addRow(row);
			}
		}
		catch(TableException e)
		{
			throw new JobException("Could not create table to report state of droplet-based microfluidic job.", e);
		}
		sendTableToListeners(stateTable);
		
		if(callback != null)
		{
			try {
				callback.dropletMeasured(executionInformation, stateTable);
			} catch (Exception e) {
				callback = null;
				sendErrorMessage("Exception occurred in visual callback. Not sending any state updates anymore, but continuing normally.", e);
			}
		}
	}

	@Override
	protected String getDefaultName()
	{
		return "Droplet based microfluidic controller";
	}

	@Override
	public void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.initializeJob(microscope, measurementContext);

		lastInputTable = null;
		// Initialize sub-jobs
		if(inputJob != null)
		{
			inputJob.initializeJob(microscope, measurementContext);
			((TableProducer)inputJob).addTableListener(inputListener);
		}
		else
			throw new JobException("Input job not defined.");
		if(outputJob != null)
			outputJob.initializeJob(microscope, measurementContext);
		else
			throw new JobException("Output job not defined.");
		if(observer == null)
			throw new JobException("No observer defined.");
		try {
			observer.initialize(measurementContext);
			observer.registerDroplet(measurementContext, microfluidicChipID);
		} catch (ResourceException e) {
			throw new JobException("Could not initialize observer", e);
		}
		if(controller == null)
			throw new JobException("No controller defined.");
		try {
			controller.initialize(measurementContext);
		} catch (ResourceException e) {
			throw new JobException("Could not initialize controller", e);
		}
		
		if(callback != null)
		{
			try {
				callback.initializeCallback(microfluidicChipID, getConnectedSyringes());
			} catch (Exception e) {
				callback = null;
				sendErrorMessage("Exception occurred in initializing visual callback. Not sending any state updates anymore, but continuing normally.", e);
			}
		}
	}
	
	@Override
	public void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
	{
		super.uninitializeJob(microscope, measurementContext);
		lastInputTable = null;
		if(inputJob != null)
		{
			inputJob.uninitializeJob(microscope, measurementContext);
			((TableProducer)inputJob).removeTableListener(inputListener);
		}
		if(outputJob != null)
			outputJob.uninitializeJob(microscope, measurementContext);
		if(observer != null)
		{
			try {
				observer.unregisterDroplet(measurementContext, microfluidicChipID);
				observer.uninitialize(measurementContext);
			} catch (ResourceException e) {
				throw new JobException("Could not uninitialize observer", e);
			}
		}
		if(controller != null)
		{
			try {
				controller.uninitialize(measurementContext);
			} catch (ResourceException e) {
				throw new JobException("Could not uninitialize controller", e);
			}
		}
		if(callback != null)
		{
			try {
				callback.uninitializeCallback();
			} catch (Exception e) {
				callback = null;
				sendErrorMessage("Exception occurred in uninitializing visual callback. Continuing normally.", e);
			}
		}
	}

	private void sendTableToListeners(Table table)
	{
		synchronized (tableListeners) 
		{
			Iterator<TableListener> iterator = tableListeners.iterator();
			while(iterator.hasNext())
			{
				TableListener listener = iterator.next();
				try {
					listener.newTableProduced(table.clone());
				} catch (@SuppressWarnings("unused") RemoteException e) {
					iterator.remove();
				}
			}
		}
	}
	
	@Override
	public DropletControllerResource getController() throws RemoteException {
		return controller;
	}

	@Override
	public void setController(DropletControllerResource controller) throws RemoteException, ComponentRunningException {
		assertRunning();
		this.controller = controller;
	}

	@Override
	public DropletObserverResource getObserver() throws RemoteException {
		return observer;
	}

	@Override
	public void setObserver(DropletObserverResource observer) throws RemoteException, ComponentRunningException {
		assertRunning();
		this.observer = observer;
	}

	@Override
	public int getNumJobs() throws RemoteException {
		return (inputJob == null ? 0 : 1) + (outputJob == null ? 0 : 1);
	}

	@Override
	public Job getJob(int jobIndex) throws RemoteException, IndexOutOfBoundsException {
		return getJobs()[jobIndex];
	}

	@Override
	public void removeTableListener(TableListener listener) throws RemoteException {
		synchronized(tableListeners)
		{
			tableListeners.remove(listener);
		}
	}

	@Override
	public void addTableListener(TableListener listener) throws RemoteException {
		synchronized(tableListeners)
		{
			tableListeners.add(listener);
		}
	}

	@Override
	public TableDefinition getProducedTableDefinition() throws RemoteException {
		return DropletMicrofluidicTable.getTableDefinition();
	}
	public DropletMicrofluidicJobCallback getCallback() {
		return callback;
	}

	public void setCallback(DropletMicrofluidicJobCallback callback) throws ComponentRunningException
	{
		assertRunning();
		this.callback = callback;
	}

	@Override
	public int getMicrofluidicChipID() throws RemoteException {
		return microfluidicChipID;
	}

	@Override
	public void setMicrofluidicChipID(int microfluidicChipID) throws RemoteException, ComponentRunningException {
		assertRunning();
		this.microfluidicChipID = microfluidicChipID;
		
	}

	@Override
	public void setConnectedSyringes(int[] connectedSyringes) throws RemoteException, ComponentRunningException {
		assertRunning();
		if(connectedSyringes == null)
			connectedSyringes = new int[0];
		this.connectedSyringes = connectedSyringes;
	}

	@Override
	public int[] getConnectedSyringes() 
	{
		int[] copy = new int[connectedSyringes.length];
		for(int i=0; i<connectedSyringes.length; i++)
			copy[i] = connectedSyringes[i];
		return copy;
	}

	@Override
	public void addJob(Job job) throws RemoteException, ComponentRunningException, JobException {
		throw new JobException("Use setInputJob() or setOutputJob() to edit child jobs.");
	}

	@Override
	public void insertJob(Job job, int jobIndex)
			throws RemoteException, ComponentRunningException, IndexOutOfBoundsException, JobException {
		throw new JobException("Use setInputJob() or setOutputJob() to edit child jobs.");
	}

	@Override
	public void removeJob(int jobIndex)
			throws RemoteException, ComponentRunningException, IndexOutOfBoundsException, JobException {
		throw new JobException("Use setInputJob() or setOutputJob() to edit child jobs.");
	}

	@Override
	public void clearJobs() throws RemoteException, ComponentRunningException, JobException {
		throw new JobException("Use setInputJob() or setOutputJob() to edit child jobs.");
	}
}
