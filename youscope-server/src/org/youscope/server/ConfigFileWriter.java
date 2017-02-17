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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import org.youscope.addon.microscopeaccess.CameraDeviceInternal;
import org.youscope.addon.microscopeaccess.DeviceInternal;
import org.youscope.addon.microscopeaccess.MicroscopeInternal;
import org.youscope.addon.microscopeaccess.PropertyInternal;
import org.youscope.addon.microscopeaccess.StageDeviceInternal;
import org.youscope.addon.microscopeaccess.StateDeviceInternal;
import org.youscope.common.YouScopeVersion;
import org.youscope.common.microscope.DeviceException;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.MicroscopeConfigurationException;
import org.youscope.common.microscope.MicroscopeDriverException;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.SettingException;

/**
 * Class to write the current microscope configuration to a config file.
 * @author Moritz Lang
 * 
 */
class ConfigFileWriter extends ConfigFileManipulator
{
	private final MicroscopeInternal	microscope;
	private final ChannelManagerImpl	channelManager;
	private final PixelSizeManagerImpl	pixelSizeManager;

	public ConfigFileWriter(MicroscopeInternal microscope, ChannelManagerImpl channelManager, PixelSizeManagerImpl pixelSizeManager)
	{
		this.microscope = microscope;
		this.channelManager = channelManager;
		this.pixelSizeManager = pixelSizeManager;
	}

	public void writeConfigFile(Writer outputWriter, @SuppressWarnings("unused") int accessID) throws MicroscopeConfigurationException, MicroscopeLockedException
	{
		BufferedWriter writer = new BufferedWriter(outputWriter);
		try
		{

			writeHeader(writer);
			writeEmptyLine(writer);
			writeIdentification(writer);
			writeEmptyLine(writer);
			writeUninitialize(writer);
			writeEmptyLine(writer);
			writeDevices(writer);
			writeEmptyLine(writer);
			writeInitialize(writer);
			writeEmptyLine(writer);
			writeDelays(writer);
			writeEmptyLine(writer);
			writeStandardDevices(writer);
			writeEmptyLine(writer);
			writeSynchroDevices(writer);
			writeEmptyLine(writer);
			writeLabels(writer);
			writeEmptyLine(writer);
			writeCommunicationTimeout(writer);
			writeImageBufferSize(writer);
			writeEmptyLine(writer);
			writeAxesStages(writer);
			writeEmptyLine(writer);
			writeAxesCameras(writer);
			writeEmptyLine(writer);
			writeSystemStartup(writer);
			writeEmptyLine(writer);
			writeSystemShutdown(writer);
			writeEmptyLine(writer);
			writeChannels(writer);
			writeEmptyLine(writer);
			writePixelSizeSettings(writer);
			writeEmptyLine(writer);
		}
		catch(Exception e)
		{
			throw new MicroscopeConfigurationException(e);
		}
		finally
		{
			try
			{
				writer.close();
			}
			catch(IOException e)
			{
				throw new MicroscopeConfigurationException(e);
			}
		}
	}

	private void writeIdentification(BufferedWriter writer) throws IOException
	{
		writeComment(writer, "Identification of how and for which machines this configuration was created.");
		writeComment(writer, "Do not remove or edit the following lines.");
		writeIdentTokens(writer, ConfigFileIdentification.IDENT_GENERATOR, ConfigFileIdentification.THIS_GENERATOR.name, ConfigFileIdentification.THIS_GENERATOR.version);
		for(ConfigFileGenerator compatibleGenerator : ConfigFileIdentification.COMPATIBLE_GENERATORS)
		{
			writeIdentTokens(writer, ConfigFileIdentification.IDENT_COMPATIBLE, compatibleGenerator.name, compatibleGenerator.version);
		}
	}

