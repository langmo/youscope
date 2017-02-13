package org.youscope.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.youscope.common.MetadataProperty;
import org.youscope.common.microscope.Channel;
import org.youscope.common.microscope.Device;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.microscope.Microscope;
import org.youscope.common.microscope.MicroscopeException;
import org.youscope.common.microscope.Property;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.DomDriver;

class InformationSaver 
{
	@XStreamAlias("measurement-information")
	static class Measurement
	{
		@XStreamAlias("name")
		final String name;
		
		@XStreamAlias("description")
		final String description;
		
		@XStreamAlias("metadata-properties")
		final MetadataProperty[] metadataProperties;
		
		@XStreamAlias("device-settings-at-startup")
		final ScopeDevice[] scopeDevices;
		
		@XStreamAlias("channel-definitions")
		final ScopeChannel[] scopeChannels;
		Measurement(MeasurementImpl measurement, Microscope microscope)
		{
			name = measurement.getName();
			description = measurement.getMetadata().getDescription();
			metadataProperties = measurement.getMetadata().getMetadataProperties();
			
			ArrayList<ScopeDevice> tempScopeDevices = new ArrayList<>();
			try {
				for(Device device : microscope.getDevices())
				{
					try {
						tempScopeDevices.add(new ScopeDevice(device));
					} catch (@SuppressWarnings("unused") RemoteException e) {
						// do nothing, just less info.
						continue;
					}
				}
			} catch (@SuppressWarnings("unused") RemoteException e) {
				// do nothing, just less info.
			}
			scopeDevices = tempScopeDevices.toArray(new ScopeDevice[tempScopeDevices.size()]);
			
			ArrayList<ScopeChannel> tempScopeChannels = new ArrayList<>();
			try {
				for(Channel channel : microscope.getChannelManager().getChannels())
				{
					tempScopeChannels.add(new ScopeChannel(channel));
				}
			} catch (@SuppressWarnings("unused") RemoteException e) {
				// do nothing, just less info.
			}
			scopeChannels = tempScopeChannels.toArray(new ScopeChannel[tempScopeChannels.size()]);
		}
	}
	
	static class DescriptionConverter implements Converter
	{
		private boolean paragraph = false;
		private boolean list = false;
		@Override
		public boolean canConvert(@SuppressWarnings("rawtypes") Class clazz) {
			return clazz.equals(String.class);
		}

		@Override
		public void marshal(Object value, HierarchicalStreamWriter writer,
                MarshallingContext context) 
		{
			paragraph = false;
			list = false;
			String[] lines = ((String)value).split("\n");
			StringBuilder content = new StringBuilder();
			for(int lineID = 0; lineID < lines.length; lineID++)
			{
				String line = lines[lineID].trim();
				if(line.isEmpty())
				{
					content = endList(content, writer);
					content = endParagraph(content, writer);
				}
				else if(line.startsWith("-"))
				{
					line = line.substring(1).trim();
					content = endParagraph(content, writer);
					content = beginList(content, writer);
					writer.startNode("li");
					writer.setValue(line);
					writer.endNode(); // li
					content = new StringBuilder();
				}
				else
				{
					content = endList(content, writer);
					content = beginParagraph(content, writer);
					content.append(line);
				}		
			}
			
			content = endList(content, writer);
			content = endParagraph(content, writer);			
		}
		
		private StringBuilder beginParagraph(StringBuilder content, HierarchicalStreamWriter writer)
		{
			if(paragraph)
				return content.append(" ");
			writer.startNode("p");
			paragraph = true;
			return new StringBuilder();
		}
		
		private StringBuilder endParagraph(StringBuilder content, HierarchicalStreamWriter writer)
		{
			if(!paragraph)
				return content;
			writer.setValue(content.toString());
			writer.endNode(); // p
			paragraph = false;
			return new StringBuilder();
		}
		
		
		private StringBuilder beginList(StringBuilder content, HierarchicalStreamWriter writer)
		{
			if(list)
				return content;
			writer.startNode("ul");
			list = true;
			return new StringBuilder();
		}
		private StringBuilder endList(StringBuilder content, HierarchicalStreamWriter writer)
		{
			if(!list)
				return content;
			writer.endNode(); // ul
			list = false;
			return new StringBuilder();
		}
		
