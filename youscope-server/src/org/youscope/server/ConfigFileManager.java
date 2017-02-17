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

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

import org.youscope.addon.microscopeaccess.MicroscopeInternal;
import org.youscope.common.microscope.MicroscopeConfigurationException;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;
import org.youscope.common.microscope.SettingException;

/**
 * Class to load and save configuration files.
 * Provides the implementation for the respective functions in the Microscope interface.
 * @author Moritz Lang
 * 
 */
class ConfigFileManager
{
	private final MicroscopeInternal		microscope;
	private final ChannelManagerImpl		channelManager;
	private final PixelSizeManagerImpl		pixelSizeManager;
	private volatile ConfigFileParseResult	lastParseResult	= null;

	ConfigFileManager(MicroscopeInternal microscope, ChannelManagerImpl channelManager, PixelSizeManagerImpl pixelSizeManager)
	{
		this.microscope = microscope;
		this.channelManager = channelManager;
		this.pixelSizeManager = pixelSizeManager;
	}

	public String loadConfiguration(Reader configurationReader, int accessID) throws MicroscopeConfigurationException, InterruptedException, MicroscopeException, MicroscopeLockedException
	{
		if(configurationReader == null)
		{
			// Read in empty configuration
			try
			{
				configurationReader = new InputStreamReader(ConfigFileManager.class.getClassLoader().getResource("org/youscope/server/YSConfig_Empty.cfg").openStream());
			}
			catch(Exception e)
			{
				throw new MicroscopeConfigurationException(e);
			}
		}

		microscope.lockExclusiveWrite(accessID);
		try
		{
			// parse the configuration.
			ServerSystem.out.println("Loading microscope configuration...");
			lastParseResult = (new ConfigFileParser(microscope, channelManager, pixelSizeManager)).parseConfigFile(configurationReader, accessID);
			ServerSystem.out.println("Finished loading microscope configuration.");

			// Set the system startup settings.
			ServerSystem.out.println("Applying startup settings.");
			try
			{
				microscope.applyDeviceSettings(microscope.getMicroscopeConfiguration().getSystemStartupSettings(), accessID);
			}
			catch(SettingException e)
			{
				throw new MicroscopeException("Could not apply startup settings.", e);
			}
			ServerSystem.out.println("Applied startup settings.");
		}
		finally
		{
			microscope.unlockExclusiveWrite(accessID);
		}

		return lastParseResult.getWarningMessage();
	}

	public void saveConfiguration(Writer configurationWriter, int accessID) throws MicroscopeConfigurationException, MicroscopeLockedException
	{
		(new ConfigFileWriter(microscope, channelManager, pixelSizeManager)).writeConfigFile(configurationWriter, accessID);
	}

	public ConfigFileParseResult getLastParseResult()
	{
		return lastParseResult;
	}
}
