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
 * Types of devices of a microscope.
 * 
 * @author Moritz Lang
 */
public enum DeviceType
{

	/**
	 * Camera Device.
	 */
	CameraDevice("Camera"),
	/**
	 * Shutter.
	 */
	ShutterDevice("Shutter"),
	/**
	 * Filter, Turrets, ...
	 */
	StateDevice("Filter, Turrets, ..."),
	/**
	 * 1D Stage, Focus, ...
	 */
	StageDevice("1D Stage, Focus, ..."),
	/**
	 * 2D Stage
	 */
	XYStageDevice("2D Stage"),
	/**
	 * Serial Port
	 */
	SerialDevice("Serial Port"),
	/**
	 * Auto-Focus
	 */
	AutoFocusDevice("Auto-Focus"),
	/**
	 * Signal IO
	 */
	SignalIODevice("Signal IO"),
	/**
	 * Programmable IO
	 */
	ProgrammableIODevice("Programmable IO"),
	/**
	 * Image Processor
	 */
	ImageProcessorDevice("Image Processor"),
	/**
	 * Image Streamer
	 */
	ImageStreamerDevice("Image Streamer"),
	/**
	 * Magnifier
	 */
	MagnifierDevice("Magnifier"),
	/**
	 * Generic Device
	 */
	GenericDevice("Generic"),
	/**
	 * Special Device.
	 */
	AnyType("Special"),
	/**
	 * Unknown Device.
	 */
	UnknownType("Unknown");

	private final String	description;

	private DeviceType(String description)
	{
		this.description = description;
	}

	/**
	 * Returns a human readable name/description of this device type.
	 * @return Human readable name/description.
	 */
	public String getDescription()
	{
		return description;
	}

	@Override
	public String toString()
	{
		return description;
	}
}
