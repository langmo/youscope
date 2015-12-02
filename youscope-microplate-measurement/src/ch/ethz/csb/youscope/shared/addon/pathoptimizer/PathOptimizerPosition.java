/**
 * 
 */
package ch.ethz.csb.youscope.shared.addon.pathoptimizer;

import ch.ethz.csb.youscope.addon.microplatemeasurement.XYAndFocusPositionDTO;

/**
 * Structure to save x, y, and focus position, as well as information to which well and position the information belongs.
 * @author Moritz Lang
 *
 */
public class PathOptimizerPosition extends XYAndFocusPositionDTO
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 70299304224787360L;
	private final int wellX;
	private final int wellY;
	private final int posX;
	private final int posY;
	/**
	 * Constructor.
	 * @param parent The absolute stage and focus position of this path element.
	 * @param wellY The well row.
	 * @param wellX The well column.
	 * @param posY The position in well row.
	 * @param posX The position in well column.
	 */
	public PathOptimizerPosition(XYAndFocusPositionDTO parent, int wellY, int wellX, int posY, int posX)
	{
		super(parent.getX(), parent.getY(), parent.getFocus());
		this.wellX = wellX;
		this.wellY = wellY;
		this.posX = posX;
		this.posY = posY;
	}
	/**
	 * Returns the well column number.
	 * @return Well column number, starting at 0.
	 */
	public int getWellX()
	{
		return wellX;
	}
	/**
	 * Returns the well row number.
	 * @return Well row number, starting at 0.
	 */
	public int getWellY()
	{
		return wellY;
	}
	/**
	 * Returns the position in well column number.
	 * @return Position in well column number, starting at 0.
	 */
	public int getPositionX()
	{
		return posX;
	}
	/**
	 * Returns the position in well row number.
	 * @return Position in well row number, starting at 0.
	 */
	public int getPositionY()
	{
		return posY;
	}

}
