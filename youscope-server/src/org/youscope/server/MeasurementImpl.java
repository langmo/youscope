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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;

import org.youscope.common.MessageListener;
import org.youscope.common.measurement.MeasurementContext;
import org.youscope.common.measurement.MeasurementException;
import org.youscope.common.measurement.MeasurementListener;
import org.youscope.common.measurement.MeasurementRunningException;
import org.youscope.common.measurement.MeasurementState;
import org.youscope.common.measurement.task.MeasurementTask;
import org.youscope.common.measurement.task.TaskListener;
import org.youscope.common.microscope.DeviceSettingDTO;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.MicroscopeException;

/**
 * @author Moritz Lang
 */
class MeasurementImpl
{
	private volatile boolean						running						= false;

	private volatile Vector<MeasurementListener>	measurementListeners		= new Vector<MeasurementListener>();

	private volatile int							measurementRuntime			= -1;

	private volatile Vector<MeasurementTaskImpl>	tasks						= new Vector<MeasurementTaskImpl>();

	private volatile String							name						= "unnamed";

	// True if the current measurement should shut down.
	private volatile boolean						shouldMeasurementStop		= false;
	private volatile boolean shouldMeasurementQuickStop = false;

	DeviceSettingDTO[]								startUpDeviceSettings		= new DeviceSettingDTO[0];

	DeviceSettingDTO[]								shutDownDeviceSettings		= new DeviceSettingDTO[0];

	private volatile boolean						lockMicroscopeWhileRunning	= true;

	private String									userDefinedType				= "";

	private MeasurementState						measurementState			= MeasurementState.READY;

	private Date									startTime					= null;

	private Date									endTime						= null;
	
	private final MeasurementContextImpl measurementContext = new MeasurementContextImpl(this);
	
	private final HashMap<String, Serializable> initialMeasurementContextProperties = new HashMap<String, Serializable>();

	private final ArrayList<MessageListener> messageWriters = new ArrayList<MessageListener>();
	
	private final UUID uniqueIdentifier = UUID.randomUUID();
	
	MeasurementImpl()
	{
		this(-1);
	}

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
	public boolean isRunning()
	{
		return running;
	}
	
	public synchronized void quickStopMeasurement()
    {
        shouldMeasurementQuickStop = true;
        stopMeasurement();
    }
	
	void measurementStructureModified()
    {
        MeasurementListener[] listeners = measurementListeners.toArray(new MeasurementListener[0]);
        for (MeasurementListener l : listeners)
        {
            try
            {
                l.measurementStructureModified();
            } catch (RemoteException e)
            {
                ServerSystem.err.println("Measurement listener not answering. Removing him from the queue.", e);
                measurementListeners.remove(l);
            }
        }
    }

