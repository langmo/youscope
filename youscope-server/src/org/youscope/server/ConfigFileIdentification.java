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
 * @author langmo
 * 
 */
class ConfigFileIdentification
{
	public final static String					IDENT_GENERATOR			= "Generator";
	public final static String					IDENT_COMPATIBLE		= "Compatible";
	public final static String					IDENT_CORE_VERSION		= "CoreVersion";
	public final static String					IDENT_CORE_API_VERSION	= "CoreAPIVersion";

	public final static ConfigFileGenerator		THIS_GENERATOR			= new ConfigFileGenerator();

	public final static ConfigFileGenerator[]	COMPATIBLE_GENERATORS	= new ConfigFileGenerator[] {new ConfigFileGenerator("YouScope", "1.0"),new ConfigFileGenerator("MicroManager", "1.3"), new ConfigFileGenerator("MicroManager", "1.4")};
}
