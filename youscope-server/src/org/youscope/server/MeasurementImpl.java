/**
 * 
 */
package org.youscope.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.MeasurementContext;
import org.youscope.common.MessageListener;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.measurement.MeasurementListener;
import org.youscope.common.measurement.MeasurementState;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.task.Task;
import org.youscope.common.task.TaskException;
import org.youscope.common.task.TaskListener;
import org.youscope.common.task.TaskState;

/**
 * @author Moritz Lang
 */
class MeasurementImpl
{
	private volatile MeasurementJobQueue jobQueue = null;
	
	private final ArrayList<MeasurementListener>	measurementListeners		= new ArrayList<MeasurementListener>();

	private volatile int							measurementRuntime			= -1;

	private final ArrayList<MeasurementTaskImpl>	tasks						= new ArrayList<MeasurementTaskImpl>();

	private volatile String							name						= "unnamed";

	private DeviceSetting[]								startUpDeviceSettings		= new DeviceSetting[0];

	private DeviceSetting[]								shutDownDeviceSettings		= new DeviceSetting[0];

	private volatile boolean						lockMicroscopeWhileRunning	= true;

	private String									userDefinedType				= "";

	private volatile long									measurementStartTime					= -1;

	private volatile long									measurementEndTime						= -1;
	
	private final MeasurementContextImpl measurementContext = new MeasurementContextImpl(this);
	
	private final HashMap<String, Serializable> initialMeasurementContextProperties = new HashMap<String, Serializable>();

	private final ArrayList<MessageListener> messageWriters = new ArrayList<MessageListener>();
	
	private final UUID uniqueIdentifier = UUID.randomUUID();
	
	/**
	 * Class to set if the current measurement should run, and to wait until it shouldn't.
	 * @author Moritz Lang
	 *
	 */
	private final class RunState
	{
		private  MeasurementState measurementState = MeasurementState.READY;
		private boolean shouldRun = false;
		synchronized void setShouldRun(boolean shouldRun)
		{
			this.shouldRun = shouldRun;
			if(!shouldRun && measurementState == MeasurementState.RUNNING)
			{
				try {
					toState(MeasurementState.STOPPING);
				} catch (@SuppressWarnings("unused") MeasurementException e) {
					// do nothing: means we are already in error state, which we aren't, because we just checked...
				}
			}
			notifyAll();
		}
		synchronized void waitUntilStop() throws InterruptedException
		{
			while(shouldRun)
			{
				wait();
			}
		}
		synchronized void assertEditable() throws ComponentRunningException
		{
			if(!measurementState.isEditable())
				throw new ComponentRunningException();
		}
		/**
		 * Sets the state of the measurement and notifies all listeners.
		 * @param newState New state of the measurement.
		 * @throws MeasurementException Thrown if trying to change to non-error state while current state is {@link MeasurementState#ERROR}.
		 * @return true if new state is different from old.
		 */
		boolean toState(MeasurementState newState) throws MeasurementException
		{
			MeasurementState oldState;
			synchronized(this)
			{
				oldState = this.measurementState;
				// prevent error state from being accidentally cleared.
				if(oldState.isError() && !newState.isError())
					throw new MeasurementException("Cannot change state of measurement to "+newState.toString()+", because measurement is in error state.");
				this.measurementState = newState;
			}
			if(oldState != newState)
			{
				notifyMeasurementStateChanged(oldState, newState);
				return true;
			}
			return false;
		}
		
		MeasurementState getState()
		{
			return measurementState;
		}
		
		/**
		 * Sets the measurement state to {@link MeasurementState#ERROR}, notifies all listeners of the state change, notifies all listeners of the error, and returns the exception (e.g. to be used in throws).
		 * @param e Exception which occurred.
		 * @return The exception gotten as a parameter.
		 */
		<T extends Exception> T toErrorState(T e)
		{
			try {
				toState(MeasurementState.ERROR);
			} catch (@SuppressWarnings("unused") MeasurementException e1) {
				// should not happen, since changing to error state should be safe.
				// Since we are anyways trying to handle an error, do nothing.
			}
			notifyMeasurementError(e);
			return e;
		}
	
