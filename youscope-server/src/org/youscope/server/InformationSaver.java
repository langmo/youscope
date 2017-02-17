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
package org.youscope.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
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
import org.youscope.common.resource.ResourceException;
import org.youscope.server.MeasurementSaverImpl.SaverInformation;

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
	@XStreamAlias("file")
	static class MeasurementFile
	{
		@XStreamAlias("name")
		@XStreamAsAttribute
		final String name;
		@XStreamAlias("path")
		@XStreamAsAttribute
		final String path;
		MeasurementFile(String name, String path)
		{
			this.name = name;
			this.path = path;
		}
	}
	
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
		
		@XStreamAlias("files")
		final MeasurementFile[] files;
		
		@XStreamAlias("images")
		final File images;
		
		private static MeasurementFile[] initializeFiles(SaverInformation saverInformation, String xmlFilePath)
		{
			final ArrayList<MeasurementFile> files = new ArrayList<>();
			File xmlFolder = new File(xmlFilePath).getParentFile();			
			
			try
			{
				File file = new File(saverInformation.getFullImageTablePath());
				files.add(new MeasurementFile("image-table", xmlFolder.toURI().relativize(file.toURI()).getPath()));
			}
			catch(@SuppressWarnings("unused") RemoteException | ResourceException e)
			{
				// do nothing.
			}
				
			try
			{
				File file = new File(saverInformation.getFullLogErrFilePath());
				files.add(new MeasurementFile("error-log", xmlFolder.toURI().relativize(file.toURI()).getPath()));
			}
			catch(@SuppressWarnings("unused") RemoteException | ResourceException e)
			{
				// do nothing.
			}
			
			try
			{
				File file = new File(saverInformation.getFullLogOutFilePath());
				files.add(new MeasurementFile("log", xmlFolder.toURI().relativize(file.toURI()).getPath()));
			}
			catch(@SuppressWarnings("unused") RemoteException | ResourceException e)
			{
				// do nothing.
			}
			
			try
			{
				File file = new File(saverInformation.getFullMeasurementConfigurationFilePath());
				files.add(new MeasurementFile("measurement-configuration", xmlFolder.toURI().relativize(file.toURI()).getPath()));
			}
			catch(@SuppressWarnings("unused") RemoteException | ResourceException e)
			{
				// do nothing.
			}
			
			try
			{
				File file = new File(saverInformation.getFullMicroscopeConfigurationFilePath());
				files.add(new MeasurementFile("microscope-configuration", xmlFolder.toURI().relativize(file.toURI()).getPath()));
			}
			catch(@SuppressWarnings("unused") RemoteException | ResourceException e)
			{
				// do nothing.
			}
			return files.toArray(new MeasurementFile[files.size()]);
		}
		
		Measurement(MeasurementImpl measurement, Microscope microscope, SaverInformation saverInformation, String xmlFilePath)
		{
			name = measurement.getName();
			description = measurement.getMetadata().getDescription();
			metadataProperties = measurement.getMetadata().getMetadataProperties();
			files = initializeFiles(saverInformation, xmlFilePath);
			
			String imageTablePath;
			try {
				imageTablePath = saverInformation.getFullImageTablePath();
			} catch (@SuppressWarnings("unused") ResourceException | RemoteException e1) {
				imageTablePath = null;
			}
			images = imageTablePath == null ? null : new File(imageTablePath);
		
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
	class ImageTableIncluder implements Converter
	{

		@Override
		public boolean canConvert(@SuppressWarnings("rawtypes") Class clazz) {
			return clazz.equals(File.class);
		}

		@Override
		public void marshal(Object value, HierarchicalStreamWriter writer,
                MarshallingContext context) 
		{
			if(value == null || !(value instanceof File) || !((File)value).exists())
				return;
			URI xmlURI = new File(xmlFilePath).getParentFile().toURI();
			File tableFile = (File)value;
			File tableFolder = tableFile.getParentFile();
			try(FileReader fileReader = new FileReader(tableFile); BufferedReader br = new BufferedReader(fileReader);)
			{
				// ignore header line
				String line = br.readLine();
			    while(true) 
			    {
			    	line = br.readLine();
			    	if(line == null)
			    		break;
			    	String[] tokens = line.split(";");
			    	if(tokens.length < 12)
			    		continue;
			    	String path = xmlURI.relativize(new File(tableFolder, tokens[6].substring(1, tokens[6].length()-1)).toURI()).getPath();
			    	writer.startNode("image");
			    	writer.addAttribute("evaluation", tokens[0].substring(1, tokens[0].length()-1));
			    	writer.addAttribute("runtime", tokens[1].substring(1, tokens[1].length()-1));
			    	writer.addAttribute("time", tokens[2].substring(1, tokens[2].length()-1));
			    	writer.addAttribute("well", tokens[4].substring(1, tokens[4].length()-1));
			    	writer.addAttribute("position", tokens[5].substring(1, tokens[5].length()-1));
			    	writer.addAttribute("path", path);
			    	writer.addAttribute("name", tokens[7].substring(1, tokens[7].length()-1));
			    	writer.addAttribute("camera", tokens[8].substring(1, tokens[8].length()-1));
			    	writer.addAttribute("channel-group", tokens[9].substring(1, tokens[9].length()-1));
			    	writer.addAttribute("channel", tokens[10].substring(1, tokens[10].length()-1));
			    	writer.addAttribute("original-bit-depth", tokens[11].substring(1, tokens[11].length()-1));
			    	
			    	
			    	writer.endNode();
			        
			    }
			} catch (@SuppressWarnings("unused") IOException e) {
				return;
			}
		}

		@Override
		public Object unmarshal(HierarchicalStreamReader arg0, UnmarshallingContext arg1) {
			// we don't have to do anything: we are only writing XML objects, but not reading them.
			return null;
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
		xstream.registerLocalConverter(Measurement.class, "images", new ImageTableIncluder());
		// Process the annotations of the classes needed to know.
		xstream.processAnnotations(new Class<?>[] {MetadataProperty.class, InformationSaver.ScopeChannel.class, InformationSaver.ScopeChannelSetting.class, InformationSaver.ScopeDevice.class, InformationSaver.Measurement.class, InformationSaver.ScopeDeviceSetting.class});
		return xstream;
	}
	private final Measurement root;
	private final String xmlFilePath;
	private final String htmlFilePath;
	public InformationSaver(String xmlFilePath, String htmlFilePath, MeasurementImpl measurement, Microscope microscope, SaverInformation saverInformation)
	{
		this.xmlFilePath = xmlFilePath;
		this.htmlFilePath = htmlFilePath;
		root = new Measurement(measurement, microscope, saverInformation, xmlFilePath);
	}
	public void saveXMLInformation() throws IOException
	{
		// Write XML file
		XStream xstream = getSerializerInstance();
		File xmlFile = new File(xmlFilePath);
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
	public void saveHTMLInformation() throws FileNotFoundException, IOException, SAXException, TransformerException, ParserConfigurationException
	{
		if(htmlFilePath == null)
			return;
		File xmlFile = new File(xmlFilePath);
		// Convert to HTML
		File htmlFile = new File(htmlFilePath);
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
