/**
 * 
 */
package org.youscope.plugin.microplatemeasurement;

import java.util.Hashtable;
import java.util.Map.Entry;

import org.youscope.common.Microplate;
import org.youscope.common.MicroplateType;
import org.youscope.common.Well;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;

/**
 * Configuration of the positions to measure in a microplate measurement.
 * @author langmo
 */
@XStreamAlias("microplate-positions")
public class MicroplatePositionConfigurationDTO extends Microplate
{
	/**
	 * Serial Version UID.
	 */
	private static final long				serialVersionUID	= 4794459938367016630L;
	
	@XStreamAlias("positions")
	@XStreamConverter(FineConfigurationConverter.class)
	private Hashtable<WellAndTileIdentifier, XYAndFocusPositionDTO> hashPositions = new Hashtable<WellAndTileIdentifier, XYAndFocusPositionDTO>(500);
	
	//@XStreamAlias("fine-configuration")
	//private XYAndFocusPositionDTO[][][][]	positions;
	
	@XStreamAlias("measured-wells")
	@XStreamConverter(MeasuredWellsAndPositionsConverter.class)
	private boolean[][]						measuredWells;
	@XStreamAlias("measured-tiles")
	@XStreamConverter(MeasuredWellsAndPositionsConverter.class)
	private boolean[][]						measuredPositions;

	/**
	 * Default constructor.
	 * Initializes a 96 well plate.
	 */
	public MicroplatePositionConfigurationDTO()
	{
		super();
	}

	/**
	 * Constructor for a predefined microplate type.
	 * @param microplateType The predefined microplate type.
	 */
	public MicroplatePositionConfigurationDTO(MicroplateType microplateType)
	{
		super(microplateType);
	}

	/**
	 * Constructor for a custom defined microplate.
	 * @param numWellsX number of wells in the horizontal direction.
	 * @param numWellsY number of wells in the vertical direction.
	 * @param wellWidth width of a well (well-well distance).
	 * @param wellHeight height of a well (well-well distance).
	 */
	public MicroplatePositionConfigurationDTO(int numWellsX, int numWellsY, double wellWidth, double wellHeight)
	{
		super(numWellsX, numWellsY, wellWidth, wellHeight);
	}

	/**
	 * Constructor for a microplate type, when no microplate is used, but the functionality of the microplate
	 * measurement methods should be used for custom positions.
	 * @param numPositions Number of positions.
	 */
	public MicroplatePositionConfigurationDTO(int numPositions)
	{
		super(numPositions);
	}

	/**
	 * Sets if the given well should be measured or not.
	 * @param measure True if the given well should be measured during the microplate measurement.
	 * @param well The well.
	 */
	public void setMeasureWell(boolean measure, Well well)
	{
		measuredWells[well.getWellY()][well.getWellX()] = measure;
		if(!measure)
		{
			// Delete invalidated positions
			for(int posY = 0; posY < getWellNumPositionsY(); posY++)
			{
				for(int posX = 0; posX < getWellNumPositionsX(); posX++)
				{
					hashPositions.remove(new WellAndTileIdentifier(well.getWellY(), well.getWellX(), posY, posX));
					//positions[well.getWellY()][well.getWellX()][posY][posX] = null;
				}
			}
		}
	}

	/**
	 * Returns true if this configuration is configured such that only one position per well is defined
	 * (and not necessarily if only one position is activated).
	 * @return True if only one position per well is defined.
	 */
	public boolean isSinglePosition()
	{
		return (measuredPositions.length == 1 && measuredPositions[0].length == 1);
	}

	/**
	 * Returns true if the given well should be measured.
	 * @param well The well.
	 * @return True if the given well should be measured, false otherwise.
	 */
	public boolean isMeasureWell(Well well)
	{
		return measuredWells[well.getWellY()][well.getWellX()];
	}

	/**
	 * Sets if a given position in a well should be measured.
	 * @param measure True if the position should be measured.
	 * @param posY The y-index of the position in a well, starting with 0.
	 * @param posX The x-index of the position in a well, starting with 0.
	 */
	public void setMeasurePosition(boolean measure, int posY, int posX)
	{
		measuredPositions[posY][posX] = measure;
		if(!measure)
		{
			// Delete invalidated positions
			for(int wellY = 0; wellY < getNumWellsY(); wellY++)
			{
				for(int wellX = 0; wellX < getNumWellsX(); wellX++)
				{
					hashPositions.remove(new WellAndTileIdentifier(wellY, wellX, posY, posX));
					//positions[wellY][wellX][posY][posX] = null;
				}
			}
		}
	}

