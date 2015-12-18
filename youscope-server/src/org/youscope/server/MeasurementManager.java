/**
 * 
 */
package org.youscope.server;

import java.rmi.RemoteException;
import java.util.Vector;

import org.youscope.common.ExecutionInformation;
import org.youscope.common.job.JobException;
import org.youscope.common.measurement.Measurement;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.measurement.MeasurementRunningException;
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
	 * Queue for the jobs. Jobs are added by the currently running measurement and processed first
	 * in first out.
	 */
	private volatile Vector<JobExecutionQueueElement>	jobQueue					= new Vector<JobExecutionQueueElement>();

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
	 * Indicating if currently a measurement is processed or if the worker thread is waiting for new
	 * measurements to arrive.
	 */
	private volatile boolean							isProcessingMeasurement		= false;

	/**
	 * The currently running measurement.
	 */
	private volatile MeasurementImpl					currentMeasurement			= null;

	/**
	 * All listener which get notified if there is a change in the current measurement or in the
	 * measurement queue.
	 */
	protected Vector<MicroscopeStateListener>			queueListener				= new Vector<MicroscopeStateListener>();

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
		synchronized(queueListener)
		{
			queueListener.addElement(listener);
		}
	}

	/**
	 * Removes a listener.
	 * 
	 * @param listener
	 */
	void removeQueueListener(MicroscopeStateListener listener)
	{
		synchronized(queueListener)
		{
			queueListener.removeElement(listener);
		}
	}

	/**
	 * Used internally to notify all queue listener that the queue changed.
	 */
	private void signalQueueChanged()
	{
		synchronized(queueListener)
		{
			for(int i = 0; i < queueListener.size(); i++)
			{
				MicroscopeStateListener listener = queueListener.elementAt(i);
				try
				{
					listener.measurementQueueChanged();
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					queueListener.removeElementAt(i);
				}
			}
		}
	}

	/**
	 * Used internally to notify all queue listener that the current measurement changed.
	 */
	private void signalCurrentMeasurementChanged()
	{
		synchronized(queueListener)
		{
			for(int i = 0; i < queueListener.size(); i++)
			{
				MicroscopeStateListener listener = queueListener.elementAt(i);
				try
				{
					listener.currentMeasurementChanged();
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					queueListener.removeElementAt(i);
				}
			}
		}
	}

	/**
	 * Adds a measurement to the measurement queue and eventually starts it automatically.
	 * 
	 * @param measurement The measurement to add.
	 */
	void addMeasurement(MeasurementImpl measurement)
	{
		int queueLength;
		boolean currentlyRunningMeasurement;
		synchronized(this)
		{
			measurementQueue.add(measurement);
			queueLength = measurementQueue.size();
			currentlyRunningMeasurement = isProcessingMeasurement;
			notifyAll();
		}

		// If the measurement is the only element in the queue AND no measurement is currently
		// running, than this measurement will be
		// started immediately. If this is not true, inform the measurement that its execution will
		// be delayed.
		if(queueLength > 1 || currentlyRunningMeasurement)
		{
			measurement.measurementQueued();
			ServerSystem.out.println("Queued measurement \"" + measurement.getName() + "\" (" + Integer.toString(queueLength) + " measurements currently in the queue).");
		}

		signalQueueChanged();
	}

	/**
	 * Removes a measurement from the queue. Does nothing if the measurement is not in the queue or
	 * currently running.
	 * 
	 * @param measurement
	 */
	void removeMeasurement(MeasurementImpl measurement)
	{
		boolean unqueued;
		synchronized(this)
		{
			unqueued = measurementQueue.remove(measurement);
		}
		if(unqueued)
		{
			measurement.measurementUnqueued();
			signalQueueChanged();
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
					currentMeasurement = measurementQueue.remove(0);
					isProcessingMeasurement = true;
				}

				ServerSystem.out.println("Starting measurement \"" + currentMeasurement.getName() + "\".");
				signalQueueChanged();
				signalCurrentMeasurementChanged();

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

					// Initialize measurement
					currentMeasurement.initializeMeasurement(microscope);

					// Run measurement
					MeasurementSupervision supervision = new MeasurementSupervision(currentMeasurement);
					measurementThread = currentMeasurement.runMeasurement(supervision);

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
							while(jobQueue.isEmpty() && currentMeasurement.isRunning())
							{
								wait();
							}

							if(jobQueue.isEmpty())
							{
								// Current measurement finished. Stop the inner loop.
								currentMeasurement.uninitializeMeasurement(microscope);
								break;
							}

							// Set current job to newly arrived job
							currentJob = jobQueue.remove(0);
						}

						currentJob.job.executeJob(new ExecutionInformation(currentJob.measurementStartTime, currentJob.evaluationNumber), microscope, currentMeasurement.getMeasurementContext());

						// Check if thread got interrupted.
						if(Thread.interrupted())
						{
							throw new InterruptedException();
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
					jobQueue.clear();
					isProcessingMeasurement = false;
				}

				// Measurement finished normally.
				ServerSystem.out.println("Finished measurement \"" + currentMeasurement.getName() + "\".");
			}
			catch(@SuppressWarnings("unused") InterruptedException e)
			{
				// User wants to stop the current measurement OR quit the program. We here only stop
				// the measurement, if
				// user wants to quit completely, he has also set shouldStop = true and the loop
				// won't be executed again...
				if(measurementThread != null)
					measurementThread.interrupt();
				if(currentMeasurement != null)
					ServerSystem.out.println("Processing of measurement \"" + currentMeasurement.getName() + "\" was interrupted.");
			}
			catch(JobException e)
			{
				if(measurementThread != null)
					measurementThread.interrupt();
				if(currentMeasurement != null)
				{
					ServerSystem.err.println("One of the jobs of measurement \"" + currentMeasurement.getName() + "\" produced an error and thus the measurement was interrupted.", e);
					currentMeasurement.measurementFailed(e);
				}
			}
			catch(MeasurementException e)
			{
				if(measurementThread != null)
					measurementThread.interrupt();
				if(currentMeasurement != null)
				{
					ServerSystem.err.println("The measurement \"" + currentMeasurement.getName() + "\" produced an error and thus was interrupted.", e);
					currentMeasurement.measurementFailed(e);
				}
			}
			catch(MeasurementRunningException e)
			{
				if(currentMeasurement != null)
				{
					ServerSystem.err.println("The measurement \"" + currentMeasurement.getName() + "\" should have been started, however, was already running. Do not restart a measurement before it was finished.", e);
					currentMeasurement.measurementFailed(e);
				}
			}
			catch(RemoteException e)
			{
				if(measurementThread != null)
					measurementThread.interrupt();
				if(currentMeasurement != null)
				{
					ServerSystem.err.println("Measurement \"" + currentMeasurement.getName() + "\" had problems to remotely communicate with the microscope and thus was interrupted.", e);
					currentMeasurement.measurementFailed(null);
				}
			}
			catch(RuntimeException e) 
			{
				if(measurementThread != null)
					measurementThread.interrupt();
				if(currentMeasurement != null)
				{
					ServerSystem.err.println("Measurement \"" + currentMeasurement.getName() + "\" produced an unexpected error and, thus, the measurement was interrupted.", e);
					currentMeasurement.measurementFailed(e);
				}
			}
			finally
			{
				// Set current measurement to zero
				measurementThread = null;
				currentMeasurement = null;
			}
			signalCurrentMeasurementChanged();

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
	 * Class to monitor state of a measurement.
	 * 
	 * @author langmo
	 */
	class MeasurementSupervision implements MeasurementControlListener
	{
		private MeasurementImpl	measurement;

		MeasurementSupervision(MeasurementImpl measurement)
		{
			this.measurement = measurement;
		}

		@Override
		public void measurementFinished()
		{
			synchronized(MeasurementManager.this)
			{
				MeasurementManager.this.notifyAll();
			}
		}

		@Override
		public void addJobToExecutionQueue(JobExecutionQueueElement job)
		{
			if(measurement.isRunning())
				addElementaryJob(job);
		}
	}

	/**
	 * Internally used if a measurement wants to start (queue) a new job.
	 * 
	 * @param job The job to start.
	 */
	synchronized void addElementaryJob(JobExecutionQueueElement job)
	{
		jobQueue.add(job);
		notifyAll();
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
		if(currentMeasurement == null || currentMeasurement != measurement)
		{
			// Measurement is not currently running. Just remove it from the queue.
			removeMeasurement(measurement);
			return;
		}
		interruptCurrentMeasurement();
	}
	synchronized void quickStopMeasurement(MeasurementImpl measurement)
    {
        if (measurement == null)
            throw new NullPointerException();
        if (currentMeasurement == null || currentMeasurement != measurement)
        {
            // Measurement is not currently running. Just remove it from the queue.
            removeMeasurement(measurement);
            return;
        }
       
        jobQueue.clear();
        notifyAll();
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
			signalCurrentMeasurementChanged();
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
	synchronized Measurement getCurrentMeasurement() throws RemoteException
	{
		if(currentMeasurement == null)
			return null;
		return new MeasurementRMI(currentMeasurement, this);
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
