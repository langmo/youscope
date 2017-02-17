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
package org.youscope.server;

/**
 * @author Moritz Lang
 * 
 */
abstract class ConfigFileManipulator
{
	/**
	 * The microscope access ID with which the microscope is manipulated.
	 */
	protected static final String[]	COMMAND_IMAGE_SYNCHRO				= {"ImageSynchro", null};

	protected static final String[]	COMMAND_AXIS_CONFIGURATION_X		= {"ConfigGroup", "System", "Startup", null, "TransposeMirrorX", null};
	protected static final String[]	COMMAND_AXIS_CONFIGURATION_Y		= {"ConfigGroup", "System", "Startup", null, "TransposeMirrorY", null};
	protected static final String[]	COMMAND_AXIS_CONFIGURATION_XY		= {"ConfigGroup", "System", "Startup", null, "TransposeXY", null};

	protected static final String[]	COMMAND_SYSTEM_STARTUP				= {"ConfigGroup", "System", "Startup", null, null, null};
	protected static final String[]	COMMAND_SYSTEM_STARTUP_CORE			= {"ConfigGroup", "System", "Startup", "Core", null, null};
	protected static final String[]	COMMAND_SYSTEM_SHUTDOWN				= {"ConfigGroup", "System", "Shutdown", null, null, null};
	protected static final String[]	COMMAND_SYSTEM_SHUTDOWN_CORE		= {"ConfigGroup", "System", "Shutdown", "Core", null, null};

	protected static final String[]	COMMAND_CORE_AUTOSHUTTER			= {"Property", "Core", "AutoShutter", null};

	protected static final String[]	COMMAND_COMMUNICATION_TIMEOUT		= {"ConfigGroup", "System", "Startup", "Core", "TimeoutMs", null};

	protected static final String[]	COMMAND_IMAGE_BUFFER_SIZE			= {"ConfigGroup", "System", "Startup", "Core", "ImageBufferMB", null};

	protected static final String[]	COMMAND_CHANNEL						= {"ConfigGroup", null, null, null, null, null};
	protected static final String[]	COMMAND_CHANNEL_AUTOSHUTTER			= {"ConfigGroup", null, null, "Core", "AutoShutter", null};
	protected static final String[]	COMMAND_CHANNEL_AUTOSHUTTER_DEVICE	= {"ConfigGroup", null, null, "Core", "Shutter", null};
	protected static final String[]	COMMAND_CHANNEL_DELAY				= {"ChannelDelay", null, null, null};

	protected static final String[]	COMMAND_DEVICE						= {"Device", null, null, null};

	protected static final String[]	COMMAND_LABEL						= {"Label", null, null, null};

	protected static final String[]	COMMAND_DELAY						= {"Delay", null, null};

	protected static final String[]	COMMAND_STANDARD_FOCUS				= {"Property", "Core", "Focus", null};
	protected static final String[]	COMMAND_STANDARD_AUTO_FOCUS			= {"Property", "Core", "AutoFocus", null};
	protected static final String[]	COMMAND_STANDARD_CAMERA				= {"Property", "Core", "Camera", null};
	protected static final String[]	COMMAND_STANDARD_STAGE				= {"Property", "Core", "XYStage", null};
	protected static final String[]	COMMAND_STANDARD_SHUTTER			= {"Property", "Core", "Shutter", null};

	protected static final String[]	COMMAND_SYSTEM_STANDARD_FOCUS		= {"ConfigGroup", "System", "Startup", "Core", "Focus", null};
	protected static final String[]	COMMAND_SYSTEM_STANDARD_AUTO_FOCUS	= {"ConfigGroup", "System", "Startup", "Core", "AutoFocus", null};
	protected static final String[]	COMMAND_SYSTEM_STANDARD_CAMERA		= {"ConfigGroup", "System", "Startup", "Core", "Camera", null};
	protected static final String[]	COMMAND_SYSTEM_STANDARD_STAGE		= {"ConfigGroup", "System", "Startup", "Core", "XYStage", null};
	protected static final String[]	COMMAND_SYSTEM_STANDARD_SHUTTER		= {"ConfigGroup", "System", "Startup", "Core", "Shutter", null};
	protected static final String[]	COMMAND_SYSTEM_CORE_AUTOSHUTTER		= {"ConfigGroup", "System", "Startup", "Core", "AutoShutter", null};

	protected static final String[]	COMMAND_SYSTEM						= {"ConfigGroup", "System", null, null, null, null};

	protected static final String[]	COMMAND_CONFIG_PIXEL_SIZE			= {"ConfigPixelSize", null, null, null, null};
	protected static final String[]	COMMAND_PIXEL_SIZE					= {"PixelSize_um", null, null};

	protected static final String[]	COMMAND_UNINITIALIZE				= {"Property", "Core", "Initialize", "0"};
	protected static final String[]	COMMAND_INITIALIZE					= {"Property", "Core", "Initialize", "1"};

	protected static final String[]	COMMAND_PROPERTY					= {"Property", null, null, null};
	protected static final String[]	COMMAND_PROPERTY_CORE				= {"Property", "Core", null, null};

	protected static final String[]	IDENT_GENERATOR						= {"#@" + ConfigFileIdentification.IDENT_GENERATOR, null, null};
	protected static final String[]	IDENT_COMPATIBLE					= {"#@" + ConfigFileIdentification.IDENT_COMPATIBLE, null, null};

	protected static final String[]	COMMAND_STAGE_UNITS					= {"XYStageUnit", null, null};
}