	/**
	 * Returns if a given position in a well should be measured.
	 * @param posY The y-index of the position in a well, starting with 0.
	 * @param posX The x-index of the position in a well, starting with 0.
	 * @return True if the position should be measured.
	 */
	public boolean isMeasurePosition(int posY, int posX)
	{
		return measuredPositions[posY][posX];
	}

	@Override
	public boolean setNumPositions(int numPositions)
	{
		boolean returnVal = super.setNumPositions(numPositions);
		setSelectAllWells(true);
		return returnVal;
	}

	@Override
	public boolean setMicroplateDimensions(int numWellsX, int numWellsY, double wellWidth, double wellHeight, int wellPositionsX, int wellPositionsY, double wellMarginX, double wellMarginY)
	{
		boolean anythingChanged = super.setMicroplateDimensions(numWellsX, numWellsY, wellWidth, wellHeight, wellPositionsX, wellPositionsY, wellMarginX, wellMarginY);
		if(!anythingChanged)
			return false;
		if(hashPositions != null)
			hashPositions.clear();
		//positions = new XYAndFocusPositionDTO[getNumWellsY()][getNumWellsX()][getWellNumPositionsY()][getWellNumPositionsX()];
		if(measuredWells == null || measuredWells.length < 1 || measuredWells.length != getNumWellsY() || measuredWells[0].length != getNumWellsX())
		{
			measuredWells = new boolean[getNumWellsY()][getNumWellsX()];
		}
		if(measuredPositions == null || measuredPositions.length < 1 || measuredPositions.length != getWellNumPositionsY() || measuredPositions[0].length != getWellNumPositionsX())
		{
			measuredPositions = new boolean[getWellNumPositionsY()][getWellNumPositionsX()];
			if(getWellNumPositionsY() % 2 == 1 && getWellNumPositionsX() % 2 == 1)
			{
				measuredPositions[(getWellNumPositionsY() - 1) / 2][(getWellNumPositionsX() - 1) / 2] = true;
			}
		}
		return true;
	}

	/**
	 * Returns a matrix of the size of the microplate, where each element is indicating if the respective well should be measured or not.
	 * @return Matrix of measured wells.
	 */
	public boolean[][] getMeasuredWells()
	{
		boolean[][] returnArray = new boolean[getNumWellsY()][getNumWellsX()];
		for(int wellY = 0; wellY < getNumWellsY(); wellY++)
		{
			for(int wellX = 0; wellX < getNumWellsX(); wellX++)
			{
				returnArray[wellY][wellX] = measuredWells[wellY][wellX];
			}
		}
		return returnArray;
	}

	/**
	 * Returns a matrix of the size of the positions in a well, where each element is indicating if the respective position should be measured or not.
	 * @return Matrix of measured positions.
	 */
	public boolean[][] getMeasuredPositionsInWell()
	{
		boolean[][] returnArray = new boolean[getWellNumPositionsY()][getWellNumPositionsX()];
		for(int posY = 0; posY < getWellNumPositionsY(); posY++)
		{
			for(int posX = 0; posX < getWellNumPositionsX(); posX++)
			{
				returnArray[posY][posX] = measuredPositions[posY][posX];
			}
		}
		return returnArray;
	}

	/**
	 * Creates a default path given this configuration.
	 * Same as <code>createDefaultPath(zeroPositionX, zeroPositionY, 0)</code>.
	 * @param zeroPositionX The x-position of the center of the well A1;
	 * @param zeroPositionY The y-position of the center of the well A1;
	 */
	public void createDefaultPath(double zeroPositionX, double zeroPositionY)
	{
		createDefaultPath(zeroPositionX, zeroPositionY, 0.0);
	}

