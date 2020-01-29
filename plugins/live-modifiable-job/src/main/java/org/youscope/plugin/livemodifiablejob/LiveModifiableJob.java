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

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.CompositeJob;
import org.youscope.common.job.Job;
import org.youscope.common.job.JobConfiguration;

/**
 * Interface of a measurement job which collects statistical data about the evaluation and appends it to a file.
 * 
 * @author Moritz Lang
 */
public interface LiveModifiableJob extends Job, CompositeJob
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
     * @throws ConfigurationException 
     */
    public JobConfiguration[] getChildJobConfigurations() throws RemoteException, ConfigurationException;
    
    /**
     * Sets the configurations of the child jobs. Any previously existing child jobs will be removed and replaced by the jobs defined in the configuration.
     * @param newConfigurations array of new child jobs
     * @throws RemoteException
     * @throws ConfigurationException 
     */
    public void setChildJobConfigurations(JobConfiguration[] newConfigurations) throws RemoteException, ConfigurationException;
}
