/**
 * 
 */
package org.youscope.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.job.JobException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.measurement.MeasurementState;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.MicroscopeStateListener;

/**
 * @author Moritz Lang
 */
class MeasurementManager
{
	/**
	 * Queue for the measurements. They are started first in first out. Only one measurement may run
	 * at a given time.
	 */
	private volatile Vector<MeasurementImpl>			measurementQueue			= new Vector<MeasurementImpl>();

	/**
	 * The main worker thread.
	 */
	protected volatile Thread							measurementManagerThread	= null;

	/**
	 * Reference to the microscope needed to process the jobs.
	 */
	protected Microscope								microscope;

	/**
	 * If true, the measurement manager tries to shut down.
	 */
	private volatile boolean							shouldStop					= false;

	/**
	 * The currently running measurement.
	 */
	private volatile MeasurementSupervision currentMeasurement = null;
	
	/**
	 * All listener which get notified if there is a change in the current measurement or in the
	 * measurement queue.
	 */
	private ArrayList<MicroscopeStateListener>			queueListeners				= new ArrayList<MicroscopeStateListener>();

	/**
	 * Constructor
	 * 
	 * @param microscope The pointer to the microscope.
	 */
	protected MeasurementManager(Microscope microscope)
	{
		this.microscope = microscope;
	}

	/**
	 * Adds a listener.
	 * 
	 * @param listener
	 */
	void addQueueListener(MicroscopeStateListener listener)
	{
		synchronized(queueListeners)
		{
			queueListeners.add(listener);
		}
	}

	/**
	 * Removes a listener.
	 * 
	 * @param listener
	 */
	void removeQueueListener(MicroscopeStateListener listener)
	{
		synchronized(queueListeners)
		{
			queueListeners.remove(listener);
		}
	}

