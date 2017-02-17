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
package org.youscope.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;

import org.youscope.common.ExecutionInformation;
import org.youscope.common.job.JobException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.MeasurementProcessingListener;

/**
 * @author Moritz Lang
 */
class MeasurementManager
{
	/**
	 * Queue for the measurements. They are started first in first out. Only one measurement may run
	 * at a given time.
	 */
	private final ArrayList<MeasurementImpl>			measurementQueue			= new ArrayList<MeasurementImpl>();

	/**
	 * The main worker thread.
	 */
	protected volatile Thread							measurementManagerThread	= null;

	/**
	 * Reference to the microscope needed to process the jobs.
	 */
	private final Microscope								microscope;

	/**
	 * If true, the measurement manager tries to shut down.
	 */
	private volatile boolean							shouldStop					= false;

	/**
	 * The currently running measurement.
	 */
	private volatile MeasurementImpl currentMeasurement = null;
	
	/**
	 * All listener which get notified if there is a change in the current measurement or in the
	 * measurement queue.
	 */
	private final ArrayList<MeasurementProcessingListener>			measurementProcessingListeners				= new ArrayList<MeasurementProcessingListener>();

	/**
	 * Constructor
	 * 
	 * @param microscope The pointer to the microscope.
	 */
	MeasurementManager(Microscope microscope)
	{
		this.microscope = microscope;
	}

	/**
	 * Adds a listener.
	 * 
	 * @param listener
	 */
	void addMeasurementProcessingListener(MeasurementProcessingListener listener)
	{
		synchronized(measurementProcessingListeners)
		{
			measurementProcessingListeners.add(listener);
		}
	}

	/**
	 * Removes a listener.
	 * 
	 * @param listener
	 */
	void removeMeasurementProcessingListener(MeasurementProcessingListener listener)
	{
		synchronized(measurementProcessingListeners)
		{
			measurementProcessingListeners.remove(listener);
		}
	}

	/**
	 * Used internally to notify all queue listener that the queue changed.
	 */
	private void notifyQueueChanged()
	{
		synchronized(measurementProcessingListeners)
		{
			for(Iterator<MeasurementProcessingListener> iterator = measurementProcessingListeners.iterator(); iterator.hasNext();)
			{
				MeasurementProcessingListener listener = iterator.next();
				try
				{
					listener.measurementQueueChanged();
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					iterator.remove();
				}
			}
		}
	}

	/**
	 * Used internally to notify all queue listener that the current measurement changed.
	 */
	private void notifyCurrentMeasurementChanged()
	{
		synchronized(measurementProcessingListeners)
		{
			for(Iterator<MeasurementProcessingListener> iterator = measurementProcessingListeners.iterator(); iterator.hasNext();)
			{
				MeasurementProcessingListener listener = iterator.next();
				try
				{
					listener.currentMeasurementChanged();
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					iterator.remove();
				}
			}
		}
	}
	
	private void notifyMeasurementProcessingStopped()
	{
		synchronized(measurementProcessingListeners)
		{
			for(Iterator<MeasurementProcessingListener> iterator = measurementProcessingListeners.iterator(); iterator.hasNext();)
			{
				MeasurementProcessingListener listener = iterator.next();
				try
				{
					listener.measurementProcessingStopped();
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					iterator.remove();
				}
			}
		}
	}

	/**
	 * Adds a measurement to the measurement queue and eventually starts it automatically.
	 * 
	 * @param measurement The measurement to add.
	 * @throws MeasurementException 
	 */
	void addMeasurement(MeasurementImpl measurement) throws MeasurementException
	{
		String currentlyRunningMeasurement;
		int queueLength;
		synchronized(this)
		{
			if(measurementQueue.contains(measurement))
				throw new MeasurementException("Tried to queue measurement "+measurement.getName()+", but measurement is already queued.");
			
			currentlyRunningMeasurement =  currentMeasurement != null ? currentMeasurement.getName() : null;
			measurement.queueMeasurement();
			queueLength = measurementQueue.size();
			measurementQueue.add(measurement);
			notifyAll();
		}
		if(currentlyRunningMeasurement!=null)
			ServerSystem.out.println("Queued measurement " + measurement.getName() + " for execution (Currently running measurement "+currentlyRunningMeasurement+", "+Integer.toString(queueLength)+" other mesurement queued before).");
		else if(queueLength >0)
			ServerSystem.out.println("Queued measurement " + measurement.getName() + " for execution ("+Integer.toString(queueLength)+" other mesurement queued before).");
		else
			ServerSystem.out.println("Queued measurement " + measurement.getName() + " for immediate execution.");
		notifyQueueChanged();
	}

