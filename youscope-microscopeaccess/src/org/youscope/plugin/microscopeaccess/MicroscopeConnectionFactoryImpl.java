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
package org.youscope.plugin.microscopeaccess;

import java.io.File;

import org.youscope.addon.microscopeaccess.MicroscopeConnectionException;
import org.youscope.addon.microscopeaccess.MicroscopeConnectionFactory;
import org.youscope.addon.microscopeaccess.MicroscopeInternal;

/**
 * @author langmo
 *
 */
public class MicroscopeConnectionFactoryImpl implements MicroscopeConnectionFactory
{
	private final static String CONNECTION_TYPE_STANDALONE = "YouScope.StandAlone";
	private final static String CONNECTION_TYPE_EXTERNAL_WINDOWS = "YouScope.External_Windows";
	private final static String CONNECTION_TYPE_EXTERNAL = "YouScope.External";
	
	// Parameters for standalone type
	private static final String STANDALONE_64_DLL_LOCATION = "drivers64/";
	private static final String STANDALONE_32_DLL_LOCATION = "drivers32/";
	private static final String STANDALONE_POSIX_DLL_LOCATION = "drivers/";
	private static final String STANDALONE_JAR_LOCATION = "lib/MMCoreJ.jar";
	
	// Parameters for external type
	private static final String EXTERNAL_JAR_LOCATION = "plugins/Micro-Manager/MMCoreJ.jar";
	
	@Override
	public MicroscopeInternal createMicroscopeConnection(String microscopeConnectionID, String driverPath) throws MicroscopeConnectionException
	{
		if(microscopeConnectionID.equals(CONNECTION_TYPE_STANDALONE))
		{
			if(isWindows64())
				return ConnectionEstablisher.createMicroscopeConnection(new File(STANDALONE_64_DLL_LOCATION), new File(STANDALONE_JAR_LOCATION), false, false, true);
			else if(isWindows32())
				return ConnectionEstablisher.createMicroscopeConnection(new File(STANDALONE_32_DLL_LOCATION), new File(STANDALONE_JAR_LOCATION), false, false, true);
			else
				return ConnectionEstablisher.createMicroscopeConnection(new File(STANDALONE_POSIX_DLL_LOCATION), new File(STANDALONE_JAR_LOCATION), false, false, true);
		}
		else if(microscopeConnectionID.equals(CONNECTION_TYPE_EXTERNAL_WINDOWS))
		{
			return ConnectionEstablisher.createMicroscopeConnection(new File(driverPath), new File((new File(driverPath)), EXTERNAL_JAR_LOCATION), true, false, true);
		}
		else if(microscopeConnectionID.equals(CONNECTION_TYPE_EXTERNAL))
		{
			return ConnectionEstablisher.createMicroscopeConnection(new File(driverPath), new File((new File(driverPath)), EXTERNAL_JAR_LOCATION), false, false, false);
		}
		else
		{
			throw new MicroscopeConnectionException("Microscope connecting type " + microscopeConnectionID + " is not supported by this plugin.");
		}
	}

	@Override
	public boolean supportsMicroscopeConnectionID(String microscopeConnectionID)
	{
		for(String supportedType : getSupportedMicroscopeConnectionIDs())
		{
			if(microscopeConnectionID.compareTo(supportedType) == 0)
				return true;
		}
		return false;
	}