	/**
	 * Creates a default path given this configuration.
	 * @param zeroPositionX The x-position of the center of the well A1;
	 * @param zeroPositionY The y-position of the center of the well A1;
	 * @param zeroPositionFocus The focus with which the positions should be initialized.
	 */
	public void createDefaultPath(double zeroPositionX, double zeroPositionY, double zeroPositionFocus)
	{
		double midXPos = (getWellNumPositionsX() - 1) / 2.0;
		double midYPos = (getWellNumPositionsY() - 1) / 2.0;

		for(int wellY = 0; wellY < getNumWellsY(); wellY++)
		{
			for(int wellX = 0; wellX < getNumWellsX(); wellX++)
			{
				if(!isMeasureWell(new Well(wellY, wellX)))
					continue;
				for(int posY = 0; posY < getWellNumPositionsY(); posY++)
				{
					for(int posX = 0; posX < getWellNumPositionsX(); posX++)
					{
						if(!isMeasurePosition(posY, posX))
							continue;
						double x = zeroPositionX + wellX * getWellWidth() + (posX - midXPos) * getWellPositionDistanceX();
						double y = zeroPositionY + wellY * getWellHeight() + (posY - midYPos) * getWellPositionDistanceY();
						setPosition(new XYAndFocusPositionDTO(x, y, zeroPositionFocus), new Well(wellY, wellX), posY, posX);

					}
				}
			}
		}
	}

