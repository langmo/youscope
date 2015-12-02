/**
 * 
 */
package ch.ethz.csb.youscope.server.microscopeaccess.access14;

import java.io.File;

import ch.ethz.csb.youscope.server.microscopeaccess.MicroscopeConnectionFactory;
import ch.ethz.csb.youscope.server.microscopeaccess.MicroscopeConnectionException;
import ch.ethz.csb.youscope.server.microscopeaccess.MicroscopeInternal;

/**
 * @author langmo
 *
 */
public class MicroscopeConnectionFactoryImpl implements MicroscopeConnectionFactory
{
	private final static String CONNECTION_TYPE_14_STANDALONE = "CSB::StandAlone14";
	private final static String CONNECTION_TYPE_14_EXTERNAL_WINDOWS = "CSB::External14_Windows";
	private final static String CONNECTION_TYPE_14_EXTERNAL = "CSB::External14";
	
	// Parameters for standalone type
	private static final String STANDALONE_DLL_LOCATION = "drivers/";
	private static final String STANDALONE_JAR_LOCATION = "drivers/MMCoreJ.jar";
	
	// Parameters for external type
	private static final String EXTERNAL_JAR_LOCATION = "plugins/Micro-Manager/MMCoreJ.jar";
	
	@Override
	public MicroscopeInternal createMicroscopeConnection(String microscopeConnectionID, String driverPath) throws MicroscopeConnectionException
	{
		if(microscopeConnectionID.equals(CONNECTION_TYPE_14_STANDALONE))
		{
			return ConnectionEstablisher.createMicroscopeConnection(new File(STANDALONE_DLL_LOCATION), new File(STANDALONE_JAR_LOCATION), false, false, true);
		}
		else if(microscopeConnectionID.equals(CONNECTION_TYPE_14_EXTERNAL_WINDOWS))
		{
			return ConnectionEstablisher.createMicroscopeConnection(new File(driverPath), new File((new File(driverPath)), EXTERNAL_JAR_LOCATION), true, false, true);
		}
		else if(microscopeConnectionID.equals(CONNECTION_TYPE_14_EXTERNAL))
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

	static boolean isWindows()
	{
		// Get Operating system
        String osName = System.getProperty("os.name");
        if (osName.indexOf("Windows") >= 0)
        {
        	return true;
        }
		return false;
	}
	@Override
	public String[] getSupportedMicroscopeConnectionIDs()
	{
		if(isWindows())
		{
			if(new File(STANDALONE_DLL_LOCATION).exists())
			{
				return new String[]{CONNECTION_TYPE_14_STANDALONE, CONNECTION_TYPE_14_EXTERNAL_WINDOWS, CONNECTION_TYPE_14_EXTERNAL};
			}
			return new String[]{CONNECTION_TYPE_14_EXTERNAL_WINDOWS, CONNECTION_TYPE_14_EXTERNAL};	
		}
		if(new File(STANDALONE_DLL_LOCATION).exists())
		{
			return new String[]{CONNECTION_TYPE_14_STANDALONE, CONNECTION_TYPE_14_EXTERNAL};
		}
		return new String[]{CONNECTION_TYPE_14_EXTERNAL};
	}

	@Override
	public boolean needsDriverPath(String microscopeConnectionID)
	{
		if(microscopeConnectionID.equals(CONNECTION_TYPE_14_EXTERNAL_WINDOWS)
				|| microscopeConnectionID.equals(CONNECTION_TYPE_14_EXTERNAL))
			return true;
		return false;
	}

	@Override
	public String getMicroscopeConnectionDescription(String microscopeConnectionID)
	{
		if(microscopeConnectionID.equals(CONNECTION_TYPE_14_STANDALONE))
		{
			return "Connects to the microscope directly through drivers supplied by YouScope.\n"
				+ "These drivers are the same as provided by μManager 1.4.";
		}
		else if(microscopeConnectionID.equals(CONNECTION_TYPE_14_EXTERNAL_WINDOWS))
		{
			return "Connects to the microscope through drivers supplied by an external μManager 1.3/1.4 installation.\n"
				+ "This connection type is a convenient variant for Windows based systems of the more general type \"" + getShortMicroscopeConnectionDescription(CONNECTION_TYPE_14_EXTERNAL)
				+ "\", which makes it unneccessary to add the μManager base folder to the system's path. "
				+ "If you have problems using this method, consider using the more general one.\n"
				+ "The μManager base folder has to be selected in the respective field.\n"
				+ "The location of the base folder is typically "
				+ "\"C:\\Program Files\\Micro-Manager-X\\\" or "
				+ "\"C:\\Program Files (x86)\\Micro-Manager-X\\\", where X is the μManager version.\n"
				+ "Please be aware that the 32 bit (64 bit) version of YouScope can only "
				+ "connect to the 32 bit (64 bit) version of μManager.";
		}
		else if(microscopeConnectionID.equals(CONNECTION_TYPE_14_EXTERNAL))
		{
			return "Connects to the microscope through drivers supplied by an external μManager 1.3/1.4 installation. This connection method is suited for any OS.\n"
				+ "The μManager base folder (the main folder of the μManager installation) "
				+ "has to be added to the PATH variable of the operating system.\n"
				+ "Furthermore, the same folder has to be selected in the respective field.\n"
				+ "Please be aware that the 32 bit (64 bit) version of YouScope can only "
				+ "connect to the 32 bit (64 bit) version of μManager.";
		}
		return "";
	}

	@Override
	public String getShortMicroscopeConnectionDescription(String microscopeConnectionID)
	{
		if(microscopeConnectionID.equals(CONNECTION_TYPE_14_STANDALONE))
		{
			return "Compatible μManager 1.4, stand-alone installation (standard).";
		}
		else if(microscopeConnectionID.equals(CONNECTION_TYPE_14_EXTERNAL_WINDOWS))
		{
			return "Compatible μManager 1.3/1.4, independent μManager installation (Windows only).";
		}
		else if(microscopeConnectionID.equals(CONNECTION_TYPE_14_EXTERNAL))
		{
			return "Compatible μManager 1.3/1.4, independent μManager installation (all OS).";
		}
		else
			return "";
	}
}
