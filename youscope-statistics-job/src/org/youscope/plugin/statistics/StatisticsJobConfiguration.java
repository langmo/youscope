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
package org.youscope.plugin.statistics;


import java.util.ArrayList;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.CompositeJobConfiguration;
import org.youscope.common.job.basicjobs.StatisticsJob;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableProducerConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This job/task displays a message to the user and waits for his or her acknowledgment.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("statistics-job")
public class StatisticsJobConfiguration implements CompositeJobConfiguration, TableProducerConfiguration
{
	/**
	 * The jobs which should be run when the composite job starts.
	 */
	@XStreamAlias("jobs")
	private final ArrayList<JobConfiguration>	jobs				= new ArrayList<JobConfiguration>();
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		StatisticsJobConfiguration other = (StatisticsJobConfiguration)obj;
		if(fileName == null)
		{
			if(other.fileName != null)
				return false;
		}
		else if(!fileName.equals(other.fileName))
			return false;
		return true;
	}

	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 7144732041177941146L;

	@XStreamAlias("file-name")
	@XStreamAsAttribute
	private String fileName = null;
	
	@Override
	public String getDescription()
	{
		if(jobs == null || jobs.size() == 0)
			return "<p>Empty Statistics Job (" + fileName +")</p>";
		String description = "<p>" + fileName + " &lt;&lt; recordStatistics</p>" +
			"<p>begin</p>" +
			"<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
		for(JobConfiguration job : jobs)
		{
			description += "<li>" +job.getDescription() + "</li>";
		}
		description += "</ul><p>end</p>";
		return description;
	}

	

	/**
	 * Returns the file name where statistical data is saved. Returns null if not saved.
	 * @return File name for statistical data.
	 */
	public String getFileName()
	{
		return fileName;
	}

	/**
	 * Sets the file name where statistical data should be saved. Set to null to not save.
	 * @param fileName File name for statistical data.
	 */
	public void setFileName(String fileName)
	{
		this.fileName = fileName;
	}

	@Override
	public String getTypeIdentifier()
	{
		return StatisticsJob.DEFAULT_TYPE_IDENTIFIER;
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
		for(JobConfiguration job:jobs)
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
	public TableDefinition getProducedTableDefinition() {
		return StatisticsTable.getTableDefinition();
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		for(JobConfiguration job : jobs)
			job.checkConfiguration();
	}
	
}
