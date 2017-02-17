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

import org.youscope.common.YouScopeVersion;

/**
 * Holds information which program generated a microscope configuration file.
 * @author Moritz Lang
 * 
 */
class ConfigFileGenerator
{
	/**
	 * Name of the program which generated or might generate a configuration file.
	 */
	public final String	name;
	/**
	 * Version of this generating function.
	 */
	public final String	version;

	/**
	 * Creates a config file generator with the given name and version.
	 * @param name Name of the generating program.
	 * @param version Version of the program.
	 */
	ConfigFileGenerator(String name, String version)
	{
		this.name = name;
		this.version = version;
	}

	/**
	 * Creates a config file generation object representing this YouScope version as generator.
	 */
	ConfigFileGenerator()
	{
		this.name = "YouScope";
		this.version = YouScopeVersion.getDeveloperVersion();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigFileGenerator other = (ConfigFileGenerator) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}
}
