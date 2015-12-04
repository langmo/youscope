/**
 * 
 */
package org.youscope.server;

import java.util.ServiceLoader;

import org.youscope.addon.microscopeaccess.MicroscopeConnectionException;
import org.youscope.addon.microscopeaccess.MicroscopeConnectionFactory;
import org.youscope.addon.microscopeaccess.MicroscopeInternal;

/**
 * Helper class to obtain a plug-in with which the microscope hardware can be accessed.
 * @author Moritz Lang
 */
class MicroscopeAccess
{
	/**
	 * The one and only microscope object.
	 */
	private static MicroscopeInternal	microscope	= null;

	/**
	 * Returns the one and only microscope object. If it doesn't exist yet, it it constructed.
	 * 
	 * @param driverFolder Directory where to find the microscope drivers (plug-in specific).
	 * @param microscopeConnectionID The connection type with which the microscope connection should be established.
	 * @return The one and only microscope object.
	 * @throws MicroscopeConnectionException
	 */
	public static MicroscopeInternal getMicroscope(String microscopeConnectionID, String driverFolder) throws MicroscopeConnectionException
	{
		synchronized(MicroscopeAccess.class)
		{
			if(microscope != null)
				return microscope;
			for(MicroscopeConnectionFactory factory : getMicroscopeConnectionFactories())
			{
				if(!factory.supportsMicroscopeConnectionID(microscopeConnectionID))
					continue;
				microscope = factory.createMicroscopeConnection(microscopeConnectionID, driverFolder);
				return microscope;
			}
		}
		throw new MicroscopeConnectionException("Microscope Driver connection of type " + microscopeConnectionID + " is not available. Please choose another connection type.");
	}

	public static Iterable<MicroscopeConnectionFactory> getMicroscopeConnectionFactories()
	{
		ServiceLoader<MicroscopeConnectionFactory> factories = ServiceLoader.load(MicroscopeConnectionFactory.class, MicroscopeAccess.class.getClassLoader());
		return factories;
	}
}
