/**
 * 
 */
package org.youscope.plugin.custommicroplates;

import java.io.File;
import java.io.FilenameFilter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.youscope.addon.component.ComponentMetadataAdapter;

/**
 * @author Moritz Lang
 *
 */
class CustomMicroplateManager
{
	private static final String CUSTOM_MICROPLATE_FOLDER_NAME = "configuration/microplates";
	private static final String CUSTOM_MICROPLATE_TYPE_IDENTIFIER_PREFIX = "YouScope.CustomMicroplate.";
	private static final String CUSTOM_MICROPLATE_FILE_ENDING = ".xml";
	
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
	static String getCustomMicroplateFileName(String typeIdentifier)
	{
		return typeIdentifier.substring(CUSTOM_MICROPLATE_TYPE_IDENTIFIER_PREFIX.length())+CUSTOM_MICROPLATE_FILE_ENDING;
	}
	static String getCustomMicroplateFileNameFromName(String customMicroplateName)
	{
		return customMicroplateName+CUSTOM_MICROPLATE_FILE_ENDING;
	}
	
	static ComponentMetadataAdapter<CustomMicroplateConfiguration> getMetadata(String typeIdentifier)
	{
		return new ComponentMetadataAdapter<CustomMicroplateConfiguration>(typeIdentifier, 
				CustomMicroplateConfiguration.class, 
				CustomMicroplateResource.class, 
				getCustomMicroplateName(typeIdentifier), 
				new String[0],
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
		        return (name.endsWith(CUSTOM_MICROPLATE_FILE_ENDING));
		    }

		});
		customMicroplateIdentifiers = new String[xmlFiles.length];
		for(int i=0; i<xmlFiles.length; i++)
		{
			customMicroplateIdentifiers[i] = xmlFiles[i].getName(); 
			customMicroplateIdentifiers[i] = getCustomMicroplateTypeIdentifier(customMicroplateIdentifiers[i].substring(0, customMicroplateIdentifiers[i].length()-CUSTOM_MICROPLATE_FILE_ENDING.length()));
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
		File file = new File(folder, getCustomMicroplateFileName(typeIdentifier));
		if(!file.exists())
		{
			return true;
		}		
		boolean success =  file.delete();
		customMicroplateIdentifiers = null; // enforce reloading.
		return success;
	}
	
	static synchronized boolean saveCustomMicroplate(CustomMicroplateDefinition customMicroplate) throws CustomMicroplateException
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
			StreamResult result =  new StreamResult(new File(folder, getCustomMicroplateFileNameFromName(customMicroplate.getCustomMicroplateName())).toString());
		 
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
		File xmlFile = new File(folder, getCustomMicroplateFileName(typeIdentifier));

		Document document;
		try
		{
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
		}
		catch(Exception e)
		{
			throw new CustomMicroplateException("Could not open or parse script file \"" + xmlFile.getAbsolutePath() + "\".", e);
		}
		Element rootNode = document.getDocumentElement();
		
		// Get values of elements
		
		String numWellsX = getAttributeOfNode(rootNode, NUM_WELLS_X_NODE, VALUE_ATTRIBUTE);
		String numWellsY = getAttributeOfNode(rootNode, NUM_WELLS_Y_NODE, VALUE_ATTRIBUTE);
		String wellWidth = getAttributeOfNode(rootNode, WELL_WIDTH_NODE, VALUE_ATTRIBUTE);
		String wellHeight = getAttributeOfNode(rootNode, WELL_HEIGHT_NODE, VALUE_ATTRIBUTE);
		String microplateName = getCustomMicroplateName(typeIdentifier);
		
		if(numWellsX == null || numWellsY == null || wellWidth == null || wellHeight == null || microplateName == null)
		{
			throw new CustomMicroplateException("Microplate definition file \"" + xmlFile.getAbsolutePath() + "\" is not valid since at least one node or its value attribute is missing.");
			
		}
		
		CustomMicroplateDefinition customMicroplate = new CustomMicroplateDefinition();
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
