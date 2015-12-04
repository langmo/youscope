/**
 * 
 */
package org.youscope.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.Date;
import java.util.ServiceLoader;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.youscope.addon.measurement.MeasurementAddonFactory;
import org.youscope.addon.microplate.MicroplateAddonFactory;
import org.youscope.addon.postprocessing.PostProcessorAddonFactory;
import org.youscope.addon.tool.ToolAddonFactory;
import org.youscope.common.YouScopeMessageListener;
/**
 * Static class providing similar functionality as System.out.println() and System.err.println(), only that the messages are displayed
 * inside YouScope instead of in the console. Since we do not want to display thousand lines of "unimportant debug messages", we only use
 * ClientSystem.out.println and ClientSystem.err.println to display information interesting for the user.
 * @author Moritz Lang
 */
class ClientSystem
{
    private static final String fileNameOut = "log_client.txt";

    private static final String fileNameErr = "error_client.txt";

    private Vector<YouScopeMessageListener> messageErrListener =
            new Vector<YouScopeMessageListener>();

    private Vector<YouScopeMessageListener> messageOutListener =
            new Vector<YouScopeMessageListener>();

    private PrintStream logOut = null;

    private PrintStream logErr = null;

    private static ClientSystem clientSystem = null;

    public static final ClientMessageStream out = new ClientMessageStream();

    public static final ClientErrorStream err = new ClientErrorStream();

    private YouScopeMessageListener microscopeListener = null;

    static
    {
    	System.setErr(new PrintStream(new SystemErrListener(), false));
    }
    
    /**
     * Output stream to replace System.err.
     * An implementation of an output stream that flushes its buffer at least one second after the last message arrived.
     * This is a solution for that errors are written out continuously, whereas we see an error as one block
     * (one error can produce many System.err.println() calls).
     * @author Moritz Lang
     *
     */
    private static class SystemErrListener extends OutputStream
    {
    	private final StringBuffer buffer = new StringBuffer();
		private volatile long lastIncome = 0;
		
		public SystemErrListener()
		{
			Timer timer = new Timer("System.err listener", true);
	        timer.schedule(new TimerTask()
	            {
	                @Override
	                public void run()
	                {
	                	synchronized(buffer)
	        			{
	                		if (buffer.length() == 0 || System.currentTimeMillis() - lastIncome < 1000)
	                			return;
	                		flush();
	        			}
	                }
	            }, 1000, 1000);
		}
		
		@Override
		public void close()
		{
		    // Do nothing.
		}

		@Override
		public void flush() 
		{
			String message;
			synchronized(buffer)
			{
				message = buffer.toString();
				buffer.setLength(0);
			}
			ClientSystem.err.println("Unexpected error occured.", new Exception(message));
	    	
		}

		@Override
		public void write(int val) 
		{
		    write(new byte[]{(byte)val},0,1);
		}

		@Override
		public void write(byte[] ba) 
		{
		    write(ba,0,ba.length);
		}

		@Override
		public void write(byte[] ba,int str,int len) 
		{
			synchronized(buffer)
			{
				lastIncome = System.currentTimeMillis();
				buffer.append(new String(ba, str, len));
			}
		}
    }
    
    private ClientSystem()
    {
        try
        {
            logOut = new PrintStream(fileNameOut);
        } 
        catch (FileNotFoundException e)
        {
            System.err.println("Could not set output logging file ("+e.getMessage()+").");
        }
        try
        {
            logErr = new PrintStream(fileNameErr);
        } 
        catch (FileNotFoundException e)
        {
            System.err.println("Could not set error logging file ("+e.getMessage()+")");
        }
    }
    
    public static File getLogFile()
    {
    	return new File(fileNameOut);
    }

    private static ClientSystem getClientSystem()
    {
        synchronized (ClientSystem.class)
        {
            if (clientSystem == null)
                clientSystem = new ClientSystem();
        }
        return clientSystem;
    }

    static class ClientMessageStream
    {
        private ClientMessageStream()
        {
            // Do nothing.
        }

        public void println(String message)
        {
            getClientSystem().printMessage(message, null);
        }
        
