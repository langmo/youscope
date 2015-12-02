/**
 * 
 */
package ch.ethz.csb.youscope.addon.microplatemeasurement;

import java.io.Serializable;


/**
 * The exact position (x, y, and focus position) of one measurement point.
 * @author langmo
 */
/**
 * @author langmo
 * 
 */
public class XYAndFocusPositionDTO implements Cloneable, Serializable, Comparable<XYAndFocusPositionDTO>
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 7619329389877542260L;

	/**
	 * The focus position.
	 */
	private double				focus				= 0;

	/**
	 * The intended x-position of the microscope.
	 */
	private double				x					= 0;

	/**
	 * The intended y-position of the microscope.
	 */
	private double				y					= 0;

	/**
	 * The default constructor.
	 * Initializes the x, y, and focus position to 0.
	 */
	public XYAndFocusPositionDTO()
	{
		this(0.0, 0.0, 0.0);
	}

	/**
	 * Constructor.
	 * Sets the x, y position to the given values and focus to 0.
	 * @param x The x position.
	 * @param y The y position
	 */
	public XYAndFocusPositionDTO(double x, double y)
	{
		this(x, y, 0.0);
	}

	/**
	 * Constructor.
	 * Sets the x, y, and focus position to the given values.
	 * @param x The x position.
	 * @param y The y position
	 * @param focus The focus position.
	 */
	public XYAndFocusPositionDTO(double x, double y, double focus)
	{
		this.x = x;
		this.y = y;
		this.focus = focus;
	}

	/**
	 * Sets the focus position. The unit of this value depends on the focusing mechanism.
	 * @param focus the focus position.
	 */
	public void setFocus(double focus)
	{
		this.focus = focus;
	}

	/**
	 * Returns the focus position. The unit of this value depends on the focusing mechanism.
	 * @return the focus position.
	 */
	public double getFocus()
	{
		return focus;
	}

	/**
	 * Sets the value of the x-position in um.
	 * @param x x-position in um.
	 */
	public void setX(double x)
	{
		this.x = x;
	}

	/**
	 * Returns the value of the x-position in um.
	 * @return x-position in um.
	 */
	public double getX()
	{
		return x;
	}

	/**
	 * Sets the value of the y-position in um.
	 * @param y y-position in um.
	 */
	public void setY(double y)
	{
		this.y = y;
	}

	/**
	 * Returns the value of the y-position in um.
	 * @return y-position in um.
	 */
	public double getY()
	{
		return y;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(focus);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XYAndFocusPositionDTO other = (XYAndFocusPositionDTO) obj;
		if (Double.doubleToLongBits(focus) != Double.doubleToLongBits(other.focus))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}

	/**
	 * Compares the two positions. First, the x position, then the y position and finally the focus position is compared.
	 * In each of the three steps 1 is returned if the position of this object is greater than the one of the compared object, and -1
	 * if it is smaller. If they are equal, the next value (x position -> y position -> focus position) is compared.
	 * If all three values are equal, 0 is returned.
	 * @param otherPosition The object to which this object should be compared to.
	 * @return 1 if this object is greater than the compared one, -1 if it is smaller, and zero if they are equal.
	 */
	@Override
	public int compareTo(XYAndFocusPositionDTO otherPosition)
	{
		if(otherPosition == null)
			return 1;

		if(otherPosition.x > x)
			return -1;
		else if(otherPosition.x < x)
			return 1;

		if(otherPosition.y > y)
			return -1;
		else if(otherPosition.y < y)
			return 1;

		if(otherPosition.focus > focus)
			return -1;
		else if(otherPosition.focus < focus)
			return 1;

		// positions are equal
		return 0;
	}
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}
}
