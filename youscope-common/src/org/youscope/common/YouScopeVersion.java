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
/* Copyright 2012 ETH Zuerich, CISD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */

package org.youscope.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract of all classes providing build and environment information.
 * <p>
 * Does <em>not</em> depend on any library jar files.
 * </p>
 * 
 * @author Franz-Josef Elmer
 */
public class YouScopeVersion
{
	private static final String	UNKNOWN	= "UNKNOWN";

	private final static String		DEVELOPER_VERSION = "2.1.0-beta.1";

	private final static String		PRODUCT_VERSION = "R2017"; 

	private final static String		APPLICATION_NAME = "YouScope";
	
	private final static boolean PRE_RELEASE = false;

	/**
	 * Constructor.
	 * @param APPLICATION_NAME The part of YouScope (client, server,...). for which the Version should be returned.
	 */
	private YouScopeVersion()
	{
		// static functions only
	}

	private final static String getProperty(final String property)
	{
		return System.getProperty(property, UNKNOWN);
	}

	private final static boolean isPropertyUnknown(final String property)
	{
		return property.equals(UNKNOWN);
	}

	/**
	 * @return Name of the CPU architecture.
	 */
	public static final String getCPUArchitecture()
	{
		return getProperty("os.arch");
	}

	/**
	 * @return Name and version of the operating system.
	 */
	public static final String getOS()
	{
		final String osName = getProperty("os.name");
		final String osVersion = getProperty("os.version");
		if(isPropertyUnknown(osName) || isPropertyUnknown(osVersion))
		{
			return osName;
		}
		return osName + " (v" + osVersion + ")";
	}

	/**
	 * @return Name and version of the Java Virtual Machine.
	 */
	public static final String getJavaVM()
	{
		final String vmName = getProperty("java.vm.name");
		final String vmVersion = getProperty("java.vm.version");
		if(isPropertyUnknown(vmName) || isPropertyUnknown(vmVersion))
		{
			return vmName;
		}
		return vmName + " (v" + vmVersion + ")";
	}

	/**
	 * @return The version of the software.
	 */
	public static final String getProductVersion()
	{
		return PRODUCT_VERSION;
	}
	/**
	 * Returns true if the current YouScope version corresponds to a pre-release.
	 * @return True if pre-release, false if regular release.
	 */
	public static boolean isPreRealease()
	{
		return PRE_RELEASE;
	}
	
	/**
	 * Returns the version accompanied by the build number of the software.
	 * @return Full version.
	 */
	public static final String getFullVersion()
	{
		final StringBuilder builder = new StringBuilder(getProductVersion());
		if(PRE_RELEASE)
			builder.append("*");
		return builder.append(" [build: ").append(getDeveloperVersion()).append("]").toString();
	}

	/**
	 * Returns the name of the (sub-) application/library which is versioned.
	 * @return Name of library/application.
	 */
	public static String getApplicationName()
	{
		return APPLICATION_NAME;
	}

	/**
	 * Returns version, build number, Java VM, and OS as a {@link List} with four entries.
	 * @return List of environmental variables.
	 */
	public static final List<String> getEnvironmentInfo()
	{
		final List<String> environmentInfo = new ArrayList<String>();
		environmentInfo.add("Application: " + getApplicationName());
		environmentInfo.add("Version: " + getFullVersion());
		environmentInfo.add("Java VM: " + getJavaVM());
		environmentInfo.add("CPU Architecture: " + getCPUArchitecture());
		environmentInfo.add("OS: " + getOS());
		return environmentInfo;
	}

	/**
	 * Returns version, build number, Java VM, and OS in a four-liner as one {@link String}.
	 */
	@Override
	public final String toString()
	{
		final StringBuilder builder = new StringBuilder();
		final List<String> environmentInfo = getEnvironmentInfo();
		final int n = environmentInfo.size();
		for(int i = 0; i < n; i++)
		{
			builder.append(environmentInfo.get(i));
			if(i < n - 1)
			{
				builder.append(System.getProperty("line.separator"));
			}
		}
		return builder.toString();
	}

	/**
	 * @return Major version number.
	 */
	public static String getDeveloperVersion()
	{
		return DEVELOPER_VERSION;
	}
}