		@Override
		public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
			// we don't have to do anything: we are only writing XML objects, but not reading them.
			return null;
		}
		
	}
	@XStreamAlias("channel-definition")
	static class ScopeChannel
	{
		@XStreamAlias("name")
		@XStreamAsAttribute
		final String name;
		@XStreamAlias("group")
		@XStreamAsAttribute
		final String channelGroup;
		@XStreamAlias("device-settings-on")
		final ScopeChannelSetting[] settingsOn;
		@XStreamAlias("device-settings-off")
		final ScopeChannelSetting[] settingsOff;
		@XStreamAlias("shutter-delay")
		@XStreamAsAttribute
		final int delay;
		@XStreamAlias("shutter-name")
		@XStreamAsAttribute
		final String shutter;
		ScopeChannel(Channel channel) throws RemoteException
		{
			name = channel.getChannelID();
			channelGroup = channel.getChannelGroupID();
			delay = channel.getChannelTimeout();
			shutter = channel.getShutter();			
			
			ArrayList<ScopeChannelSetting> tempSettingsOn = new ArrayList<>();
			try {
				for(DeviceSetting  setting : channel.getChannelOnSettings())
				{
					tempSettingsOn.add(new ScopeChannelSetting(setting));
				}
			} catch (@SuppressWarnings("unused") RemoteException e1) {
				// do nothing, just less info.
			}
			settingsOn = tempSettingsOn.toArray(new ScopeChannelSetting[tempSettingsOn.size()]);
			
			ArrayList<ScopeChannelSetting> tempSettingsOff = new ArrayList<>();
			try {
				for(DeviceSetting  setting : channel.getChannelOffSettings())
				{
					tempSettingsOff.add(new ScopeChannelSetting(setting));
				}
			} catch (@SuppressWarnings("unused") RemoteException e) {
				// do nothing, just less info.
			}
			settingsOff = tempSettingsOff.toArray(new ScopeChannelSetting[tempSettingsOff.size()]);
		}
	}
	@XStreamAlias("device-setting")
	static class ScopeChannelSetting
	{
		@XStreamAlias("device")
		@XStreamAsAttribute
		final String device;
		@XStreamAlias("property")
		@XStreamAsAttribute
		final String property;
		@XStreamAlias("value")
		@XStreamAsAttribute
		final String value;
		@XStreamAlias("is-absolute-value")
		@XStreamAsAttribute
		final boolean absoluteValue;
		ScopeChannelSetting(DeviceSetting setting)
		{
			device = setting.getDevice();
			property = setting.getProperty();
			value = setting.getStringValue();
			absoluteValue = setting.isAbsoluteValue();
		}
	}
	@XStreamAlias("device")
	static class ScopeDevice
	{
		@XStreamAlias("name")
		@XStreamAsAttribute
		final String name;
		@XStreamAlias("properties")
		@XStreamImplicit(itemFieldName="property")
		final ScopeDeviceSetting[] deviceSettings;
		ScopeDevice(Device device) throws RemoteException
		{
			name = device.getDeviceID();
			Property[] properties = device.getProperties();
			ArrayList<ScopeDeviceSetting> tempDeviceSettings = new ArrayList<>(properties.length);
			for(int i=0; i<properties.length; i++)
			{
				try {
					tempDeviceSettings.add(new ScopeDeviceSetting(properties[i]));
				} catch (@SuppressWarnings("unused") RemoteException | MicroscopeException | InterruptedException e) {
					// do nothing, just less info.
					continue;
				}
			}
			deviceSettings = tempDeviceSettings.toArray(new ScopeDeviceSetting[tempDeviceSettings.size()]);
		}
	}
	@XStreamAlias("property")
	static class ScopeDeviceSetting
	{
		@XStreamAlias("name")
		@XStreamAsAttribute
		final String property;
		@XStreamAlias("value")
		@XStreamAsAttribute
		final String value;
		public ScopeDeviceSetting(Property prop) throws RemoteException, MicroscopeException, InterruptedException {
			property = prop.getPropertyID();
			value = prop.getValue();
		}
	}
	private XStream getSerializerInstance()
	{
		XStream xstream = new XStream(new DomDriver("UTF-8"));
		xstream.aliasSystemAttribute("type", "class");
		xstream.registerLocalConverter(Measurement.class, "description", new DescriptionConverter());
		// Process the annotations of the classes needed to know.
		xstream.processAnnotations(new Class<?>[] {MetadataProperty.class, InformationSaver.ScopeChannel.class, InformationSaver.ScopeChannelSetting.class, InformationSaver.ScopeDevice.class, InformationSaver.Measurement.class, InformationSaver.ScopeDeviceSetting.class});
		return xstream;
	}
	public void saveXMLInformation(MeasurementImpl measurement, Microscope microscope, String xmlFileName) throws IOException
	{
		// Write XML file
		Measurement root = new Measurement(measurement, microscope);
		XStream xstream = getSerializerInstance();
		File xmlFile = new File(xmlFileName);
		File xmlFolder = xmlFile.getParentFile();
		if(!xmlFolder.exists())
			xmlFolder.mkdirs();
		try(FileOutputStream fos = new FileOutputStream(xmlFile))
		{
			fos.write(new String("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n").getBytes()); 
			fos.flush();
			xstream.toXML(root, fos);
		} 		
	}
	public void saveHTMLInformation(String xmlFileName, String htmlFileName) throws FileNotFoundException, IOException, SAXException, TransformerException, ParserConfigurationException
	{
		File xmlFile = new File(xmlFileName);
		// Convert to HTML
		File htmlFile = new File(htmlFileName);
		File htmlFolder = htmlFile.getParentFile();
		if(!htmlFolder.exists())
			htmlFolder.mkdirs();
		
		copyDefaultStyle();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try(FileOutputStream fos = new FileOutputStream(htmlFile))
        {
            File stylesheet = new File(INFORMATION_STYLE_FILE);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            StreamSource stylesource = new StreamSource(stylesheet);
            Transformer transformer = tFactory.newTransformer(stylesource);

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(fos);
            transformer.transform(source, result);
        } 
	
	}
	
	private static final String DEFAULT_INFORMATION_STYLE_RESOURCE = "org/youscope/server/information_style.xsl";
	private static final String INFORMATION_STYLE_FILE = "configuration/information_style.xsl";
	private void copyDefaultStyle() throws IOException
	{
		File dest = new File(INFORMATION_STYLE_FILE).getAbsoluteFile();
		if(dest.exists())
			return;
		File folder = dest.getParentFile();
		if(!folder.exists())
			folder.mkdirs();
		// Copy library from jar achieve to the file system
        try(InputStream inputStream= InformationSaver.class.getClassLoader().getResourceAsStream(DEFAULT_INFORMATION_STYLE_RESOURCE);
        		FileOutputStream fileOutputStream = new FileOutputStream(dest))
        {
        	byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) > 0)
            {
                fileOutputStream.write(buffer, 0, bytesRead);
            }
            fileOutputStream.close();
        } 
	}
}