	/**
	 * Used internally to notify all queue listener that the queue changed.
	 */
	private void notifyQueueChanged()
	{
		synchronized(queueListeners)
		{
			for(Iterator<MicroscopeStateListener> iterator = queueListeners.iterator(); iterator.hasNext();)
			{
				MicroscopeStateListener listener = iterator.next();
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
		synchronized(queueListeners)
		{
			for(Iterator<MicroscopeStateListener> iterator = queueListeners.iterator(); iterator.hasNext();)
			{
				MicroscopeStateListener listener = iterator.next();
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

	/**
	 * Adds a measurement to the measurement queue and eventually starts it automatically.
	 * 
	 * @param measurement The measurement to add.
	 * @throws MeasurementException 
	 */
	void addMeasurement(MeasurementImpl measurement) throws MeasurementException
	{
		boolean currentlyRunningMeasurement;
		synchronized(this)
		{
			currentlyRunningMeasurement =  currentMeasurement != null || !measurementQueue.isEmpty();
			measurement.queueMeasurement();
			measurementQueue.add(measurement);
			notifyAll();
		}

		// If the measurement is the only element in the queue AND no measurement is currently
		// running, than this measurement will be
		// started immediately. If this is not true, inform the measurement that its execution will
		// be delayed.
		if(currentlyRunningMeasurement)
		{
			ServerSystem.out.println("Queued measurement \"" + measurement.getName() + "\" for future execution when currently running measurement is finished.");
		}
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
		boolean unqueued;
		synchronized(this)
		{
			unqueued = measurementQueue.remove(measurement);
		}
		if(unqueued)
		{
			notifyQueueChanged();
			measurement.unqueueMeasurement();
			ServerSystem.out.println("Unqueued measurement \"" + measurement.getName() + "\" (" + Integer.toString(measurementQueue.size()) + " measurements currently in the queue).");
		}
	}

	/**
	 * Main function which runs the queued measurements. Should be started in an own thread. This
	 * function only returnes if "stop()" is called.
	 * 
	 * @throws RemoteException
	 */
	void runMeasurements()
	{
		synchronized(this)
		{
			measurementManagerThread = Thread.currentThread();
		}

		// Main loop that iterates and processes measurements until the server gets a stop signal
		while(true)
		{
			Thread measurementThread = null;
			try
			{
				// Get a new measurement if one is in the queue.
				synchronized(this)
				{
					// Wait until new measurement arrives
					while(measurementQueue.isEmpty() || microscope.isEmergencyStopped())
					{
						wait();
					}

					// Set current measurement to newly arrived measurement
					currentMeasurement = new MeasurementSupervision(measurementQueue.remove(0));
				}

				ServerSystem.out.println("Starting measurement \"" + currentMeasurement.getName() + "\".");
				notifyQueueChanged();
				notifyCurrentMeasurementChanged();

				// Lock microscope
				boolean lockMicroscopeWhileRunning = currentMeasurement.isLockMicroscopeWhileRunning();
				try
				{
					if(lockMicroscopeWhileRunning)
					{
						// Lock microscope
						try
						{
							microscope.lockExclusiveWrite();
						}
						catch(MicroscopeLockedException e)
						{
							ServerSystem.err.println("Could not get exclusive write access to microscope for measurement execution. Trying to execute measurement without exclusive rights. If error occurs, somebody else might have exclusive rights.", e);
						}
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
						JobExecutionQueueElement currentJob = null;
						synchronized(this)
						{
							// Wait until new job arrives or measurement is finished.
							while(currentMeasurement.isJobQueueEmpty() && currentMeasurement.isRunning())
							{
								wait();
							}

							if(!currentMeasurement.isRunning())
							{
								// Current measurement finished. Stop the inner loop.
								currentMeasurement.shutdownMeasurement(microscope);
								break;
							}

							// Set current job to newly arrived job
							currentJob = currentMeasurement.unqueueJob();
						}

						if(currentJob != null)
						{
							currentJob.job.executeJob(new ExecutionInformation(currentMeasurement.getMeasurementStartTime(), currentJob.evaluationNumber), microscope, currentMeasurement.getMeasurementContext());
	
							// Check if thread got interrupted.
							if(Thread.interrupted())
							{
								throw new InterruptedException();
							}
						}
					}
				}
				finally
				{
					if(lockMicroscopeWhileRunning)
					{
						try
						{
							microscope.unlockExclusiveWrite();
						}
						catch(MicroscopeLockedException e)
						{
							ServerSystem.err.println("Could not give exclusive write access to microscope back after measurement execution finished.", e);
						}
					}
				}

				// Measurement finished normally.
				ServerSystem.out.println("Finished measurement \"" + currentMeasurement.getName() + "\".");
			}
			catch(InterruptedException e)
			{
				// User wants to stop the current measurement OR quit the program. We here only stop
				// the measurement, if
				// user wants to quit completely, he has also set shouldStop = true and the loop
				// won't be executed again...
				if(currentMeasurement != null)
				{
					currentMeasurement.failMeasurement(e);
					ServerSystem.out.println("Measurement \"" + currentMeasurement.getName() + "\" was interrupted.");
				}
			}
			catch(JobException | MeasurementException | RemoteException | RuntimeException e)
			{
				if(currentMeasurement != null)
				{
					currentMeasurement.failMeasurement(e);
					ServerSystem.err.println("Measurement \"" + currentMeasurement.getName() + "\" produced an error and thus the measurement was interrupted.", e);
				}
			}
			finally
			{
				// Set current measurement to zero
				measurementThread = null;
				currentMeasurement = null;
			}
			notifyCurrentMeasurementChanged();

			// Go out of the loop if signal was send.
			synchronized(this)
			{
				if(shouldStop)
					break;
			}

		}

		synchronized(this)
		{
			ServerSystem.out.println("Measurement manager quitting...");
			measurementManagerThread = null;
		}
	}

	/**
	 * Helper class to simplify access to {@link MeasurementImpl} during measurement processing.
	 * 
	 * @author Moritz Lang
	 */
	private class MeasurementSupervision
	{
		private final MeasurementImpl	measurement;
		MeasurementSupervision(MeasurementImpl measurement)
		{
			this.measurement = measurement;
		}

		public MeasurementContext getMeasurementContext() {
			return measurement.getMeasurementContext();
		}

		public void failMeasurement(Exception e) {
			measurement.failMeasurement(e);
		}

		public long getMeasurementStartTime() {
			return measurement.getStartTime();
		}

		boolean isMeasurement(MeasurementImpl measurement)
		{
			return this.measurement == measurement;
		}

		boolean isRunning()
		{
			return measurement.getState() == MeasurementState.RUNNING;
		}
		boolean isLockMicroscopeWhileRunning()
		{
			return measurement.isLockMicroscopeWhileRunning();
		}
		String getName()
		{
			return measurement.getName();
		}
		void startupMeasurement(Microscope microscope) throws MeasurementException, InterruptedException
		{
			measurement.startupMeasurement(microscope);
		}
		void shutdownMeasurement(Microscope microscope) throws MeasurementException, InterruptedException
		{
			measurement.shutdownMeasurement(microscope);
		}
		boolean isJobQueueEmpty()
		{
			MeasurementJobQueue queue = measurement.getJobQueue();
			return queue == null || queue.isJobQueueEmpty();
		}
		JobExecutionQueueElement unqueueJob()
		{
			MeasurementJobQueue queue = measurement.getJobQueue();
			return queue == null ? null : queue.unqueueJob();
		}
	}

	/**
	 * Interrupts a running measurement. If the measurement is currently not running, it tries to
	 * remove it from the measurement queue.
	 * 
	 * @param measurement Measurement to intterrupt.
	 */
	void interruptMeasurement(MeasurementImpl measurement)
	{
		if(measurement == null)
			throw new NullPointerException();
		if(currentMeasurement == null || !currentMeasurement.isMeasurement(measurement))
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
		if(currentMeasurement == null)
			return;
		if(measurementManagerThread == null)
		{
			// Measurement is current measurement, but is not executed.
			synchronized(this)
			{
				currentMeasurement = null;
			}
			notifyCurrentMeasurementChanged();
			return;
		}
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
		if(measurementManagerThread != null)
			measurementManagerThread.interrupt();
	}

	/**
	 * Returns the currently processed measurement or null, if no measurement is running.
	 * 
	 * @return Currently running measurement.
	 * @throws RemoteException
	 */
	Measurement getCurrentMeasurement() throws RemoteException
	{
		MeasurementSupervision measurement = currentMeasurement;
		if(measurement == null)
			return null;
		return new MeasurementRMI(measurement.measurement, this);
	}

	/**
	 * Returns a list of all currently queued measurements.
	 * 
	 * @return List of queued measurements.
	 * @throws RemoteException
	 */
	synchronized Measurement[] getMeasurementQueue() throws RemoteException
	{
		Measurement[] measurements = new Measurement[measurementQueue.size()];
		for(int i = 0; i < measurementQueue.size(); i++)
		{
			MeasurementImpl measurement = measurementQueue.elementAt(i);
			measurements[i] = new MeasurementRMI(measurement, this);
		}
		return measurements;
	}
}
