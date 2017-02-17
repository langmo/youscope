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
package org.youscope.plugin.composedimaging;

import java.util.Vector;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.CompositeJobConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * @author langmo
 */
@XStreamAlias("plate-scanning-job")
public class PlateScanningJobConfiguration implements CompositeJobConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 4548542891764517275L;
	
	
	/**
	 * Number of tiles in the x-direction.
	 */
	@XStreamAlias("num-tiles-x")
	@XStreamAsAttribute
	private int					numTilesX					= 3;

	/**
	 * Number of tiles in the y-direction
	 */
	@XStreamAlias("num-tiles-y")
	@XStreamAsAttribute
	private int					numTilesY					= 3;
	
	@XStreamAlias("delta-x-um")
	@XStreamAsAttribute
	private double deltaX = 0;
	
	@XStreamAlias("delta-y-um")
	@XStreamAsAttribute
	private double deltaY = 0;
	
	@XStreamAlias("jobs")
	private Vector<JobConfiguration>	jobs				= new Vector<JobConfiguration>();

	@Override
	public String getDescription()
	{
		String description;
		description = "<p>for i = 0 : " + Integer.toString(numTilesX-1) + ", j = 0 : " + Integer.toString(numTilesY-1) + "</p>";
		description += "<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
		description += "<li>[x, y] += [i &times; " + Double.toString(deltaX) + ", j &times; " + Double.toString(deltaY) + "]</li>";
		for(JobConfiguration job : jobs)
		{
			description += "<li>" + job.getDescription() + "</li>";
		}
		description += "<li>[x, y] -= [i &times; " + Double.toString(deltaX) + ", j &times; " + Double.toString(deltaY) + "]</li>";
		description += "</ul><p>end</p>";
		return description;
	}
	
	/**
	 * @param nx number of tiles in x-direction
	 */
	public void setNumTilesX(int nx)
	{
		this.numTilesX = nx;
	}

	/**
	 * @return number of tiles in x-direction
	 */
	public int getNumTilesX()
	{
		return numTilesX;
	}

	/**
	 * @param ny number of tiles in y-direction
	 */
	public void setNumTilesY(int ny)
	{
		this.numTilesY = ny;
	}

	/**
	 * @return number of tiles in y-direction
	 */
	public int getNumTilesY()
	{
		return numTilesY;
	}
	
	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.PlateScanningJob";

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
		jobs.removeElementAt(index);
	}

	@Override
	public void addJob(JobConfiguration job, int index)
	{
		jobs.insertElementAt(job, index);
	}

	/**
	 * @param deltaX Distance between neighboring tiles in the x-direction in micro m.
	 */
	public void setDeltaX(double deltaX)
	{
		this.deltaX = deltaX;
	}

	/**
	 * @return Distance between neighboring tiles in the x-direction in micro m.
	 */
	public double getDeltaX()
	{
		return deltaX;
	}

	/**
	 * @param deltaY Distance between neighboring tiles in the y-direction in micro m.
	 */
	public void setDeltaY(double deltaY)
	{
		this.deltaY = deltaY;
	}

	/**
	 * @return Distance between neighboring tiles in the y-direction in micro m.
	 */
	public double getDeltaY()
	{
		return deltaY;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(numTilesX <= 0 || numTilesY <=0)
			throw new ConfigurationException("Number of tiles in X and Y direction must each be bigger than zero.");
		for(JobConfiguration job : jobs)
			job.checkConfiguration();
		
	}
}
