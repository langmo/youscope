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
package org.youscope.plugin.livemodifiablejob;

import java.rmi.RemoteException;
import java.util.ArrayList;

import org.youscope.common.ComponentRunningException;
import org.youscope.common.ExecutionInformation;
import org.youscope.common.MeasurementContext;
import org.youscope.common.PositionInformation;
import org.youscope.common.callback.CallbackException;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobAdapter;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.JobException;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.util.ConfigurationTools;
import org.youscope.serverinterfaces.ConstructionContext;

/**
 * @author Moritz Lang
 */
class LiveModifiableJobImpl extends JobAdapter implements LiveModifiableJob
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = -1153160533381916941L;
    
    private volatile boolean enabled = true;
    
    private final ArrayList<JobConfiguration> childJobConfigurations = new ArrayList<JobConfiguration>();
    private volatile boolean childJobsModified = false;
    private ConstructionContext jobInitializer = null;
    private LiveModifiableJobCallback callback = null;
    private volatile boolean initialized = false;
    
    private boolean initializationEnabled = true;
    private final ArrayList<Job>	initializationJobs				= new ArrayList<Job>();
    private final ArrayList<JobConfiguration> initializationJobConfigurations = new ArrayList<JobConfiguration>();
    
    private final ArrayList<Job>	jobs				= new ArrayList<Job>();
    
    public LiveModifiableJobImpl(PositionInformation positionInformation) throws RemoteException
    {
        super(positionInformation);
    }
    void setData(ConstructionContext jobInitializer, LiveModifiableJobCallback callback)
    {
        this.jobInitializer = jobInitializer;
        this.callback = callback;
    }

    @Override
    public void runJob(ExecutionInformation executionInformation, Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
    {
        if(childJobsModified)
        {
            synchronized(childJobConfigurations)
            {
                synchronized(jobs)
                {
                    for(Job job : jobs)
                    {
                        job.uninitializeJob(microscope, measurementContext);
                    }
                    jobs.clear();
                    for (JobConfiguration childJobConfig : childJobConfigurations)
                    {
                        Job childJob;
                        try
                        {
                            childJob = jobInitializer.getComponentProvider().createJob(getPositionInformation(), childJobConfig);
                            childJob.initializeJob(microscope, measurementContext);
                        } catch (Exception e)
                        {
                            throw new JobException("Could not update child jobs.", e);
                        }
                        jobs.add(childJob);
                    }
                }
                measurementContext.notifyMeasurementStructureChanged();
                childJobsModified = false;
            }
        }
        if(enabled)
        {
            synchronized (jobs)
            {
                for (Job job : jobs)
                {
                    job.executeJob(executionInformation, microscope, measurementContext);
                    if (Thread.interrupted())
                        throw new InterruptedException();
                }
            }
        }
    }

    @Override
    public synchronized void initializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
    {
        if(initialized)
            throw new JobException("Job already initialized.");
        super.initializeJob(microscope, measurementContext);
        childJobsModified = false;
        // initialize child jobs
        synchronized (jobs)
        {
            for (Job job : jobs)
            {
                job.initializeJob(microscope, measurementContext);
            }
        }

        try
        {
            callback.initializeCallback();
            callback.registerJob(this);
        } catch (CallbackException e)
        {
            throw new JobException("Could not initialize live modifiable job callback.", e);
        }
        
        // store settings which can be modified to reset them when measurement finishes
        initializationEnabled = enabled;
        initializationJobs.clear();
        initializationJobs.addAll(jobs);
        initializationJobConfigurations.clear();
        initializationJobConfigurations.addAll(childJobConfigurations);
        
        initialized = true;
    }

    @Override
    public synchronized void uninitializeJob(Microscope microscope, MeasurementContext measurementContext) throws JobException, InterruptedException, RemoteException
    {
        if(!initialized)
            throw new JobException("Job already uninitialized.");
        
        try
        {
            callback.unregisterJob(this);
            callback.uninitializeCallback();
        } catch (CallbackException e)
        {
            throw new JobException("Could not uninitialize live modifiable job callback.", e);
        }
        super.uninitializeJob(microscope, measurementContext);

        synchronized (jobs)
        {
            for (Job job : jobs)
            {
                job.uninitializeJob(microscope, measurementContext);
            }
        }
        
        // reset settings which can be manipulated to the settings which were set initially.
        enabled = initializationEnabled;
        jobs.clear();
        jobs.addAll(initializationJobs);
        childJobConfigurations.clear();
        childJobConfigurations.addAll(initializationJobConfigurations);
        
        initialized = false;
    }

    @Override
    protected String getDefaultName()
    {
        return "Modifiable Job";
    }

    @Override
    public void setEnabled(boolean enabled) throws RemoteException
    {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() throws RemoteException
    {
        return enabled;
    }

    @Override
    public JobConfiguration[] getChildJobConfigurations() throws RemoteException, ConfigurationException
    {
        synchronized(childJobConfigurations)
        {
            JobConfiguration[] copiedChildJobs = new JobConfiguration[childJobConfigurations.size()];
            for(int i=0; i<childJobConfigurations.size(); i++)
            {
                copiedChildJobs[i] = ConfigurationTools.deepCopy(childJobConfigurations.get(i), JobConfiguration.class);
            }
            return copiedChildJobs;
             
        }
    }

    @Override
    public void setChildJobConfigurations(JobConfiguration[] newConfigurations) throws RemoteException, ConfigurationException
    {
        synchronized(childJobConfigurations)
        {
            childJobConfigurations.clear();
            for(JobConfiguration job:newConfigurations)
            {
                childJobConfigurations.add(ConfigurationTools.deepCopy(job, JobConfiguration.class));
            }
            childJobsModified = true;
        }
    }
    
    @Override
	public synchronized void addJob(Job job) throws ComponentRunningException
	{
		assertRunning();
		synchronized(jobs)
		{
			jobs.add(job);
		}
	}

	@Override
	public synchronized void removeJob(int jobIndex) throws ComponentRunningException, IndexOutOfBoundsException
	{
		assertRunning();
		synchronized(jobs)
		{
			jobs.remove(jobIndex);
		}
	}

	@Override
	public synchronized void clearJobs() throws ComponentRunningException
	{
		assertRunning();
		synchronized(jobs)
		{
			jobs.clear();
		}
	}

	@Override
	public Job[] getJobs()
	{
		synchronized(jobs)
		{
			return jobs.toArray(new Job[jobs.size()]);
		}
	}
	@Override
	public int getNumJobs()
	{
		return jobs.size();
	}
	@Override
	public Job getJob(int jobIndex) throws IndexOutOfBoundsException {
		return jobs.get(jobIndex);
	}
	@Override
	public void insertJob(Job job, int jobIndex)
			throws RemoteException, ComponentRunningException, IndexOutOfBoundsException {
		assertRunning();
		jobs.add(jobIndex, job);
	}
}