	private static void writeIdentTokens(BufferedWriter writer, String ident, String... tokens) throws IOException
	{
		writer.write("#@" + ident);
		for(int i = 0; i < tokens.length; i++)
		{
			writer.write(',' + tokens[i]);
		}
		writeEmptyLine(writer);
	}

	private static void writeCommand(BufferedWriter writer, String[] command, String... tokens) throws ArrayIndexOutOfBoundsException, IOException
	{
		int tokenIndex = 0;
		String[] filledCommand = new String[command.length];
		for(int i = 0; i < command.length; i++)
		{
			if(command[i] != null)
				filledCommand[i] = command[i];
			else
			{
				if(tokenIndex >= tokens.length)
					throw new ArrayIndexOutOfBoundsException("Not enough tokens for command.\n" + getCommandVersusTokensLayout(command, tokens));
				filledCommand[i] = tokens[tokenIndex];
				tokenIndex++;
			}
		}
		if(tokenIndex < tokens.length)
			throw new ArrayIndexOutOfBoundsException("To many tokens for command.\n" + getCommandVersusTokensLayout(command, tokens));
		writeTokens(writer, filledCommand);
	}

	private static String getCommandVersusTokensLayout(String[] command, String[] tokens)
	{
		String returnValue = "Suplied Layout: ";
		boolean first = true;
		for(String c : command)
		{
			if(first)
				first = false;
			else
				returnValue += ",";
			if(c != null)
				returnValue += c;
			else
				returnValue += "*";
		}
		returnValue += "\n";

		returnValue += "Suplied Layout: ";
		first = true;
		for(String c : tokens)
		{
			if(first)
				first = false;
			else
				returnValue += ",";
			returnValue += c;
		}
		return returnValue;

	}

	private void writePixelSizeSettings(BufferedWriter writer) throws IOException
	{
		boolean firstConfig = true;
		for(PixelSizeImpl pixelSize : pixelSizeManager.getPixelSizes())
		{
			if(firstConfig)
			{
				firstConfig = false;
			}
			else
			{
				writeEmptyLine(writer);
			}
			writeComment(writer, "Pixel Size setting " + pixelSize.getPixelSizeID() + ".");
			for(DeviceSetting setting : pixelSize.getPixelSizeSettings())
			{
				writeCommand(writer, COMMAND_CONFIG_PIXEL_SIZE, pixelSize.getPixelSizeID(), setting.getDevice(), setting.getProperty(), setting.getStringValue());
			}
			writeCommand(writer, COMMAND_PIXEL_SIZE, pixelSize.getPixelSizeID(), Double.toString(pixelSize.getPixelSize()));
		}
	}

	private void writeChannels(BufferedWriter writer) throws IOException, MicroscopeException, InterruptedException, SettingException
	{
		boolean firstChannel = true;
		for(String channelGroupID : channelManager.getChannelGroupIDs())
		{
			for(ChannelImpl channel : channelManager.getChannels(channelGroupID))
			{
				if(firstChannel)
				{
					firstChannel = false;
				}
				else
				{
					writeEmptyLine(writer);
				}

				// YouScope supports on and off settings for channels, MicroManager not.
				// To stay compatible, we add an "_on", resp. "_off" behind the channel name, iff
				// there exists channel off settings (otherwise not).
				boolean useSuffix = channel.getChannelOffSettings().length > 0;
				String channelOnID = channel.getChannelID() + (useSuffix ? "_on" : "");
				String channelOffID = channel.getChannelID() + (useSuffix ? "_off" : "");

				writeComment(writer, "Channel " + channelGroupID + "." + channel.getChannelID());
				for(DeviceSetting setting : channel.getChannelOnSettings())
				{
					writeCommand(writer, COMMAND_CHANNEL, channelGroupID, channelOnID, setting.getDevice(), setting.getProperty(), setting.getStringValue());
				}
				for(DeviceSetting setting : channel.getChannelOffSettings())
				{
					writeCommand(writer, COMMAND_CHANNEL, channelGroupID, channelOffID, setting.getDevice(), setting.getProperty(), setting.getStringValue());
				}
				String shutterID = channel.getShutter();
				if(shutterID == null)
				{
					writeCommand(writer, COMMAND_CHANNEL_AUTOSHUTTER, channelGroupID, channelOnID, "0");
				}
				else
				{
					writeCommand(writer, COMMAND_CHANNEL_AUTOSHUTTER, channelGroupID, channelOnID, "1");
					writeCommand(writer, COMMAND_CHANNEL_AUTOSHUTTER_DEVICE, channelGroupID, channelOnID, shutterID);
				}
				if(channel.getChannelTimeout() > 0)
				{
					writeCommand(writer, COMMAND_CHANNEL_DELAY, channelGroupID, channelOnID, Integer.toString(channel.getChannelTimeout()));
				}
			}
		}
	}

