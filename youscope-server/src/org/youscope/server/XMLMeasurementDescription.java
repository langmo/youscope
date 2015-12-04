/**
 * 
 */
package org.youscope.server;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceSettingDTO;
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

	public static final String	SCOPE_SETTINGS				= "scope-settings";

	public static final String	SCOPE_SETTING				= "scope-setting";

	public static final String	CHANNEL_GROUPS				= "channel-groups";

	public static final String	CHANNELS					= "channels";

	public static final String	CHANNEL						= "channel";

	public static final String	CHANNEL_SETTING_ON			= "channel-setting-on";

	public static final String	CHANNEL_SETTING_OFF			= "channel-setting-off";

	public static final String	ATTR_CHANNEL_NAME			= "name";

	public static final String	ATTR_CONFIG_GROUP_NAME		= "name";

	public static final String	IMAGING						= "imaging";

	public static final String	IMAGES						= "images";

	public static final String	IMAGE						= "image";

	public static final String	IMAGING_PERIOD				= "imaging-period";

	public static final String	ATTR_PERIOD_PERIOD			= "period";

	public static final String	ATTR_PERIOD_UNIT			= "unit";

	public static final String	ATTR_CHANNEL				= "channel";

	public static final String	ATTR_EXPOSURE				= "exposure";

	public static final String	ATTR_EXPOSURE_UNIT			= "exposure-unit";

	public static final String	ATTR_PERIOD_TYPE			= "type";

	public static final String	ATTRVAL_PERIOD_BURST		= "burst";

	public static final String	ATTRVAL_PERIOD_TIME_LAPS	= "time-laps";

	public static final String	ATTRVAL_PERIOD_UNKNOWN		= "unknown";

	public static final String	ATTRVAL_MS					= "ms";

	public static final String	ATTR_DEVICE					= "device";

	public static final String	ATTR_PROPERTY				= "property";

	public static final String	ATTR_VALUE					= "value";

	/**
	 * Creates description for measurement.
	 * @param measurementConfiguration
	 * @param microscope
	 * @return XML document containing description.
	 * @throws ParserConfigurationException
	 * @throws RemoteException
	 * @throws DOMException
	 * @throws MicroscopeException
	 * @throws InterruptedException
	 * @throws SettingException
	 */
	public static Document createDescription(MeasurementConfiguration measurementConfiguration, Microscope microscope) throws ParserConfigurationException, RemoteException, DOMException, MicroscopeException, InterruptedException, SettingException
	{
		// Create new XML Document
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.newDocument();

		// Add root Element.
		Element rootElement = document.createElement(ROOT_NAME);
		document.appendChild(rootElement);

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
				DeviceSettingDTO[] channelSettings = channels[i].getChannelOnSettings();
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

		// Write measurement
		Element imagingElement = document.createElement(IMAGING);
		rootElement.appendChild(imagingElement);
		Element imagesElement = document.createElement(IMAGES);
		imagingElement.appendChild(imagesElement);

		return document;
	}

	public static void writeDocumentToFile(Document document, String file) throws TransformerException, FileNotFoundException
	{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		DOMSource source = new DOMSource(document);
		FileOutputStream outputStream = null;
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

	public static boolean saveDescription(MeasurementConfiguration microtiterConfiguration, Microscope microscope, String file)
	{
		Document document;
		try
		{
			document = createDescription(microtiterConfiguration, microscope);
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
