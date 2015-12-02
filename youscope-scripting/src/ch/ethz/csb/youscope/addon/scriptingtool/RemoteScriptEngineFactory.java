package ch.ethz.csb.youscope.addon.scriptingtool;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import ch.ethz.csb.youscope.client.addon.CallbackAddonFactory;
import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.shared.YouScopeServer;
import ch.ethz.csb.youscope.shared.measurement.callback.Callback;
import ch.ethz.csb.youscope.shared.measurement.callback.CallbackCreationException;

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