	private synchronized void setRunning(boolean running)
	{
		this.running = running;
		if(running)
			startTime = new Date();
		else
			endTime = new Date();
		notifyAll();
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

	private synchronized void assertRunning() throws MeasurementRunningException
	{
		if(isRunning())
			throw new MeasurementRunningException();
	}

	private synchronized void addTask(MeasurementTaskImpl task) throws MeasurementRunningException
	{
		assertRunning();
		tasks.add(task);
	}

	MeasurementTask[] getTasks()
	{
		return tasks.toArray(new MeasurementTask[tasks.size()]);
	}

	synchronized void setTypeIdentifier(String type) throws MeasurementRunningException
	{
		assertRunning();
		userDefinedType = type;
	}

	String getTypeIdentifier()
	{
		return userDefinedType;
	}

	synchronized void initializeMeasurement(Microscope microscope) throws RemoteException, MeasurementException, InterruptedException, MeasurementRunningException
	{
		assertRunning();
		sendMessage("Starting initializing measurement.");
		
		measurementState = MeasurementState.INITIALIZING;
		for(int i = 0; i < measurementListeners.size(); i++)
		{
			MeasurementListener listener = measurementListeners.elementAt(i);
			try
			{
				listener.measurementInitializing();
			}
			catch(RemoteException e)
			{
				ServerSystem.err.println("Measurement listener not answering. Removing him from the queue.", e);
				measurementListeners.removeElementAt(i);
				i--;
			}
		}
		
		// Setup empty measurement context
        measurementContext.clear();
        synchronized (initialMeasurementContextProperties)
        {
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
            		throw new MeasurementException("Serialization of initial measurement context property " + entry.getKey() + " failed. Is the context property not only implementing Serializable, but also follow the rules what is allowed and what not when implementing Serializable?", e);
            	}
            	
                measurementContext.setProperty(entry.getKey(), clone);
            }
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
				throw new MeasurementException("Could not apply measurement start settings.", e);
			}
			// Stop if measurement got interrupted
			if(Thread.interrupted())
				throw new InterruptedException();
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
				throw new MeasurementException("Could not initialize all tasks of measurement.", e);
			}
			if(Thread.interrupted())
				throw new InterruptedException();
		}
		
		sendMessage("Finished initializing measurement.");
	}

	void uninitializeMeasurement(Microscope microscope) throws RemoteException, MeasurementException, InterruptedException
	{
		sendMessage("Starting uninitializing measurement.");
		
		measurementState = MeasurementState.UNINITIALIZING;
		for(int i = 0; i < measurementListeners.size(); i++)
		{
			MeasurementListener listener = measurementListeners.elementAt(i);
			try
			{
				listener.measurementUninitializing();
			}
			catch(RemoteException e)
			{
				ServerSystem.err.println("Measurement listener not answering. Removing him from the queue.", e);
				measurementListeners.removeElementAt(i);
				i--;
			}
		}

		// Uninitialize tasks and their jobs
		for(MeasurementTaskImpl task : tasks)
		{
			try
			{
				task.uninitializeTask(microscope, measurementContext);
			}
			catch(Exception e)
			{
				throw new MeasurementException("Could not uninitialize all tasks.", e);
			}
			if(Thread.interrupted())
				throw new InterruptedException();
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
				throw new MeasurementException("Could not apply all measurement shutdown device settings.", e);
			}
			// Stop if measurement got interrupted
			if(Thread.interrupted())
				throw new InterruptedException();
		}

		// Notify all listeners that measurement stopped.
		for(int i = 0; i < measurementListeners.size(); i++)
		{
			MeasurementListener listener = measurementListeners.elementAt(i);
			try
			{
				listener.measurementFinished();
			}
			catch(RemoteException e)
			{
				ServerSystem.err.println("Measurement listener not answering. Removing him from the queue.", e);
				measurementListeners.removeElementAt(i);
				i--;
			}
		}

		sendMessage("Finished uninitializing measurement.");
	}

	public boolean isMeasurementQuickStop()
    {
        return shouldMeasurementQuickStop;
    }
	
	/**
	 * This function is called by the measurement manager to run this measurement.
	 * 
	 * @param controlListener
	 * @return The thread in which the measurement is running.
	 * @throws MeasurementRunningException
	 * @throws RemoteException
	 */
	Thread runMeasurement(MeasurementControlListener controlListener) throws MeasurementRunningException
	{
		synchronized(this)
		{
			if(isRunning())
				throw new MeasurementRunningException();
			shouldMeasurementStop = false;
			shouldMeasurementQuickStop = false;
			setRunning(true);
		}
		sendMessage("Starting measurement execution.");

		class MeasurementRunner implements Runnable
		{
			private final MeasurementControlListener	controlListener;

			MeasurementRunner(MeasurementControlListener controlListener)
			{
				this.controlListener = controlListener;
			}

			@SuppressWarnings("unused")
			@Override
			public void run()
			{
				try
				{
					runMeasurementInternal(controlListener);
				}
				catch(MeasurementRunningException e)
				{
					// Should not happen.
					ServerSystem.err.println("Measurement was already running and, thus, could not be run.", e);
				}
				catch(InterruptedException e)
				{
					// This exception is thrown when measurement is stopped by
					// the measurement
					// manager.
					// Thus, this is "normal" behavior and we don't have to do
					// anything.
				}
			}

		}

		Thread thread = new Thread(new MeasurementRunner(controlListener), "Measurement");
		thread.start();
		return thread;
	}

	private void runMeasurementInternal(MeasurementControlListener controlListener) throws MeasurementRunningException, InterruptedException
	{
		// Startup the measurement
		try
		{
			measurementState = MeasurementState.RUNNING;
			for(int i = 0; i < measurementListeners.size(); i++)
			{
				MeasurementListener listener = measurementListeners.elementAt(i);
				try
				{
					listener.measurementStarted();
				}
				catch(RemoteException e)
				{
					ServerSystem.err.println("Measurement listener not answering. Removing him from the queue.", e);
					measurementListeners.removeElementAt(i);
					i--;
				}
			}

			// Stop if measurement got interrupted
			if(Thread.interrupted())
				throw new InterruptedException();

			// Process all tasks
			runTasks(controlListener);

			// Stop if measurement got interrupted
			if(Thread.interrupted())
				throw new InterruptedException();
		}
		finally
		{
			// Set state of the measurement to "not running".
			setRunning(false);
			// Inform the measurement manager, that this thread stopped, such
			// that it can start a
			// new measurement.
			controlListener.measurementFinished();
		}
		sendMessage("Finished measurement execution.");
	}

	private void runTasks(MeasurementControlListener controlListener) throws MeasurementRunningException, InterruptedException
	{
		// Wait until measurement should be finished
		TaskListener taskFinishListener = new TaskListener()
		{
			@Override
			public void taskStarted() throws RemoteException
			{
				// do nothing.
			}

			@Override
			public void taskFinished() throws RemoteException
			{
				checkIfFinished();
			}

			@Override
			public void jobsSubmitted(int submissionNumber) throws RemoteException
			{
				// do nothing.
			}
		};
		try
		{
			// Activate tasks
			for(MeasurementTaskImpl task : tasks)
			{
				task.addTaskListener(taskFinishListener);
				task.startTask(controlListener, startTime);
			}
			synchronized(this)
			{
				if(measurementRuntime > 0)
				{
					// Measurement should be stopped after a certain time. Set a
					// timer for this
					// task.
					(new Timer("Measurement Stop Timer")).schedule(new TimerTask()
					{
						@Override
						public void run()
						{
							stopMeasurement();
						}
					}, measurementRuntime);
				}
				while(!shouldMeasurementStop)
				{
					wait();
				}
			}
		}
		finally
		{
			// Stop tasks
			for(MeasurementTaskImpl task : tasks)
			{
				task.cancelTask();
				task.removeTaskListener(taskFinishListener);
			}
		}
	}

	private synchronized void checkIfFinished()
	{
		if(!isRunning())
			return;
		for(MeasurementTaskImpl task : tasks)
		{
			if(task.isRunning())
				return;
		}
		// All tasks are finished.
		shouldMeasurementStop = true;
		notifyAll();
	}

	synchronized void stopMeasurement()
	{
		shouldMeasurementStop = true;
		notifyAll();
	}

	synchronized void waitForMeasurementFinish()
	{
		if(!isRunning())
			return;
		while(isRunning())
		{
			try
			{
				wait();
			}
			catch(InterruptedException e)
			{
				ServerSystem.err.println("Waiting for measurement stop got interrupted.", e);
			}
		}
	}

	void errorOccured(MicroscopeException e)
	{
		measurementState = MeasurementState.ERROR;
		for(int i = 0; i < measurementListeners.size(); i++)
		{
			MeasurementListener listener = measurementListeners.elementAt(i);
			try
			{
				listener.errorOccured(e);
			}
			catch(RemoteException e1)
			{
				ServerSystem.err.println("Measurement listener not answering. Removing him from the queue.", e1);
				measurementListeners.removeElementAt(i);
				i--;
			}
		}
	}

	void measurementQueued()
	{
		measurementState = MeasurementState.QUEUED;
		for(int i = 0; i < measurementListeners.size(); i++)
		{
			MeasurementListener listener = measurementListeners.elementAt(i);
			try
			{
				listener.measurementQueued();
			}
			catch(RemoteException e)
			{
				ServerSystem.err.println("Measurement listener not answering. Removing him from the queue.", e);
				measurementListeners.removeElementAt(i);
				i--;
			}
		}
	}

	void measurementFailed(Exception exception)
	{
		measurementState = MeasurementState.ERROR;
		for(int i = 0; i < measurementListeners.size(); i++)
		{
			MeasurementListener listener = measurementListeners.elementAt(i);
			try
			{
				listener.errorOccured(exception);
			}
			catch(RemoteException e)
			{
				ServerSystem.err.println("Measurement listener not answering. Removing him from the queue.", e);
				measurementListeners.removeElementAt(i);
				i--;
			}
		}
	}

	void measurementUnqueued()
	{
		measurementState = MeasurementState.UNQUEUED;
		for(int i = 0; i < measurementListeners.size(); i++)
		{
			MeasurementListener listener = measurementListeners.elementAt(i);
			try
			{
				listener.measurementUnqueued();
			}
			catch(RemoteException e)
			{
				ServerSystem.err.println("Measurement listener not answering. Removing him from the queue.", e);
				measurementListeners.removeElementAt(i);
				i--;
			}
		}
	}

	synchronized void setStartupDeviceSettings(DeviceSettingDTO[] settings) throws MeasurementRunningException
	{
		assertRunning();
		startUpDeviceSettings = settings;
	}

	synchronized void addStartupDeviceSetting(DeviceSettingDTO setting) throws MeasurementRunningException
	{
		assertRunning();
		DeviceSettingDTO[] newSettings = new DeviceSettingDTO[startUpDeviceSettings.length + 1];
		System.arraycopy(startUpDeviceSettings, 0, newSettings, 0, startUpDeviceSettings.length);
		newSettings[startUpDeviceSettings.length] = new DeviceSettingDTO(setting);
		startUpDeviceSettings = newSettings;
	}

	synchronized void setFinishDeviceSettings(DeviceSettingDTO[] settings) throws MeasurementRunningException
	{
		assertRunning();
		shutDownDeviceSettings = settings;
	}

	synchronized void addFinishDeviceSetting(DeviceSettingDTO setting) throws MeasurementRunningException
	{
		assertRunning();
		DeviceSettingDTO[] newSettings = new DeviceSettingDTO[shutDownDeviceSettings.length + 1];
		System.arraycopy(shutDownDeviceSettings, 0, newSettings, 0, shutDownDeviceSettings.length);
		newSettings[shutDownDeviceSettings.length] = new DeviceSettingDTO(setting);
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

	public synchronized void removeMeasurementListener(MeasurementListener listener)
	{
		measurementListeners.remove(listener);
	}

	public synchronized void addMeasurementListener(MeasurementListener listener)
	{
		measurementListeners.add(listener);
	}

	public synchronized void setRuntime(int measurementRuntime) throws MeasurementRunningException
	{
		assertRunning();
		this.measurementRuntime = measurementRuntime;
	}

	public int getRuntime()
	{
		return measurementRuntime;
	}

	public synchronized void setLockMicroscopeWhileRunning(boolean lock) throws MeasurementRunningException
	{
		assertRunning();
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
		assertRunning();
		return addTask(period, fixedTimes, startTime, -1);
	}

	synchronized MeasurementTask addTask(int period, boolean fixedTimes, int startTime, int numExecutions) throws RemoteException, MeasurementRunningException
	{
		assertRunning();
		MeasurementTaskImpl measurementTask = new MeasurementTaskImpl(period, fixedTimes, startTime, numExecutions);
		addTask(measurementTask);
		return measurementTask;
	}

	synchronized MeasurementTask addMultiplePeriodTask(int[] periods, int breakTime, int startTime) throws RemoteException, MeasurementRunningException
	{
		assertRunning();
		return addMultiplePeriodTask(periods, breakTime, startTime, -1);
	}

	synchronized MeasurementTask addMultiplePeriodTask(int[] periods, int breakTime, int startTime, int numExecutions) throws RemoteException, MeasurementRunningException
	{
		assertRunning();
		MeasurementTaskImpl measurementTask = new MeasurementTaskImpl(periods, breakTime, startTime, numExecutions);
		addTask(measurementTask);
		return measurementTask;
	}

	public Date getStartTime()
	{
		if(startTime != null)
			return (Date)startTime.clone();
		return null;
	}

	public Date getEndTime()
	{
		if(endTime != null)
			return (Date)endTime.clone();
		return null;
	}
	public void setInitialMeasurementContextProperty(String identifier, Serializable property)
    {
        synchronized (initialMeasurementContextProperties)
        {
            initialMeasurementContextProperties.put(identifier, property);
        }
    }
}
