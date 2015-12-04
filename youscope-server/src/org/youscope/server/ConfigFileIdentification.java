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
