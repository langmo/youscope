/**
 * 
 */
package org.youscope.plugin.custommicroplates;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.youscope.common.measurement.microplate.Microplate;

/**
 * @author langmo
 *
 */
public class CustomMicroplatesManager
{
	private static final String ROOT_NODE = "microplate-type";
	private static final String MICROPLATES_FOLDER_NAME = "configuration/microplates";
	
	private static final String NUM_WELLS_X_NODE = "num-wells-x";
	private static final String NUM_WELLS_Y_NODE = "num-wells-y";
	private static final String WELL_WIDTH_NODE = "well-width-um";
	private static final String WELL_HEIGHT_NODE = "well-height-um";
	private static final String MICROPLATE_NAME_NODE = "microplate-name";
	
	private static final String VALUE_ATTRIBUTE = "value";
	
	private static File microplateDefinitionFolder;
	static
	{
		microplateDefinitionFolder = new File(MICROPLATES_FOLDER_NAME);
		try
		{
			microplateDefinitionFolder = microplateDefinitionFolder.getCanonicalFile();
		}
		catch(@SuppressWarnings("unused") IOException e)
		{
			microplateDefinitionFolder = microplateDefinitionFolder.getAbsoluteFile();
		}
	}	
	
	/**
	 * Deletes a given microplate type.
	 * @param microplateType The type to be saved.
	 * @return True if successful.
	 * @throws FileNotFoundException
	 */
	public static boolean deleteMicroplateTypeDefinition(Microplate microplateType) throws FileNotFoundException
	{
		if(microplateType == null)
			return false;
		if(microplateDefinitionFolder==null || !microplateDefinitionFolder.exists() || !microplateDefinitionFolder.isDirectory())
		{
			throw new FileNotFoundException("Folder of microplate definitions could not be found.");
		}
		File file = new File(microplateDefinitionFolder, microplateType.getMicroplateID());
		if(!file.exists())
		{
			throw new FileNotFoundException("Microplate definition could not be found.");
		}
		
		return file.delete();
	}
	/**
	 * Saves a given microplate type.
	 * @param microplateType The type to be saved.
	 * @return True if successful.
	 * @throws IOException
	 */
	public static boolean saveMicroplateTypeDefinition(Microplate microplateType) throws IOException
	{
		if(microplateDefinitionFolder==null)
		{
			throw new FileNotFoundException("Folder of microplate definitions could not be found.");
		}
		if(!microplateDefinitionFolder.exists())
		{
			boolean result = microplateDefinitionFolder.mkdirs();
			if(!result)
			{
				throw new IOException("Microplate definitions folder could not be created.");
			}
		}
		
		Document document;
		try
		{
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		}
		catch(Exception e)
		{
			throw new IOException("Could not create microplate definition file.", e);
		}
		Element rootNode = document.createElement(ROOT_NODE);
		
		Element numWellsXNode = document.createElement(NUM_WELLS_X_NODE);
		numWellsXNode.setAttribute(VALUE_ATTRIBUTE, Integer.toString(microplateType.getNumWellsX()));  
        rootNode.appendChild(numWellsXNode);
        
        Element numWellsYNode = document.createElement(NUM_WELLS_Y_NODE);
		numWellsYNode.setAttribute(VALUE_ATTRIBUTE, Integer.toString(microplateType.getNumWellsY()));  
        rootNode.appendChild(numWellsYNode);
		
        Element wellWidthNode = document.createElement(WELL_WIDTH_NODE);
        wellWidthNode.setAttribute(VALUE_ATTRIBUTE, Double.toString(microplateType.getWellWidth()));  
        rootNode.appendChild(wellWidthNode);
        
        Element wellHeightNode = document.createElement(WELL_HEIGHT_NODE);
        wellHeightNode.setAttribute(VALUE_ATTRIBUTE, Double.toString(microplateType.getWellHeight()));  
        rootNode.appendChild(wellHeightNode);
        
        Element microplateNameNode = document.createElement(MICROPLATE_NAME_NODE);
        microplateNameNode.setAttribute(VALUE_ATTRIBUTE, microplateType.getMicroplateName());  
        rootNode.appendChild(microplateNameNode);
		
		document.appendChild(rootNode);
		document.setXmlStandalone(true);

		//write the content into xml file
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer;
		try
		{
			transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult result =  new StreamResult(new File(microplateDefinitionFolder, microplateType.getMicroplateID()));
		 
			transformer.transform(source, result);
			return true;
		}
		catch(Exception e)
		{
			throw new IOException("Could not save microplate type definition.", e);
		}
	}
	
	/**
	 * Returns a list of all custom (using this addon) defined microplate types.
	 * @return List of microplate types.
	 */
	public static Microplate[] getMicroplateTypes()
	{
		if(microplateDefinitionFolder==null || !microplateDefinitionFolder.exists() || !microplateDefinitionFolder.isDirectory())
		{
			return new Microplate[0];
		}
		File[] xmlFiles = microplateDefinitionFolder.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(File dir, String name) {
		        return (name.endsWith(".xml"));
		    }

		});
		Vector<Microplate> microplateTypes = new Vector<Microplate>();
		for(File xmlFile :xmlFiles)
		{
			Microplate microplateType;
			try
			{
				microplateType = getMicroplateType(xmlFile);
			}
			catch(@SuppressWarnings("unused") Exception e)
			{
				continue;
			}
			if(microplateType != null)
				microplateTypes.add(microplateType);
		}
		return microplateTypes.toArray(new Microplate[microplateTypes.size()]);
	}
	private static Microplate getMicroplateType(File xmlFile) throws IOException
	{
		Document document;
		try
		{
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);
		}
		catch(Exception e)
		{
			throw new IOException("Could not open or parse script file \"" + xmlFile.getAbsolutePath() + "\".", e);
		}
		Element rootNode = document.getDocumentElement();
		
		// Get values of elements
		
		String numWellsX = getAttributeOfNode(rootNode, NUM_WELLS_X_NODE, VALUE_ATTRIBUTE);
		String numWellsY = getAttributeOfNode(rootNode, NUM_WELLS_Y_NODE, VALUE_ATTRIBUTE);
		String wellWidth = getAttributeOfNode(rootNode, WELL_WIDTH_NODE, VALUE_ATTRIBUTE);
		String wellHeight = getAttributeOfNode(rootNode, WELL_HEIGHT_NODE, VALUE_ATTRIBUTE);
		String microplateName = getAttributeOfNode(rootNode, MICROPLATE_NAME_NODE, VALUE_ATTRIBUTE);
		String microplateID = xmlFile.getName();
		
		if(numWellsX == null || numWellsY == null || wellWidth == null || wellHeight == null || microplateName == null)
		{
			throw new IOException("Microplate definition file \"" + xmlFile.getAbsolutePath() + "\" is not valid since at least one node or its value attribute is missing.");
			
		}
		
		CustomMicroplateType microplateType;
		try
		{
			microplateType = new CustomMicroplateType(Integer.parseInt(numWellsX), Integer.parseInt(numWellsY), Double.parseDouble(wellWidth), Double.parseDouble(wellHeight), microplateID, microplateName);
		}
		catch(NumberFormatException e)
		{
			throw new IOException("At least one parameter corresponding to a number was not parseable.", e);
		}
		
		
		return microplateType;
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