        public void println(String message, Date time)
        {
            getClientSystem().printMessage(message, time);
        }
    }

    static class ClientErrorStream
    {
        private ClientErrorStream()
        {
            // Do nothing.
        }

        public void println(String message)
        {
            getClientSystem().printError(message, null, null);
        }

        public void println(String message, Throwable e)
        {
            getClientSystem().printError(message, e, null);
        }
        public void println(String message, Throwable e, Date time)
        {
            getClientSystem().printError(message, e, time);
        }
    }

    private void printMessage(String message, Date time)
    {
    	if(message == null)
    		return;
    	if(time == null)
    		time = new Date();
    	
        // Print message in file
        if (logOut != null)
        {
        	String NL = System.getProperty("line.separator");
        	logOut.println(time.toString() + ": " +message.replace("\n", NL +"\t"));
        }

        // Print message to listeners.

        for (int i = 0; i < messageOutListener.size(); i++)
        {
            YouScopeMessageListener listener = messageOutListener.elementAt(i);
            try
            {
                listener.consumeMessage(message, time);
            } catch (@SuppressWarnings("unused") RemoteException e)
            {
                // Remove the listener
                messageOutListener.removeElementAt(i);
            }
        }
    }

    private void printError(String message, Throwable e, Date time)
    {
        if (message == null)
            return;
        if(time == null)
    		time = new Date();
        
        if(logErr != null)
        {
        	String NL = System.getProperty("line.separator");
        	NL = (NL == null? "\n" : NL);
	        String errorMessage = 
	        	 "************************************************************************" + NL
	        	+"Error occured." + NL
	        	+"Time: " +time.toString() +NL
	        	+"Message: " + message.replace("\n", NL+"\t\t")+NL;
	        if (e != null)
	        {
	        	errorMessage += "Cause: ";
	        	for (Throwable throwable = e; throwable != null; throwable = throwable.getCause())
	            {
	                if (throwable.getMessage() != null)
	                	errorMessage +=
	                            "\t" + throwable.getClass().getName() +": " + throwable.getMessage().replace("\n", NL+"\t\t") + NL;
	            }
	        	errorMessage+="Stack: " +NL;
	        	for (Throwable throwable = e; throwable != null; throwable = throwable.getCause())
	            {
	        		errorMessage+= "\t"+throwable.getClass().getName()+": " +NL;
	        		StackTraceElement[] stacks = throwable.getStackTrace();
	                for(StackTraceElement stack : stacks)
	                {
	                	errorMessage += "\t\t" + stack.toString() + NL;
	                }
	            }
	        }
	        errorMessage+=NL;
	
	        // Print message in file
	        if (logErr != null)
	        	logErr.println(errorMessage);
        }

        // Print message to listeners.
        for (int i = 0; i < messageErrListener.size(); i++)
        {
            YouScopeMessageListener listener = messageErrListener.elementAt(i);
            try
            {
                listener.consumeError(message, e, time);
            } catch (@SuppressWarnings("unused") RemoteException e1)
            {
                // Remove the listener
                messageErrListener.removeElementAt(i);
                i--;
            }
        }
        
    }

    static void addMessageOutListener(YouScopeMessageListener listener)
    {
        synchronized (ClientSystem.class)
        {
            getClientSystem().messageOutListener.add(listener);
        }
    }

    static void addMessageErrListener(YouScopeMessageListener listener)
    {
        synchronized (ClientSystem.class)
        {
            getClientSystem().messageErrListener.add(listener);
        }
    }

    static YouScopeMessageListener getMicroscopeMessageListener()
    {
        synchronized (ClientSystem.class)
        {
            if (getClientSystem().microscopeListener == null)
            {
                try
                {
                    class MicroscopeListener extends UnicastRemoteObject implements
                            YouScopeMessageListener
                    {
                        /**
						 * 
						 */
                        private static final long serialVersionUID = 2670267144687981490L;

                        MicroscopeListener() throws RemoteException
                        {
                            super();
                        }

                        @Override
                        public void consumeMessage(String message, Date time) throws RemoteException
                        {
                            ClientSystem.out.println(message, time);
                        }

                        @Override
                        public void consumeError(String message, Throwable exception, Date time)
                                throws RemoteException
                        {
                            ClientSystem.err.println(message, exception, time);
                        }
                    }
                    getClientSystem().microscopeListener = new MicroscopeListener();
                } catch (RemoteException e)
                {
                    // Should not happen
                    ClientSystem.err.println("Could not create listener for microscope.", e);
                }
            }
        }
        return getClientSystem().microscopeListener;
    }

