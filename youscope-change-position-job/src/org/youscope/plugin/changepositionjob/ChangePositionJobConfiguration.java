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
package org.youscope.plugin.changepositionjob;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.job.basicjobs.ChangePositionJob;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.converters.basic.BooleanConverter;

/**
 * This job/task changes the position of the microscope in regular intervals.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("change-position-job")
public class ChangePositionJobConfiguration implements JobConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 625890415353745764L;

	/**
	 * The intended x-position of the microscope.
	 */
	@XStreamAlias("x")
	@XStreamAsAttribute
	private double				x					= 0;

	/**
	 * The intended y-position of the microscope.
	 */
	@XStreamAlias("y")
	@XStreamAsAttribute
	private double				y					= 0;

	/**
	 * TRUE if x and y values are absolute, FALSE if relative.
	 */
	@XStreamAlias("absolute-value")
	@XStreamAsAttribute
	@XStreamConverter(value = BooleanConverter.class, booleans = {false}, strings = {"yes", "no"})
	private boolean				absolute			= true;
	
	@XStreamAlias("stage-device")
	@XStreamAsAttribute
	private String stageDevice = null;

	/**
	 * @param x the x to set
	 */
	public void setX(double x)
	{
		this.x = x;
	}

	/**
	 * @return the x
	 */
	public double getX()
	{
		return x;
	}

	@Override
	public String getDescription()
	{
		if(isAbsolute())
			return "<p>[x, y] = [" + Double.toString(x) + ", " + Double.toString(getY()) + "]</p>";
		return "<p>[x, y] = [x + " + Double.toString(x) + ", y + " + Double.toString(getY()) + "]</p>";
	}

	/**
	 * @param absolute the absolute to set
	 */
	public void setAbsolute(boolean absolute)
	{
		this.absolute = absolute;
	}

	/**
	 * @return the absolute
	 */
	public boolean isAbsolute()
	{
		return absolute;
	}

	/**
	 * @param y the y to set
	 */
	public void setY(double y)
	{
		this.y = y;
	}

	/**
	 * @return the y
	 */
	public double getY()
	{
		return y;
	}	

	@Override
	public String getTypeIdentifier()
	{
		return ChangePositionJob.DEFAULT_TYPE_IDENTIFIER;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		// nothing to check, always valid.
		
	}

	/**
	 * Returns the stage device which should be moved, or null for the default stage.
	 * @return Stage device to move.
	 */
	public String getStageDevice() {
		return stageDevice;
	}

	/**
	 * Set to the stage device which should be moved, or to null for the default stage.
	 * @param stageDevice Stage which should be moved.
	 */
	public void setStageDevice(String stageDevice) {
		this.stageDevice = stageDevice;
	}
}
