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

import mmcorej.CMMCore;
import mmcorej.CharVector;

import org.youscope.addon.microscopeaccess.SerialDeviceInternal;
import org.youscope.common.microscope.DeviceType;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.MicroscopeLockedException;

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
