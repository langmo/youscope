/**
 * 
 */
package ch.ethz.csb.youscope.server.microscopeaccess.access14;

import mmcorej.CMMCore;
import mmcorej.CharVector;
import ch.ethz.csb.youscope.server.microscopeaccess.SerialDeviceInternal;
import ch.ethz.csb.youscope.shared.microscope.DeviceType;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeException;
import ch.ethz.csb.youscope.shared.microscope.MicroscopeLockedException;

/**
 * @author Moritz Lang
 *
 */
public class SerialDeviceImpl extends DeviceImpl implements SerialDeviceInternal 
{
	private final static String PROPERTY_SEND_COMMAND = "Send Command";
	
	SerialDeviceImpl(MicroscopeImpl microscope, String deviceName, String libraryID, String driverID)
	{
		super(microscope, deviceName, libraryID, driverID, DeviceType.SerialDevice);
	}

	@Override
	protected void initializeDevice(int accessID) throws MicroscopeException
	{
		super.initializeDevice(accessID);
		
		// Add some additional properties...
		properties.put(PROPERTY_SEND_COMMAND, new StringPropertyImpl(microscope, getDeviceID(), PROPERTY_SEND_COMMAND, false, this)
		{
			@Override
			public String getValue() throws MicroscopeException, InterruptedException
			{
				return "";
			}
			
			@Override
			protected void setStringValue(String value, int accessID) throws MicroscopeException, MicroscopeLockedException, InterruptedException
			{
				sendCommand(value, accessID);
			}
		});
	}
	/**
	 * Sends a serial command to the corresponding port
	 * @param command The command to send. Use only ASCI
	 * @param accessID The access ID.
	 * @throws MicroscopeLockedException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 */
	@Override
	public void sendCommand(String command, int accessID) throws MicroscopeLockedException, MicroscopeException, InterruptedException
	{
		if(Thread.interrupted())
			throw new InterruptedException();
		try
		{
			CMMCore core = microscope.startWrite(accessID);
			try
			{
				CharVector chars = new CharVector();
				for(char aChar : command.toCharArray())
				{
					chars.add(aChar);
				}
				core.writeToSerialPort(getDeviceID(), chars);
				core.waitForDevice(getDeviceID());
			}
			catch(Exception e)
			{
				throw new MicroscopeException("Could not send serial command \"" + command + "\" to port " + getDeviceID() + ".", e);
			}
		}
		finally
		{
			deviceStateModified();
			microscope.unlockWrite();
		}
		microscope.stateChanged("Serial command \"" + command + "\" sent to port " + getDeviceID() + ".");
	}
}
