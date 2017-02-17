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
 * @author Moritz Lang
 */
class Client extends ClientServerConnection
{

    private static final String clientJarFileLocation = "client/youscope-client.jar";

    private volatile Method startProgram = null;
    private volatile Method addClientFinishListener = null;

    private volatile Object client = null;

    private volatile Class<?> clientClass = null;

    private volatile boolean connectToServer = false;

    private static final String CLIENT_CLASS = "org.youscope.client.YouScopeClientImpl";

    @Override
    public synchronized boolean isConnected()
    {
        return client != null;
    }

    public synchronized boolean isConnectedToServer()
    {
        return connectToServer;
    }

    @Override
    HashSet<URL> getNecessaryJARs() throws MalformedURLException
    {
        // Get URLs to JAR files.
        URL jarURL = new File(clientJarFileLocation).toURI().toURL();

        HashSet<URL> classLoaderURLs = new HashSet<URL>();
        classLoaderURLs.addAll(getPluginsJars());
        classLoaderURLs.addAll(getLibJars());
        classLoaderURLs.addAll(getSubJars(jarURL, null));
        return classLoaderURLs;
    }

    /**
     * Connects to the client.
     * 
     * @param classLoader
     * @throws ConnectionFailedException
     */
    public synchronized void connectToClient(ClassLoader classLoader)
            throws ConnectionFailedException
    {
        // Get URLs to JAR files.
        try
        {
            clientClass = classLoader.loadClass(CLIENT_CLASS);
        } catch (ClassNotFoundException e)
        {
            throw new ConnectionFailedException("Could not obtain main class.", e);
        }

        // Get methods of server.
        Method getMainProgram;
        try
        {
            // public static YouScopeClint getMainProgram()
            getMainProgram = clientClass.getMethod("getMainProgram", new Class<?>[] {});
            getMainProgram.setAccessible(true);

            // public void startAndRunProgram()
            startProgram = clientClass.getMethod("startProgram", new Class<?>[] {});
            startProgram.setAccessible(true);
            
            addClientFinishListener = clientClass.getMethod("addClientFinishListener", new Class[]{ActionListener.class});
            addClientFinishListener.setAccessible(true);

        } 
        catch (Exception e)
        {
            throw new ConnectionFailedException("Could not obtain function handlers from client. Client interfaces might have changed.", e);
        } 

        // Invoke methods such that server gets started.
        try
        {
            client = getMainProgram.invoke(null, new Object[] {});

        } 
        catch(InvocationTargetException e)
        {
        	if(e.getCause() != null && e.getCause() instanceof ServiceConfigurationError)
        	{
        		throw new ConnectionFailedException("Could not initialize client since one or more plugins are invalid.\nRemove the corresponding plugin from the YouScope plugins directory, and try again.", e);
        	}
			throw new ConnectionFailedException("Error occured while starting the client.", e);
        }
        catch (Exception e)
        {
            throw new ConnectionFailedException("Could not create client because client is at a different version.\nWe recommend installing YouScope again.", e);
        } 
    }

    public synchronized void connectToServer(String ip, int port, String password)
            throws ConnectionFailedException
    {
        if (!isConnected())
            throw new IllegalStateException("Not connected to client yet.");
        if (isConnectedToServer())
            throw new IllegalStateException("Client is already connected to server.");

        // Get methods of server.
        Method connectToServerIP;
        try
        {
            // public void connectToServer(String ip, int port)
            connectToServerIP = clientClass.getMethod("connectToServer", new Class<?>[]
                { String.class, int.class, String.class });
            connectToServerIP.setAccessible(true);

        } catch (SecurityException e)
        {
            throw new ConnectionFailedException(
                    "Could not obtain function handler \"connectToServer\".", e);
        } catch (NoSuchMethodException e)
        {
            throw new ConnectionFailedException(
                    "Could not obtain function handler \"connectToServer\".", e);
        }

        // Invoke methods such that the client connects to the server.
        try
        {
            connectToServerIP.invoke(client, new Object[]
                { ip, port, password });

        } catch (IllegalArgumentException e)
        {
            throw new ConnectionFailedException("Could not invoke function \"connectToServer\".", e);
        } catch (IllegalAccessException e)
        {
            throw new ConnectionFailedException("Could not invoke function \"connectToServer\".", e);
        } catch (InvocationTargetException e)
        {
            throw new ConnectionFailedException("Could not invoke function \"connectToServer\".", e);
        }

        connectToServer = true;
    }

    public synchronized void connectToServer(Class<?> serverClass, Object server)
            throws ConnectionFailedException
    {
        if (!isConnected())
            throw new IllegalStateException("Not connected to client yet.");
        if (isConnectedToServer())
            throw new IllegalStateException("Client is already connected to server.");
        if (serverClass == null || server == null)
            throw new NullPointerException("Server object is NULL.");

        // Get methods of server.
        Method connectToServerObject;
        try
        {
            // public void connectToServer(String ip, int port)
            connectToServerObject = clientClass.getMethod("connectToServer", new Class<?>[]
                { serverClass });
            connectToServerObject.setAccessible(true);

        } catch (SecurityException e)
        {
            throw new ConnectionFailedException(
                    "Could not obtain function handler \"connectToServer\".", e);
        } catch (NoSuchMethodException e)
        {
            throw new ConnectionFailedException(
                    "Could not obtain function handler \"connectToServer\".", e);
        }

        // Invoke methods such that the client connects to the server.
        try
        {
            connectToServerObject.invoke(client, new Object[]
                { server });

        } catch (IllegalArgumentException e)
        {
            throw new ConnectionFailedException("Could not invoke function \"connectToServer\".", e);
        } catch (IllegalAccessException e)
        {
            throw new ConnectionFailedException("Could not invoke function \"connectToServer\".", e);
        } catch (InvocationTargetException e)
        {
            throw new ConnectionFailedException("Could not invoke function \"connectToServer\".", e);
        }

        connectToServer = true;
    }

    public void runClient(ActionListener listener) throws ConnectionFailedException
    {
        if (!isConnectedToServer())
            throw new IllegalStateException("Not connected to server yet.");
        try
        {
        	addClientFinishListener.invoke(client, new Object[]{listener});
            startProgram.invoke(client, new Object[] {});
        } 
        catch (Exception e)
        {
            throw new ConnectionFailedException("Could not start client.", e);
        } 
    }

    @Override
    boolean exists()
    {
        return new File(clientJarFileLocation).exists();
    }
}
