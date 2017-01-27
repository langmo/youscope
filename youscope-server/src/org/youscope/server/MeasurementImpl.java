/**
 * 
 */
package org.youscope.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.Vector;

import org.youscope.common.MeasurementContext;
import org.youscope.common.MessageListener;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.measurement.MeasurementListener;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.MeasurementState;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.task.MeasurementTask;
import org.youscope.common.task.TaskException;
import org.youscope.common.task.TaskListener;
import org.youscope.common.task.TaskState;

/**
 * @author Moritz Lang
 */
class MeasurementImpl
{
	private volatile MeasurementJobQueue jobQueue = null;
	
	private volatile ArrayList<MeasurementListener>	measurementListeners		= new ArrayList<MeasurementListener>();

	private volatile int							measurementRuntime			= -1;

	private volatile Vector<MeasurementTaskImpl>	tasks						= new Vector<MeasurementTaskImpl>();

	private volatile String							name						= "unnamed";

	// True if the current measurement should shut down.
	private volatile boolean						shouldMeasurementStop		= false;

	DeviceSetting[]								startUpDeviceSettings		= new DeviceSetting[0];

	DeviceSetting[]								shutDownDeviceSettings		= new DeviceSetting[0];

	private volatile boolean						lockMicroscopeWhileRunning	= true;

	private String									userDefinedType				= "";

	private volatile MeasurementState						measurementState			= MeasurementState.READY;

	private long									measurementStartTime					= -1;

	private long									measurementEndTime						= -1;
	
	private final MeasurementContextImpl measurementContext = new MeasurementContextImpl(this);
	
	private final HashMap<String, Serializable> initialMeasurementContextProperties = new HashMap<String, Serializable>();

	private final ArrayList<MessageListener> messageWriters = new ArrayList<MessageListener>();
	
	private final UUID uniqueIdentifier = UUID.randomUUID();
	
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

	MeasurementJobQueue getJobQueue()
	{
		return jobQueue;
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
	
	@SuppressWarnings("unused")
	protected void sendMessage(String message)
	{
		synchronized(messageWriters)
		{
			for(int i=0; i<messageWriters.size(); i++)
			{
				try {
					messageWriters.get(i).sendMessage(message);
				} 
				catch (RemoteException e) 
				{
					messageWriters.remove(i);
					i--;
				}
			}
		}
	}

	/**
	 * Sets the state of the measurement and notifies all listeners.
	 * Does nothing if the state remained the same, or if the current state is {@link MeasurementState#ERROR}.
	 * @param state New state of the measurement.
	 */
	private void toState(MeasurementState state)
	{
		MeasurementState oldState;
		synchronized(this)
		{
			oldState = this.measurementState;
			// prevent error state from being accidentially cleared.
			if(oldState == MeasurementState.ERROR)
				state = MeasurementState.ERROR;
			this.measurementState = state;
		}
		if(oldState != state)
			notifyMeasurementStateChanged(oldState, state);
	}
	/**
	 * Sets the measurement state to {@link MeasurementState#ERROR}, notifies all listeners of the state change, notifies all listeners of the error, and returns the exception (e.g. to be used in throws).
	 * @param e Exception which occurred.
	 * @return The exception gotten as a parameter.
	 */
	private <T extends Exception> T toErrorState(T e)
	{
		toState(MeasurementState.ERROR);
		notifyMeasurementError(e);
		return e;
	}
	
	private void assertCorrectState(String errorMessage, MeasurementState... allowedStates) throws MeasurementException
	{
		MeasurementState state = this.measurementState;
		for(MeasurementState allowedState : allowedStates)
		{
			if(state == allowedState)
				return;
		}
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
		message.append(". Current state: "+state.toString()+".");
		throw new MeasurementException(message.toString());
	}
	
	private void assertEditable() throws MeasurementRunningException
	{
		MeasurementState state = this.measurementState;
		if(state != MeasurementState.READY && state != MeasurementState.UNINITIALIZED && state != MeasurementState.QUEUED)
			throw new MeasurementRunningException();
	}

	private synchronized void addTask(MeasurementTaskImpl task) throws MeasurementRunningException
	{
		assertEditable();
		tasks.add(task);
	}

	MeasurementTask[] getTasks()
	{
		return tasks.toArray(new MeasurementTask[tasks.size()]);
	}

	synchronized void setTypeIdentifier(String type) throws MeasurementRunningException
	{
		assertEditable();
		userDefinedType = type;
	}

	String getTypeIdentifier()
	{
		return userDefinedType;
	}
	/**
	 * Sets the state of the measurement to {@link MeasurementState#QUEUED}.
	 * Should only be called by {@link MeasurementManager}.
	 */
	synchronized void queueMeasurement() throws MeasurementException 
	{
		assertCorrectState("Cannot queue measurement.", MeasurementState.READY, MeasurementState.UNINITIALIZED, MeasurementState.PAUSED);
		toState(MeasurementState.QUEUED);
	}
	/**
	 * Sets the state of the measurement to {@link MeasurementState#READY}.
	 * Should only be called by {@link MeasurementManager}.
	 */
	synchronized void unqueueMeasurement() throws MeasurementException 
	{
		assertCorrectState("Cannot unqueue measurement.", MeasurementState.QUEUED);
		toState(MeasurementState.READY);
	}
	/**
	 * Interrupts the measurement, sets it to error state, and notifies all listeners.
	 * Should only be called by {@link MeasurementManager#runMeasurements()} when processing of the measurement failed.
	 * @param e
	 */
	synchronized void failMeasurement(Exception e) 
	{
		toErrorState(e);
		stopMeasurement();
	}
	
	private synchronized void initializeMeasurement(Microscope microscope) throws MeasurementException, InterruptedException
	{
		assertCorrectState("Cannot initialize measurement.", MeasurementState.READY, MeasurementState.UNINITIALIZED, MeasurementState.QUEUED);
		
		toState(MeasurementState.INITIALIZING);
		sendMessage("Initializing measurement...");
		jobQueue = new MeasurementJobQueue();
					
		// Setup empty measurement context
        measurementContext.clear();
        for (Map.Entry<String, Serializable> entry : initialMeasurementContextProperties.entrySet())
        {
        	Serializable clone;
        	// we want to make a copy of the serializable object. However, we cannot assume that it implements Cloneable. Thus, we simply
        	// serializa and deserialize it, which has the same effect.
        	
        	try
        	{
            	ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            	ObjectOutputStream out = new ObjectOutputStream(outStream);
                out.writeObject(entry.getValue());
                out.close();
                ByteArrayInputStream inStream = new ByteArrayInputStream(outStream.toByteArray());
                ObjectInputStream in = new ObjectInputStream(inStream);
                clone = (Serializable) in.readObject();
                in.close();
        	}
        	catch(Exception e)
        	{
        		throw toErrorState(new MeasurementException("Serialization of initial measurement context property " + entry.getKey() + " failed. Is the context property not only implementing Serializable, but also follow the rules what is allowed and what not when implementing Serializable?", e));
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
				throw toErrorState(new MeasurementException("Could not apply measurement startup settings.", e));
			}
			// Stop if measurement got interrupted
			if(Thread.interrupted())
				throw toErrorState(new InterruptedException());
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
				throw toErrorState(new MeasurementException("Could not initialize all tasks of measurement.", e));
			}
			if(Thread.interrupted())
				throw toErrorState(new InterruptedException());
		}
		toState(MeasurementState.INITIALIZED);
		sendMessage("Finished initializing measurement.");
	}

