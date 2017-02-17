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
package org.youscope.plugin.custommicroplates;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.youscope.addon.ConfigurationManagement;
import org.youscope.addon.component.ComponentMetadataAdapter;


/**
 * @author Moritz Lang
 *
 */
class CustomMicroplateManager
{
	private static final String CUSTOM_MICROPLATE_FOLDER_NAME = "configuration/microplates";
	private static final String CUSTOM_MICROPLATE_TYPE_IDENTIFIER_PREFIX = "YouScope.CustomMicroplate.";
	private static final String CUSTOM_RECTANGULAR_MICROPLATE_FILE_ENDING = ".xml";
	private static final String CUSTOM_ARBITRARY_MICROPLATE_FILE_ENDING = ".csb";
	
	private static String[] customMicroplateIdentifiers = null;
	
	private static final String ROOT_NODE = "microplate-type";
	private static final String NUM_WELLS_X_NODE = "num-wells-x";
	private static final String NUM_WELLS_Y_NODE = "num-wells-y";
	private static final String WELL_WIDTH_NODE = "well-width-um";
	private static final String WELL_HEIGHT_NODE = "well-height-um";
	private static final String VALUE_ATTRIBUTE = "value";
	
	static String getCustomMicroplateTypeIdentifier(String customMicroplateName)
	{
		return CUSTOM_MICROPLATE_TYPE_IDENTIFIER_PREFIX + customMicroplateName;
	}
	static String getCustomMicroplateName(String typeIdentifier)
	{
		return typeIdentifier.substring(CUSTOM_MICROPLATE_TYPE_IDENTIFIER_PREFIX.length());
	}
	static String getCustomMicroplateFileName(String typeIdentifier, boolean rectangular)
	{
		return getCustomMicroplateFileNameFromName(typeIdentifier.substring(CUSTOM_MICROPLATE_TYPE_IDENTIFIER_PREFIX.length()), rectangular);
	}
	static String getCustomMicroplateFileNameFromName(String customMicroplateName, boolean rectangular)
	{
		if(rectangular)
			return customMicroplateName+CUSTOM_RECTANGULAR_MICROPLATE_FILE_ENDING;
		return customMicroplateName+CUSTOM_ARBITRARY_MICROPLATE_FILE_ENDING;
	}
	
	static ComponentMetadataAdapter<CustomMicroplateConfiguration> getMetadata(String typeIdentifier)
	{
		return new ComponentMetadataAdapter<CustomMicroplateConfiguration>(typeIdentifier, 
				CustomMicroplateConfiguration.class, 
				CustomRectangularMicroplateResource.class, 
				getCustomMicroplateName(typeIdentifier), 
				new String[0],
				"A user defined (generalized) microplate layout, which can either represent a real microplate, or microplate-similar entities like microfluidic channels. The microplate consists of several generalized wells, representing positions in which images are taken in the microplate measurement of YouScope.",
				"icons/block-share.png");
	}
	
