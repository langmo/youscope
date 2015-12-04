/**
 * 
 */
package org.youscope.common;

import java.io.Serializable;

/**
 * @author langmo
 */
public interface MicroplateType extends Serializable
{
	/**
	 * Returns the number of wells in the x-direction.
	 * @return the Horizontal well number.
	 */
	public int getNumWellsX();

	/**
	 * Returns the number of wells in the y-direction.
	 * @return the Vertical well number.
	 */
	public int getNumWellsY();

	/**
	 * Returns the width of one well (distance of well centers) in mu;
	 * @return width of one well.
	 */
	public double getWellWidth();

	/**
	 * Returns the height of one well (distance of well centers) in mu;
	 * @return height of one well.
	 */
	public double getWellHeight();

	/**
	 * Returns the ID of this microplate type.
	 * 
	 * @return ID of the microplate.
	 */
	String getMicroplateID();

	/**
	 * Should return a short human readable name of this microplate type.
	 * 
	 * @return Human readable name of the microplate.
	 */
	String getMicroplateName();
}