	static boolean isWindows32()
	{
		// Get Operating system
        String osName = System.getProperty("os.name");
        if (osName.indexOf("Windows") < 0)
        {
        	return false;
        }
        String architecture = System.getProperty("sun.arch.data.model");
        if(architecture.indexOf("32")< 0)
        	return false;
        return true;
	}
	static boolean isWindows64()
	{
		// Get Operating system
        String osName = System.getProperty("os.name");
        if (osName.indexOf("Windows") < 0)
        {
        	return false;
        }
        String architecture = System.getProperty("sun.arch.data.model");
        if(architecture.indexOf("64")< 0)
        	return false;
        return true;
	}
	@Override
	public String[] getSupportedMicroscopeConnectionIDs()
	{
		if(isWindows64())
		{
			if(new File(STANDALONE_64_DLL_LOCATION).exists())
			{
				return new String[]{CONNECTION_TYPE_STANDALONE, CONNECTION_TYPE_EXTERNAL_WINDOWS, CONNECTION_TYPE_EXTERNAL};
			}
			return new String[]{CONNECTION_TYPE_EXTERNAL_WINDOWS, CONNECTION_TYPE_EXTERNAL};	
		}
		else if(isWindows32())
		{
			if(new File(STANDALONE_32_DLL_LOCATION).exists())
			{
				return new String[]{CONNECTION_TYPE_STANDALONE, CONNECTION_TYPE_EXTERNAL_WINDOWS, CONNECTION_TYPE_EXTERNAL};
			}
			return new String[]{CONNECTION_TYPE_EXTERNAL_WINDOWS, CONNECTION_TYPE_EXTERNAL};	
		}
		else if(new File(STANDALONE_POSIX_DLL_LOCATION).exists())
		{
			return new String[]{CONNECTION_TYPE_STANDALONE, CONNECTION_TYPE_EXTERNAL};
		}
		return new String[]{CONNECTION_TYPE_EXTERNAL};
	}

	@Override
	public boolean needsDriverPath(String microscopeConnectionID)
	{
		if(microscopeConnectionID.equals(CONNECTION_TYPE_EXTERNAL_WINDOWS)
				|| microscopeConnectionID.equals(CONNECTION_TYPE_EXTERNAL))
			return true;
		return false;
	}

	@Override
	public String getMicroscopeConnectionDescription(String microscopeConnectionID)
	{
		if(microscopeConnectionID.equals(CONNECTION_TYPE_STANDALONE))
		{
			return "Connects to the microscope directly through Windows 32bit or 64bit drivers or POSIX drivers supplied with YouScope.\nThe drivers wich are selected (either in drivers32/ or drivers64/ for Windows, or drivers/ for POSIX) depend on the started Java Virtual machine.\n"
				+ "No additional installations necessary.";
		}
		else if(microscopeConnectionID.equals(CONNECTION_TYPE_EXTERNAL_WINDOWS))
		{
			return "Connects to the microscope through drivers supplied by an external MicroManager 1.3/1.4 installation.\n"
				+ "This connection type is a convenient variant for Windows based systems of the more general type \"" + getShortMicroscopeConnectionDescription(CONNECTION_TYPE_EXTERNAL)
				+ "\", which makes it unneccessary to add the MicroManager base folder to the system's path. "
				+ "If you have problems using this method, consider using the more general one.\n"
				+ "The MicroManager base folder has to be selected in the respective field.\n"
				+ "The location of the base folder is typically "
				+ "\"C:\\Program Files\\Micro-Manager-X\\\" or "
				+ "\"C:\\Program Files (x86)\\Micro-Manager-X\\\", where X is the MicroManager version.\n"
				+ "Please be aware that the 32 bit (64 bit) version of YouScope can only "
				+ "connect to the 32 bit (64 bit) version of MicroManager.";
		}
		else if(microscopeConnectionID.equals(CONNECTION_TYPE_EXTERNAL))
		{
			return "Connects to the microscope through drivers supplied by an external MicroManager 1.3/1.4 installation. This connection method is suited for any OS.\n"
				+ "The MicroManager base folder (the main folder of the MicroManager installation) "
				+ "has to be added to the PATH variable of the operating system.\n"
				+ "Furthermore, the same folder has to be selected in the respective field.\n"
				+ "Please be aware that the 32 bit (64 bit) version of YouScope can only "
				+ "connect to the 32 bit (64 bit) version of MicroManager.";
		}
		return "";
	}

	@Override
	public String getShortMicroscopeConnectionDescription(String microscopeConnectionID)
	{
		if(microscopeConnectionID.equals(CONNECTION_TYPE_STANDALONE))
		{
			return "YouScope standalone (standard).";
		}
		else if(microscopeConnectionID.equals(CONNECTION_TYPE_EXTERNAL_WINDOWS))
		{
			return "Use drivers of independent MicroManager installation (Windows only).";
		}
		else if(microscopeConnectionID.equals(CONNECTION_TYPE_EXTERNAL))
		{
			return "Use drivers of independent MicroManager installation (all OS, advanced).";
		}
		else
			return "";
	}
}
