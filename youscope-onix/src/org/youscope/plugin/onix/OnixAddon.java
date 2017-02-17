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
package org.youscope.plugin.onix;

import java.rmi.RemoteException;

import org.youscope.addon.serveraddon.ServerAddon;
import org.youscope.common.MessageListener;
import org.youscope.common.util.RMIReader;

/**
 * Addon allowing to control the CellAsic Onix microfluidic device.
 * @author Moritz Lang
 *
 */
public interface OnixAddon extends ServerAddon
{
	/**
	 * Initialize the onix device. Must be called before any other function of this device.
	 * Calling it more than once has no effect.
	 * @throws RemoteException
	 * @throws OnixException
	 */
	public void initialize() throws RemoteException, OnixException;
	/**
	 * Uninitialize the onix device. After calling this function, no other function should be called until initialize is called again.
	 * Calling this function more than once has no effect. 
	 * @throws RemoteException
	 * @throws OnixException
	 * @throws OnixProtocolRunningException 
	 */
	public void uninitialize() throws RemoteException, OnixException, OnixProtocolRunningException;
	
	/**
	 * Adds a listener which gets informed about the actions the Onix device does.
	 * @param listener Listener to be added.
	 * @throws RemoteException
	 */
	public void addMessageListener(MessageListener listener) throws RemoteException;
	
	/**
	 * Removes a previously added listener.
	 * @param listener Listener to be removed.
	 * @throws RemoteException
	 */
	public void removeMessageListener(MessageListener listener) throws RemoteException;
	
	/**
	 * Returns the x-pressure.
	 * @return x-pressure;
	 * @throws RemoteException 
	 * @throws OnixException 
	 */
	float getXPressure() throws RemoteException, OnixException;
	
	/**
	 * Returns the y-pressure.
	 * @return y-pressure;
	 * @throws RemoteException 
	 * @throws OnixException 
	 */
	float getYPressure() throws RemoteException, OnixException;
	/**
	 * Returns true if the connection to the onix device is established.
	 * @return true if connected.
	 * @throws RemoteException 
	 * @throws OnixException 
	 */
	boolean isConnected() throws RemoteException, OnixException;
		
	/**
	 * Have no idea yet what this device function is doing...
	 * @return no idea.
	 * @throws RemoteException 
	 * @throws OnixException 
	 */
	boolean isOn() throws RemoteException, OnixException;
	/**
	 * Returns if given valve is active.
	 * @param valve Number of the valve (0<=valve<=7)
	 * @return True, if valve active
	 * @throws RemoteException 
	 * @throws OnixException 
	 */
	boolean isValve(int valve) throws RemoteException, OnixException;
	
	/**
	 * Reconnects to the device.
	 * @return True if successfull.
	 * @throws RemoteException 
	 * @throws OnixException 
	 * @throws OnixProtocolRunningException 
	 */
	boolean reconnect() throws RemoteException, OnixException, OnixProtocolRunningException;
	
	/**
	 * No idea about the functionality behind this function. Try out what happens!
	 * @throws RemoteException 
	 * @throws OnixException 
	 * @throws OnixProtocolRunningException 
	 */
	void setSwitch() throws RemoteException, OnixException, OnixProtocolRunningException;
	
	/**
	 * Sets if a given valve is active
	 * @param valve the number of the valve.
	 * @param active true if active
	 * @throws RemoteException 
	 * @throws OnixException 
	 * @throws OnixProtocolRunningException 
	 */
	void setValve(int valve, boolean active) throws RemoteException, OnixException, OnixProtocolRunningException;
	
