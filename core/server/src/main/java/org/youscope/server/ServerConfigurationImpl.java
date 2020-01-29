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

/**
 * @author langmo
 * 
 */
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.script.ScriptEngineFactory;

import org.youscope.addon.serveraddon.ServerAddon;
import org.youscope.serverinterfaces.YouScopeServerProperties;

/**
 * @author langmo
 */
class ServerConfigurationImpl extends UnicastRemoteObject implements YouScopeServerProperties
{

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = 856785424972712007L;

    /**
     * @throws RemoteException
     */
    protected ServerConfigurationImpl() throws RemoteException
    {
        super();
    }

    @Override
	public String[] getSupportedImageFormats()
    {
    	String[] fileSuffixes = ImageIO.getWriterFileSuffixes();
    	HashSet<String> uniqueSuffixes = new HashSet<>(fileSuffixes.length);
    	for(String fileSuffix : fileSuffixes)
    	{
    		if(fileSuffix!= null && fileSuffix.length() >= 1)
    			uniqueSuffixes.add(fileSuffix.toLowerCase());
    	}
    	fileSuffixes = uniqueSuffixes.toArray(new String[uniqueSuffixes.size()]);
    	Arrays.sort(fileSuffixes);
        return fileSuffixes;
    }

    @Override
	public byte[] getIP() throws UnknownHostException
    {
        InetAddress addr = InetAddress.getLocalHost();
        // Get IP Address
        return addr.getAddress();
    }

    @Override
	public String[] getSupportedScriptEngines() throws RemoteException
    {
        Vector<String> engineNames = new Vector<String>();
        for (ScriptEngineFactory factory : ServerSystem.getScriptEngineManager()
                .getEngineFactories())
        {
            engineNames.add(factory.getEngineName());
        }

        return engineNames.toArray(new String[engineNames.size()]);
    }

    @Override
	public <T extends ServerAddon> T getServerAddon(Class<T> addonInterface)
            throws RemoteException
    {
        return ServerSystem.getGeneralAddon(addonInterface);
    }

    @Override
	public <T extends ServerAddon> T[] getServerAddons(Class<T> addonInterface)
            throws RemoteException
    {
        return ServerSystem.getGeneralAddons(addonInterface);
    }

    @Override
	public ServerAddon[] getGeneralAddons() throws RemoteException
    {
        return ServerSystem.getGeneralAddons();
    }
}