    public static boolean isLocalServer()
    {
        try
        {
            byte[] serverIP = YouScopeClientImpl.getServerConfiguration().getIP();
            byte[] clientIP = InetAddress.getLocalHost().getAddress();
            return Arrays.equals(serverIP, clientIP);
        } 
        catch (Exception e1)
        {
            ClientSystem.err
                    .println(
                            "Could not detect if server and client are on the same computer. Assuming different computers.", e1);
            return false;
        } 
    }
    
    static Iterable<ScriptEngineFactory> getScriptEngines()
    {
    	ScriptEngineManager mgr = new ScriptEngineManager(ClientSystem.class.getClassLoader());
        return mgr.getEngineFactories();
    }
    
    static ScriptEngineFactory getScriptEngine(String engineName)
    {
    	for(ScriptEngineFactory factory : getScriptEngines())
    	{
    		if(factory.getEngineName().compareTo(engineName) == 0)
    			return factory;
    	}
    	return null;
    }
        
    static Iterable<MeasurementAddonFactory> getMeasurementAddons()
    {
        ServiceLoader<MeasurementAddonFactory> jobAddons =
                ServiceLoader.load(MeasurementAddonFactory.class,
                        ClientSystem.class.getClassLoader());
        return jobAddons;
    }
    
    static Iterable<PostProcessorAddonFactory> getMeasurementPostProcessorAddons()
    {
        ServiceLoader<PostProcessorAddonFactory> addons =
                ServiceLoader.load(PostProcessorAddonFactory.class,
                        ClientSystem.class.getClassLoader());
        return addons;
    }
    
    static Iterable<ToolAddonFactory> getToolAddons()
    {
        ServiceLoader<ToolAddonFactory> toolAddons =
                ServiceLoader.load(ToolAddonFactory.class,
                        ClientSystem.class.getClassLoader());
        return toolAddons;
    }
    
    static MeasurementAddonFactory getMeasurementAddon(String addonID)
    {
        for (MeasurementAddonFactory addon : getMeasurementAddons())
        {
        	if(addon.isSupportingTypeIdentifier(addonID))
                return addon;
        }
        return null;
    }
    
    static PostProcessorAddonFactory getMeasurementPostProcessorAddon(String addonID)
    {
    	for (PostProcessorAddonFactory addon : getMeasurementPostProcessorAddons())
        {
        	if(addon.supportsPostProcessorID(addonID))
                return addon;
        }
        return null;
    }
    
    static ToolAddonFactory getToolAddon(String addonID)
    {
        for (ToolAddonFactory addon : getToolAddons())
        {
        	if(addon.supportsToolID(addonID))
                return addon;
        }
        return null;
    }
    
    static Iterable<MicroplateAddonFactory> getMicroplateTypeAddons()
	{
    	ServiceLoader<MicroplateAddonFactory> microplateTypeAddons =
            ServiceLoader.load(MicroplateAddonFactory.class,
                    ClientSystem.class.getClassLoader());
    	return microplateTypeAddons;
	}

	
	static MicroplateAddonFactory getMicroplateTypeAddon(String addonID)
	{
		for (MicroplateAddonFactory addon : getMicroplateTypeAddons())
        {
        	if(addon.supportsMicroplateID(addonID))
                return addon;
        }
        return null;
	}

	public static class YouScopeUncaughtExceptionHandler  implements Thread.UncaughtExceptionHandler
	{
		@Override
		public void uncaughtException(Thread thread, Throwable e)
		{
			err.println("Uncaught Exception in thread " + thread.getName() + ".", e);
		}
		public void handle(Throwable e) 
		{
		   // for EDT exceptions
			err.println("Uncaught Exception in AWT event dispatch thread " + Thread.currentThread().getName() + ".", e);
		}
	}	
}
