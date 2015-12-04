/**
 * 
 */
package org.youscope.plugin.livemodifiablejob;

import java.rmi.RemoteException;

import org.youscope.common.configuration.JobConfiguration;
import org.youscope.common.measurement.job.EditableJobContainer;
import org.youscope.common.measurement.job.Job;

/**
 * Interface of a measurement job which collects statistical data about the evaluation and appends it to a file.
 * 
 * @author Moritz Lang
 */
public interface LiveModifiableJob extends Job, EditableJobContainer
{
    /**
     * Set to true if the child jobs should be executed during the next execution of this job, and to false otherwise.
     * @param enabled TRUE to execute child jobs.
     * @throws RemoteException
     */
    public void setEnabled(boolean enabled) throws RemoteException;
    
    /**
     * Returns true if child jobs are executed during this jobs execution.
     * @return TRUE if child jobs are executed.
     * @throws RemoteException
     */
    public boolean isEnabled() throws RemoteException;
    
    /**
     * Returns the job configurations of the currently active child jobs.
     * @return array of child jobs.
     * @throws RemoteException
     * @throws CloneNotSupportedException 
     */
    public JobConfiguration[] getChildJobConfigurations() throws RemoteException, CloneNotSupportedException;
    
    /**
     * Sets the configurations of the child jobs. Any previously existing child jobs will be removed and replaced by the jobs defined in the configuration.
     * @param newConfigurations array of new child jobs
     * @throws RemoteException
     * @throws CloneNotSupportedException 
     */
    public void setChildJobConfigurations(JobConfiguration[] newConfigurations) throws RemoteException, CloneNotSupportedException;
}
