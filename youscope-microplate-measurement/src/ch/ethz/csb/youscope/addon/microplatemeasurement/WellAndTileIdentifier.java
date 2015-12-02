/**
 * 
 */
package ch.ethz.csb.youscope.addon.microplatemeasurement;

import java.io.Serializable;

/**
 * Class to uniquely identify a well and a tile of a measurement.
 * @author Moritz Lang
 *
 */
public class WellAndTileIdentifier implements Cloneable, Serializable
{
	/**
	 * Serial version UID.
	 */
	private static final long	serialVersionUID	= 5968207151925849154L;
	private final int wellX;
	private final int wellY;
	private final int tileX;
	private final int tileY;
	
	/**
	 * Constructor.
	 * @param wellY y-position of the well, starting at 0.
	 * @param wellX x-position of the well, starting at 0.
	 * @param tileY y-position of the tile, starting at 0.
	 * @param tileX x-position of the tile, starting at 0.
	 */
	public WellAndTileIdentifier(int wellY, int wellX, int tileY, int tileX)
	{
		this.wellY = wellY;
		this.wellX = wellX;
		this.tileX = tileX;
		this.tileY = tileY;
	}

	/**
	 * Returns the x-position of the well.
	 * @return x-position of the well, starting at 0.
	 */
	public int getWellX()
	{
		return wellX;
	}

	/**
	 * Returns the y-position of the well.
	 * @return y-position of the well, starting at 0.
	 */
	public int getWellY()
	{
		return wellY;
	}

	/**
	 * Returns the x-position of the tile.
	 * @return x-position of the tile, starting at 0.
	 */
	public int getTileX()
	{
		return tileX;
	}

	/**
	 * Returns the y-position of the tile.
	 * @return y-position of the tile, starting at 0.
	 */
	public int getTileY()
	{
		return tileY;
	}

	@Override
	public int hashCode()
	{
		final int prime = 100;
		int result = 1;
		result = prime * result + tileX;
		result = prime * result + tileY;
		result = prime * result + wellX;
		result = prime * result + wellY;
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
		WellAndTileIdentifier other = (WellAndTileIdentifier)obj;
		if(tileX != other.tileX)
			return false;
		if(tileY != other.tileY)
			return false;
		if(wellX != other.wellX)
			return false;
		if(wellY != other.wellY)
			return false;
		return true;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
