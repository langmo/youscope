/**
 * 
 */
package org.youscope.plugin.matlabscripting;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

/**
 * @author langmo
 */
public class MatlabScriptEngine extends AbstractScriptEngine
{
    /**
     * The factory used to create the proxies.
     */
    private static RemoteMatlabProxyFactory _factory = null;

    /**
     * The URL of the policy file.
     */
    private static final String policyFile = "org/youscope/plugin/matlabscripting/MatlabScripting.policy";

    /**
     * The proxy which the static methods are being relayed to.
     */
    private RemoteMatlabProxy _proxy;

    /**
     * Creates a new Matlab script engine. Every engine starts a new Matlab process.
     */
    public MatlabScriptEngine()
    {
        setBindings(new MatlabBindings(), ScriptContext.ENGINE_SCOPE);
        if (_factory == null)
            _factory = new RemoteMatlabProxyFactory();
        
    }

    /**
     * Create a connection to MATLAB. If a connection already exists this method will not do
     * anything. This must be called before any methods that control MATLAB are called, or those
     * methods will throw runtime exceptions.
     * 
     * @throws MatlabConnectionException
     */
    private void createConnection() throws MatlabConnectionException
    {
        if (!isConnected())
        {
            // Set policy
            URL policyURL = getClass().getClassLoader().getResource(policyFile);
            if (policyURL == null)
            {
                throw new MatlabConnectionException("Could not locate policy file (" + policyFile + ").");
            }
            System.setProperty("java.security.policy", policyURL.toString());

            _factory = new RemoteMatlabProxyFactory();

            _factory.addConnectionListener(new MatlabConnectionListener()
                {
                    @Override
					public void connectionEstablished(RemoteMatlabProxy proxy)
                    {
                        _proxy = proxy;
                    }

                    @Override
					public void connectionLost(RemoteMatlabProxy proxy)
                    {
                        _proxy = null;
                    }
                });

            _factory.getProxy();
        }
    }

    /**
     * Returns whether or not this engine is connected to MATLAB.
     * 
     * @return if connected to MATLAB
     */
    public boolean isConnected()
    {
        return (_proxy != null && _proxy.isConnected());
    }
    @Override
    protected void finalize() throws Throwable
    {
    	if(isConnected())
    	{
    		try
			{
				_proxy.exit();
			}
			catch(MatlabInvocationException e)
			{
				e.printStackTrace();
			}
    	}
    	super.finalize();
    }
    private void printMessage(String message)
    {
    	if(getContext() == null || getContext().getWriter() == null)
    	{
    		System.err.println(message);
    		return;
    	}
    	try
		{
			getContext().getWriter().write(message + "\n");
		}
		catch(@SuppressWarnings("unused") IOException e)
		{
			System.err.println(message);
		}
    }
    private class MatlabBindings implements Bindings
    {
        @Override
        public Object put(String name, Object value)
        {
            try
            {
            	createConnection();
            	Object oldValue;
            	if(containsKey(name))
            		oldValue = get(name);
            	else
            		oldValue = null;
                _proxy.setVariable(name, value);
                return oldValue;
            } 
            catch (Exception e)
            {
            	printMessage("Variable with name " + name + " could not be set in Matlab. Remark: Every variable which should be set must be of a class either implementing the Remote or Serializable interface.");
            	printMessage("Error message: " + createErrorMessage(e));
            	return null;
            } 
        }

		@Override
		public void clear()
		{
			try
            {
				createConnection();
				_proxy.eval("clear all");
            } 
            catch (Exception e)
            {
            	printMessage("Could not clear defined variables.");
            	printMessage("Error message: " + createErrorMessage(e));
            } 
		}

		@Override
		public boolean containsValue(Object arg0)
		{
			return values().contains(arg0);
		}

