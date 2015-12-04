/**
 * 
 */
package org.youscope.plugin.matlabscripting;

import java.util.List;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * @author langmo
 */
public class MatlabScriptEngineFactory implements ScriptEngineFactory
{

    private static MatlabScriptEngine engine = null;

    @Override
    public String getEngineName()
    {
        return "Matlab Scripting";
    }

    @Override
    public String getEngineVersion()
    {
        return "1.0";
    }

    @Override
    public List<String> getExtensions()
    {
        Vector<String> extensions = new Vector<String>();
        extensions.add("m");
        extensions.add("mat");
        return extensions;
    }

    @Override
    public String getLanguageName()
    {
        return "MatlabScript";
    }

    @Override
    public String getLanguageVersion()
    {
        return "R11 and higher (depends on local Matlab installation)";
    }

    @Override
    public String getMethodCallSyntax(String obj, String m, String... args)
    {
        String ret = obj;
        ret += "." + m + "(";
        for (int i = 0; i < args.length; i++)
        {
            ret += args[i];
            if (i == args.length - 1)
            {
                ret += ")";
            } else
            {
                ret += ",";
            }
        }
        return ret;

    }

    @Override
    public List<String> getMimeTypes()
    {
        Vector<String> extensions = new Vector<String>();
        extensions.add("Matlab");
        extensions.add("MatlabScript");
        return extensions;
    }

    @Override
    public List<String> getNames()
    {
        Vector<String> extensions = new Vector<String>();
        extensions.add("Matlab");
        extensions.add("MatlabScript");
        extensions.add("mat");
        return extensions;
    }

    @Override
    public String getOutputStatement(String arg0)
    {
        return "disp(" + arg0 + ")";

    }

    @Override
    public Object getParameter(String key)
    {
        if (key == ScriptEngine.ENGINE)
            return getEngineName();
        else if (key == ScriptEngine.ENGINE_VERSION)
            return getEngineVersion();
        else if (key == ScriptEngine.NAME)
            return getNames().get(0);
        else if (key == ScriptEngine.LANGUAGE)
            return getLanguageName();
        else if (key == ScriptEngine.LANGUAGE_VERSION)
            return getLanguageVersion();
        else if (key == "THREADING")
            return null;
        return null;
    }

    @Override
    public String getProgram(String... statements)
    {
        String retVal = "";
        int len = statements.length;
        for (int i = 0; i < len; i++)
        {
            retVal += statements[i] + ";\n";
        }
        return retVal;
    }

    @Override
    public ScriptEngine getScriptEngine()
    {
        if (engine == null)
            engine = new MatlabScriptEngine();
        return engine;
    }

}
