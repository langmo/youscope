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

import java.util.ArrayList;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.CompositeJobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * A job container which can be activated or deactivated during measurement runtime.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("live-modifiable-job")
public class LiveModifiableJobConfiguration implements CompositeJobConfiguration
{
    /**
     * The jobs which should be run when the composite job starts.
     */
    @XStreamAlias("jobs")
    private ArrayList<JobConfiguration> jobs = new ArrayList<JobConfiguration>();

    @Override
    public int hashCode()
    {
        final int prime = 7;
        int result = 1;
        result = prime * result + new Boolean(enabledAtStartup).hashCode();
        return result;
    }

    /**
     * returns true if job is enabled at startup.
     * @return true if job is by default enabled.
     */
    public boolean isEnabledAtStartup()
    {
        return enabledAtStartup;
    }

    /**
     * set if job is enabled at startup.
     * @param enabledAtStartup true if by default enabled.
     */
    public void setEnabledAtStartup(boolean enabledAtStartup)
    {
        this.enabledAtStartup = enabledAtStartup;
    }

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 7144732041222941146L;

    @XStreamAlias("enabled-at-startup")
    @XStreamAsAttribute
    private boolean enabledAtStartup = true;

    @Override
    public String getDescription()
    {
        if (jobs == null || jobs.size() == 0)
            return "<p>Empty Live Modifiable Job</p>";
        String description = "<p>Live modifiable job</p>" +
                "<p>begin</p>" +
                "<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
        for (JobConfiguration job : jobs)
        {
            description += "<li>" + job.getDescription() + "</li>";
        }
        description += "</ul><p>end</p>";
        return description;
    }

    /**
     * The identifier for this job type.
     */
    public static final String TYPE_IDENTIFIER = "YouScope.LiveModifiableJob";

    @Override
    public String getTypeIdentifier()
    {
        return TYPE_IDENTIFIER;
    }

    @Override
    public JobConfiguration[] getJobs()
    {
        return jobs.toArray(new JobConfiguration[jobs.size()]);
    }

    @Override
    public void setJobs(JobConfiguration[] jobs)
    {
        this.jobs.clear();
        for (JobConfiguration job : jobs)
        {
            this.jobs.add(job);
        }
    }

    @Override
    public void addJob(JobConfiguration job)
    {
        jobs.add(job);
    }

    @Override
    public void clearJobs()
    {
        jobs.clear();
    }

    @Override
    public void removeJobAt(int index)
    {
        jobs.remove(index);
    }

    @Override
    public void addJob(JobConfiguration job, int index)
    {
        jobs.add(index, job);
    }

	@Override
	public void checkConfiguration() throws ConfigurationException {
		for(JobConfiguration childJob : jobs)
		{
			childJob.checkConfiguration();
		}
		
	}
}
