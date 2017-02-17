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
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.youscope.addon.AddonException;

/**
 * @author langmo
 *
 */
class ScriptDefinitionManager
{
	private static final String NAME_NODE = "name";
	private static final String ENGINE_NODE = "engine";
	private static final String FILE_NODE = "file";
	private static final String VALUE_ATTRIBUTE = "value";
	
	private static final String ROOT_NODE = "script_shortcut";
	private static final String SCRIPT_FOLDER_NAME = "configuration/script_shortcuts";
	
	private static File scriptDefinitionFolder = new File(SCRIPT_FOLDER_NAME);
	
	public static String getScriptDefinitionFolder()
	{
		if(scriptDefinitionFolder == null)
		{
			try
			{
				return new File(".").getCanonicalPath();
			}
			catch(@SuppressWarnings("unused") IOException e)
			{
				return new File(".").getAbsolutePath();
			}
		}
		return scriptDefinitionFolder.getAbsolutePath();
	}
	static boolean deleteScriptDefinition(ScriptDefinition scriptDefinition)
	{
		if(scriptDefinition == null || scriptDefinition.getShortCutFile() == null)
			return false;
		if(scriptDefinitionFolder==null || !scriptDefinitionFolder.exists() || !scriptDefinitionFolder.isDirectory())
		{
			ClientSystem.err.println("Script definitions folder could not be located.");
			return false;
		}
		File file = new File(scriptDefinitionFolder, scriptDefinition.getShortCutFile());
		if(!file.exists())
		{
			ClientSystem.err.println("Script definition " + scriptDefinition.getShortCutFile() + " does not exist on file system and could thus not be deleted.");
			return false;
		}
		
		
		return file.delete();
	}
	static boolean saveScriptDefinition(ScriptDefinition scriptDefinition)
	{
		if(scriptDefinitionFolder == null)
		{
			ClientSystem.err.println("Script definitions folder could not be located.");
			return false;
		}
		if(!scriptDefinitionFolder.exists())
		{
			boolean result = scriptDefinitionFolder.mkdirs();
			if(!result)
			{
				ClientSystem.err.println("Script definitions folder could not be created.");
				return false;
			}
		}
		
		Document document;
		try
		{
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not create script file.", e);
			return false;
		}
		Element rootNode = document.createElement(ROOT_NODE);
		
		Element nameNode = document.createElement(NAME_NODE);
		nameNode.setAttribute(VALUE_ATTRIBUTE, scriptDefinition.getName());  
        rootNode.appendChild(nameNode);

        Element engineNode = document.createElement(ENGINE_NODE);
        engineNode.setAttribute(VALUE_ATTRIBUTE, scriptDefinition.getEngine());  
        rootNode.appendChild(engineNode);
        
        Element fileNode = document.createElement(FILE_NODE);
		fileNode.setAttribute(VALUE_ATTRIBUTE, scriptDefinition.getScriptFile());  
        rootNode.appendChild(fileNode);
        
        document.appendChild(rootNode);

		//write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try
		{
			transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult result =  new StreamResult(new File(scriptDefinitionFolder, scriptDefinition.getShortCutFile()));
		 
			transformer.transform(source, result);
			return true;
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not save script definition.", e);
			return false;
		}


	}
	
	static ScriptDefinition[] getScriptDefinitions()
	{
		if(scriptDefinitionFolder==null || !scriptDefinitionFolder.exists() || !scriptDefinitionFolder.isDirectory())
		{
			return new ScriptDefinition[0];
		}
		File[] xmlFiles = scriptDefinitionFolder.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name) {
		        return (name.endsWith(".xml"));
		    }

		});
		Vector<ScriptDefinition> scriptDefinitions = new Vector<ScriptDefinition>();
		for(File xmlFile :xmlFiles)
		{
			ScriptDefinition scriptDefinition = getScriptDefinition(xmlFile);
			if(scriptDefinition != null)
				scriptDefinitions.add(scriptDefinition);
		}
		return scriptDefinitions.toArray(new ScriptDefinition[scriptDefinitions.size()]);
	}
	private static ScriptDefinition getScriptDefinition(File xmlFile)
	{
		Document document;
		try
		{
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
		}
		catch(Exception e)
		{
			ClientSystem.err.println("Could not open or parse script file (" + xmlFile.getAbsolutePath() + ").", e);
			return null;
		}
		Element rootNode = document.getDocumentElement();
		
		// Get name
		String name = getAttributeOfNode(rootNode, NAME_NODE, VALUE_ATTRIBUTE);
		String engine = getAttributeOfNode(rootNode, ENGINE_NODE, VALUE_ATTRIBUTE);
		String file = getAttributeOfNode(rootNode, FILE_NODE, VALUE_ATTRIBUTE);
		if(name == null || engine == null || file == null)
		{
			ClientSystem.err.println("Script file (" + xmlFile.getAbsolutePath() + ") is not valid since at least one node or its value attribute is missing.");
			return null;
		}
		ScriptDefinition scriptDefinition = new ScriptDefinition(name, engine, file);
		scriptDefinition.setShortCutFile(xmlFile.getName());
		return scriptDefinition;
	}
	
	private static String getAttributeOfNode(Element parent, String nodeName, String attrName)
	{
		NodeList nodeList = parent.getElementsByTagName(nodeName);
		for(int i = 0; i<nodeList.getLength(); i++)
		{
			Node node = nodeList.item(i);
			if(node instanceof Element)
			{
				String value = ((Element)node).getAttribute(attrName);
				if(value != null && value.length() > 0)
					return value;
			}
		}
		return null;
	}
	
	public static void runScript(ScriptDefinition scriptDefinition)
	{
		class ScriptRunner implements Runnable
		{
			private ScriptDefinition scriptDefinition;
			ScriptRunner(ScriptDefinition scriptDefinition)
			{
				this.scriptDefinition = scriptDefinition;
			}
			@Override
			public void run()
			{
				runScriptInternal(scriptDefinition);
			}
		}
		(new Thread(new ScriptRunner(scriptDefinition))).start();
	}
	private static void runScriptInternal(ScriptDefinition scriptDefinition)
	{
		String engineName = scriptDefinition.getEngine();
		ScriptEngineFactory factory;
		try {
			factory = ClientAddonProviderImpl.getProvider().getScriptEngineFactory(engineName);
		} catch (AddonException e2) {
			ClientSystem.err.println("Could not find script engine with name " + engineName + ".", e2);
			return;
		}
		try
		{
			ScriptEngine scriptEngine = factory.getScriptEngine();
			scriptEngine.put("youscopeServer", YouScopeClientImpl.getServer());
			scriptEngine.put("youscopeClient", new YouScopeClientConnectionImpl());
        	
			FileReader fileReader =new FileReader(scriptDefinition.getScriptFile()); 
			scriptEngine.eval(fileReader);
			fileReader.close();
		}
		catch (ScriptException e1)
		{
			ClientSystem.err.println("Script file (" + scriptDefinition.getName() + ") could not be evaluated correctly.", e1);
		}
		catch(FileNotFoundException e1)
		{
			ClientSystem.err.println("Script file (" + scriptDefinition.getScriptFile() + ") of script (" + scriptDefinition.getName() + ") could not be found.", e1);
		} catch (IOException e) {
			ClientSystem.err.println("Could not close script file reader.", e);
		}
	}
}
