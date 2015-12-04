package org.youscope.plugin.livemodifiablejob;

import java.rmi.RemoteException;

import org.youscope.common.Well;
import org.youscope.common.measurement.PositionInformation;

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
        String jobDescription = null;
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
        if(well != null)
            jobDescription = "Well " + well.getWellName();
        for(int i=0; i<position.getNumPositions();i++)
        {
            String type = position.getPositionType(i);
            int pos = position.getPosition(i);
            if(jobDescription == null)
                jobDescription = "";
            else
                jobDescription +=", ";
            jobDescription += type +" "+Integer.toString(pos);
        }
        if(jobDescription != null)
            return jobDescription;
		return "empty position information";
    }
}