		/**
		 * Sets the state of the measurement and notifies all listeners, if the current state is in one of the allowed states.
		 * If not, an error is thrown and the state is not changed.
		 * @param state New state of the measurement.
		 * @return true if new state is different from old.
		 */
		boolean toState(MeasurementState newState, String errorMessage, MeasurementState... allowedStates) throws MeasurementException
		{
			MeasurementState currentState;
			synchronized(this)
			{
				currentState = this.measurementState;
				for(MeasurementState allowedState : allowedStates)
				{
					if(currentState == allowedState)
					{
						return toState(newState);
					}
				}
			}
			// if we are here, we were not in one of the allowed states.
			StringBuilder message = new StringBuilder(errorMessage);
			message.append(" Expected state: ");
			for(int i=0; i<allowedStates.length; i++)
			{
				if(i>0 && i==allowedStates.length-1)
					message.append(" or ");
				else if(i>0)
					message.append(", ");
				message.append(allowedStates[i].toString());
			}
			message.append(". Current state: "+currentState.toString()+".");
			throw new MeasurementException(message.toString());
		}
	}
	private final RunState runState = new RunState();
	
	/**
	 * Measurement with infinite runtime.
	 */
	MeasurementImpl()
	{
		this(-1);
	}
	/**
	 * Measurement which stops after a certain runtime. Set runtime to negative values for infinite runtime.
	 * @param measurementRuntime time, in ms, after which measurement should stop.
	 */
	MeasurementImpl(int measurementRuntime)
	{
		this.measurementRuntime = measurementRuntime;
	}
	MeasurementContext getMeasurementContext()
    {
        return measurementContext;
    }

	public UUID getUUID() 
	{
		return uniqueIdentifier;
	}
		
	public void addMessageListener(MessageListener writer)
	{
		synchronized(messageWriters)
		{
			messageWriters.add(writer);
		}
	}

	public void removeMessageListener(MessageListener writer)
	{
		synchronized(messageWriters)
		{
			messageWriters.remove(writer);
		}
	}
	
	private void sendMessage(String message)
	{
		synchronized(messageWriters)
		{
			for (Iterator<MessageListener> iterator = messageWriters.iterator(); iterator.hasNext();) 
			{
				try
	            {
	            	iterator.next().sendMessage(message);
	            } 
				catch (RemoteException e1)
	            {
	                ServerSystem.err.println("Measurement message listener not answering. Removing him from the queue.", e1);
	                iterator.remove();
	            }
	        }
		}
	}
	
	private Task addTask(MeasurementTaskImpl task) throws ComponentRunningException
	{
		synchronized(runState)
		{
			runState.assertEditable();
			tasks.add(task);
		}
		return task;
	}

	Task[] getTasks()
	{
		return tasks.toArray(new Task[tasks.size()]);
	}

	void setTypeIdentifier(String type) throws ComponentRunningException
	{
		synchronized(runState)
		{
			runState.assertEditable();
			userDefinedType = type;
		}
	}

	String getTypeIdentifier()
	{
		return userDefinedType;
	}
	/**
	 * Sets the state of the measurement to {@link MeasurementState#QUEUED}.
	 * Should only be called by {@link MeasurementManager}.
	 */
	void queueMeasurement() throws MeasurementException 
	{
		runState.toState(MeasurementState.QUEUED, "Cannot queue measurement.", MeasurementState.READY, MeasurementState.UNINITIALIZED, MeasurementState.PAUSED);
	}
	/**
	 * Sets the state of the measurement to {@link MeasurementState#READY}.
	 * Should only be called by {@link MeasurementManager}.
	 */
	void unqueueMeasurement() throws MeasurementException 
	{
		runState.toState(MeasurementState.READY, "Cannot unqueue measurement.", MeasurementState.QUEUED);
	}
	/**
	 * Interrupts the measurement, sets it to error state, and notifies all listeners.
	 * Should only be called by {@link MeasurementManager#runMeasurements()} when processing of the measurement failed.
	 * @param e
	 */
	void failMeasurement(Exception e) 
	{
		runState.toErrorState(e);
		stopMeasurement(false);
	}
	