	private void writeLabels(BufferedWriter writer) throws IOException, MicroscopeException, DeviceException
	{
		boolean firstDevice = true;
		for(StateDeviceInternal device : microscope.getStateDevices())
		{
			boolean firstLabel = true;
			String[] labels = device.getStateLabels();
			for(int i = 0; i < labels.length; i++)
			{
				if(firstLabel)
				{
					if(firstDevice)
					{
						firstDevice = false;
					}
					else
					{
						writeEmptyLine(writer);
					}
					firstLabel = false;
					writeComment(writer, "State labels for device " + device.getDeviceID() + ".");
				}
				writeCommand(writer, COMMAND_LABEL, device.getDeviceID(), Integer.toString(i), labels[i]);
			}
		}
	}

	private void writeSynchroDevices(BufferedWriter writer) throws IOException
	{
		writeComment(writer, "Devices to which imaging process is synchronized.");
		for(String deviceID : microscope.getMicroscopeConfiguration().getImageSynchronizationDevices())
		{
			writeCommand(writer, COMMAND_IMAGE_SYNCHRO, deviceID);
		}
	}

	private void writeStandardDevices(BufferedWriter writer) throws IOException, MicroscopeDriverException, InterruptedException, MicroscopeException, DeviceException
	{
		writeComment(writer, "Standard devices (\"Roles\").");
		try
		{
			DeviceInternal device = microscope.getAutoFocusDevice();
			writeCommand(writer, COMMAND_STANDARD_AUTO_FOCUS, device.getDeviceID());
		}
		catch(@SuppressWarnings("unused") DeviceException e)
		{
			// Do nothing, standard device is just not defined.
		}

		try
		{
			DeviceInternal device = microscope.getCameraDevice();
			writeCommand(writer, COMMAND_STANDARD_CAMERA, device.getDeviceID());
		}
		catch(@SuppressWarnings("unused") DeviceException e)
		{
			// Do nothing, standard device is just not defined.
		}

		try
		{
			DeviceInternal device = microscope.getShutterDevice();
			writeCommand(writer, COMMAND_STANDARD_SHUTTER, device.getDeviceID());
		}
		catch(@SuppressWarnings("unused") DeviceException e)
		{
			// Do nothing, standard device is just not defined.
		}

		try
		{
			DeviceInternal device = microscope.getFocusDevice();
			writeCommand(writer, COMMAND_STANDARD_FOCUS, device.getDeviceID());
		}
		catch(@SuppressWarnings("unused") DeviceException e)
		{
			// Do nothing, standard device is just not defined.
		}

		try
		{
			DeviceInternal device = microscope.getStageDevice();
			writeCommand(writer, COMMAND_STANDARD_STAGE, device.getDeviceID());
		}
		catch(@SuppressWarnings("unused") DeviceException e)
		{
			// Do nothing, standard device is just not defined.
		}
	}

