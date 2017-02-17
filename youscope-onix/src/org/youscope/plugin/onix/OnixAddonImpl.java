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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import org.youscope.common.MessageListener;
import org.youscope.common.util.RMIReader;

import onix.ONIXDevice;

/**
 * YouScope addon to control the CellAsic Onix microfluidic device.
 * @author Moritz Lang
 *
 */
class OnixAddonImpl extends UnicastRemoteObject implements OnixAddon
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -2309519217082272377L;
	private static volatile boolean nativeLibraryLoaded = false;
	
	/**
	 * Static reference to onix device driver. The device is probably accessed via static calls to the respective native library.
	 * As far as I understand it, constructing new objects of the onix device class for everybody accessing the device doesn't make sense,
	 * and thus we access the device by the same object, independent who is using the plugin.
	 */
	private static volatile ONIXDevice onixDevice = null;
	
	private static volatile boolean protocolRunning = false;
	private static volatile boolean abortProtocol = false;
	
	private static final ArrayList<MessageListener> messageListeners = new ArrayList<MessageListener>();
	
	private static final String NATIVE_LIBRARY_NAME = "K8055D";
	
	private static volatile PWMXRunner pwmxRunner = null;
	private static volatile PWMYRunner pwmyRunner = null;
	
	/**
	 * Constructor.
	 * @throws RemoteException
	 */
	public OnixAddonImpl() throws RemoteException
	{
		super();
	}

	@Override
	public String getAddonName() throws RemoteException
	{
		return "CellAsic Onix microfluidic controller.";
	}

	@Override
	public float getAddonVersion() throws RemoteException
	{
		return 0.1f;
	}

	@Override
	public String getAddonDescription() throws RemoteException
	{
		return "This addon allows to control the microfluidic device Onix fron CellAsic.";
	}

	@Override
	public String getAddonID() throws RemoteException
	{
		return "YouScope.OnixController";
	}

	@Override
	public void initialize() throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice != null)
			{
				sendMessage("Onix device already connected.");
				return;
			}
			
			// Load onix native driver library.
			if(!nativeLibraryLoaded)
			{
				File onixLibrary;
				try
				{
					onixLibrary = new File("onix/"+NATIVE_LIBRARY_NAME+".dll").getCanonicalFile();
				}
				catch(IOException e)
				{
					throw new OnixException("Could not detect onix native library file.", e);
				}
				if(!onixLibrary.exists())
					throw new OnixException("Onix native device library " + onixLibrary.toString() + "does not exist.");
				try
	            {
					System.load(onixLibrary.getAbsolutePath());
	            }
	            catch(Throwable e)
	            {
	            	throw new OnixException("Could not load Onix temporary native library file \"" + onixLibrary.getAbsolutePath() +"\".", e);
	            }
	            nativeLibraryLoaded = true;
			}
			
			pwmxRunner = new PWMXRunner();
			pwmyRunner = new PWMYRunner();
			
			// create onix driver object
			onixDevice = new ONIXDevice();
			boolean result = onixDevice.reconnect();
			if(result)
				sendMessage("Onix device connected and initialized.");
			else
				sendMessage("Onix could not connect.");
		}
	}

	@Override
	public void uninitialize() throws RemoteException, OnixException
	{
		stopPWMX();
		stopPWMY();
	}

	@Override
	public float getXPressure() throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			return onixDevice.getXoutput();
		}
	}

	@Override
	public float getXPressureSetpoint() throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			return onixDevice.getxPressure();
		}
	}

	@Override
	public float getYPressure() throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			return onixDevice.getYoutput();
		}
	}

	@Override
	public float getYPressureSetpoint() throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			return onixDevice.getyPressure();
		}
	}

	@Override
	public boolean isConnected() throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			return onixDevice.isConnected();
		}
	}

	@Override
	public boolean isPlateSealed() throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			return !onixDevice.isInput1();
		}
	}
	
	@Override
	public boolean isVacuumReady() throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			return !onixDevice.isInput2();
		}
	}
	
	@Override
	public boolean isUnknown1Alright() throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			return !onixDevice.isInput2();
		}
	}
	
	@Override
	public boolean isUnknown2Alright() throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			return !onixDevice.isInput3();
		}
	}
	
	@Override
	public boolean isOn() throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			return onixDevice.isOn();
		}
	}

	@Override
	public boolean isValve(int valve) throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			switch(valve)
			{
				case 0:
					return onixDevice.isValve1();
				case 1:
					return onixDevice.isValve2();
				case 2:
					return onixDevice.isValve3();
				case 3:
					return onixDevice.isValve4();
				case 4:
					return onixDevice.isValve5();
				case 5:
					return onixDevice.isValve6();
				case 6:
					return onixDevice.isValve7();
				case 7:
					return onixDevice.isValve8();
				default:
					throw new OnixException("Number of input valve must be in  between 0 and 7. Current input: " + Integer.toString(valve) + ".");
			}
		}
	}

	@Override
	public boolean reconnect() throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			boolean result = onixDevice.reconnect();
			if(result)
				sendMessage("Onix reconnected.");
			else
				sendMessage("Onix could not reconnect.");
			return result;
		}
	}

	@Override
	public void setXPressureSetpoint(float pressure) throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			if(pressure < 0.25 || pressure > 10)
				throw new IllegalArgumentException("Pressure must be between 0.25 and 10 psi.");
			if(getXPressureSetpoint() != pressure)
			{
				onixDevice.setFlow("x", pressure);
				sendMessage("X-Pressure set to " + Float.toString(pressure) + "psi.");
			}
		}
	}
	
	@Override
	public void setYPressureSetpoint(float pressure) throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			if(pressure < 0.25 || pressure > 10)
				throw new IllegalArgumentException("Pressure must be between 0.25 and 10 psi.");
			if(getYPressureSetpoint() != pressure)
			{
				onixDevice.setFlow("y", pressure);
				sendMessage("Y-Pressure set to " + Float.toString(pressure) + "psi.");
			}
		}
	}

	@Override
	public void setSwitch() throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			onixDevice.setSwitch();
			sendMessage("Onix switched (whatever this means).");
		}
	}

	@Override
	public void setValve(int valve, boolean active) throws RemoteException, OnixException
	{
		// we start counting with 0, CellAsic with 1...
		if(valve < 0 || valve > 7)
			throw new OnixException("Number of input valve be inbetween 0 and 7. Current input: " + Integer.toString(valve) + ".");
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			
			// reduce amount of pulses on channel, as well as unnecessary messages.
			if(isValve(valve) == active)
				return;
			
			onixDevice.setValve(valve + 1, active);
			sendMessage("Valve V" + Integer.toString(valve + 1) + (active? " activated." : " deactivated."));
		}
	}

	@Override
	public void showNativeUI() throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			onix.Main onixMain = onix.Main.getApplication();
			onixMain.show();
		}
	}

	@Override
	public void addMessageListener(MessageListener listener) throws RemoteException
	{
		synchronized(messageListeners)
		{
			messageListeners.add(listener);
		}
	}

	@Override
	public void removeMessageListener(MessageListener listener) throws RemoteException
	{
		synchronized(messageListeners)
		{
			messageListeners.remove(listener);
		}
	}
	
	private static void sendMessage(String message)
	{
		synchronized(messageListeners)
		{
			for(int i=0; i<messageListeners.size(); i++)
			{
				MessageListener listener = messageListeners.get(i);
				try
				{
					listener.sendMessage(message);
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					// remove listener and continue.
					messageListeners.remove(i);
					i--;
				}
			}
		}
	}

	@Override
	public void runProtocol(RMIReader protocolReader) throws OnixException, OnixProtocolRunningException
	{
		if(protocolReader == null)
			throw new NullPointerException("Protocol is null.");
		
		ArrayList<String> lineList = new ArrayList<String>();
		BufferedReader lineReader = new BufferedReader(protocolReader);
		while(true)
		{
			String line;
			try
			{
				line = lineReader.readLine();
			}
			catch(IOException e)
			{
				throw new OnixException("Could not read in protocol.", e);
			}
            if (line == null)
            {
                break;
            }
            // remove whitespaces
            lineList.add(line.trim());
		}
		
		synchronized(OnixAddonImpl.class)
		{
			if(protocolRunning)
				throw new OnixProtocolRunningException();
			protocolRunning = true;
			abortProtocol = false;
		}
		
		class ProtocolExecuter implements Runnable
		{
			private final String[] protocol;
			ProtocolExecuter(String[] protocol)
			{
				this.protocol = protocol;
			}
			@Override
			public void run()
			{
				int success = 0;
				try
				{
					success = runProtocolInternally(protocol, 0);
					/*stopPWMX();
            		stopPWMY();
					for(int i=0; i<8; i++)
            		{
            			setValve(i, false);
            		}*/
				}
				catch(Exception e)
				{
					sendMessage("Could not close valve at end of protocol: " + e.getMessage());
				}
				finally
				{
					synchronized(OnixAddonImpl.class)
					{
						protocolRunning = false;
					}
				}
				if(success < 0)
					sendMessage("Execution of Onix protocol was stopped due to an error.");
				else if(abortProtocol)
					sendMessage("Execution of Onix protocol interrupted.");
				else
					sendMessage("Onix protocol executed successfully.");
			}
		}
		
		new Thread(new ProtocolExecuter(lineList.toArray(new String[0]))).start();
	}
	
	@Override
	public void runProtocolAndWait(RMIReader protocolReader) throws RemoteException, OnixException, OnixProtocolRunningException
	{
		if(protocolReader == null)
			throw new NullPointerException("Protocol is null.");
		
		ArrayList<String> lineList = new ArrayList<String>();
		BufferedReader lineReader = new BufferedReader(protocolReader);
		while(true)
		{
			String line;
			try
			{
				line = lineReader.readLine();
			}
			catch(IOException e)
			{
				throw new OnixException("Could not read in protocol.", e);
			}
            if (line == null)
            {
                break;
            }
            // remove whitespaces
            lineList.add(line.trim());
		}
		
		synchronized(OnixAddonImpl.class)
		{
			if(protocolRunning)
				throw new OnixProtocolRunningException();
			protocolRunning = true;
			abortProtocol = false;
		}
		int success;
		try
		{
			success = runProtocolInternally(lineList.toArray(new String[0]), 0);
			/*stopPWMX();
    		stopPWMY();
			for(int i=0; i<8; i++)
    		{
    			setValve(i, false);
    		}*/
		}
		finally
		{
			synchronized(OnixAddonImpl.class)
			{
				protocolRunning = false;
			}
		}
		if(success < 0)
			sendMessage("Execution of Onix protocol was stopped due to an error.");
		else if(abortProtocol)
			sendMessage("Execution of Onix protocol interrupted.");
		else
			sendMessage("Onix protocol executed successfully.");
	}
	
	/**
	 * Runs the protocol, starting at the given line number.
	 * Stops when encountering an end without a repeat before, at the end of the protocol, or if an error occurs.
	 * @param protocol the protocol to execute.
	 * @param lineNumber the line number to start execution at.
	 * @return the last line which was successfully executed, or -1 if an error occurred.
	 */
	private int runProtocolInternally(String[] protocol, int lineNumber)
	{
		try
		{
	    	for(;lineNumber < protocol.length; lineNumber++)
	        {
	    		if(abortProtocol)
	    			break;
	    		// Split line in single commands
	            String[] tokens = protocol[lineNumber].split(" ");
	            
	            // get first token/the command
	            int tokenID = nextToken(tokens, 0);
	            
	            // remove empty lines.
	            if(tokenID == -1)
	            	continue;
	            
	            String command = tokens[tokenID].toLowerCase();
	            
	            // remove comments
	            if(command.startsWith("%"))
	            	continue;
	            
	            // iterate over all possible commands
	            if(command.equals("end"))
	            {
	            	// protocol should stop
	            	break;
	            }
	            else if(command.equals("setflow"))
	            {
	            	tokenID = nextToken(tokens, tokenID + 1);
	            	if(tokenID < 0)
	            		throw new OnixException("Command setflow must be followed by \"X\" or \"Y\".");
	            	String xOrY = tokens[tokenID].toLowerCase();
	            	boolean isX;
	            	if(xOrY.equals("x"))
	            		isX = true;
	            	else if(xOrY.equals("y"))
	            		isX = false;
	            	else
	            		throw new OnixException("Command setflow must be followed by \"X\" or \"Y\".");
	            	
	            	tokenID = nextToken(tokens, tokenID + 1);
	            	if(tokenID < 0)
	            		throw new OnixException("Command setflow must be followed by \"X\" or \"Y\", and then the flow rate (0.25-10).");
	            	float flowRate;
	            	try
	            	{
	            		flowRate = Float.parseFloat(tokens[tokenID]); 
	            	}
	            	catch(NumberFormatException e)
	            	{
	            		throw new OnixException("Command setflow must be followed by \"X\" or \"Y\", and then the flow rate (0.25-10).", e);
	            	}
	            	
	            	// set flow
	            	if(isX)
	            		setXPressureSetpoint(flowRate);
	            	else
	            		setYPressureSetpoint(flowRate);
	            }
	            else if(command.equals("open"))
	            {
	            	tokenID = nextToken(tokens, tokenID + 1);
	            	if(tokenID < 0)
	            		throw new OnixException("Command \"open\" must be followed by \"V1\"-\"V8\", or by \"all\".");
	            	String valve = tokens[tokenID].toLowerCase();
	            	if(valve.equals("all"))
	            	{
	            		for(int i=0; i<8; i++)
	            		{
	            			setValve(i, true);
	            		}
	            	}
	            	else if(valve.startsWith("v"))
	            	{
	            		int valveNum;
	            		try
	            		{
	            			valveNum = Integer.parseInt(valve.substring(1)) - 1;
	            		}
	            		catch(NumberFormatException e)
	            		{
	            			throw new OnixException("Command \"open\" must be followed by \"V1\"-\"V8\", or by \"all\".", e);
	            		}
	            		setValve(valveNum, true);
	            	}
	            	else
	            	{
	            		throw new OnixException("Command \"open\" must be followed by \"V1\"-\"V8\", or by \"all\".");
	            	}
	            }
	            else if(command.equals("close"))
	            {
	            	tokenID = nextToken(tokens, tokenID + 1);
	            	if(tokenID < 0)
	            		throw new OnixException("Command \"close\" must be followed by \"V1\"-\"V8\", or by \"all\".");
	            	String valve = tokens[tokenID].toLowerCase();
	            	if(valve.equals("all"))
	            	{
	            		stopPWMX();
	            		stopPWMY();
	            		for(int i=0; i<8; i++)
	            		{
	            			setValve(i, false);
	            		}
	            	}
	            	else if(valve.startsWith("v"))
	            	{
	            		int valveNum;
	            		try
	            		{
	            			valveNum = Integer.parseInt(valve.substring(1)) - 1;
	            		}
	            		catch(NumberFormatException e)
	            		{
	            			throw new OnixException("Command \"close\" must be followed by \"V1\"-\"V8\", or by \"all\".", e);
	            		}
	            		setValve(valveNum, false);
	            	}
	            	else
	            	{
	            		throw new OnixException("Command \"close\" must be followed by \"V1\"-\"V8\", or by \"all\".");
	            	}
	            }
	            else if(command.equals("wait"))
	            {
	            	tokenID = nextToken(tokens, tokenID + 1);
	            	if(tokenID < 0)
	            		throw new OnixException("Command \"wait\" must be followed by a time in minutes (float).");
	            	double waitTime;
	            	try
	            	{
	            		waitTime = Double.parseDouble(tokens[tokenID]);
	            	}
	            	catch(NumberFormatException e)
	            	{
	            		throw new OnixException("Command \"wait\" must be followed by a time in minutes (float).", e);
	            	}
	            	
	            	Thread.sleep((long)(1000* 60 * waitTime));
	            }
	            else if(command.equals("pwmx"))
	            {
	            	tokenID = nextToken(tokens, tokenID + 1);
	            	if(tokenID < 0)
	            		throw new OnixException("Command \"pwmx\" must be either followed by an interger variable indicating the time in ms and a float (>=0, <=1) indicating the mixture, or by -1 to stop the modulation.");
	            	long waitTime;
	            	try
	            	{
	            		waitTime = Long.parseLong(tokens[tokenID]);
	            	}
	            	catch(NumberFormatException e)
	            	{
	            		throw new OnixException("Command \"pwmx\" must be followed by a time in ms (integer) or -1 as a first argument.", e);
	            	}
	            	if(waitTime <= 0)
	            		stopPWMX();
	            	else
	            	{
	            		tokenID = nextToken(tokens, tokenID + 1);
	            		double fraction1;
	            		try
		            	{
	            			fraction1 = Double.parseDouble(tokens[tokenID]);
		            	}
		            	catch(NumberFormatException e)
		            	{
		            		throw new OnixException("Command \"pwmx\" must be followed by a fraction in ms as a second argument.", e);
		            	}
		            	startPWMX(waitTime, fraction1);
	            	}
	            }
	            else if(command.equals("pwmy"))
	            {
	            	tokenID = nextToken(tokens, tokenID + 1);
	            	if(tokenID < 0)
	            		throw new OnixException("Command \"pwmy\" must be either followed by an integer variable indicating the time in ms and a four floats f3-f6 (>=0, <=1) indicating the mixture, or by -1 to stop the modulation.");
	            	long waitTime;
	            	try
	            	{
	            		waitTime = Long.parseLong(tokens[tokenID]);
	            	}
	            	catch(NumberFormatException e)
	            	{
	            		throw new OnixException("Command \"pwmx\" must be followed by a time in ms (integer) or -1 as a first argument.", e);
	            	}
	            	if(waitTime <= 0)
	            		stopPWMX();
	            	else
	            	{
	            		tokenID = nextToken(tokens, tokenID + 1);
	            		double fraction3;
	            		try
		            	{
	            			fraction3 = Double.parseDouble(tokens[tokenID]);
		            	}
		            	catch(NumberFormatException e)
		            	{
		            		throw new OnixException("Command \"pwmy\" must be either followed by an integer variable indicating the time in ms and a four floats f3-f6 (>=0, <=1) indicating the mixture, or by -1 to stop the modulation.", e);
		            	}
		            	
		            	tokenID = nextToken(tokens, tokenID + 1);
	            		double fraction4;
	            		try
		            	{
	            			fraction4 = Double.parseDouble(tokens[tokenID]);
		            	}
		            	catch(NumberFormatException e)
		            	{
		            		throw new OnixException("Command \"pwmy\" must be either followed by an integer variable indicating the time in ms and a four floats f3-f6 (>=0, <=1) indicating the mixture, or by -1 to stop the modulation.", e);
		            	}
		            	
		            	tokenID = nextToken(tokens, tokenID + 1);
	            		double fraction5;
	            		try
		            	{
	            			fraction5 = Double.parseDouble(tokens[tokenID]);
		            	}
		            	catch(NumberFormatException e)
		            	{
		            		throw new OnixException("Command \"pwmy\" must be either followed by an integer variable indicating the time in ms and a four floats f3-f6 (>=0, <=1) indicating the mixture, or by -1 to stop the modulation.", e);
		            	}
		            	
		            	tokenID = nextToken(tokens, tokenID + 1);
	            		double fraction6;
	            		try
		            	{
	            			fraction6 = Double.parseDouble(tokens[tokenID]);
		            	}
		            	catch(NumberFormatException e)
		            	{
		            		throw new OnixException("Command \"pwmy\" must be either followed by an integer variable indicating the time in ms and a four floats f3-f6 (>=0, <=1) indicating the mixture, or by -1 to stop the modulation.", e);
		            	}
		            	
		            	
		            	startPWMY(waitTime, fraction3, fraction4, fraction5, fraction6);
	            	}
	            }
	            else if(command.equals("repeat"))
	            {
	            	tokenID = nextToken(tokens, tokenID + 1);
	            	int repeatNum = -1;
	            	if(tokenID >= 0)
	            	{
		            	try
		            	{
		            		repeatNum = Integer.parseInt(tokens[tokenID]);
		            	}
		            	catch(NumberFormatException e)
		            	{
		            		throw new OnixException("Command \"repeat\" must be either followed by nothing (infinite loop), or an integer number of repeats.", e);
		            	}
	            	}
	            	int nextLine = lineNumber;
	            	for(int i = 0; repeatNum < 0 || i < repeatNum; i++)
	            	{
	            		nextLine = runProtocolInternally(protocol, lineNumber+1);
	            		// Stop outer loop if error in inner loop.
	            		if(nextLine < 0)
	            			return -1;
	            	}
	            	lineNumber = nextLine;
	            }
	            else
	            	throw new OnixException("Command \"" + command + " unknown.");
	        }
		}
		catch(Exception e)
		{
			sendMessage("Error in protocol line " + Integer.toString(lineNumber + 1) + " ("+protocol[lineNumber]+"): " + e.getMessage());
			return -1;
		}
		return lineNumber;
	}
	private static int nextToken(String[] tokens, int tokenStart)
	{
		for(int i=tokenStart; i<tokens.length; i++)
		{
			if(tokens[i].length() > 0)
				return i;
		}
		return -1;
	}

	@Override
	public void stopProtocol()
	{
		abortProtocol = true;
	}

	@Override
	public boolean isProtocolRunning()
	{
		return protocolRunning;
	}
	
	private class PWMXRunner implements Runnable
	{
		private volatile long periodMS = -1;
		private volatile double fraction1 = 0;
		private volatile boolean running = false;
		
		public boolean isPWM()
		{
			return running;
		}
		public void startPWM(long periodMS, double fraction1)
		{
			if(periodMS <= 0)
				throw new IllegalArgumentException("The period of the pulse width modulaion must be at least one millisecond.");
			if(fraction1 < 0 || fraction1 > 1)
				throw new IllegalArgumentException("The fraction of valve 1 must be in between 0 and 1.");
			
			synchronized(this)
			{
				this.fraction1 = fraction1;
				this.periodMS = periodMS;
				if(!running)
				{
					running = true;
					new Thread(this).start();
				}
			}
		}
		
		public synchronized void stopPWM()
		{
			periodMS = -1;
		}
		
		@Override
		public void run()
		{
			while(true)
			{
				// make local copies
				long periodMS;
				double fraction1;
				synchronized(this)
				{
					periodMS = this.periodMS;
					fraction1 = this.fraction1;
					
					// if period is smaller than zero, this indicates that we should stop.
					if(this.periodMS <= 0)
					{
						running = false;
						break;
					}
				}
				
				try
				{
					if(!isVacuumReady())
						sendMessage("Warning: vacuum not ready!");
					
					if(fraction1 > 0)
					{
						setValve(1, false);
						setValve(0, true);
						Thread.sleep((long)(periodMS * fraction1));
						if(this.periodMS <= 0)
						{
							running = false;
							break;
						}
					}
					if(fraction1 < 1)
					{
						setValve(0, false);
						setValve(1, true);
						Thread.sleep((long)(periodMS * (1.0-fraction1)));
						if(this.periodMS <= 0)
						{
							running = false;
							break;
						}
					}
				}
				catch(Exception e)
				{
					sendMessage("Error in running pulse-wide modulation. Stop execution. Reason: " + e.getMessage());
					stopPWM();
				}
			}
			// close both valves at end?
			/*try
			{
				setValve(0, false);
				setValve(1, false);
			}
			catch(Exception e)
			{
				sendMessage("Could not close valves at end of pulse width modulation. Leaving valves open. Reason: " + e.getMessage());
			}*/
		}
	}

	@Override
	public void startPWMX(long periodMS, double fraction1) throws RemoteException, OnixException, OnixProtocolRunningException
	{
		float pressure;
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			
			pwmxRunner.startPWM(periodMS, fraction1);
			pressure = getXPressure();
		}
		
		sendMessage("PWMX started (T="+Long.toString(periodMS) + "ms, f=" + Double.toString(fraction1) + "). Current pressure: " + Float.toString(pressure) + "psi.");
	}

	@Override
	public void stopPWMX() throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			
			pwmxRunner.stopPWM();
		}
		
		sendMessage("PWMX stopped.");
	}

	@Override
	public boolean isPWMX() throws RemoteException, OnixException
	{
		return pwmxRunner.isPWM();
	}
	
	
	
	
	
	private class PWMYRunner implements Runnable
	{
		private volatile long periodMS = -1;
		private volatile double fraction3 = 0;
		private volatile double fraction4 = 0;
		private volatile double fraction5 = 0;
		private volatile double fraction6 = 0;
		private volatile boolean running = false;
		
		public boolean isPWM()
		{
			return running;
		}
		public void startPWM(long periodMS, double fraction3, double fraction4, double fraction5, double fraction6)
		{
			if(periodMS <= 0)
				throw new IllegalArgumentException("The period of the pulse width modulaion must be at least one millisecond.");
			if(fraction3 < 0 || fraction3 > 1 || fraction4 < 0 || fraction4 > 1 || fraction5 < 0 || fraction5 > 1 || fraction6 < 0 || fraction6 > 1)
				throw new IllegalArgumentException("The fractions of valve 3-6 must be in between 0 and 1.");
			double sumFractions = fraction3 + fraction4 + fraction5 + fraction6;
			if(sumFractions == 0)
				throw new IllegalArgumentException("At least one of the fractions must be greater than zero.");
			synchronized(this)
			{
				this.fraction3 = fraction3 / sumFractions;
				this.fraction4 = fraction4 / sumFractions;
				this.fraction5 = fraction5 / sumFractions;
				this.fraction6 = fraction6 / sumFractions;
				this.periodMS = periodMS;
				if(!running)
				{
					running = true;
					new Thread(this).start();
				}
			}
		}
		
		public synchronized void stopPWM()
		{
			periodMS = -1;
		}
		
		@Override
		public void run()
		{
			while(true)
			{
				// make local copies
				long periodMS;
				double fraction3;
				double fraction4;
				double fraction5;
				double fraction6;
				synchronized(this)
				{
					periodMS = this.periodMS;
					fraction3 = this.fraction3;
					fraction4 = this.fraction4;
					fraction5 = this.fraction5;
					fraction6 = this.fraction6;
					
					// if period is smaller than zero, this indicates that we should stop.
					if(this.periodMS <= 0)
					{
						running = false;
						break;
					}
				}
				
				try
				{
					if(!isVacuumReady())
						sendMessage("Warning: vacuum not ready!");
					
					if(fraction3 > 0)
					{
						setValve(3, false);
						setValve(4, false);
						setValve(5, false);
						setValve(2, true);
						Thread.sleep((long)(periodMS * fraction3));
						if(this.periodMS <= 0)
						{
							running = false;
							break;
						}
					}
					
					if(fraction4 > 0)
					{
						setValve(2, false);
						setValve(4, false);
						setValve(5, false);
						setValve(3, true);
						Thread.sleep((long)(periodMS * fraction4));
						if(this.periodMS <= 0)
						{
							running = false;
							break;
						}
					}
					
					if(fraction5 > 0)
					{
						setValve(2, false);
						setValve(3, false);
						setValve(5, false);
						setValve(4, true);
						Thread.sleep((long)(periodMS * fraction5));
						if(this.periodMS <= 0)
						{
							running = false;
							break;
						}
					}
					
					if(fraction6 > 0)
					{
						setValve(2, false);
						setValve(3, false);
						setValve(4, false);
						setValve(5, true);
						Thread.sleep((long)(periodMS * fraction6));
						if(this.periodMS <= 0)
						{
							running = false;
							break;
						}
					}
				}
				catch(Exception e)
				{
					sendMessage("Error in running pulse-wide modulation. Stop execution. Reason: " + e.getMessage());
					stopPWM();
				}
			}
			// close both valves at end?
			/*try
			{
				setValve(firstValve, false);
				setValve(secondValve, false);
			}
			catch(Exception e)
			{
				sendMessage("Could not close valves at end of pulse width modulation. Leaving valves open. Reason: " + e.getMessage());
			}*/
		}
	}

	@Override
	public void startPWMY(long periodMS, double fraction3, double fraction4, double fraction5, double fraction6) throws RemoteException, OnixException, OnixProtocolRunningException
	{
		// Check variables
		if(fraction3<0 || fraction3>1 || fraction4<0 || fraction4>1 || fraction5<0 || fraction5>1 || fraction6<0 || fraction6>1)
			throw new OnixException("For PWMY, all fractions must be greater equal to zero and smaller equal to one.");
		
		double sumFractions = fraction3 + fraction4 + fraction5 + fraction6;
		if(sumFractions == 0)
			throw new OnixException("For PWMY, at least one fraction must be non-zero.");
		float pressure;
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			
			pwmyRunner.startPWM(periodMS, fraction3, fraction4, fraction5, fraction6);
			pressure = getYPressure();
		}
		
		sendMessage("PWMY started (T="+Long.toString(periodMS) + "ms, f3=" + Double.toString(fraction3 / sumFractions) + ", f4=" + Double.toString(fraction4 / sumFractions) + ", f5=" + Double.toString(fraction5 / sumFractions) + ", f6=" + Double.toString(fraction6 / sumFractions) + "). Current pressure: " + Float.toString(pressure) + "psi.");
	}

	@Override
	public void stopPWMY() throws RemoteException, OnixException
	{
		synchronized(OnixAddonImpl.class)
		{
			if(onixDevice == null)
				throw new OnixException("OnixAddon.initialize() must be called before any other method.");
			
			pwmyRunner.stopPWM();
		}
		sendMessage("PWMY stopped.");
	}

	@Override
	public boolean isPWMY() throws RemoteException, OnixException
	{
		return pwmyRunner.isPWM();
	}
}