	private synchronized void initializeMeasurement(Microscope microscope) throws MeasurementException, InterruptedException
	{
		runState.toState(MeasurementState.INITIALIZING, "Cannot initialize measurement.", MeasurementState.READY, MeasurementState.UNINITIALIZED, MeasurementState.QUEUED);
		
		sendMessage("Initializing measurement...");
		jobQueue = new MeasurementJobQueue();
					
		// Setup empty measurement context
        measurementContext.clear();
        for (Map.Entry<String, Serializable> entry : initialMeasurementContextProperties.entrySet())
        {
        	Serializable clone;
        	// we want to make a copy of the serializable object. However, we cannot assume that it implements Cloneable. Thus, we simply
        	// serialize and deserialize it, which has the same effect.
        	
        	ByteArrayOutputStream outStream = null;
        	ObjectOutputStream out = null;
        	ByteArrayInputStream inStream = null;
        	ObjectInputStream in = null;
        	try
        	{
            	outStream = new ByteArrayOutputStream();
            	out = new ObjectOutputStream(outStream);
                out.writeObject(entry.getValue());
                inStream = new ByteArrayInputStream(outStream.toByteArray());
                in = new ObjectInputStream(inStream);
                clone = (Serializable) in.readObject();
        	}
        	catch(Exception e)
        	{
        		throw runState.toErrorState(new MeasurementException("Serialization of initial measurement context property " + entry.getKey() + " failed. Is the context property not only implementing Serializable, but also follow the rules what is allowed and what not when implementing Serializable?", e));
        	}
        	finally
        	{
        		if(outStream != null)
        		{
        			try {
						outStream.close();
					} catch (@SuppressWarnings("unused") IOException e) {
						// ignore close exceptions.
					}
        		}
        		if(out != null)
        		{
        			try {
						outStream.close();
					} catch (@SuppressWarnings("unused") IOException e) {
						// ignore close exceptions.
					}
        		}
        		if(inStream != null)
        		{
        			try {
						outStream.close();
					} catch (@SuppressWarnings("unused") IOException e) {
						// ignore close exceptions.
					}
        		}
        		if(in != null)
        		{
        			try {
						outStream.close();
					} catch (@SuppressWarnings("unused") IOException e) {
						// ignore close exceptions.
					}
        		}
        	}
        	
            measurementContext.setProperty(entry.getKey(), clone);
        }


		// Process startup settings
		if(startUpDeviceSettings != null && startUpDeviceSettings.length > 0)
		{
			try
			{
				microscope.applyDeviceSettings(startUpDeviceSettings);
			}
			catch(Exception e)
			{
				throw runState.toErrorState(new MeasurementException("Could not apply measurement startup settings.", e));
			}
			// Stop if measurement got interrupted
			if(Thread.interrupted())
				throw runState.toErrorState(new InterruptedException());
		}
		// Initialize tasks and their jobs
		for(MeasurementTaskImpl task : tasks)
		{
			try
			{
				task.initializeTask(microscope, measurementContext);
			}
			catch(Exception e)
			{
				throw runState.toErrorState(new MeasurementException("Could not initialize all tasks of measurement.", e));
			}
			if(Thread.interrupted())
				throw runState.toErrorState(new InterruptedException());
		}
		runState.toState(MeasurementState.INITIALIZED, "Cannot finish measurement initialization.", MeasurementState.INITIALIZING);
		sendMessage("Finished initializing measurement.");
	}