	private void writeDelays(BufferedWriter writer) throws IOException, MicroscopeDriverException, InterruptedException, MicroscopeException, DeviceException
	{
		writeComment(writer, "Explicit delays (in ms).");
		for(DeviceInternal device : microscope.getDevices())
		{
			if(device.getExplicitDelay() > 0)
			{
				writeCommand(writer, COMMAND_DELAY, device.getDeviceID(), Double.toString(device.getExplicitDelay()));
			}
		}
	}

	private void writeDevices(BufferedWriter writer) throws IOException, MicroscopeDriverException, InterruptedException, MicroscopeException, DeviceException
	{
		writeComment(writer, "Load devices.");
		// Get devices.
		DeviceInternal[] devices = microscope.getDevices();
		// Sort the devices in the order as they were initialized.
		Arrays.sort(devices, new DeviceInitializationTimeComparator());
		// Write the device names and drivers.
		for(DeviceInternal device : devices)
		{
			writeCommand(writer, COMMAND_DEVICE, device.getDeviceID(), device.getLibraryID(), device.getDriverID());
		}

		writeEmptyLine(writer);

		// Write the pre-initialization settings
		writeComment(writer, "Pre-initialization settings.");
		for(DeviceInternal device : devices)
		{
			String deviceID = device.getDeviceID();
			for(PropertyInternal property : device.getProperties())
			{
				String propertyID = property.getPropertyID();
				if(property.isPreInitializationProperty())
				{
					writeCommand(writer, COMMAND_PROPERTY, deviceID, propertyID, property.getValue());
				}
			}
		}
	}

	/**
	 * Comparator to sort the devices depending on their initialization time.
	 * For some specific devices it is important that they are initialized in the right order. Since we do not know the right order,
	 * we just use the order of initialization, since we know that this order works (otherwise, the initialization would have failed).
	 * @author Moritz Lang
	 * 
	 */
	private class DeviceInitializationTimeComparator implements Comparator<DeviceInternal>
	{
		@Override
		public int compare(DeviceInternal o1, DeviceInternal o2)
		{
			return o1.getInitializationTime().compareTo(o2.getInitializationTime());
		}
	}

	private static void writeUninitialize(BufferedWriter writer) throws IOException
	{
		writeComment(writer, "Reset the microscope.");
		writeComment(writer, "Do not remove or edit this line, or change its location.");
		writeCommand(writer, COMMAND_UNINITIALIZE);
	}

	private static void writeInitialize(BufferedWriter writer) throws IOException
	{
		writeComment(writer, "Initialize the microscope.");
		writeComment(writer, "Do not remove or edit this line, or change its location.");
		writeCommand(writer, COMMAND_INITIALIZE);
	}

	private static void writeComment(BufferedWriter writer, String comment) throws IOException
	{
		writer.write("# " + comment);
		writer.newLine();
	}

	private void writeCommunicationTimeout(BufferedWriter writer) throws IOException, MicroscopeDriverException, InterruptedException, MicroscopeException, DeviceException
	{
		writeComment(writer, "Communication timeout (ms).");
		writeCommand(writer, COMMAND_COMMUNICATION_TIMEOUT, Integer.toString(microscope.getMicroscopeConfiguration().getCommunicationTimeout()));
	}

	private void writeImageBufferSize(BufferedWriter writer) throws IOException, MicroscopeDriverException, InterruptedException, MicroscopeException, DeviceException
	{
		int imageBufferSize;
		try
		{
			imageBufferSize = microscope.getMicroscopeConfiguration().getImageBufferSize();
		}
		catch(@SuppressWarnings("unused") UnsupportedOperationException e)
		{
			imageBufferSize = -1;
		}
		if(imageBufferSize >= 0)
		{
			writeEmptyLine(writer);
			writeComment(writer, "Image Buffer Size (MB).");
			writeCommand(writer, COMMAND_IMAGE_BUFFER_SIZE, Integer.toString(imageBufferSize));
		}
	}