	synchronized void uninitializeMeasurement(Microscope microscope) throws MeasurementException, InterruptedException
	{
		assertCorrectState("Cannot uninitialize measurement.", MeasurementState.STOPPED, MeasurementState.INITIALIZED, MeasurementState.PAUSED, MeasurementState.UNINITIALIZED);
		if(measurementState == MeasurementState.UNINITIALIZED)
			return;
		
		toState(MeasurementState.UNINITIALIZING);
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
				throw toErrorState(new MeasurementException("Could not uninitialize all tasks.", e));
			}
			if(Thread.interrupted())
				throw toErrorState(new InterruptedException());
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
				throw toErrorState(new MeasurementException("Could not apply all measurement shutdown device settings.", e));
			}
			// Stop if measurement got interrupted
			if(Thread.interrupted())
				throw toErrorState(new InterruptedException());
		}

		toState(MeasurementState.UNINITIALIZED);
		sendMessage("Finished uninitializing measurement.");
	}
	
	/**
	 * This function starts or resumes the measurement. It should only be called by {@link MeasurementManager#runMeasurements()}.
	 * 
	 * @throws InterruptedException 
	 * @throws MeasurementRunningException
	 * @throws RemoteException
	 */
	synchronized void startupMeasurement(Microscope microscope) throws MeasurementException, InterruptedException
	{
		// TODO: resume measurement
		shouldMeasurementStop = false;
		measurementStartTime = -1;
		measurementEndTime = -1;
		initializeMeasurement(microscope);
		runMeasurement();
	}
	/**
	 * This function runs the uninitialization of the measurement, or methods to pause it. It should only be called by {@link MeasurementManager#runMeasurements()} after the measurement has stopped.
	 * 
	 * @throws InterruptedException 
	 * @throws MeasurementRunningException
	 * @throws RemoteException
	 */
	synchronized void shutdownMeasurement(Microscope microscope) throws MeasurementException, InterruptedException
	{
		// TODO: pause measurement
		uninitializeMeasurement(microscope);
	}
	
	
	private synchronized void runMeasurement() throws MeasurementException
	{
		assertCorrectState("Cannot start/run measurement.", MeasurementState.INITIALIZED);
		
		toState(MeasurementState.RUNNING);
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
			throw toErrorState(e);
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
						stopMeasurement();
					}
				}
			}, measurementRuntime);
		}
		Thread waitUntilFinishThread = new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				synchronized(MeasurementImpl.this)
				{
					while(!shouldMeasurementStop && !Thread.interrupted())
					{
						try {
							MeasurementImpl.this.wait();
						} catch (@SuppressWarnings("unused") InterruptedException e) {
							break;
						}
					}
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
					toErrorState(new MeasurementException("Could not stop task.", exception));
				else
				{
					toState(MeasurementState.STOPPED);
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
			if(task.getState() == TaskState.RUNNING)
				return;
		}
		// All tasks are finished.
		stopMeasurement();
	}

	synchronized void stopMeasurement()
	{
		shouldMeasurementStop = true;
		if(measurementState == MeasurementState.RUNNING)
			toState(MeasurementState.STOPPING);
		notifyAll();
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
	

	synchronized void setStartupDeviceSettings(DeviceSetting[] settings) throws MeasurementRunningException
	{
		assertEditable();
		startUpDeviceSettings = new DeviceSetting[settings.length];
		for(int i=0; i<settings.length; i++)
		{
			startUpDeviceSettings[i] = new DeviceSetting(settings[i]);
		}
	}

	synchronized void addStartupDeviceSetting(DeviceSetting setting) throws MeasurementRunningException
	{
		assertEditable();
		DeviceSetting[] newSettings = new DeviceSetting[startUpDeviceSettings.length + 1];
		System.arraycopy(startUpDeviceSettings, 0, newSettings, 0, startUpDeviceSettings.length);
		newSettings[startUpDeviceSettings.length] = new DeviceSetting(setting);
		startUpDeviceSettings = newSettings;
	}

	synchronized void setFinishDeviceSettings(DeviceSetting[] settings) throws MeasurementRunningException
	{
		assertEditable();
		shutDownDeviceSettings = new DeviceSetting[settings.length];
		for(int i=0; i<settings.length; i++)
		{
			shutDownDeviceSettings[i] = new DeviceSetting(settings[i]);
		}
	}

	synchronized void addFinishDeviceSetting(DeviceSetting setting) throws MeasurementRunningException
	{
		assertEditable();
		DeviceSetting[] newSettings = new DeviceSetting[shutDownDeviceSettings.length + 1];
		System.arraycopy(shutDownDeviceSettings, 0, newSettings, 0, shutDownDeviceSettings.length);
		newSettings[shutDownDeviceSettings.length] = new DeviceSetting(setting);
		shutDownDeviceSettings = newSettings;
	}

	public String getName()
	{
		return name;
	}

	public synchronized void setName(String name)
	{
		if(name != null)
			this.name = name;
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

	public synchronized void setRuntime(int measurementRuntime) throws MeasurementRunningException
	{
		assertEditable();
		this.measurementRuntime = measurementRuntime;
	}

	public int getRuntime()
	{
		return measurementRuntime;
	}

	public synchronized void setLockMicroscopeWhileRunning(boolean lock) throws MeasurementRunningException
	{
		assertEditable();
		lockMicroscopeWhileRunning = lock;
	}

	public boolean isLockMicroscopeWhileRunning()
	{
		return lockMicroscopeWhileRunning;
	}

	public MeasurementState getState()
	{
		return measurementState;
	}

	synchronized MeasurementTask addTask(int period, boolean fixedTimes, int startTime) throws RemoteException, MeasurementRunningException
	{
		assertEditable();
		return addTask(period, fixedTimes, startTime, -1);
	}

	synchronized MeasurementTask addTask(int period, boolean fixedTimes, int startTime, int numExecutions) throws RemoteException, MeasurementRunningException
	{
		assertEditable();
		MeasurementTaskImpl measurementTask = new MeasurementTaskImpl(period, fixedTimes, startTime, numExecutions);
		addTask(measurementTask);
		return measurementTask;
	}

	synchronized MeasurementTask addMultiplePeriodTask(int[] periods, int breakTime, int startTime) throws RemoteException, MeasurementRunningException
	{
		assertEditable();
		return addMultiplePeriodTask(periods, breakTime, startTime, -1);
	}

	synchronized MeasurementTask addMultiplePeriodTask(int[] periods, int breakTime, int startTime, int numExecutions) throws RemoteException, MeasurementRunningException
	{
		assertEditable();
		MeasurementTaskImpl measurementTask = new MeasurementTaskImpl(periods, breakTime, startTime, numExecutions);
		addTask(measurementTask);
		return measurementTask;
	}

	public long getStartTime()
	{
		return measurementStartTime;
	}

	public long getEndTime()
	{
		return measurementEndTime;
	}
	public void setInitialMeasurementContextProperty(String identifier, Serializable property) throws MeasurementRunningException
    {
		assertEditable();
        initialMeasurementContextProperties.put(identifier, property);
    }
}