	private synchronized void uninitializeMeasurement(Microscope microscope) throws MeasurementException, InterruptedException
	{
		boolean alreadyUninitialized = !runState.toState(MeasurementState.UNINITIALIZING, "Cannot uninitialize measurement.", MeasurementState.STOPPED, MeasurementState.INITIALIZED, MeasurementState.PAUSED, MeasurementState.UNINITIALIZED);
		if(alreadyUninitialized)
			return;
		sendMessage("Uninitializing measurement...");

		// Uninitialize tasks and their jobs
		for(MeasurementTaskImpl task : tasks)
		{
			try
			{
				task.uninitializeTask(microscope, measurementContext);
			}
			catch(Exception e)
			{
				throw runState.toErrorState(new MeasurementException("Could not uninitialize all tasks.", e));
			}
			if(Thread.interrupted())
				throw runState.toErrorState(new InterruptedException());
		}

		// Process shutdown settings
		if(shutDownDeviceSettings != null && shutDownDeviceSettings.length > 0)
		{
			try
			{
				microscope.applyDeviceSettings(shutDownDeviceSettings);
			}
			catch(Exception e)
			{
				throw runState.toErrorState(new MeasurementException("Could not apply all measurement shutdown device settings.", e));
			}
			// Stop if measurement got interrupted
			if(Thread.interrupted())
				throw runState.toErrorState(new InterruptedException());
		}
		
		jobQueue = null;

		runState.toState(MeasurementState.UNINITIALIZED, "Cannot finish uninitialization.", MeasurementState.UNINITIALIZING);
		sendMessage("Finished uninitializing measurement.");
	}
	
