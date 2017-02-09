/**
 * 
 */
package org.youscope.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.youscope.common.MetadataProperty;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.Property;
import org.youscope.common.microscope.SettingException;

/**
 * @author langmo
 */
class XMLMeasurementDescription
{
	public static final String	ROOT_NAME					= "measurement";
	
	public static final String	INFORMATION					= "information";
	
	public static final String	MEASUREMENT_NAME			= "name";
	
	public static final String	MEASUREMENT_DESCRIPTION		= "description";
	
	public static final String	METADATA			= "metadata";

	public static final String	SCOPE_SETTINGS				= "scope-settings";

	public static final String	SCOPE_SETTING				= "scope-setting";

	public static final String	CHANNEL_GROUPS				= "channel-groups";

	public static final String	CHANNELS					= "channels";

	public static final String	CHANNEL						= "channel";

	public static final String	CHANNEL_SETTING_ON			= "channel-setting-on";

	public static final String	CHANNEL_SETTING_OFF			= "channel-setting-off";

	public static final String	ATTR_CHANNEL_NAME			= "name";

	public static final String	ATTR_CONFIG_GROUP_NAME		= "name";

	public static final String	ATTR_DEVICE					= "device";

	public static final String	ATTR_PROPERTY				= "property";

	public static final String	ATTR_VALUE					= "value";

	/**
	 * Creates description for measurement.
	 * @param measurement 
	 * @param microscope
	 * @return XML document containing description.
	 * @throws ParserConfigurationException
	 * @throws RemoteException
	 * @throws DOMException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 * @throws SettingException
	 */
	public static Document createDescription(MeasurementImpl measurement, Microscope microscope) throws ParserConfigurationException, RemoteException, DOMException, MicroscopeException, InterruptedException, SettingException
	{
		// Create new XML Document
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		// Add root Element.
		Element rootElement = document.createElement(ROOT_NAME);
		document.appendChild(rootElement);

		// write metadata
		Element informationElement = document.createElement(INFORMATION);
		rootElement.appendChild(informationElement);
		Element nameElement = document.createElement(MEASUREMENT_NAME);
		informationElement.appendChild(nameElement);
		nameElement.setAttribute(ATTR_VALUE, measurement.getName());
		Element descriptionElement = document.createElement(MEASUREMENT_DESCRIPTION);
		informationElement.appendChild(descriptionElement);
		descriptionElement.setTextContent(measurement.getMetadata().getDescription());
		for(MetadataProperty metadataProperty : measurement.getMetadata().getMetadataProperties())
		{
			Element metadataElement = document.createElement(METADATA);
			metadataElement.setAttribute(ATTR_PROPERTY, metadataProperty.getName());
			metadataElement.setAttribute(ATTR_VALUE, metadataProperty.getValue());
			informationElement.appendChild(metadataElement);
		}
		
		// Write scope settings
		Element scopeSettingsElement = document.createElement(SCOPE_SETTINGS);
		rootElement.appendChild(scopeSettingsElement);

		for(Device device : microscope.getDevices())
		{
			String deviceName = device.getDeviceID();
			for(Property property : device.getProperties())
			{
				Element scopeSettingElement = document.createElement(SCOPE_SETTING);
				scopeSettingsElement.appendChild(scopeSettingElement);
				scopeSettingElement.setAttribute(ATTR_DEVICE, deviceName);
				scopeSettingElement.setAttribute(ATTR_PROPERTY, property.getPropertyID());
				scopeSettingElement.setAttribute(ATTR_VALUE, property.getValue());
			}
		}

		// Write channels
		Element configGroupsElement = document.createElement(CHANNEL_GROUPS);
		rootElement.appendChild(configGroupsElement);
		String[] configGroups = microscope.getChannelManager().getChannelGroupIDs();
		for(String configGroup : configGroups)
		{
			Element channelsElement = document.createElement(CHANNELS);
			channelsElement.setAttribute(ATTR_CONFIG_GROUP_NAME, configGroup);

			configGroupsElement.appendChild(channelsElement);
			Channel[] channels = microscope.getChannelManager().getChannels(configGroup);
			for(int i = 0; i < channels.length; i++)
			{
				Element channelElement = document.createElement(CHANNEL);
				channelsElement.appendChild(channelElement);
				channelElement.setAttribute(ATTR_CHANNEL_NAME, channels[i].getChannelID());
				DeviceSetting[] channelSettings = channels[i].getChannelOnSettings();
				for(int j = 0; j < channelSettings.length; j++)
				{
					Element channelSettingElement = document.createElement(CHANNEL_SETTING_ON);
					channelElement.appendChild(channelSettingElement);
					channelSettingElement.setAttribute(ATTR_DEVICE, channelSettings[j].getDevice());
					channelSettingElement.setAttribute(ATTR_PROPERTY, channelSettings[j].getProperty());
					channelSettingElement.setAttribute(ATTR_VALUE, channelSettings[j].getStringValue());
				}

				channelSettings = channels[i].getChannelOffSettings();
				for(int j = 0; j < channelSettings.length; j++)
				{
					Element channelSettingElement = document.createElement(CHANNEL_SETTING_OFF);
					channelElement.appendChild(channelSettingElement);
					channelSettingElement.setAttribute(ATTR_DEVICE, channelSettings[j].getDevice());
					channelSettingElement.setAttribute(ATTR_PROPERTY, channelSettings[j].getProperty());
					channelSettingElement.setAttribute(ATTR_VALUE, channelSettings[j].getStringValue());
				}
			}
		}

		return document;
	}

	public static void writeDocumentToFile(Document document, String file) throws Exception
	{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(document);
		FileOutputStream outputStream = null;
		File folder = new File(file).getParentFile();
		if(!folder.exists())
			folder.mkdirs();
		try
		{
			outputStream = new FileOutputStream(file);
			StreamResult result = new StreamResult(outputStream);
			transformer.transform(source, result);
		}
		finally
		{
			if(outputStream != null)
			{
				try {
					outputStream.close();
				} catch (@SuppressWarnings("unused") IOException e) {
					// do nothing.
				}
			}
		}
		
	}

	public static boolean saveDescription(MeasurementImpl measurement, Microscope microscope, String file)
	{
		Document document;
		try
		{
			document = createDescription(measurement, microscope);
			writeDocumentToFile(document, file);
		}
		catch(Exception e)
		{
			ServerSystem.err.println("Could not save measurement description XML file.", e);
			return false;
		}

		return true;
	}
}