	static synchronized String[] getCustomMicroplateTypeIdentifiers()
	{
		if(customMicroplateIdentifiers != null)
			return customMicroplateIdentifiers;
		File folder = new File(CUSTOM_MICROPLATE_FOLDER_NAME);
		if(!folder.exists() || !folder.isDirectory())
		{
			return new String[0];
		}
		File[] xmlFiles = folder.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name) {
		        return (name.endsWith(CUSTOM_RECTANGULAR_MICROPLATE_FILE_ENDING) || name.endsWith(CUSTOM_ARBITRARY_MICROPLATE_FILE_ENDING));
		    }

		});
		customMicroplateIdentifiers = new String[xmlFiles.length];
		for(int i=0; i<xmlFiles.length; i++)
		{
			customMicroplateIdentifiers[i] = xmlFiles[i].getName(); 
			customMicroplateIdentifiers[i] = getCustomMicroplateTypeIdentifier(customMicroplateIdentifiers[i].substring(0, customMicroplateIdentifiers[i].length()-CUSTOM_RECTANGULAR_MICROPLATE_FILE_ENDING.length()));
		}
		return customMicroplateIdentifiers;
	}
	static synchronized boolean deleteCustomMicroplate(String typeIdentifier)
	{
		File folder = new File(CUSTOM_MICROPLATE_FOLDER_NAME);
		if(!folder.exists() || !folder.isDirectory())
		{
			return true;
		}
		File file = new File(folder, getCustomMicroplateFileName(typeIdentifier, true));
		if(!file.exists())
			file = new File(folder, getCustomMicroplateFileName(typeIdentifier, false));
		if(!file.exists())
		{
			return true;
		}		
		boolean success =  file.delete();
		customMicroplateIdentifiers = null; // enforce reloading.
		return success;
	}
	
	private static synchronized boolean saveCustomMicroplateInternal(CustomArbitraryMicroplateDefinition customMicroplate) throws CustomMicroplateException
	{
		File folder = new File(CUSTOM_MICROPLATE_FOLDER_NAME);
		if(!folder.exists() || !folder.isDirectory())
		{
			boolean result = folder.mkdirs();
			if(!result)
			{
				throw new CustomMicroplateException("Custom microplate folder could not be created. Check if YouScope has sufficients rights to create sub-folders in the YouScope directory.");
			}
		}
		try {
			ConfigurationManagement.saveConfiguration(new File(folder, getCustomMicroplateFileNameFromName(customMicroplate.getCustomMicroplateName(), false)).toString(), customMicroplate);
		} catch(IOException e)
		{
			throw new CustomMicroplateException("Could not save custom microplate file.", e);
		}
		customMicroplateIdentifiers = null; // enforce reloading.
		return true;
	}
	static synchronized boolean saveCustomMicroplate(CustomMicroplateDefinition customMicroplate) throws CustomMicroplateException
	{
		if(customMicroplate instanceof CustomRectangularMicroplateDefinition)
			return saveCustomMicroplateInternal((CustomRectangularMicroplateDefinition)customMicroplate);
		else if(customMicroplate instanceof CustomArbitraryMicroplateDefinition)
			return saveCustomMicroplateInternal((CustomArbitraryMicroplateDefinition)customMicroplate);
		else
			throw new CustomMicroplateException("Custom microplate type " + customMicroplate.getClass().getName() + " unknown.");
	}
	private static synchronized boolean saveCustomMicroplateInternal(CustomRectangularMicroplateDefinition customMicroplate) throws CustomMicroplateException
	{
		File folder = new File(CUSTOM_MICROPLATE_FOLDER_NAME);
		if(!folder.exists() || !folder.isDirectory())
		{
			boolean result = folder.mkdirs();
			if(!result)
			{
				throw new CustomMicroplateException("Custom microplate folder could not be created. Check if YouScope has sufficients rights to create sub-folders in the YouScope directory.");
			}
		}
		
		Document document;
		try
		{
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch(Exception e)
		{
			throw new CustomMicroplateException("Could not create microplate definition file.", e);
		}
		Element rootNode = document.createElement(ROOT_NODE);
		
		Element numWellsXNode = document.createElement(NUM_WELLS_X_NODE);
		numWellsXNode.setAttribute(VALUE_ATTRIBUTE, Integer.toString(customMicroplate.getNumWellsX()));  
        rootNode.appendChild(numWellsXNode);
        
        Element numWellsYNode = document.createElement(NUM_WELLS_Y_NODE);
		numWellsYNode.setAttribute(VALUE_ATTRIBUTE, Integer.toString(customMicroplate.getNumWellsY()));  
        rootNode.appendChild(numWellsYNode);
		
        Element wellWidthNode = document.createElement(WELL_WIDTH_NODE);
        wellWidthNode.setAttribute(VALUE_ATTRIBUTE, Double.toString(customMicroplate.getWellWidth()));  
        rootNode.appendChild(wellWidthNode);
        
        Element wellHeightNode = document.createElement(WELL_HEIGHT_NODE);
        wellHeightNode.setAttribute(VALUE_ATTRIBUTE, Double.toString(customMicroplate.getWellHeight()));  
        rootNode.appendChild(wellHeightNode);
		
		document.appendChild(rootNode);
		document.setXmlStandalone(true);

		//write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try
		{
			transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult result =  new StreamResult(new File(folder, getCustomMicroplateFileNameFromName(customMicroplate.getCustomMicroplateName(), true)).toString());
		 
			transformer.transform(source, result);
		}
		catch(Exception e)
		{
			throw new CustomMicroplateException("Could not save microplate type definition.", e);
		}
		
		customMicroplateIdentifiers = null; // enforce reloading.
		return true;
	}
	
	static synchronized CustomMicroplateDefinition getCustomMicroplate(String typeIdentifier) throws CustomMicroplateException
	{
		File folder = new File(CUSTOM_MICROPLATE_FOLDER_NAME);
		if(!folder.exists() || !folder.isDirectory())
		{
			throw new CustomMicroplateException("Custom microplate folder does not exist, thus, custom microplate could not be localized.");
		}
		String microplateName = getCustomMicroplateName(typeIdentifier);
		File file = new File(folder, getCustomMicroplateFileName(typeIdentifier, true)); 
		if(file.exists())
			return getCustomRectangularMicroplate(file, microplateName);
		file = new File(folder, getCustomMicroplateFileName(typeIdentifier, false)); 
		if(file.exists())
			return getCustomArbitraryMicroplate(file, microplateName);
		throw new CustomMicroplateException("Custom microplate with ID " + typeIdentifier + " does not exist.");
	}
	private static synchronized CustomArbitraryMicroplateDefinition getCustomArbitraryMicroplate(File file, String microplateName) throws CustomMicroplateException
	{
		try
		{
			CustomArbitraryMicroplateDefinition config = (CustomArbitraryMicroplateDefinition) ConfigurationManagement.loadConfiguration(file.toString());
			config.setCustomMicroplateName(microplateName);
			return config;
		}
		catch(Throwable e)
		{
			throw new CustomMicroplateException("Could not load custom microplate.", e);
		}
	}
	private static synchronized CustomRectangularMicroplateDefinition getCustomRectangularMicroplate(File file, String microplateName) throws CustomMicroplateException
	{
		Document document;
		try
		{
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
		}
		catch(Exception e)
		{
			throw new CustomMicroplateException("Could not open or parse script file \"" + file.getAbsolutePath() + "\".", e);
		}
		Element rootNode = document.getDocumentElement();
		
		// Get values of elements
		
		String numWellsX = getAttributeOfNode(rootNode, NUM_WELLS_X_NODE, VALUE_ATTRIBUTE);
		String numWellsY = getAttributeOfNode(rootNode, NUM_WELLS_Y_NODE, VALUE_ATTRIBUTE);
		String wellWidth = getAttributeOfNode(rootNode, WELL_WIDTH_NODE, VALUE_ATTRIBUTE);
		String wellHeight = getAttributeOfNode(rootNode, WELL_HEIGHT_NODE, VALUE_ATTRIBUTE);
		
		if(numWellsX == null || numWellsY == null || wellWidth == null || wellHeight == null || microplateName == null)
		{
			throw new CustomMicroplateException("Microplate definition file \"" + file.getAbsolutePath() + "\" is not valid since at least one node or its value attribute is missing.");
			
		}
		
		CustomRectangularMicroplateDefinition customMicroplate = new CustomRectangularMicroplateDefinition();
		try
		{
			customMicroplate.setNumWellsX(Integer.parseInt(numWellsX));
			customMicroplate.setNumWellsY(Integer.parseInt(numWellsY));
			customMicroplate.setWellWidth(Double.parseDouble(wellWidth));
			customMicroplate.setWellHeight(Double.parseDouble(wellHeight));
			customMicroplate.setCustomMicroplateName(microplateName);
		}
		catch(NumberFormatException e)
		{
			throw new CustomMicroplateException("At least one parameter corresponding to a number was not parseable.", e);
		}
		
		return customMicroplate;
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
}