	private void writeAxesStages(BufferedWriter writer) throws IOException, MicroscopeDriverException, MicroscopeException, DeviceException
	{
		StageDeviceInternal[] stages = microscope.getStageDevices();
		if(stages.length <= 0)
			return;
		writeComment(writer, "Axes direction settings of stages.");
		for(StageDeviceInternal stage : stages)
		{
			writeComment(writer, "Stage " + stage.getDeviceID() + ".");
			writeCommand(writer, COMMAND_AXIS_CONFIGURATION_X, stage.getDeviceID(), stage.isTransposeX() ? "1" : "0");
			writeCommand(writer, COMMAND_AXIS_CONFIGURATION_Y, stage.getDeviceID(), stage.isTransposeY() ? "1" : "0");

			// Save stage unit multiplier.
			if(stage.getUnitMagnifier() != 1.0)
			{
				writeCommand(writer, COMMAND_STAGE_UNITS, stage.getDeviceID(), Double.toString(stage.getUnitMagnifier()));
			}
		}
	}

	private void writeAxesCameras(BufferedWriter writer) throws IOException, MicroscopeDriverException, MicroscopeException, DeviceException
	{
		CameraDeviceInternal[] cameras = microscope.getCameraDevices();
		if(cameras.length <= 0)
			return;
		writeComment(writer, "Axes direction settings of cameras.");
		boolean first = true;
		for(CameraDeviceInternal camera : cameras)
		{
			if(first)
				first = false;
			else
				writeEmptyLine(writer);
			writeComment(writer, "Axes direction settings of camera " + camera.getDeviceID() + ".");
			writeCommand(writer, COMMAND_AXIS_CONFIGURATION_X, camera.getDeviceID(), camera.isTransposeX() ? "1" : "0");
			writeCommand(writer, COMMAND_AXIS_CONFIGURATION_Y, camera.getDeviceID(), camera.isTransposeY() ? "1" : "0");
			writeCommand(writer, COMMAND_AXIS_CONFIGURATION_XY, camera.getDeviceID(), camera.isSwitchXY() ? "1" : "0");
		}
	}

	private void writeSystemStartup(BufferedWriter writer) throws IOException
	{
		writeComment(writer, "Startup settings (executed when config file loads).");
		for(DeviceSetting setting : microscope.getMicroscopeConfiguration().getSystemStartupSettings())
		{
			writeCommand(writer, COMMAND_SYSTEM_STARTUP, setting.getDevice(), setting.getProperty(), setting.getStringValue());
		}
	}

	private void writeSystemShutdown(BufferedWriter writer) throws IOException
	{
		writeComment(writer, "Shutdown settings (executed when config file loads).");
		for(DeviceSetting setting : microscope.getMicroscopeConfiguration().getSystemShutdownSettings())
		{
			writeCommand(writer, COMMAND_SYSTEM_SHUTDOWN, setting.getDevice(), setting.getProperty(), setting.getStringValue());
		}
	}

	private static void writeTokens(BufferedWriter writer, String... tokens) throws IOException
	{
		for(int i = 0; i < tokens.length; i++)
		{
			if(i > 0)
				writer.write(',');
			writer.write(tokens[i]);
		}
		writeEmptyLine(writer);
	}

	private static void writeHeader(BufferedWriter writer) throws IOException
	{
		String[] header = new String[] {"############################################################", "# Microscope Configuration File                            #", "# Generated by YouScope "+YouScopeVersion.getDeveloperVersion()+"                                #", "# Compatible with Micro-Manager 1.4                        #", "# Created: " + new Date().toString(), "# Visit www.youscope.org                                   #", "############################################################"};
		while(header[4].length() < header[0].length() - 1)
		{
			header[4] += " ";
		}
		header[4] += "#";
		for(String line : header)
		{
			writer.write(line);
			writer.newLine();
		}
	}

	private static void writeEmptyLine(BufferedWriter writer) throws IOException
	{
		writer.newLine();
	}
}