		@Override
		public Set<java.util.Map.Entry<String, Object>> entrySet()
		{
			HashSet<java.util.Map.Entry<String, Object>> entrySet = new HashSet<java.util.Map.Entry<String, Object>>();
			for(String key : keySet())
			{
				Object value = get(key);
				if(value == null)
					continue;
				AbstractMap.SimpleEntry<String, Object> entry = new AbstractMap.SimpleEntry<String, Object>(key, value);
				entrySet.add(entry);
			}
			return entrySet;
		}

		@Override
		public boolean isEmpty()
		{
			return keySet().isEmpty();
		}

		@Override
		public Set<String> keySet()
		{
			HashSet<String> keySet = new HashSet<String>();
			Object retValues;
			try
            {
				createConnection();
				retValues = _proxy.returningFeval("who", null, 1);
            } 
            catch (Exception e)
            {
            	printMessage("Could not obtain defined variables.");
            	printMessage("Error message: " + createErrorMessage(e));
            	return keySet;
            } 
            if(retValues instanceof String)
            	retValues = new String[]{(String)retValues};
            if(!(retValues instanceof String[]))
            {
            	printMessage("Could not obtain defined variables.");
            	printMessage("Returned names are not of type String[], put of type " +retValues.getClass().getSimpleName() + ".");
            	return keySet;
            }
            for(String key : (String[])retValues)
            {
            	keySet.add(key);
            }
            return keySet;
		}

		@Override
		public int size()
		{
			return keySet().size();
		}

		@Override
		public Collection<Object> values()
		{
			Vector<Object> values = new Vector<Object>();
			for(String key : keySet())
			{
				Object value = get(key);
				if(value == null)
					continue;
				values.add(key);
			}
			return values;
		}

		@Override
		public boolean containsKey(Object key)
		{
			return keySet().contains(key);
		}

		@Override
		public Object get(Object key)
		{
			try
            {
            	createConnection();
            	return _proxy.getVariable(key.toString());
            } 
            catch (Exception e)
            {
            	printMessage("Variable with name " + key.toString() + " could not be obtained from Matlab.");
            	printMessage("Error message: " + createErrorMessage(e));
            	return null;
            } 
		}

		@Override
		public void putAll(Map<? extends String, ? extends Object> variables)
		{
			for(Map.Entry<? extends String, ? extends Object> variable : variables.entrySet())
			{
				put(variable.getKey(), variable.getValue());
			}
		}

		@Override
		public Object remove(Object key)
		{
			Object value = get(key);
			try
            {
				createConnection();
				_proxy.eval("clear " + key.toString());
            } 
            catch (Exception e)
            {
            	printMessage("Could not clear variable " + key.toString() + ".");
            	printMessage("Error message: " + createErrorMessage(e));
            } 
			return value;
		}
    }

   @Override
    public Bindings createBindings()
    {
        return new MatlabBindings();
    }

    @Override
    public Object eval(String script, ScriptContext context) throws ScriptException
    {
        try
        {
            createConnection();
            return _proxy.eval(script);
        } 
        catch (MatlabConnectionException e)
        {
            throw new ScriptException("Could not connect to Matlab: " + e.getMessage());
        } 
        catch (MatlabInvocationException e)
        {
            throw new ScriptException(createErrorMessage(e));
        }
    }
    
    private String createErrorMessage(Throwable throwable)
    {
    	String message = "";
    	for(int i = 0; throwable != null && i < 10; i++)
    	{
    		if(message.length() > 0)
    			message += "\n";
    		message += throwable.getMessage();
    		throwable = throwable.getCause();
    	}
    	return message;
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException
    {
    	String script = "";
    	
    	LineNumberReader lineReader = new LineNumberReader(reader);
    	while (true)
        {
            try
            {
                String line = lineReader.readLine();
                if (line == null)
                    break;
                script+= line + "\n";

            }
            catch (IOException e)
            {
                throw new ScriptException("Could not read file: " + e.getMessage());
            }
        }
    	return eval(script, context);
    }

    @Override
    public ScriptEngineFactory getFactory()
    {
        return new MatlabScriptEngineFactory();
    }

}
