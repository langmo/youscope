/**
 * 
 */
package ch.ethz.csb.youscope.server.microscopeaccess;

import ch.ethz.csb.youscope.shared.microscope.MicroscopeException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;

/**
 * @author Moritz Lang
 *
 */
public interface SerialDeviceInternal extends DeviceInternal
{

	/**
	 * Sends a serial command to the corresponding port
	 * @param command The command to send. Use only ASCI
	 * @param accessID The access ID.
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	void sendCommand(String command, int accessID) throws MicroscopeLockedException, MicroscopeException, InterruptedException;
}
