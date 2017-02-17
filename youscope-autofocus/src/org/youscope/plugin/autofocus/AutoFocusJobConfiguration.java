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
package org.youscope.plugin.autofocus;

import java.util.Vector;

import org.youscope.addon.focusscore.FocusScoreConfiguration;
import org.youscope.addon.focussearch.FocusSearchConfiguration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.image.ImageProducerConfiguration;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.CompositeJobConfiguration;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableProducerConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * Configuration for a job which automatically searches for the focal plane.
 * @author Moritz Lang
 *
 */
@XStreamAlias("auto-focus-job") 
public class AutoFocusJobConfiguration implements ImageProducerConfiguration, CompositeJobConfiguration, TableProducerConfiguration
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 7911732041188942146L;
	
	/**
	 * The identifier for this job type.
	 */
	public static final String	TYPE_IDENTIFIER	= "YouScope.AutoFocusJob";

	/**
	 * The jobs which should be run when the autofocus job has adjusted the focus.
	 */
	@XStreamAlias("jobs")
	private Vector<JobConfiguration>	jobs				= new Vector<JobConfiguration>();
	
	/**
	 * Configuration of the focus device used for focusing.
	 */
	@XStreamAlias("focus-configuration")
	private FocusConfiguration	focusConfiguration	= null;

	@XStreamAlias("remember-focus")
	private boolean rememberFocus = true;
	
	/**
	 * Default name for focus table.
	 */
	public static final String FOCUS_TABLE_DEFAULT_NAME = "autofocus";
	/**
	 * The name under which the focus table is saved.
	 */
	@XStreamAlias("focus-table-save-name")
	private String focusTableSaveName = FOCUS_TABLE_DEFAULT_NAME;
	
	@XStreamAlias("reset-focus-after-search")
	private boolean resetFocusAfterSearch = false;
	/**
	 * The channel where the images should be made. Set to an empty string if
	 * the current channel should be taken.
	 */
	@XStreamAlias("channel")
	@XStreamAsAttribute
	private String				channel				= "";

	/**
	 * The config group where the channel is defined.
	 */
	@XStreamAlias("channel-group")
	@XStreamAsAttribute
	private String				channelGroup			= "";
	/**
	 * The exposure time for the imaging.
	 */
	@XStreamAlias("exposure-ms")
	@XStreamAsAttribute
	private double				exposure			= 20.0;

	/**
	 * Default name for focus images.
	 */
	public final static String IMAGE_SAVE_NAME_DEFAULT = "autofocus";
	/**
	 * The name under which the image is saved.
	 */
	@XStreamAlias("image-save-name")
	@XStreamAsAttribute
	private String				imageSaveName		= null;
	
	@XStreamAlias("focus-score-configuration")
	private FocusScoreConfiguration focusScoreAlgorithm = null;
	
	@XStreamAlias("focus-search-configuration")
	private FocusSearchConfiguration focusSearchAlgorithm = null;
	
	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}
	
	/**
	 * Returns the focus search algorithm.
	 * @return focus search algorithm.
	 */
	public FocusSearchConfiguration getFocusSearchAlgorithm() {
		return focusSearchAlgorithm;
	}

	/**
	 * sets the focus search algorithm.
	 * @param focusSearchAlgorithm focus search algorithm.
	 */
	public void setFocusSearchAlgorithm(FocusSearchConfiguration focusSearchAlgorithm) {
		this.focusSearchAlgorithm = focusSearchAlgorithm;
	}

	/**
	 * @param focusConfiguration
	 *            The configuration of the focus .
	 */
	public void setFocusConfiguration(FocusConfiguration focusConfiguration)
	{
		this.focusConfiguration = focusConfiguration;
	}

	/**
	 * @return The configuration of the focus.
	 */
	public FocusConfiguration getFocusConfiguration()
	{
		return focusConfiguration;
	}
	
	@Override
	public String getDescription()
	{
		String description = "<p>Auto-Focus search in channel " + channelGroup + "." + channel
			+ ", exposure " + Double.toString(exposure) + "ms</p>";
		if(jobs != null && jobs.size() > 0)
		{
			description += 
				"<p>begin</p>" +
				"<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
			for(JobConfiguration job : jobs)
			{
				description += "<li>" +job.getDescription() + "</li>";
			}
			description += "</ul><p>end</p>";
		}
		return description;
	}
	
	/**
	 * Sets the channel which should be imaged.
	 * @param channelGroup The group of the channel.
	 * @param channel The channel.
	 */
	public void setChannel(String channelGroup, String channel)
	{
		this.channelGroup = channelGroup;
		this.channel = channel;
	}

	/**
	 * @return the channel in which should be imaged.
	 */
	public String getChannel()
	{
		return channel;
	}

	/**
	 * @return the group of the channel
	 */
	public String getChannelGroup()
	{
		return channelGroup;
	}

	/**
	 * @param exposure
	 *            the exposure to set
	 */
	public void setExposure(double exposure)
	{
		this.exposure = exposure;
	}

	/**
	 * @return the exposure
	 */
	public double getExposure()
	{
		return exposure;
	}

	/**
	 * Returns the name under which the images should be saved.
	 * @return Name of imaging job.
	 */
	public String getImageSaveName()
	{
		return imageSaveName;
	}
	
	/**
	 * Sets the name under which the images should be saved.
	 * @param name Name of imaging job.
	 */
	public void setImageSaveName(String name)
	{
		this.imageSaveName = name;
	}
	
	@Override
	public String[] getImageSaveNames()
	{
		if(imageSaveName!=null)
			return new String[]{imageSaveName};
		return null;
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
	 * Sets the focus algorithm with which it is detected if the focus of an image made at a give focus position is better than the other.
	 * @param focusScoreAlgorithm
	 */
	public void setFocusScoreAlgorithm(FocusScoreConfiguration focusScoreAlgorithm)
	{
		this.focusScoreAlgorithm = focusScoreAlgorithm;
	}

	/**
	 * Returns the focus algorithm with which it is detected if the focus of an image made at a give focus position is better than the other.
	 * @return focus score algorithm configuration, or null.
	 */
	public FocusScoreConfiguration getFocusScoreAlgorithm()
	{
		return focusScoreAlgorithm;
	}

	@Override
	public int getNumberOfImages()
	{
		return -1;
	}

	/**
	 * Defines if the focus should be reset to its original value after the job (and all sub-jobs) finished.
	 * @param resetFocusAfterSearch Set to true to return to original value.
	 */
	public void setResetFocusAfterSearch(boolean resetFocusAfterSearch)
	{
		this.resetFocusAfterSearch = resetFocusAfterSearch;
	}

	/**
	 * Defines if the focus should be reset to its original value after the job (and all sub-jobs) finished.
	 * @return true if returning to original value.
	 */
	public boolean isResetFocusAfterSearch()
	{
		return resetFocusAfterSearch;
	}

	/**
	 * Sets the name under which the focus-table should be saved. Set to null to not save the focus table.
	 * @param focusTableSaveName Name for the focus table (without extension), or null.
	 */
	public void setFocusTableSaveName(String focusTableSaveName)
	{
		this.focusTableSaveName = focusTableSaveName;
	}
	/**
	 * Returns the name under which the focus-table is saved, or null, if it is not saved.
	 * @return Name for the focus table (without extension), or null.
	 */
	public String getFocusTableSaveName()
	{
		return focusTableSaveName;
	}

	/**
	 * Set to true if next focus search should be centered around the last iterations maximal focal plane. If false, it always starts from focus before the job.
	 * @param rememberFocus True if next focus search should be centered around last maximum.
	 */
	public void setRememberFocus(boolean rememberFocus)
	{
		this.rememberFocus = rememberFocus;
	}

	/**
	 * Returns true if next focus search should be centered around the last iterations maximal focal plane. If false, it always starts from focus before the job.
	 * @return True if next focus search is centered around last maximum.
	 */
	public boolean isRememberFocus()
	{
		return rememberFocus;
	}

	@Override
	public TableDefinition getProducedTableDefinition()
	{
		return AutoFocusTable.getTableDefinition();
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(focusScoreAlgorithm == null)
			throw new ConfigurationException("Not focus score algorithm selected.");
		focusScoreAlgorithm.checkConfiguration();
		if(focusSearchAlgorithm == null)
			throw new ConfigurationException("Not focus search algorithm selected.");
		focusSearchAlgorithm.checkConfiguration();
		for(JobConfiguration childJob:jobs)
		{
			childJob.checkConfiguration();
		}
	}
}