	/**
	 * Removes a measurement from the queue. Does nothing if the measurement is not in the queue or
	 * currently running.
	 * 
	 * @param measurement
	 * @throws MeasurementException 
	 */
	void removeMeasurement(MeasurementImpl measurement) throws MeasurementException
	{
		int queueLength;
		synchronized(this)
		{
			boolean unqueued = measurementQueue.remove(measurement);
			if(!unqueued)
				return;
			measurement.unqueueMeasurement();
			queueLength = measurementQueue.size();
			notifyAll();
		}
		ServerSystem.out.println("Unqueued measurement " + measurement.getName() + " (" + Integer.toString(queueLength) + " measurements still queued).");
		notifyQueueChanged();
	}

	/**
	 * Main function which runs the queued measurements. Should be started in an own thread. This
	 * function only returns if {@link #stop()} is called.
	 * 
	 * @throws RemoteException
	 */
	void runMeasurements()
	{
		synchronized(this)
		{
			if(measurementManagerThread != null)
				throw new RuntimeException("Only one measurement manager thread should be running at any time.");
			measurementManagerThread = Thread.currentThread();
		}

		// Main loop that iterates and processes measurements until the server gets a stop signal
		while(!shouldStop)
		{
			// Get a new measurement if one is in the queue.
			synchronized(this)
			{
				// Wait until new measurement arrives
				try
				{
					while(!shouldStop && (measurementQueue.isEmpty() || microscope.isEmergencyStopped()))
					{
						try {
							wait();
						} 
						catch (@SuppressWarnings("unused") InterruptedException e) 
						{
							// do nothing. Either we should stop anyway, or we simply continue to wait.
						}
					}
				}
				catch (RemoteException e) 
				{
					ServerSystem.err.println("Error while trying to determine if microscope is emergency stopped. Shutting down measurement processing for safety reasons.", e);
					break;
				}
				if(shouldStop)
					break;

				// Set current measurement to newly arrived measurement
				currentMeasurement = measurementQueue.remove(0);
			}
			ServerSystem.out.println("Starting measurement " + currentMeasurement.getName() + ".");
			notifyQueueChanged();
			notifyCurrentMeasurementChanged();
			
			boolean lockMicroscopeWhileRunning = false;
			try
			{
				// Lock microscope
				if(currentMeasurement.isLockMicroscopeWhileRunning())
				{
					try
					{
						microscope.lockExclusiveWrite();
						lockMicroscopeWhileRunning = true;
					}
					catch(MicroscopeLockedException e)
					{
						ServerSystem.err.println("Could not get exclusive write access to microscope for measurement execution. Trying to execute measurement without exclusive rights. If error occurs, somebody else might have exclusive rights.", e);
					}
				}
				
				// Check if thread got interrupted.
				if(Thread.interrupted())
				{
					throw new InterruptedException();
				}

				// Start measurement
				currentMeasurement.startupMeasurement(microscope);

				// Check if thread got interrupted.
				if(Thread.interrupted())
				{
					throw new InterruptedException();
				}

				// Process jobs of that measurement
				while(true)
				{
					// Get a new job if one is in the queue.
					JobExecutionQueueElement currentJob =currentMeasurement.waitForNextJob();
					if(currentJob == null)
						break;
				
					currentJob.job.executeJob(new ExecutionInformation(currentJob.evaluationNumber), microscope, currentMeasurement.getMeasurementContext());

					// Check if thread got interrupted.
					if(Thread.interrupted())
					{
						throw new InterruptedException();
					}
					
				}
				
				// run uninitialization or pause tasks of measurement
				currentMeasurement.shutdownMeasurement(microscope);

				// Measurement finished normally.
				if(currentMeasurement.getPauseTime() >= 0)
					ServerSystem.out.println("Paused measurement " + currentMeasurement.getName() + ".");
				else
					ServerSystem.out.println("Finished measurement " + currentMeasurement.getName() + ".");
			}
			catch(InterruptedException e)
			{
				// User wants to stop the current measurement OR quit the program. We here only stop
				// the measurement, if
				// user wants to quit completely, he has also set shouldStop = true and the loop
				// won't be executed again...
				currentMeasurement.failMeasurement(e);
				ServerSystem.out.println("Processing of measurement " + currentMeasurement.getName() + " was interrupted.");
			}
			catch(JobException | MeasurementException | RemoteException | RuntimeException e)
			{
				currentMeasurement.failMeasurement(e);
				ServerSystem.err.println("Measurement " + currentMeasurement.getName() + " produced an error and was interrupted.", e);
			}
			finally
			{
				// Set current measurement to zero
				if(lockMicroscopeWhileRunning)
				{
					try
					{
						microscope.unlockExclusiveWrite();
					}
					catch(MicroscopeLockedException | RemoteException e)
					{
						ServerSystem.err.println("Could not give exclusive write access to microscope back after measurement execution finished.", e);
					}
				}
				currentMeasurement = null;
			}
			notifyCurrentMeasurementChanged();
		}

		synchronized(this)
		{
			measurementManagerThread = null;
		}
		notifyMeasurementProcessingStopped();
		ServerSystem.out.println("Measurement processing finished. Restart YouScope to process new measurements.");
	}

