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

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Vector;

import javax.script.ScriptEngineManager;

import org.youscope.addon.measurement.MeasurementAddonFactory;
import org.youscope.addon.serveraddon.ServerAddon;
import org.youscope.common.MessageListener;

/**
 * @author Moritz Lang
 */
final class ServerSystem
{
	private static final String								fileNameOut			= "log_server.txt";

	private static final String								fileNameErr			= "error_server.txt";

	private static volatile ArrayList<MessageListener>	messageErrListener	= new ArrayList<MessageListener>();

	private static volatile ArrayList<MessageListener>	messageOutListener	= new ArrayList<MessageListener>();

	private static PrintStream								logOut				= null;

	private static PrintStream								logErr				= null;

	private static ServerSystem								serverSystem		= null;

	public static final ServerOutStream					out					= new ServerOutStream();

	public static final ServerErrorStream					err					= new ServerErrorStream();
	
	static class ServerOutStream
	{
		private ServerOutStream()
		{
			// Do nothing.
		}

		public void println(String message)
		{
			getServerSystem().printMessage(message, null);
		}
		public void println(String message, Date time)
		{
			getServerSystem().printMessage(message, time);
		}
	}

	static class ServerErrorStream
	{
		private ServerErrorStream()
		{
			// Do nothing.
		}

		public void println(String message, Throwable e)
		{
			getServerSystem().printError(message, e, null);
		}

		public void println(String message, Throwable e, Date time)
		{
			getServerSystem().printError(message, e, time);
		}
	}

	private ServerSystem()
	{
		try
		{
			logOut = new PrintStream(fileNameOut);
		}
		catch(@SuppressWarnings("unused") FileNotFoundException e)
		{
			System.err.println("Could not set output logging file!");
		}
		try
		{
			logErr = new PrintStream(fileNameErr);
		}
		catch(@SuppressWarnings("unused") FileNotFoundException e)
		{
			System.err.println("Could not set error logging file!");
		}
	}

	private static ServerSystem getServerSystem()
	{
		synchronized(ServerSystem.class)
		{
			if(serverSystem == null)
				serverSystem = new ServerSystem();
		}
		return serverSystem;
	}

	

	private void printMessage(String message, Date time)
	{
		if(time == null)
			time = new Date();
		// Print message in console
		System.out.println(message);

		// Print message in file
		if(logOut != null)
		{
			String NL = System.getProperty("line.separator");
			logOut.println(time.toString() + ": " + message.replace("\n", NL + "\t"));
		}

		// Print message to listeners.
		synchronized(messageOutListener)
		{
			for(int i = 0; i < messageOutListener.size(); i++)
			{
				MessageListener listener = messageOutListener.get(i);
				try
				{
					listener.sendMessage(message);
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					// Remove the listener
					messageOutListener.remove(i);
				}
			}
		}
	}

	private void printError(String message, Throwable e, Date time)
	{
		if(message == null)
			return;
		if(time == null)
			time = new Date();
		String fullMessage = message;
		if(e != null)
			fullMessage = message + " (" + e.getMessage() + ")";
		// Print message in console
		System.err.println(fullMessage);

		// Print message in file
		if(logErr != null)
		{
			String NL = System.getProperty("line.separator");
			NL = (NL == null ? "\n" : NL);
			String errorMessage = "************************************************************************" + NL + "Error occured." + NL + "Time: " + time.toString() + NL + "Message: " + message.replace("\n", NL + "\t\t") + NL;
			if(e != null)
			{
				errorMessage += "Cause: ";
				for(Throwable throwable = e; throwable != null; throwable = throwable.getCause())
				{
					if(throwable.getMessage() != null)
						errorMessage += "\t" + throwable.getClass().getName() + ": " + throwable.getMessage().replace("\n", NL + "\t\t") + NL;
				}
				errorMessage += "Stack: " + NL;
				for(Throwable throwable = e; throwable != null; throwable = throwable.getCause())
				{
					errorMessage += "\t" + throwable.getClass().getName() + ": " + NL;
					StackTraceElement[] stacks = throwable.getStackTrace();
					for(StackTraceElement stack : stacks)
					{
						errorMessage += "\t\t" + stack.toString() + NL;
					}
				}
			}
			errorMessage += NL;

			// Print message in file
			if(logErr != null)
				logErr.println(errorMessage);
		}

		// Print message to listeners.
		synchronized(messageErrListener)
		{
			for(int i = 0; i < messageErrListener.size(); i++)
			{
				MessageListener listener = messageErrListener.get(i);
				try
				{
					listener.sendErrorMessage(message, e);
				}
				catch(@SuppressWarnings("unused") RemoteException e1)
				{
					// Remove the listener
					messageErrListener.remove(i);
					i--;
				}
			}
		}
	}

	static void addMessageOutListener(MessageListener listener)
	{
		synchronized(messageOutListener)
		{
			messageOutListener.add(listener);
		}
	}

	static void addMessageErrListener(MessageListener listener)
	{
		synchronized(messageErrListener)
		{
			messageErrListener.add(listener);
		}
	}

	static void removeMessageOutListener(MessageListener listener)
	{
		synchronized(messageOutListener)
		{
			messageOutListener.remove(listener);
		}
	}

	static void removeMessageErrListener(MessageListener listener)
	{
		synchronized(messageErrListener)
		{
			messageErrListener.remove(listener);
		}
	}

	static ScriptEngineManager getScriptEngineManager()
	{
		return new ScriptEngineManager(ServerSystem.class.getClassLoader());
	}

	static MeasurementAddonFactory getMeasurementAddonFactory(String addonID)
	{
		ServiceLoader<MeasurementAddonFactory> addonFactories = ServiceLoader.load(MeasurementAddonFactory.class, ServerSystem.class.getClassLoader());
		for(MeasurementAddonFactory addon : addonFactories)
		{
			if(addon.isSupportingTypeIdentifier(addonID))
				return addon;
		}
		return null;
	}

	public static ServerAddon[] getGeneralAddons()
	{
		Iterator<ServerAddon> addonFactories = ServiceLoader.load(ServerAddon.class, ServerSystem.class.getClassLoader()).iterator();
		Vector<ServerAddon> result = new Vector<ServerAddon>();
		for(; addonFactories.hasNext();)
		{
			result.addElement(addonFactories.next());
		}
		return result.toArray(new ServerAddon[0]);
	}

	public static <T extends ServerAddon> T getGeneralAddon(Class<T> addonInterface) throws RemoteException
	{
		T[] results = getGeneralAddons(addonInterface);
		if(results.length <= 0)
			return null;
		return results[0];
	}

	
	public static <T extends ServerAddon> T[] getGeneralAddons(Class<T> addonInterface) throws RemoteException
	{
		ArrayList<T> result = new ArrayList<T>(10);
		for(ServerAddon addon : getGeneralAddons())
		{
			if(addonInterface.isInstance(addon))
			{
				result.add(addonInterface.cast(addon));
			}
		}
		
		@SuppressWarnings("unchecked")
		T[] temp = (T[])Array.newInstance(addonInterface, result.size());
		return result.toArray(temp);
	}
}
