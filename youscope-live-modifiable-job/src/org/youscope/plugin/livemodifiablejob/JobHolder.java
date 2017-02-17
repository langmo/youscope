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
package org.youscope.plugin.livemodifiablejob;

import java.rmi.RemoteException;

import org.youscope.common.PositionInformation;
import org.youscope.common.Well;

class JobHolder
{
    private volatile LiveModifiableJob job;
    JobHolder(final LiveModifiableJob job)
    {
        this.job = job;
    }
    public synchronized boolean isJobEqual(final LiveModifiableJob job)
    {
        if(this.job == null)
            return false;
        return this.job == job;
    }
    public synchronized void jobUninitilized()
    {
        job = null;
    }
    public synchronized LiveModifiableJob getJob()
    {
        return job;
    }
    
    @Override
    public String toString()
    {
        PositionInformation position;
        try
        {
            synchronized(this)
            {
                if(job == null)
                    return "uninitialized job";
                position = job.getPositionInformation();
            }
        } catch (@SuppressWarnings("unused") RemoteException e)
        {
            return "unknown job";
        }
        Well well = position.getWell();
        String jobDescription = "Jobs ";
        if(well != null)
            jobDescription += "well " + well.getWellName();
        for(int i=0; i<position.getNumPositions();i++)
        {
            String type = position.getPositionType(i);
            int pos = position.getPosition(i);
            if(i>0 || well != null)
                jobDescription +=", ";
            jobDescription += type +" "+Integer.toString(pos);
        }
        return jobDescription;
    }
}