	/**
	 * This function starts or resumes the measurement. It should only be called by {@link MeasurementManager#runMeasurements()}.
	 * 
	 * @throws InterruptedException 
	 * @throws ComponentRunningException
	 * @throws RemoteException
	 */
	synchronized MeasurementJobQueue startupMeasurement(Microscope microscope) throws MeasurementException, InterruptedException
	{
		// TODO: resume measurement
		runState.setShouldRun(true);
		measurementStartTime = -1;
		measurementEndTime = -1;
		initializeMeasurement(microscope);
		runMeasurement();
		return jobQueue;
	}
	/**
	 * This function runs the uninitialization of the measurement, or methods to pause it. It should only be called by {@link MeasurementManager#runMeasurements()} after the measurement has stopped.
	 * 
	 * @throws InterruptedException 
	 * @throws ComponentRunningException
	 * @throws RemoteException
	 */
	synchronized void shutdownMeasurement(Microscope microscope) throws MeasurementException, InterruptedException
	{
		// TODO: pause measurement
		uninitializeMeasurement(microscope);
	}
	
	
	private synchronized void runMeasurement() throws MeasurementException
	{
		runState.toState(MeasurementState.RUNNING, "Cannot start/run measurement.", MeasurementState.INITIALIZED);
		sendMessage("Starting measurement...");
		
		// start tasks.
		final TaskListener taskListener = new TaskListener() {
			
			@Override
			public void taskStateChanged(TaskState oldState, TaskState newState) throws RemoteException {
				checkIfFinished();
			}
			
			@Override
			public void taskExecuted(int executionNumber) throws RemoteException {
				// do nothing.
			}
			
			@Override
			public void taskError(Exception e) throws RemoteException {
				// do nothing
			}
		};
		try
		{
			// Activate tasks
			for(MeasurementTaskImpl task : tasks)
			{
				task.addTaskListener(taskListener);
				try {
					task.startTask(jobQueue);
				} catch (TaskException e1) {
					throw new MeasurementException("Error while starting tasks.", e1);
				}
			}
		}
		catch(MeasurementException e)
		{
			for(MeasurementTaskImpl task : tasks)
			{
				try 
				{
					task.stopTask();
				} 
				catch (@SuppressWarnings("unused") TaskException e1) 
				{
					// do nothing, we already have an exception to throw
				}
				task.removeTaskListener(taskListener);
			}
			measurementStartTime = System.currentTimeMillis();
			measurementEndTime = System.currentTimeMillis();
			throw runState.toErrorState(e);
		}
		
		measurementStartTime = System.currentTimeMillis();
		final Timer measurementStopTimer = new Timer("Measurement Stop Timer");
		if(measurementRuntime > 0)
		{
			// Measurement should be stopped after a certain time. Set a
			// timer to do so. 
			measurementStopTimer.schedule(new TimerTask()
			{
				@Override
				public void run()
				{
					synchronized(MeasurementImpl.this)
					{
						stopMeasurement(true);
					}
				}
			}, measurementRuntime);
		}
		Thread waitUntilFinishThread = new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				try {
					runState.waitUntilStop();
				} catch (@SuppressWarnings("unused") InterruptedException e) {
					// if interrupted, somebody wants us to stop, which we do anyways...
					runState.setShouldRun(false);
				}
				
				measurementStopTimer.cancel();
				// Stop tasks
				TaskException exception = null;
				for(MeasurementTaskImpl task : tasks)
				{
					try 
					{
						task.stopTask();
					} 
					catch (TaskException e1) 
					{
						exception = e1;
					}
					task.removeTaskListener(taskListener);
				}
				measurementEndTime = System.currentTimeMillis();
				if(exception != null)
					runState.toErrorState(new MeasurementException("Could not stop task.", exception));
				else
				{
					try {
						runState.toState(MeasurementState.STOPPED);
					} catch (@SuppressWarnings("unused") MeasurementException e) {
						// do nothing, means we are already in error state...
					}
					sendMessage("Finished measurement.");
				}
			}
	
		});
		waitUntilFinishThread.setDaemon(true);
		waitUntilFinishThread.start();
	}

	private synchronized void checkIfFinished()
	{
		for(MeasurementTaskImpl task : tasks)
		{
			if(task.getState().isRunning())
				return;
		}
		// All tasks are finished.
		stopMeasurement(true);
	}

	void stopMeasurement(boolean processJobQueue)
	{
		runState.setShouldRun(false);
		if(!processJobQueue)
		{
			MeasurementJobQueue jobQueue = this.jobQueue;
			if(jobQueue != null)
				jobQueue.clearAndBlock();
		}
	}
	
	/**
	 * Calls {@link MeasurementListener#measurementError(Exception)} on all measurement listeners.
	 */
	private void notifyMeasurementError(Exception e)
    {
		synchronized(measurementListeners)
		{
			for (Iterator<MeasurementListener> iterator = measurementListeners.iterator(); iterator.hasNext();) 
			{
				try
	            {
	            	iterator.next().measurementError(e);
	            } 
				catch (RemoteException e1)
	            {
	                ServerSystem.err.println("Measurement listener not answering. Removing him from the queue.", e1);
	                iterator.remove();
	            }
	        }
		}
    }
	/**
	 * Calls {@link MeasurementListener#measurementStateChanged(MeasurementState, MeasurementState)} on all measurement listeners.
	 */
	private void notifyMeasurementStateChanged(MeasurementState oldState, MeasurementState newState)
    {
		synchronized(measurementListeners)
		{
			for (Iterator<MeasurementListener> iterator = measurementListeners.iterator(); iterator.hasNext();) 
			{
				try
	            {
	            	iterator.next().measurementStateChanged(oldState, newState);
	            } 
				catch (RemoteException e)
	            {
	                ServerSystem.err.println("Measurement listener not answering. Removing him from the queue.", e);
	                iterator.remove();
	            }
	        }
		}
    }
	
	/**
	 * Calls {@link MeasurementListener#measurementStructureModified()} on all measurement listeners.
	 */
	void notifyMeasurementStructureModified()
    {
		synchronized(measurementListeners)
		{
			for (Iterator<MeasurementListener> iterator = measurementListeners.iterator(); iterator.hasNext();) 
			{
				try
	            {
	            	iterator.next().measurementStructureModified();
	            } 
				catch (RemoteException e)
	            {
	                ServerSystem.err.println("Measurement listener not answering. Removing him from the queue.", e);
	                iterator.remove();
	            }
	        }
		}
    }
	

	void setStartupDeviceSettings(DeviceSetting[] settings) throws ComponentRunningException
	{
		
		DeviceSetting[] copy = new DeviceSetting[settings.length];
		for(int i=0; i<settings.length; i++)
		{
			copy[i] = new DeviceSetting(settings[i]);
		}
		
		synchronized(runState)
		{
			runState.assertEditable();
			startUpDeviceSettings = copy;
		}
	}

	void addStartupDeviceSetting(DeviceSetting setting) throws ComponentRunningException
	{
		DeviceSetting[] newSettings = new DeviceSetting[startUpDeviceSettings.length + 1];
		System.arraycopy(startUpDeviceSettings, 0, newSettings, 0, startUpDeviceSettings.length);
		newSettings[startUpDeviceSettings.length] = new DeviceSetting(setting);
		
		synchronized(runState)
		{
			runState.assertEditable();
			startUpDeviceSettings = newSettings;
		}
	}

	void setFinishDeviceSettings(DeviceSetting[] settings) throws ComponentRunningException
	{
		DeviceSetting[] copy = new DeviceSetting[settings.length];
		for(int i=0; i<settings.length; i++)
		{
			copy[i] = new DeviceSetting(settings[i]);
		}
		synchronized(runState)
		{
			runState.assertEditable();
			shutDownDeviceSettings = copy;
		}
	}

	void addFinishDeviceSetting(DeviceSetting setting) throws ComponentRunningException
	{
		DeviceSetting[] newSettings = new DeviceSetting[shutDownDeviceSettings.length + 1];
		System.arraycopy(shutDownDeviceSettings, 0, newSettings, 0, shutDownDeviceSettings.length);
		newSettings[shutDownDeviceSettings.length] = new DeviceSetting(setting);
		synchronized(runState)
		{
			runState.assertEditable();
			shutDownDeviceSettings = newSettings;
		}
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name) throws ComponentRunningException
	{
		if(name == null)
			throw new NullPointerException();
		synchronized(runState)
		{
			runState.assertEditable();
			this.name = name;
		}
	}

	public void removeMeasurementListener(MeasurementListener listener)
	{
		synchronized(measurementListeners)
		{
			measurementListeners.remove(listener);
		}
	}

	public void addMeasurementListener(MeasurementListener listener)
	{
		synchronized(measurementListeners)
		{
			measurementListeners.add(listener);
		}
	}

	public void setRuntime(int measurementRuntime) throws ComponentRunningException
	{
		synchronized(runState)
		{
			runState.assertEditable();
			this.measurementRuntime = measurementRuntime;
		}
	}

	public int getRuntime()
	{
		return measurementRuntime;
	}

	public void setLockMicroscopeWhileRunning(boolean lock) throws ComponentRunningException
	{
		synchronized(runState)
		{
			runState.assertEditable();
			lockMicroscopeWhileRunning = lock;
		}
	}

	public boolean isLockMicroscopeWhileRunning()
	{
		return lockMicroscopeWhileRunning;
	}

	public MeasurementState getState()
	{
		return runState.getState();
	}

	Task addTask(int period, boolean fixedTimes, int startTime) throws RemoteException, ComponentRunningException
	{
		synchronized(runState)
		{
			runState.assertEditable();
			return addTask(period, fixedTimes, startTime, -1);
		}
	}

	Task addTask(int period, boolean fixedTimes, int startTime, int numExecutions) throws RemoteException, ComponentRunningException
	{
		MeasurementTaskImpl measurementTask = new MeasurementTaskImpl(period, fixedTimes, startTime, numExecutions);
		synchronized(runState)
		{
			runState.assertEditable();
			return addTask(measurementTask);
		}
	}

	Task addMultiplePeriodTask(int[] periods, int breakTime, int startTime) throws RemoteException, ComponentRunningException
	{
		synchronized(runState)
		{
			runState.assertEditable();
			return addMultiplePeriodTask(periods, breakTime, startTime, -1);
		}
	}

	Task addMultiplePeriodTask(int[] periods, int breakTime, int startTime, int numExecutions) throws RemoteException, ComponentRunningException
	{
		MeasurementTaskImpl measurementTask = new MeasurementTaskImpl(periods, breakTime, startTime, numExecutions);
		
		synchronized(runState)
		{
			runState.assertEditable();
			return addTask(measurementTask);
		}
	}

	public long getStartTime()
	{
		return measurementStartTime;
	}

	public long getEndTime()
	{
		return measurementEndTime;
	}
	public void setInitialMeasurementContextProperty(String identifier, Serializable property) throws ComponentRunningException
    {
		synchronized(runState)
		{
			runState.assertEditable();
			initialMeasurementContextProperties.put(identifier, property);
		}
    }
}
