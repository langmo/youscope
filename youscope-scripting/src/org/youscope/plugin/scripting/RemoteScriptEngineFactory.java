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
package org.youscope.plugin.scripting;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.youscope.addon.callback.CallbackAddonFactory;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.callback.Callback;
import org.youscope.common.callback.CallbackCreationException;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * Factory to create remote script engines as callbacks.
 * @author Moritz Lang
 *
 */
public class RemoteScriptEngineFactory implements CallbackAddonFactory
{
	private String[] supportedTypeIdentifiers = null;
	private Hashtable<String, ScriptEngineFactory> scriptEngineFactories = null;
	private synchronized void initialize()
	{
		if(scriptEngineFactories != null)
			return;
		
		ScriptEngineManager mgr = new ScriptEngineManager(RemoteScriptEngineFactory.class.getClassLoader());		
		ArrayList<String> typeIdentifiers = new ArrayList<String>();
		scriptEngineFactories = new Hashtable<String, ScriptEngineFactory>();
		for(ScriptEngineFactory factory : mgr.getEngineFactories())
		{
			scriptEngineFactories.put(factory.getEngineName(), factory);
			typeIdentifiers.add(factory.getEngineName());
		}
		supportedTypeIdentifiers = typeIdentifiers.toArray(new String[typeIdentifiers.size()]);
	}
	
	@Override
	public Callback createCallback(String typeIdentifier, YouScopeClient client,
			YouScopeServer server) throws CallbackCreationException {
		initialize();
		ScriptEngineFactory factory = scriptEngineFactories.get(typeIdentifier);
		if(factory == null)
			throw new CallbackCreationException("Remote script engines with type identifiers " + typeIdentifier + " not supported by this callback factory.");
		try {
			return new RemoteScriptEngineImpl(factory.getScriptEngine(), typeIdentifier);
		} catch (RemoteException e) {
			throw new CallbackCreationException("Remote exception occured while constructing remote script engine.", e);
		}
	}

	@Override
	public String[] getSupportedTypeIdentifiers() {
		initialize();
		return supportedTypeIdentifiers;
	}

	@Override
	public boolean isSupportingTypeIdentifier(String typeIdentifier) 
	{
		initialize();
		for(String supported : supportedTypeIdentifiers)
		{
			if(supported.equals(typeIdentifier))
				return true;
		}
		return false;
	}

}
