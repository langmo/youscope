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
