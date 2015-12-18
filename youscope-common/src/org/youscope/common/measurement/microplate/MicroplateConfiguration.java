/**
 * 
 */
package org.youscope.common.measurement.microplate;

import java.io.Serializable;

import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * Configuration of structural properties of a microplate, like number and sizes of wells.
 * @author Moritz Lang
 * 
 */
@XStreamAlias("microplate")
public class MicroplateConfiguration implements Cloneable, Serializable, Configuration
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -938899584751802633L;

	@XStreamAlias("type")
	private String				microplateTypeID	= "ANSI_SBS_96";
	@XStreamAlias("num-wells-x")
	private int					numWellsX			= 1;
	@XStreamAlias("num-wells-y")
	private int					numWellsY			= 1;
	@XStreamAlias("well-width")
	private double				wellWidth			= 9000;
	@XStreamAlias("well-height")
	private double				wellHeight			= 9000;
	@XStreamAlias("num-positions-x")
	private int					wellPositionsX		= 1;
	@XStreamAlias("num-positions-y")
	private int					wellPositionsY		= 1;
	@XStreamAlias("well-margin-x")
	private double				wellMarginX			= 0.2;
	@XStreamAlias("well-margin-y")
	private double				wellMarginY			= 0.2;
	@XStreamConverter(value = BooleanConverter.class, booleans = {false}, strings = {"yes", "no"})
	private boolean				aliasMicroplate		= false;

	/**
	 * Default constructor.
	 * Initializes a 96 well plate.
	 */
	public MicroplateConfiguration()
	{
		setMicroplateDimensions(12, 8, 9000, 9000, 1, 1, 0.2, 0.2);
		microplateTypeID = "ANSI_SBS_96";
	}
	
	@Override
	public String getTypeIdentifier() 
	{
		return "YouScope.Microplate";
	}

	/**
	 * Constructor for a predefined microplate type.
	 * @param microplateType The predefined microplate type.
	 */
	public MicroplateConfiguration(Microplate microplateType)
	{
		if(microplateType == null)
			throw new NullPointerException("Type must not be NULL.");
		setMicroplateType(microplateType);
	}

	/**
	 * Constructor for a custom defined microplate.
	 * @param numWellsX number of wells in the horizontal direction.
	 * @param numWellsY number of wells in the vertical direction.
	 * @param wellWidth width of a well (well-well distance).
	 * @param wellHeight height of a well (well-well distance).
	 */
	public MicroplateConfiguration(int numWellsX, int numWellsY, double wellWidth, double wellHeight)
	{
		setMicroplateDimensions(numWellsX, numWellsY, wellWidth, wellHeight, getWellNumPositionsX(), getWellNumPositionsY(), getWellMarginX(), getWellMarginY());
	}

	/**
	 * Constructor for a microplate type, when no microplate is used, but the functionality of the microplate
	 * measurement methods should be used for custom positions.
	 * @param numPositions Number of positions.
	 */
	public MicroplateConfiguration(int numPositions)
	{
		setNumPositions(numPositions);
	}

	/**
	 * Microplate measurements can be "misused" to measure just some arbitrary positions (see <code>setNumPositions()</code>).
	 * If this is true, this function returns TRUE.
	 * @return TRUE, if this is not really a microplate.
	 */
	public boolean isAliasMicroplate()
	{
		return aliasMicroplate;
	}

	/**
	 * Microplate measurements can be "misused" to measure just some arbitrary positions.
	 * Call this function to define the number of positions. Automatically sets <code>isAliasMicroplate()</code> to true.
	 * @param numPositions The number of abitrary positions to measure.
	 * @return Returns TRUE if microplate dimensions changed.
	 */
	public boolean setNumPositions(int numPositions)
	{
		boolean changed = setMicroplateDimensions(numPositions, 1, 0.0, 0.0, 1, 1, getWellMarginX(), getWellMarginY());
		aliasMicroplate = true;
		return changed;
	}

	/**
	 * Microplate measurements can be "misused" to measure just some arbitrary positions.
	 * If <code>isAliasMicroplate()</code> is true, than this function returns the number of positions, otherwise it returns the x-dimension of the microplate.
	 * @return The number of abitrary positions to measure.
	 */
	public int getNumPositions()
	{
		return getNumWellsX();
	}

	/**
	 * Returns the number of wells in the x-direction.
	 * @return the Horizontal well number.
	 */
	public int getNumWellsX()
	{
		return numWellsX;
	}

	/**
	 * Returns the number of wells in the y-direction.
	 * @return the Vertical well number.
	 */
	public int getNumWellsY()
	{
		return numWellsY;
	}

	/**
	 * Sets the number of wells in the x-direction.
	 * @param numWellsX the Horizontal well number.
	 * @return Returns TRUE if microplate dimensions changed.
	 */
	public boolean setNumWellsX(int numWellsX)
	{
		return setMicroplateDimensions(numWellsX, getNumWellsY(), getWellWidth(), getWellHeight(), getWellNumPositionsX(), getWellNumPositionsY(), getWellMarginX(), getWellMarginY());
	}

	/**
	 * Sets the number of wells in the y-direction.
	 * @param numWellsY the Horizontal well number.
	 * @return Returns TRUE if microplate dimensions changed.
	 */
	public boolean setNumWellsY(int numWellsY)
	{
		return setMicroplateDimensions(getNumWellsX(), numWellsY, getWellWidth(), getWellHeight(), getWellNumPositionsX(), getWellNumPositionsY(), getWellMarginX(), getWellMarginY());
	}

	/**
	 * Sets the dimensions of the microplate and the measured positions.
	 * @param numWellsX Number of wells in the x-direction.
	 * @param numWellsY Number of wells in the y-direction.
	 * @param wellWidth Width of a well.
	 * @param wellHeight Height of a well.
	 * @param wellPositionsX Number of measured positions in one well in the x-direction.
	 * @param wellPositionsY Number of measured positions in one well in the y-direction.
	 * @param wellMarginX Relative margin between the well border and the first measured position in the x-direction.
	 * @param wellMarginY Relative margin between the well border and the first measured position in the y-direction.
	 * @return Returns TRUE if microplate dimensions changed.
	 */
	public boolean setMicroplateDimensions(int numWellsX, int numWellsY, double wellWidth, double wellHeight, int wellPositionsX, int wellPositionsY, double wellMarginX, double wellMarginY)
	{
		microplateTypeID = null;
		aliasMicroplate = false;

		if(this.numWellsX == numWellsX && this.numWellsY == numWellsY && this.wellWidth == wellWidth && this.wellHeight == wellHeight && this.wellPositionsX == wellPositionsX && this.wellPositionsY == wellPositionsY && this.wellMarginX == wellMarginX && this.wellMarginY == wellMarginY)
			return false;

		// Store configuration
		this.numWellsX = numWellsX;
		this.numWellsY = numWellsY;
		this.wellWidth = wellWidth;
		this.wellHeight = wellHeight;
		this.wellPositionsX = wellPositionsX;
		this.wellPositionsY = wellPositionsY;
		this.wellMarginX = wellMarginX;
		this.wellMarginY = wellMarginY;
		return true;
	}

	/**
	 * Returns the width of one well (distance of well centers)
	 * @return width of one well.
	 */
	public double getWellWidth()
	{
		return wellWidth;
	}

	/**
	 * Returns the height of one well (distance of well centers)
	 * @return height of one well.
	 */
	public double getWellHeight()
	{
		return wellHeight;
	}

	/**
	 * Sets the width of one well (distance of well centers)
	 * @param wellWidth width of one well.
	 * @return Returns TRUE if microplate dimensions changed.
	 */
	public boolean setWellWidth(double wellWidth)
	{
		return setMicroplateDimensions(getNumWellsX(), getNumWellsY(), wellWidth, getWellHeight(), getWellNumPositionsX(), getWellNumPositionsY(), getWellMarginX(), getWellMarginY());
	}

	/**
	 * Sets the height of one well (distance of well centers)
	 * @param wellHeight width of one well.
	 * @return Returns TRUE if microplate dimensions changed.
	 */
	public boolean setWellHeight(double wellHeight)
	{
		return setMicroplateDimensions(getNumWellsX(), getNumWellsY(), getWellWidth(), wellHeight, getWellNumPositionsX(), getWellNumPositionsY(), getWellMarginX(), getWellMarginY());
	}

	/**
	 * Returns the type ID of the microplate, or NULL if the microplate does not correspond to a predefined type.
	 * @return the type ID of microplate.
	 */
	public String getMicroplateTypeID()
	{
		return microplateTypeID;
	}

	/**
	 * Sets the type of the microplate. If the type is not NULL, the number of wells in each direction as well as their dimensions are also set.
	 * @param microplateType the type of microplate.
	 * @return Returns TRUE if microplate dimensions changed.
	 */
	public boolean setMicroplateType(Microplate microplateType)
	{
		boolean changed = false;
		if(microplateType != null)
		{
			changed = setMicroplateDimensions(microplateType.getNumWellsX(), microplateType.getNumWellsY(), microplateType.getWellWidth(), microplateType.getWellHeight(), getWellNumPositionsX(), getWellNumPositionsY(), getWellMarginX(), getWellMarginY());
		}
		microplateTypeID = microplateType.getMicroplateID();
		return changed;
	}

	/**
	 * Sets the number of positions measured in the x-direction in each well.
	 * @param wellPositionsX number of positions in a well in the x-direction.
	 * @return Returns TRUE if microplate dimensions changed.
	 */
	public boolean setWellNumPositionsX(int wellPositionsX)
	{
		String type = microplateTypeID;
		boolean changed = setMicroplateDimensions(getNumWellsX(), getNumWellsY(), getWellWidth(), getWellHeight(), wellPositionsX, getWellNumPositionsY(), getWellMarginX(), getWellMarginY());
		microplateTypeID = type;
		return changed;
	}

	/**
	 * Returns the number of positions measured in the x-direction in each well.
	 * @return number of positions in a well in the x-direction.
	 */
	public int getWellNumPositionsX()
	{
		return wellPositionsX;
	}

	/**
	 * Sets the number of positions measured in the y-direction in each well.
	 * @param wellPositionsY number of positions in a well in the y-direction.
	 * @return Returns TRUE if microplate dimensions changed.
	 */
	public boolean setWellNumPositionsY(int wellPositionsY)
	{
		String type = microplateTypeID;
		boolean changed = setMicroplateDimensions(getNumWellsX(), getNumWellsY(), getWellWidth(), getWellHeight(), getWellNumPositionsX(), wellPositionsY, getWellMarginX(), getWellMarginY());
		microplateTypeID = type;
		return changed;
	}

	/**
	 * Returns the number of positions measured in the y-direction in each well.
	 * @return number of positions in a well in the y-direction.
	 */
	public int getWellNumPositionsY()
	{
		return wellPositionsY;
	}

	/**
	 * Sets the margin in each well between the well-border and the first measured point.
	 * The value is relative (e.g. in percent) to the well width.
	 * @param wellMarginX margin in the x-direction
	 * @return Returns TRUE if microplate dimensions changed.
	 */
	public boolean setWellMarginX(double wellMarginX)
	{
		String type = microplateTypeID;
		boolean changed = setMicroplateDimensions(getNumWellsX(), getNumWellsY(), getWellWidth(), getWellHeight(), getWellNumPositionsX(), getWellNumPositionsY(), wellMarginX, getWellMarginY());
		microplateTypeID = type;
		return changed;
	}

	/**
	 * Returns the margin in each well between the well-border and the first measured point.
	 * The value is relative (e.g. in percent) to the well width.
	 * @return margin in the x-direction
	 */
	public double getWellMarginX()
	{
		return wellMarginX;
	}

	/**
	 * Sets the margin in each well between the well-border and the first measured point.
	 * The value is relative (e.g. in percent) to the well height.
	 * @param wellMarginY margin in the y-direction
	 * @return Returns TRUE if microplate dimensions changed.
	 */
	public boolean setWellMarginY(double wellMarginY)
	{
		String type = microplateTypeID;
		boolean changed = setMicroplateDimensions(getNumWellsX(), getNumWellsY(), getWellWidth(), getWellHeight(), getWellNumPositionsX(), getWellNumPositionsY(), getWellMarginX(), wellMarginY);
		microplateTypeID = type;
		return changed;
	}

	/**
	 * Returns the margin in each well between the well-border and the first measured point.
	 * The value is relative (e.g. in percent) to the well height.
	 * @return margin in the y-direction
	 */
	public double getWellMarginY()
	{
		return wellMarginY;
	}

	/**
	 * Returns the distance between two adjacent positions in a well in the x-direction.
	 * @return Distance of two adjacent positions in a well.
	 */
	public double getWellPositionDistanceX()
	{
		return (getWellWidth() * (1.0 - 2 * getWellMarginX())) / getWellNumPositionsX();
	}

	/**
	 * Returns the distance between two adjacent positions in a well in the y-direction.
	 * @return Distance of two adjacent positions in a well.
	 */
	public double getWellPositionDistanceY()
	{
		return (getWellHeight() * (1.0 - 2 * getWellMarginY())) / getWellNumPositionsY();
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(numWellsX <= 0 || numWellsY <= 0)
			throw new ConfigurationException("Number of wells must be at least one.");
		if(wellPositionsX <= 0 || wellPositionsY <= 0)
			throw new ConfigurationException("Number of selected wells must be at least one.");
	}
}
