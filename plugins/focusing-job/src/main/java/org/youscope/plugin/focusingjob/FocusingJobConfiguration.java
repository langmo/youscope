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
package org.youscope.plugin.focusingjob;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.configuration.YSConfigAlias;
import org.youscope.common.configuration.YSConfigClassification;
import org.youscope.common.configuration.YSConfigIcon;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.basicjobs.FocusingJob;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Configuration of a job setting the focus.
 * @author Moritz Lang
 */
@XStreamAlias("focusing-job")
@YSConfigAlias("Focussing")
@YSConfigClassification("elementary")
@YSConfigIcon("icons/application-dock-270.png")
public class FocusingJobConfiguration implements JobConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 7547751129638360499L;

	/**
	 * @param focusConfiguration
	 *            The configuration of the focus .
	 */
	public void setFocusConfiguration(FocusConfiguration focusConfiguration)
	{
		this.focusConfiguration = focusConfiguration;
	}

	/**
	 * @return The configuration of the focus.
	 */
	public FocusConfiguration getFocusConfiguration()
	{
		return focusConfiguration;
	}

	/**
	 * Configuration of the focus device used for focussing.
	 */
	@XStreamAlias("focusing-configuration")
	@YSConfigAlias("focus device")
	private FocusConfiguration	focusConfiguration	= null;

	/**
	 * The new position/offset of the focus/autofocus.
	 */
	@YSConfigAlias("focus position (um)")
	@XStreamAlias("focus-position-um")
	private double							position			= 0;
	
	@YSConfigAlias("Focus relative to current focus (otherwise, absolute)")
	@XStreamAlias("focus-position-relative")
	private boolean relative = false;

	/**
	 * Returns true if the focus position is an offset to the current focus position. Returns false if the focus
	 * position is an absolute value.
	 * @return true if relative focus position, false if absolute focus position.
	 */
	public boolean isRelative() {
		return relative;
	}

	/**
	 * Set to true if the focus position is an offset to the current focus position. Set to false if the focus
	 * position is an absolute value.
	 * @param relative true if relative focus position, false if absolute focus position.
	 */
	public void setRelative(boolean relative) {
		this.relative = relative;
	}

	@Override
	public String getDescription()
	{
		if(focusConfiguration == null)
			return "<p>Job not completely initialized.";
		String returnVal = "<p>";
		if(focusConfiguration != null && focusConfiguration.getFocusDevice() != null)
			returnVal += focusConfiguration.getFocusDevice();
		else
			returnVal += "focus";

		if(isRelative())
			returnVal += ".position += ";
		else
			returnVal += ".position = ";
		returnVal += Double.toString(position);
		if(focusConfiguration.getAdjustmentTime() > 0)
			returnVal += "<br />wait(" + Integer.toString(focusConfiguration.getAdjustmentTime()) + ")";
		returnVal += "</p>";

		return returnVal;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(double position)
	{
		this.position = position;
	}

	/**
	 * @return the position
	 */
	public double getPosition()
	{
		return position;
	}

	@Override
	public String getTypeIdentifier()
	{
		return FocusingJob.DEFAULT_TYPE_IDENTIFIER;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// do nothing, always correct.
		
	}
}
