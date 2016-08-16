/**
 * 
 */
package org.youscope.plugin.microplate.measurement;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.youscope.addon.microplate.MicroplateConfiguration;
import org.youscope.addon.pathoptimizer.PathOptimizerConfiguration;
import org.youscope.common.PositionInformation;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.JobContainerConfiguration;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.Well;
import org.youscope.common.task.PeriodConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

/**
 * This class represents the configuration of a microplate measurement.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("microplate-measurement")
public class MicroplateMeasurementConfiguration extends MeasurementConfiguration implements Cloneable, Serializable, JobContainerConfiguration
{
	/**
	 * Serial version UID.
	 */
	private static final long						serialVersionUID	= 3413810800791783902L;

	/**
	 * A list of all the jobs which should be done during the measurement.
	 */
	private Vector<JobConfiguration>	jobs				= new Vector<JobConfiguration>();

	@XStreamAlias("statistics-file")
	private String statisticsFileName = "statistics";
	
	@XStreamAlias("allow-edits-while-running")
	private boolean allowEditsWhileRunning = false;

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
	
	@XStreamAlias("zero-position-type")
	private ZeroPositionType zeroPositionType = ZeroPositionType.FIRST_WELL_TILE;
	
	@XStreamAlias("zero-position")
	private XYAndFocusPosition zeroPosition = null;
	/**
	 * Type of position to return to after each iteration.
	 * @author Moritz
	 *
	 */
	public enum ZeroPositionType
	{
		/**
		 * Do not automatically return to any position
		 */
		NONE("Do not set stage/focus position"),
		/**
		 * Go to first well or tile.
		 */
		FIRST_WELL_TILE("Set stage/focus position to first well/tile"),
		/**
		 * Go to custom position.
		 */
		CUSTOM("Set stage/focus position to custom position");
		
		private final String description;
		ZeroPositionType(String description)
		{
			this.description = description;
		}
		@Override
		public String toString()
		{
			return description;
		}
	}
	
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
	 * Returns true if measurement configuration can be edited while it is running.
	 * @return True if measurement can be edited.
	 */
	public boolean isAllowEditsWhileRunning() {
		return allowEditsWhileRunning;
	}

	/**
	 * Set to true to allow the measurement to be edited while running.
	 * @param allowEditsWhileRunning True if measurement should be changeable while running.
	 */
	public void setAllowEditsWhileRunning(boolean allowEditsWhileRunning) {
		this.allowEditsWhileRunning = allowEditsWhileRunning;
	}

	/**
	 * The identifier for this measurement type.
	 */
	public static final String								TYPE_IDENTIFIER		= "YouScope.MicroPlateMeasurement";

	/**
	 * Time maximal needed per well in microseconds. Set to "-1" for
	 * "as fast as possible".
	 */
	@XStreamAlias("time-per-well")
	private int												timePerWell			= -1;

	/**
	 * Period in which every well should be visited in microseconds. NULL is
	 * interpreted as as fast as possible.
	 */
	@XStreamAlias("period")
	private PeriodConfiguration										period				= null;

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
	public Object clone() throws CloneNotSupportedException
	{
		MicroplateMeasurementConfiguration clone = (MicroplateMeasurementConfiguration)super.clone();
		clone.jobs = new Vector<JobConfiguration>();
		for(int i = 0; i < jobs.size(); i++)
		{
			clone.jobs.add((JobConfiguration)jobs.elementAt(i).clone());
		}
		if(period != null)
		{
			clone.period = (PeriodConfiguration)period.clone();
		}
		if(focusConfiguration != null)
			clone.focusConfiguration = focusConfiguration.clone();
		
		clone.configuredPositions = new HashMap<>(configuredPositions);
		clone.selectedTiles = new HashSet<>(selectedTiles);
		clone.selectedWells = new HashSet<>(selectedWells);
		if(zeroPosition != null)
			clone.zeroPosition = new XYAndFocusPosition(zeroPosition.getX(), zeroPosition.getY(), zeroPosition.getFocus());
		return clone;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		super.checkConfiguration();
		if(configuredPositions.isEmpty())
		{
			throw new ConfigurationException("Position/well configuration not yet run.\nThe position fine configuration has to be run in order for the measurement to obtain valid stage positions.");
		}
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
	 * @param timePerWell
	 *            the timePerWell to set
	 */
	public void setTimePerWell(int timePerWell)
	{
		this.timePerWell = timePerWell;
	}

	/**
	 * @return the timePerWell
	 */
	public int getTimePerWell()
	{
		return timePerWell;
	}

	/**
	 * @param period
	 *            the period to set
	 */
	public void setPeriod(PeriodConfiguration period)
	{
		try
		{
			this.period = (PeriodConfiguration)period.clone();
		}
		catch(CloneNotSupportedException e)
		{
			throw new IllegalArgumentException("Period can not be cloned.", e);
		}
	}

	/**
	 * @return the period
	 */
	public PeriodConfiguration getPeriod()
	{
		return period;
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
	 * Sets the name (without extension) of the file in which statistics of the measurement should be saved to.
	 * Set to null to not generate statistics.
	 * @param statisticsFileName name for the file (without extension) in which statistics should be saved, or null.
	 */
	public void setStatisticsFileName(String statisticsFileName)
	{
		this.statisticsFileName = statisticsFileName;
	}

	/**
	 * Returns the name (without extension) of the file in which statistics of the measurement should be saved to.
	 * Returns null if no statistics are generated.
	 * @return name for the file (without extension) in which statistics should be saved, or null.
	 */
	public String getStatisticsFileName()
	{
		return statisticsFileName;
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

	/**
	 * Returns the type of position where the stage/focus goes before the first iteration, and after each iteration.
	 * @return zero position type.
	 */
	public ZeroPositionType getZeroPositionType() {
		return zeroPositionType;
	}
	/**
	 * Sets the type of position where the stage/focus goes before the first iteration, and after each iteration.
	 * @param zeroPositionType zero position type.
	 */
	public void setZeroPositionType(ZeroPositionType zeroPositionType) {
		this.zeroPositionType = zeroPositionType;
	}

	/**
	 * Returns the zero position where the stage/focus goes before the first iteration, and after each iteration.
	 * Only has an effect if {@link #getZeroPosition()} returns {@link ZeroPositionType#CUSTOM}.
	 * @return Zero position of stage/focus.
	 */
	public XYAndFocusPosition getZeroPosition() {
		return zeroPosition;
	}
	/**
	 * Sets the zero position where the stage/focus goes before the first iteration, and after each iteration.
	 * Only has an effect if {@link #getZeroPosition()} returns {@link ZeroPositionType#CUSTOM}.
	 * @param zeroPosition Zero position of stage/focus.
	 */
	public void setZeroPosition(XYAndFocusPosition zeroPosition) {
		this.zeroPosition = zeroPosition;
	}
}
