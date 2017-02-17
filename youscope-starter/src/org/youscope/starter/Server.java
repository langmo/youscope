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
package org.youscope.starter;

import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.ServiceConfigurationError;

/**
 * @author langmo
 */
class Server extends ClientServerConnection
{
    private static final String serverJarFileLocation = "server/youscope-server.jar";

    // The name of the main class and the respective interface
    private static final String SERVER_INTERFACE_CLASS =
            "org.youscope.serverinterfaces.YouScopeServer";

    private static final String SERVER_CLASS = "org.youscope.server.YouScopeServerImpl";

    private volatile Method startProgram = null;
    private volatile Method quitProgram = null;
    private volatile Method addServerFinishListener = null;

    private volatile Class<?> serverClass = null;

    private volatile Class<?> serverInterfaceClass = null;

    private volatile Object server = null;

    private volatile boolean isRunning = false;

    @Override
    public synchronized boolean isConnected()
    {
        return server != null;
    }

    public synchronized boolean isRunning()
    {
        return isRunning;
    }

    /**
     * Connects to the server. Same as connectToServer(classLoader, -1)
     * 
     * @param classLoader
     * @param configFile The microscope configuration file address.
     * @param shouldReset True if the configuration where which version of microManager is, should be reset.
     * @throws ConnectionFailedException
     */
    public void connectToServer(ClassLoader classLoader, String configFile, boolean shouldReset) throws ConnectionFailedException
    {
        connectToServer(classLoader, configFile, -1, null, shouldReset);
    }

    public synchronized Class<?> getServerInterfaceClass()
    {
        return serverInterfaceClass;
    }

    public synchronized Object getServer()
    {
        return server;
    }

    @Override
    HashSet<URL> getNecessaryJARs() throws MalformedURLException
    {
        // Get URLs to JAR files.
        URL jarURL = new File(serverJarFileLocation).toURI().toURL();

        HashSet<URL> classLoaderURLs = new HashSet<URL>();
        classLoaderURLs.addAll(getPluginsJars());
        classLoaderURLs.addAll(getLibJars());
        classLoaderURLs.addAll(getSubJars(jarURL, null));
        return classLoaderURLs;
    }

    /**
     * Connects to the server.
     * 
     * @param classLoader
     * @param configFile The microscope configuration file address.
     * @param port If >0, the server will be exported to the registry at the given port such that it can be accessed through the network or a process boundary.
     * @param password If the server gets exported to the registry, it is protected by this password.
     * @param shouldReset True if the configuration where which version of microManager is, should be reset.
     * @throws ConnectionFailedException
     */
    public synchronized void connectToServer(ClassLoader classLoader, String configFile, int port, String password, boolean shouldReset) throws ConnectionFailedException
    {
        // Get URLs to JAR files.
        try
        {
            serverClass = classLoader.loadClass(SERVER_CLASS);
            serverInterfaceClass = classLoader.loadClass(SERVER_INTERFACE_CLASS);
        } 
        catch (ClassNotFoundException e)
        {
            throw new ConnectionFailedException("Could not obtain main class.", e);
        }

        // Get methods of server.
        Method getMainProgram;
        Method startServer;
        Method initializeProgram;
        Method setConfigureMicroscopeConnection;
        try
        {
            getMainProgram = serverClass.getMethod("getMainProgram", new Class<?>[] {});
            getMainProgram.setAccessible(true);
            
            setConfigureMicroscopeConnection = serverClass.getMethod("setConfigureMicroscopeConnection", new Class<?>[] {boolean.class});
            setConfigureMicroscopeConnection.setAccessible(true);

            startServer = serverClass.getMethod("startServer", new Class[] { int.class, String.class });
            startServer.setAccessible(true);
            initializeProgram = serverClass.getMethod("initializeProgram", new Class[] { String.class });
            initializeProgram.setAccessible(true);
            startProgram = serverClass.getMethod("startProgram", new Class[] {});
            startProgram.setAccessible(true);
            quitProgram = serverClass.getMethod("quitProgram", new Class[] {});
            quitProgram.setAccessible(true);
            addServerFinishListener = serverClass.getMethod("addServerFinishListener", new Class[]{ActionListener.class});
            addServerFinishListener.setAccessible(true);
        } 
        catch (SecurityException e)
        {
            throw new ConnectionFailedException("Could not obtain function handler.", e);
        } 
        catch (NoSuchMethodException e)
        {
            throw new ConnectionFailedException("Could not obtain function handler.", e);
        }

        // Invoke methods such that server gets started.
        try
        {
            server = getMainProgram.invoke(null, new Object[] {});
        } 
        catch(InvocationTargetException e)
        {
        	if(e.getCause() != null && e.getCause() instanceof ServiceConfigurationError)
        	{
        		throw new ConnectionFailedException("Could not initialize server since one or more plugins are invalid.\nDetect the respective plugin and remove it from the plugin folder.", e);
        	}
			throw new ConnectionFailedException("Error occured while starting the server.", e);
        }
        catch (Exception e)
        {
            throw new ConnectionFailedException("Could not create server because server is at a different version.\nWe recommend installing YouScope again.", e);
        }  
        if(shouldReset)
        {
        	try
        	{
        		setConfigureMicroscopeConnection.invoke(server, new Object[]{true});
        	}
	        catch(Exception e)
	        {
	            throw new ConnectionFailedException("Could not reset microscope connection type.", e);
	        }
        }
        if (port > 0)
        {
        	try
        	{
        		startServer.invoke(server, new Object[] { port, password });
        	} 
            catch(Exception e)
            {
                throw new ConnectionFailedException("Could not open server ports.", e);
            }
        }
        try
        {
            initializeProgram.invoke(server, new Object[] { configFile });
        } 
        catch(Exception e)
        {
            throw new ConnectionFailedException("Could not initialize microscope with given configuration file.", e);
        }

        
    }

    public void runServer(ActionListener listener) throws IllegalStateException, ConnectionFailedException
    {
        synchronized (this)
        {
            if (!isConnected())
                throw new IllegalStateException("Not connected to server yet.");
            if (isRunning())
                throw new IllegalStateException("Server already running.");
            isRunning = true;
        }
        try
        {
        	addServerFinishListener.invoke(server, new Object[]{listener});
            startProgram.invoke(server, new Object[] {});
        }
        catch (Exception e)
        {
            throw new ConnectionFailedException("Could not start server.", e);
        }
    }
    
    public void quitServer() throws ConnectionFailedException
    {
    	synchronized (this)
        {
            if (!isConnected())
                throw new IllegalStateException("Not connected to server yet.");
        }
        try
        {
            quitProgram.invoke(server, new Object[] {});
        }
        catch (Exception e)
        {
            throw new ConnectionFailedException("Could not quit server.", e);
        }
    }

    @Override
    boolean exists()
    {
        return new File(serverJarFileLocation).exists();
    }
}