	/**
	 * Interrupts a running measurement. If the measurement is currently not running, it tries to
	 * remove it from the measurement queue.
	 * 
	 * @param measurement Measurement to interrupt.
	 */
	void interruptMeasurement(MeasurementImpl measurement)
	{
		if(measurement == null)
			throw new NullPointerException();
		// Interruption shouldn't wait for synchronization. Thus, make local copies.
		MeasurementImpl currentMeasurement = this.currentMeasurement;
		
		if(currentMeasurement == null || currentMeasurement != measurement)
		{
			// Measurement is not currently running. Just remove it from the queue.
			try {
				removeMeasurement(measurement);
			} catch (@SuppressWarnings("unused") MeasurementException e) {
				// do nothing. Just means that the measurement had an error, but unqueing worked.
			}
			return;
		}
		interruptCurrentMeasurement();
	}

	/**
	 * Interrupts the currently running measurement. If no measurement is running, nothing happens.
	 */
	void interruptCurrentMeasurement()
	{
		// we don't want to synchronize, because interruption shouldn't wait. Thus, we make local copies...
		Thread measurementManagerThread = this.measurementManagerThread;
		if(measurementManagerThread != null)
			measurementManagerThread.interrupt();
	}

	/**
	 * The processing of the measurements stops if an emergency stop happens. This function is used
	 * to resume the processing afterwards. Does nothing if emergency-state is still active.
	 */
	synchronized void resumeAfterEmergencyStop()
	{
		notifyAll();
	}

	/**
	 * Stops (finally) the processing of the measurements.
	 */
	synchronized void stop()
	{
		shouldStop = true;
		interruptCurrentMeasurement();
	}

	/**
	 * Returns the currently processed measurement or null, if no measurement is running.
	 * 
	 * @return Currently running measurement.
	 * @throws RemoteException
	 */
	Measurement getCurrentMeasurement() throws RemoteException
	{
		MeasurementImpl measurement = currentMeasurement;
		if(measurement == null)
			return null;
		return new MeasurementRMI(measurement, this);
	}

	/**
	 * Returns an array of all currently queued measurements.
	 * 
	 * @return Queued measurements.
	 * @throws RemoteException
	 */
	Measurement[] getMeasurementQueue() throws RemoteException
	{
		synchronized(this)
		{
			Measurement[] measurements = new Measurement[measurementQueue.size()];
			for(int i = 0; i < measurementQueue.size(); i++)
			{
				measurements[i] = new MeasurementRMI(measurementQueue.get(i), this);
			}
			return measurements;
		}
	}
}
