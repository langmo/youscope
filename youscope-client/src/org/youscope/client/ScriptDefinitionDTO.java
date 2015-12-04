/**
 * 
 */
package org.youscope.client;

import java.io.Serializable;

/**
 * @author langmo
 *
 */
class ScriptDefinitionDTO implements Cloneable, Serializable
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -8948143769514581841L;
	private String engine = "";
	private String scriptFile = "";
	private String shortCutFile = null;
	private String name = "";
	public ScriptDefinitionDTO(String name, String engine, String scriptFile)
	{
		this.setName(name);
		this.setEngine(engine);
		this.setScriptFile(scriptFile);
		this.setShortCutFile(name+".xml");
	}
	public void setEngine(String engine)
	{
		this.engine = engine;
	}
	public String getEngine()
	{
		return engine;
	}
	public void setScriptFile(String scriptFile)
	{
		this.scriptFile = scriptFile;
	}
	public String getScriptFile()
	{
		return scriptFile;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getName()
	{
		return name;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
	
	@Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
	public void setShortCutFile(String shortCutFile)
	{
		this.shortCutFile = shortCutFile;
	}
	public String getShortCutFile()
	{
		return shortCutFile;
	}
}