	/**
	 * Displays the native Onix UI from CellAsic. Should only be called for debugging reasons.
	 * @throws RemoteException
	 * @throws OnixException
	 */
	void showNativeUI() throws RemoteException, OnixException;
	/**
	 * Returns the x-pressure setpoint.
	 * @return x-pressure.
	 * @throws RemoteException
	 * @throws OnixException
	 */
	float getXPressureSetpoint() throws RemoteException, OnixException;
	/**
	 * Returns the y-pressure setpoint.
	 * @return y-pressure setpoint.
	 * @throws RemoteException
	 * @throws OnixException
	 */
	float getYPressureSetpoint() throws RemoteException, OnixException;
	/**
	 * Returns true if the plate is sealed.
	 * @return TRUE if plate is sealed.
	 * @throws RemoteException
	 * @throws OnixException
	 */
	boolean isPlateSealed() throws RemoteException, OnixException;
	/**
	 * Returns true if vacuum is ready.
	 * @return TRUE if vacuum is ready.
	 * @throws RemoteException
	 * @throws OnixException
	 */
	boolean isVacuumReady() throws RemoteException, OnixException;
	/**
	 * Yet, I don't know what this is returning. However, if it returns false, probably something is not
	 * ready and operation shouldn't start.
	 * @return True if something yet not known is ready...
	 * @throws RemoteException
	 * @throws OnixException
	 */
	boolean isUnknown1Alright() throws RemoteException, OnixException;
	/**
	 * Yet, I don't know what this is returning. However, if it returns false, probably something is not
	 * ready and operation shouldn't start.
	 * @return True if something yet not known is ready...
	 * @throws RemoteException
	 * @throws OnixException
	 */
	boolean isUnknown2Alright() throws RemoteException, OnixException;
	/**
	 * Sets the pressure setpoint in x.
	 * @param pressure Pressure setpoint.
	 * @throws RemoteException
	 * @throws OnixException
	 * @throws OnixProtocolRunningException 
	 */
	void setXPressureSetpoint(float pressure) throws RemoteException, OnixException, OnixProtocolRunningException;
	/**
	 * Sets the pressure setpoint in y.
	 * @param pressure Pressure setpoint.
	 * @throws RemoteException
	 * @throws OnixException
	 * @throws OnixProtocolRunningException 
	 */
	void setYPressureSetpoint(float pressure) throws RemoteException, OnixException, OnixProtocolRunningException;
	
	/**
	 * Runs a standard CellAsic Onix protocol, utilizing the given reader (e.g. a FileReader). Each line of the protocol must either contain a valid command, a comment, or must be empty.
	 * The protocol is run in a separate thread. To stop the execution of the protocol before completion, call stopProtocol().
	 * @param protocolReader Reader which supplies the protocol.
	 * 
	 * @param configurationReader A reader to read in the configuration, e.g. a file reader.
	 * @throws RemoteException
	 * @throws OnixException 
	 * @throws OnixProtocolRunningException 
	 */
	void runProtocol(RMIReader protocolReader) throws RemoteException, OnixException, OnixProtocolRunningException;
	
	/**
	 * Runs a standard CellAsic Onix protocol, utilizing the given reader (e.g. a FileReader). Each line of the protocol must either contain a valid command, a comment, or must be empty.
	 * The protocol is run in the current thread. To stop the execution of the protocol before completion, call stopProtocol().
	 * @param protocolReader Reader which supplies the protocol.
	 * 
	 * @param configurationReader A reader to read in the configuration, e.g. a file reader.
	 * @throws RemoteException
	 * @throws OnixException 
	 * @throws OnixProtocolRunningException 
	 */
	void runProtocolAndWait(RMIReader protocolReader) throws RemoteException, OnixException, OnixProtocolRunningException;
	
	/**
	 * Stops a currently executed protocol at the next possible step. Has no effect if no protocol is running.
	 * Note that the success of this method is not guaranteed.
	 * @throws RemoteException
	 * @throws OnixException
	 */
	void stopProtocol() throws RemoteException, OnixException;
	
	/**
	 * Returns true if currently a protocol is running (which blocks the device), and false otherwise.
	 * @return True if a protocol is running.
	 * @throws RemoteException
	 * @throws OnixException
	 */
	boolean isProtocolRunning() throws RemoteException, OnixException;
	
