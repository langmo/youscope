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
package org.youscope.common.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allowed range of an integer property.
 * @author Moritz Lang
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface YSConfigIntegerRange {
	/**
	 * Minimal value allowed for an integer property.
	 * 
	 * @return minimal allowed value.
	 */
	public int minValue() default Integer.MIN_VALUE;

	/**
	 * Maximal value allowed for an integer property.
	 * 
	 * @return maximal allowed value.
	 */
	public int maxValue() default Integer.MAX_VALUE;

	/**
	 * Null String used to indicate that no min/max value function is given.
	 */
	public static final String NULL = "";

	/**
	 * Name of a function taking no arguments and returning an int. This
	 * function is used during runtime to determine the minimal allowed value.
	 * If set, overwrites anything set in minValue.
	 * 
	 * @return Name of the function to determine minimal value.
	 */
	public String minValueFunction() default NULL;

	/**
	 * Name of a function taking no arguments and returning an int. This
	 * function is used during runtime to determine the maximal allowed value.
	 * If set, overwrites anything set in maxValue.
	 * 
	 * @return Name of the function to determine maximal value.
	 */
	public String maxValueFunction() default NULL;
}
