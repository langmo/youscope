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
package org.youscope.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.youscope.common.MessageListener;
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

    private ArrayList<MessageListener> messageErrListener =
            new ArrayList<MessageListener>();

    private ArrayList<MessageListener> messageOutListener =
            new ArrayList<MessageListener>();

    private PrintStream logOut = null;

    private PrintStream logErr = null;

    private static ClientSystem clientSystem = null;

    public static final ClientMessageStream out = new ClientMessageStream();

    public static final ClientErrorStream err = new ClientErrorStream();

    private MessageListener microscopeListener = null;

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
            MessageListener listener = messageOutListener.get(i);
            try
            {
                listener.sendMessage(message);
            } catch (@SuppressWarnings("unused") RemoteException e)
            {
                // Remove the listener
                messageOutListener.remove(i);
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
            MessageListener listener = messageErrListener.get(i);
            try
            {
                listener.sendErrorMessage(message, e);
            } catch (@SuppressWarnings("unused") RemoteException e1)
            {
                // Remove the listener
                messageErrListener.remove(i);
                i--;
            }
        }
        
    }

    static void addMessageOutListener(MessageListener listener)
    {
        synchronized (ClientSystem.class)
        {
            getClientSystem().messageOutListener.add(listener);
        }
    }

    static void addMessageErrListener(MessageListener listener)
    {
        synchronized (ClientSystem.class)
        {
            getClientSystem().messageErrListener.add(listener);
        }
    }

    static MessageListener getMicroscopeMessageListener()
    {
        synchronized (ClientSystem.class)
        {
            if (getClientSystem().microscopeListener == null)
            {
                try
                {
                    class MicroscopeListener extends UnicastRemoteObject implements
                            MessageListener
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
						public void sendMessage(String message) throws RemoteException
                        {
                            ClientSystem.out.println(message);
                        }

                        @Override
						public void sendErrorMessage(String message, Throwable error) throws RemoteException
                        {
                            ClientSystem.err.println(message, error);
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
