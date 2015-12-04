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
import org.youscope.common.YouScopeMessageListener;
import org.youscope.serverinterfaces.GeneralPurposeAddon;

/**
 * @author Moritz Lang
 */
final class ServerSystem
{
	private static final String								fileNameOut			= "log_server.txt";

	private static final String								fileNameErr			= "error_server.txt";

	private static volatile Vector<YouScopeMessageListener>	messageErrListener	= new Vector<YouScopeMessageListener>();

	private static volatile Vector<YouScopeMessageListener>	messageOutListener	= new Vector<YouScopeMessageListener>();

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
				YouScopeMessageListener listener = messageOutListener.elementAt(i);
				try
				{
					listener.consumeMessage(message, time);
				}
				catch(@SuppressWarnings("unused") RemoteException e)
				{
					// Remove the listener
					messageOutListener.removeElementAt(i);
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
				YouScopeMessageListener listener = messageErrListener.elementAt(i);
				try
				{
					listener.consumeError(message, e, time);
				}
				catch(@SuppressWarnings("unused") RemoteException e1)
				{
					// Remove the listener
					messageErrListener.removeElementAt(i);
					i--;
				}
			}
		}
	}

	static void addMessageOutListener(YouScopeMessageListener listener)
	{
		synchronized(messageOutListener)
		{
			messageOutListener.add(listener);
		}
	}

	static void addMessageErrListener(YouScopeMessageListener listener)
	{
		synchronized(messageErrListener)
		{
			messageErrListener.add(listener);
		}
	}

	static void removeMessageOutListener(YouScopeMessageListener listener)
	{
		synchronized(messageOutListener)
		{
			messageOutListener.remove(listener);
		}
	}

	static void removeMessageErrListener(YouScopeMessageListener listener)
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

	public static GeneralPurposeAddon[] getGeneralAddons()
	{
		Iterator<GeneralPurposeAddon> addonFactories = ServiceLoader.load(GeneralPurposeAddon.class, ServerSystem.class.getClassLoader()).iterator();
		Vector<GeneralPurposeAddon> result = new Vector<GeneralPurposeAddon>();
		for(; addonFactories.hasNext();)
		{
			result.addElement(addonFactories.next());
		}
		return result.toArray(new GeneralPurposeAddon[0]);
	}

	public static <T extends GeneralPurposeAddon> T getGeneralAddon(Class<T> addonInterface) throws RemoteException
	{
		T[] results = getGeneralAddons(addonInterface);
		if(results.length <= 0)
			return null;
		return results[0];
	}

	
	public static <T extends GeneralPurposeAddon> T[] getGeneralAddons(Class<T> addonInterface) throws RemoteException
	{
		ArrayList<T> result = new ArrayList<T>(10);
		for(GeneralPurposeAddon addon : getGeneralAddons())
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
