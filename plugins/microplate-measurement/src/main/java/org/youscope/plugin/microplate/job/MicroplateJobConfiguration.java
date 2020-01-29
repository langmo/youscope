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
package org.youscope.plugin.microplate.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.youscope.addon.microplate.MicroplateConfiguration;
import org.youscope.addon.pathoptimizer.PathOptimizerConfiguration;
import org.youscope.common.PositionInformation;
import org.youscope.common.Well;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.CompositeJobConfiguration;
import org.youscope.plugin.microplate.measurement.FineConfigurationConverter;
import org.youscope.plugin.microplate.measurement.TileConfiguration;
import org.youscope.plugin.microplate.measurement.XYAndFocusPosition;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

/**
 * This class represents the configuration of a user configurable microplate job.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("microplate-job")
public class MicroplateJobConfiguration implements CompositeJobConfiguration
{
	/**
	 * Serial version UID.
	 */
	private static final long						serialVersionUID	= 3413810800791783902L;

	/**
	 * A list of all the jobs which should be done during the measurement.
	 */
	private final ArrayList<JobConfiguration>	jobs				= new ArrayList<JobConfiguration>();

	@XStreamAlias("stage")
	private String stageDevice = null;
	
	@XStreamAlias("path-optimizer")
	private PathOptimizerConfiguration pathOptimizerConfiguration = null;
	
	@XStreamAlias("microplate-configuration")
	private MicroplateConfiguration microplateConfiguration = null;
	
	@XStreamAlias("tile-configuration")
	private TileConfiguration tileConfiguration = null;
	
	@XStreamAlias("configured-positions")
	@XStreamConverter(FineConfigurationConverter.class)
	private HashMap<PositionInformation, XYAndFocusPosition> configuredPositions = new HashMap<>(200);
	
	@XStreamAlias("selected-wells")
	private HashSet<Well> selectedWells = new HashSet<>(200);
	@XStreamAlias("selected-tiles")
	private HashSet<Well> selectedTiles = new HashSet<>(200);
	
	/**
	 * Sets the configured imaging positions to the positions in the provided map.
	 * @param positions Map of positions where imaging should take place.
	 */
	public void setPositions(Map<PositionInformation, XYAndFocusPosition> positions)
	{
		configuredPositions.clear();
		configuredPositions.putAll(positions);
	}
	
	/**
	 * Returns a map of all positions where the measurement images. 
	 * @return Map of all positions where imaging takes place.
	 */
	public Map<PositionInformation, XYAndFocusPosition> getPositions()
	{
		return new HashMap<>(configuredPositions);
	}
	
	/**
	 * Sets the wells which are selected in the microplate.
	 * @param selectedWells selected wells.
	 */
	public void setSelectedWells(Set<Well> selectedWells)
	{
		this.selectedWells.clear();
		this.selectedWells.addAll(selectedWells);
	}
	
	/**
	 * Returns a collection of all wells selected in the microplate.
	 * @return selected wells.
	 */
	public Set<Well> getSelectedWells()
	{
		return new HashSet<>(selectedWells);
	}
	
	/**
	 * Sets the wells which are selected in the microplate.
	 * @param selectedTiles selected wells.
	 */
	public void setSelectedTiles(Set<Well> selectedTiles)
	{
		this.selectedTiles.clear();
		this.selectedTiles.addAll(selectedTiles);
	}
	
	/**
	 * Returns a collection of all tiles selected in the microplate.
	 * @return selected tiles.
	 */
	public Set<Well> getSelectedTiles()
	{
		return new HashSet<>(selectedTiles);
	}
	
	/**
	 * Returns the configuration of the microplate layout.
	 * @return microplate configuration
	 */
	public MicroplateConfiguration getMicroplateConfiguration() {
		return microplateConfiguration;
	}

	/**
	 * Sets the configuration of the microplate layout.
	 * @param microplateConfiguration microplate configuration.
	 */
	public void setMicroplateConfiguration(MicroplateConfiguration microplateConfiguration) {
		this.microplateConfiguration = microplateConfiguration;
	}
	
	/**
	 * Returns the configuration of the tile layout. Internally, the tile layout is stored as another microplate layout.
	 * @return tile configuration
	 */
	public TileConfiguration getTileConfiguration() {
		return tileConfiguration;
	}

	/**
	 * Sets the configuration of the tile layout. Internally, the tile layout is stored as another microplate layout.
	 * @param tileConfiguration tile configuration.
	 */
	public void setTileConfiguration(TileConfiguration tileConfiguration) {
		this.tileConfiguration = tileConfiguration;
	}
	
	/**
	 * Returns the configuration of the optimizer which should be used to minimize the distances between two wells/positions measured.
	 * Returns null if not uses any optimized path.
	 * @return The configuration of the path optimizer used, or null
	 */
	public PathOptimizerConfiguration getPathOptimizerConfiguration() {
		return pathOptimizerConfiguration;
	}
	/**
	 * Sets the configuration of the optimizer which should be used to minimize the distances between two wells/positions measured.
	 * Set to null to not use any optimized path.
	 * @param pathOptimizerConfiguration The configuration of the path optimizer used, or null
	 */
	public void setPathOptimizerConfiguration(PathOptimizerConfiguration pathOptimizerConfiguration) {
		this.pathOptimizerConfiguration = pathOptimizerConfiguration;
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

	/**
	 * The identifier for this measurement type.
	 */
	public static final String								TYPE_IDENTIFIER		= "YouScope.MicroPlateJob";

	/**
	 * Configuration of the focus device used for focussing the wells. Set to
	 * null to not set focus in wells.
	 */
	@XStreamAlias("focus")
	private FocusConfiguration		 			focusConfiguration	= null;

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
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

	/**
	 * @param focusConfiguration
	 *            The configuration of the focus used when entering a well. Set
	 *            to NULL to not set focus.
	 */
	public void setFocusConfiguration(FocusConfiguration focusConfiguration)
	{
		this.focusConfiguration = focusConfiguration;
	}

	/**
	 * @return The configuration of the focus used when entering a well. NULL if
	 *         focus is not set.
	 */
	public FocusConfiguration getFocusConfiguration()
	{
		return focusConfiguration;
	}

	/**
	 * Sets the ID of the stage device which should be used to change between wells and positions. Set to NULL to use default stage device.
	 * @param stageDevice The ID of the stage device, or NULL.
	 */
	public void setStageDevice(String stageDevice)
	{
		this.stageDevice = stageDevice;
	}

	/**
	 * Returns the ID of the stage device which should be used to change between wells and positions. Returns NULL to use default stage device.
	 * @return The ID of the stage device, or NULL.
	 */
	public String getStageDevice()
	{
		return stageDevice;
	}

	@Override
	public String getDescription()
	{
		String description = "<p>Microplate Job</p>";
		if(selectedWells != null && selectedWells.size() > 0)
		{
			description += "<p>for well=1:" + Integer.toString(selectedWells.size()) + "</p>"+
			"<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
			for(JobConfiguration job : jobs)
			{
				description += "<li>" + job.getDescription() + "</li>";
			}
			description += "</ul><p>end</p>";
		}
		else
		{
			description += "<p>No well specified.</p>";
		}
		return description;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		for(JobConfiguration childJob : jobs)
		{
			childJob.checkConfiguration();
		}
	}
}