	/**
	 * Starts a pulse-width modulation between the two wells 1 and 2 with the X pressure. The pulse width has a given period periodMS in milliseconds.
	 * During the fraction1 * periodMS milliseconds, valve 1 is opened and valve 2 is closed, and during (1-fraction1) * periodMS milliseconds vice versa.
	 * If the low pass assumption holds, the resulting medium will be a mixture of fraction1 of the medium of well 1 and (1-fraction1) of well 2.
	 * 
	 * Note that the time the wells are open are rounded to the next integer value of milliseconds. The period should thus be not too small, otherwise the
	 * true fraction will deviate from the setpoint. Note also that the total period might be slightly larger, since the time to open and close the valves is not taken care of.
	 * 
	 * @param periodMS The period in milliseconds. Must be positive. 
	 * @param fraction1 The fraction of time (0<=fraction1<=1) well 1 is active during the period.
	 * @throws RemoteException
	 * @throws OnixException
	 * @throws OnixProtocolRunningException
	 */
	public void startPWMX(long periodMS, double fraction1) throws RemoteException, OnixException, OnixProtocolRunningException;
	
	/**
	 * Stops a pulse-width modulation which was previously started.
	 * @throws RemoteException
	 * @throws OnixException
	 * @throws OnixProtocolRunningException
	 */
	public void stopPWMX() throws RemoteException, OnixException, OnixProtocolRunningException;
	
	/**
	 * Returns true if currently a pulse-width modulation between well 1 and 2 is running.
	 * @return True if pulse width modulation is running.
	 * @throws RemoteException
	 * @throws OnixException 
	 */
	public boolean isPWMX() throws RemoteException, OnixException;
	
	/**
	 * Starts a pulse-width modulation between valves 3 to valve 6 with the Y pressure. The pulse width has a given period periodMS in milliseconds.
	 * During fraction3/sum(fractions) * periodMS milliseconds, valve 3 is opened, during fraction4/sum(fractions) * periodMS milliseconds, valve 4 is opened, and so on. 
	 * All fractions must be between zero and one, with at least one non-zero fraction. Ideally, the fractions sum up to one. However, due to limited preciseness of double values, all four values must be set.
	 * If the low pass assumption holds, the resulting medium will be a mixture corresponding to the fractions.
	 * 
	 * Note that the time the wells are open are rounded to the next integer value of milliseconds. The period should thus be not too small, otherwise the
	 * true fraction will deviate from the setpoint. Note also that the total period might be slightly larger, since the time to open and close the valves is not taken care of.
	 * 
	 * @param periodMS The period in milliseconds. Must be positive. 
	 * @param fraction3 The fraction of time (0<=fraction3<=1) valve 3 is open during the period.
	 * @param fraction4 The fraction of time (0<=fraction4<=1) valve 4 is open during the period.
	 * @param fraction5 The fraction of time (0<=fraction5<=1) valve 5 is open during the period.
	 * @param fraction6 The fraction of time (0<=fraction6<=1) valve 6 is open during the period.
	 * @throws RemoteException
	 * @throws OnixException
	 * @throws OnixProtocolRunningException
	 */
	public void startPWMY(long periodMS, double fraction3, double fraction4, double fraction5, double fraction6) throws RemoteException, OnixException, OnixProtocolRunningException;

	/**
	 * Stops a pulse-width modulation which was previously started.
	 * @throws RemoteException
	 * @throws OnixException
	 * @throws OnixProtocolRunningException
	 */
	public void stopPWMY() throws RemoteException, OnixException, OnixProtocolRunningException;

	/**
	 * Returns true if currently a pulse-width modulation in Y is running.
	 * @return True if pulse width modulation is running.
	 * @throws RemoteException
	 * @throws OnixException 
	 */
	public boolean isPWMY() throws RemoteException, OnixException;
}
