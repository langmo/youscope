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
package org.youscope.client;

/**
 * Type of the table data representation in a plot.
 * @author Moritz Lang
 *
 */
enum TableDataPlotType
{
	/**
	 * Each table data element gets plotted as a scatter element.
	 */
	Scatter,
	/**
	 * If more than one element is transmitted per evaluation, only the median is plotted.
	 */
	LineMedian,
	/**
	 * If more than one element is transmitted per evaluation, only the mean is plotted.
	 */
	LineMean,
	/**
	 * If more than one element is transmitted per evaluation, only the first is plotted.
	 */
	LineFirst,
	/**
	 * If more than one element is transmitted per evaluation, all are plotted. The elements which should be connected are determined by an additional column.
	 */
	LineIdentity;
}
