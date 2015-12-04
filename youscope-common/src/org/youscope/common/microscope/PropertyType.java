/**
 * 
 */
package org.youscope.common.microscope;

/**
 * The type of a property.
 * 
 * @author Moritz Lang
 */
public enum PropertyType
{
	/**
	 * Read only property type.
	 */
	PROPERTY_READ_ONLY,
	/**
	 * String type.
	 */
	PROPERTY_STRING,
	/**
	 * Floating point number.
	 */
	PROPERTY_FLOAT,
	/**
	 * Integer.
	 */
	PROPERTY_INTEGER,
	/**
	 * Property type where value is selectable from a list of allowed values.
	 */
	PROPERTY_SELECTABLE
}
