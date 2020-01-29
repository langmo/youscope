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
package org.youscope.plugin.exhaustivefocussearch;

import org.youscope.addon.focussearch.FocusSearchConfiguration;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigIntegerRange;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * @author Moritz Lang
 *
 */
@YSConfigAlias("exhaustive search")
@XStreamAlias("exhaustive-focus-search")
public class ExhaustiveFocusSearchConfiguration extends FocusSearchConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -1111111676111652236L;
	
	@YSConfigAlias("lower bound")
	@XStreamAlias("focus-lower-bound")
	private double focusLowerBound = -10;
	@YSConfigAlias("upper bound")
	@XStreamAlias("focus-upper-bound")
	private double focusUpperBound = 10;
	@YSConfigAlias("number of search steps")
	@YSConfigIntegerRange(minValue=2)
	@XStreamAlias("focus-search-steps")
	private int numSearchSteps = 11;
	
	
	/**
	 * Constructor.
	 */
	public ExhaustiveFocusSearchConfiguration()
	{
		// do nothing.
	}

	/**
	 * The identifier for this configuration.
	 */
	public static final String	CONFIGURATION_ID	= "YouScope.SimpleFocusSearch";
	
	@Override
	public String getTypeIdentifier()
	{
		return CONFIGURATION_ID;
	}
	
	/**
	 * Sets the minimal relative focus. Must be smaller than the upper bound.
	 * @param focusLowerBound lower relative focus bound
	 */
	public void setFocusLowerBound(double focusLowerBound)
	{
		this.focusLowerBound = focusLowerBound;
	}

	/**
	 * Returns the minimal relative focus.
	 * @return lower relative focus bound
	 */
	public double getFocusLowerBound()
	{
		return focusLowerBound;
	}

	/**
	 * Sets the maximal relative focus. Must be bigger than the lower bound.
	 * @param focusUpperBound upper relative focus bound
	 */
	public void setFocusUpperBound(double focusUpperBound)
	{
		this.focusUpperBound = focusUpperBound;
	}

	/**
	 * Returns the maximal relative focus.
	 * @return upper relative focus bound
	 */
	public double getFocusUpperBound()
	{
		return focusUpperBound;
	}

	/**
	 * Sets the number of images which should be taken and analyzed to find the optional focal plane
	 * @param numSearchSteps number of focus search steps. Must be larger than zero.
	 */
	public void setNumSearchSteps(int numSearchSteps)
	{
		this.numSearchSteps = numSearchSteps;
	}
	/**
	 * Returns the number of images which should be taken and analyzed to find the optional focal plane
	 * @return number of focus search steps.
	 */
	public int getNumSearchSteps()
	{
		return numSearchSteps;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// TODO Implement some checks.
		
	}
}