	/**
	 * Returns true if the exact x/y position for every well is initialized.
	 * @return True if each position in each well has an associated x/y/focus value
	 */
	public boolean isInitialized()
	{
		for(int wellY = 0; wellY < getNumWellsY(); wellY++)
		{
			for(int wellX = 0; wellX < getNumWellsX(); wellX++)
			{
				if(!measuredWells[wellY][wellX])
					continue;
				for(int posY = 0; posY < getWellNumPositionsY(); posY++)
				{
					for(int posX = 0; posX < getWellNumPositionsX(); posX++)
					{
						if(!measuredPositions[posY][posX])
							continue;
						
						if(hashPositions.get(new WellAndTileIdentifier(wellY, wellX, posY, posX)) == null)
							return false;
						
						//if(positions[wellY][wellX][posY][posX] == null)
							//return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * Returns the number of wells which are measured in the microplate.
	 * @return Number of wells which should be measured.
	 */
	public int getNumMeasuredWells()
	{
		int numWells = 0;
		for(int wellY = 0; wellY < getNumWellsY(); wellY++)
		{
			for(int wellX = 0; wellX < getNumWellsX(); wellX++)
			{
				if(isMeasureWell(new Well(wellY, wellX)))
					numWells++;
			}
		}
		return numWells;
	}

	/**
	 * Returns the number of positions in a well which are measured.
	 * @return Number of positions which should be measured.
	 */
	public int getNumMeasuredPos()
	{
		int numPos = 0;
		for(int posY = 0; posY < getWellNumPositionsY(); posY++)
		{
			for(int posX = 0; posX < getWellNumPositionsX(); posX++)
			{
				if(isMeasurePosition(posY, posX))
					numPos++;
			}
		}
		return numPos;
	}

	/**
	 * Returns the total number of measured positions in a well times the number of measured wells.
	 * @return Overall number of measured positions.
	 */
	public int getTotalMeasuredPositions()
	{

		return getNumMeasuredWells() * getNumMeasuredPos();
	}

	/**
	 * Select or unselects all wells in a microplate.
	 * @param select True if all wells should be selected/measured, false if all wells should be unselected.
	 */
	public void setSelectAllWells(boolean select)
	{
		for(int wellY = 0; wellY < getNumWellsY(); wellY++)
		{
			for(int wellX = 0; wellX < getNumWellsX(); wellX++)
			{
				setMeasureWell(select, new Well(wellY, wellX));
			}
		}
	}

	/**
	 * Select or unselects all positions in a well.
	 * @param select True if all positions should be selected/measured, false if all positions should be unselected.
	 */
	public void setSelectAllWellPositions(boolean select)
	{
		for(int posY = 0; posY < getWellNumPositionsY(); posY++)
		{
			for(int posX = 0; posX < getWellNumPositionsX(); posX++)
			{
				setMeasurePosition(select, posY, posX);
			}
		}
	}

	/**
	 * Returns if either no well or no position inside a well is selected/should be measured.
	 * @return True if no position in no well should be measured, false if at least one position in one well should be measured.
	 */
	public boolean isNoneSelected()
	{
		for(int wellY = 0; wellY < getNumWellsY(); wellY++)
		{
			for(int wellX = 0; wellX < getNumWellsX(); wellX++)
			{
				if(!measuredWells[wellY][wellX])
					continue;
				for(int posY = 0; posY < getWellNumPositionsY(); posY++)
				{
					for(int posX = 0; posX < getWellNumPositionsX(); posX++)
					{
						if(!measuredPositions[posY][posX])
							continue;
						return false;
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * Returns true if all wells of the microplate are selected to be measured.
	 * @return True if all wells should be measured.
	 */
	public boolean isAllWellsSelected()
	{
		for(int wellY = 0; wellY < getNumWellsY(); wellY++)
		{
			for(int wellX = 0; wellX < getNumWellsX(); wellX++)
			{
				if(!measuredWells[wellY][wellX])
					return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns true if all positions in each well of the microplate are selected to be measured.
	 * @return True if all positions should be measured.
	 */
	public boolean isAllPositionsSelected()
	{
		for(int posY = 0; posY < getWellNumPositionsY(); posY++)
		{
			for(int posX = 0; posX < getWellNumPositionsX(); posX++)
			{
				if(!measuredPositions[posY][posX])
					return false;
			}
		}
		return true;
	}

	/**
	 * Sets the x, y, and focus value for the given position in the given well.
	 * @param position The x, y, and focus value.
	 * @param well The well.
	 * @param posY The y-index of the position in the well, starting with 0.
	 * @param posX The x-index of the position in the well, starting with 0.
	 */
	public void setPosition(XYAndFocusPositionDTO position, Well well, int posY, int posX)
	{
		if(position == null)
			hashPositions.remove(new WellAndTileIdentifier(well.getWellY(), well.getWellX(), posY, posX));
		else
			hashPositions.put(new WellAndTileIdentifier(well.getWellY(), well.getWellX(), posY, posX), position);
		//positions[well.getWellY()][well.getWellX()][posY][posX] = position;
	}

	/**
	 * Returns the previously initialized x, y, and focus value for the given position in the given well, or NULL if the data was yet not initialized
	 * or the given position or the given well should not be measured.
	 * @param well The well.
	 * @param posY The y-index of the position in the well, starting with 0.
	 * @param posX The x-index of the position in the well, starting with 0.
	 * @return The previously initialized x, y, and focus value for the given position in the given well.
	 */
	public XYAndFocusPositionDTO getPosition(Well well, int posY, int posX)
	{
		return hashPositions.get(new WellAndTileIdentifier(well.getWellY(), well.getWellX(), posY, posX));
		//return positions[well.getWellY()][well.getWellX()][posY][posX];
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		MicroplatePositionConfigurationDTO clone = (MicroplatePositionConfigurationDTO)super.clone();
		// Clone hashPositions
		clone.hashPositions = new Hashtable<WellAndTileIdentifier, XYAndFocusPositionDTO>((int)(hashPositions.size()*1.5));
		for(Entry<WellAndTileIdentifier, XYAndFocusPositionDTO> entry : hashPositions.entrySet())
		{
			clone.hashPositions.put((WellAndTileIdentifier)entry.getKey().clone(), (XYAndFocusPositionDTO)entry.getValue().clone());
		}
		
		// clone measured positions
		clone.measuredPositions = new boolean[measuredPositions.length][];
		for(int i=0; i<measuredPositions.length; i++)
		{
			clone.measuredPositions[i] = new boolean[measuredPositions[i].length];
			for(int j=0; j<measuredPositions[i].length; j++)
			{
				clone.measuredPositions[i][j] = measuredPositions[i][j];
			}
		}
		
		// clone measured wells
		clone.measuredWells = new boolean[measuredWells.length][];
		for(int i=0; i<measuredWells.length; i++)
		{
			clone.measuredWells[i] = new boolean[measuredWells[i].length];
			for(int j=0; j<measuredWells[i].length; j++)
			{
				clone.measuredWells[i][j] = measuredWells[i][j];
			}
		}
		
		return clone;
	}
}
