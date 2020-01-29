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

import java.io.Serializable;

/**
 * @author Moritz Lang
 *
 */
class ScriptDefinition implements Cloneable, Serializable
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -8948143769514581841L;
	private String engine = "";
	private String scriptFile = "";
	private String shortCutFile = null;
	private String name = "";
	public ScriptDefinition(String name, String engine, String scriptFile)
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
