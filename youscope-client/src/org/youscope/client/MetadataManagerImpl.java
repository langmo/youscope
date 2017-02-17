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
package org.youscope.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.youscope.clientinterfaces.MetadataDefinition;
import org.youscope.clientinterfaces.MetadataDefinitionManager;
import org.youscope.clientinterfaces.YouScopeClientException;
import org.youscope.common.util.TextTools;

import com.thoughtworks.xstream.XStream;

class MetadataManagerImpl extends HashMap<String, MetadataDefinition> implements MetadataDefinitionManager 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 64314818791363585L;
	private static final String PROPERTY_ALLOW_CUSTOM_PROPERTIES = "Youscope.Client.AllowCustomProperties";
	private static final String METADATA_FOLDER_NAME = "configuration"+File.separator+"metadata";
	private static MetadataManagerImpl instance = null; 
	private MetadataManagerImpl() 
	{
		 // do nothing.
	}
	public synchronized static MetadataManagerImpl getInstance()
	{
		if(instance == null)
		{ 
			instance = new MetadataManagerImpl();
			if(!instance.loadMetadataDefinitions())
			{
				// means folder does not exist, that is, this is the first time we get invoked.
				// Generate some new definitions, and save them (folder gets created while doing so...)
				for(MetadataDefinition defaultDefinition : generateDefaults())
				{
					try {
						instance.setMetadataDefinition(defaultDefinition);
					} 
					catch (@SuppressWarnings("unused") YouScopeClientException e) {
						// do nothing. The user can well live without defaults...
					}
				}
				
			}
		}
		return instance;
	}

	private static Collection<MetadataDefinition> generateDefaults()
	{
		ArrayList<MetadataDefinition> result = new ArrayList<>();
		result.add(new MetadataDefinition("Temperature", MetadataDefinition.Type.DEFAULT , true,0,100,null, "°C"));
		result.add(new MetadataDefinition("User", MetadataDefinition.Type.OPTIONAL, true));
		result.add(new MetadataDefinition("Species", MetadataDefinition.Type.DEFAULT, true, getModelOrganisms()));
		result.add(new MetadataDefinition("Strain", MetadataDefinition.Type.DEFAULT, true));
		return result;
	}
	private static String[] getModelOrganisms()
	{
		String[] result = new String[]
		{
				"Escherichia coli",
				"Dictyostelium discoideum",
				"Saccharomyces cerevisiae",
				"Schizosaccharomyces pombe",
				"Chlamydomonas reinhardtii",
				"Tetrahymena thermophila",
				"Emiliania huxleyi",
				"Caenorhabditis elegans",
				"Drosophila melanogaster",
				"Arabidopsis thaliana",
				"Physcomitrella patens",
				"Danio rerio",
				"Fundulus heteroclitus",
				"Nothobranchius furzeri",
				"Oryzias latipes",
				"Anolis carolinensis",
				"Mus musculus",
				"Xenopus laevis"
		};
		Arrays.sort(result);
		return result;
	}
	
	@Override
	public boolean isAllowCustomMetadata() {
		return PropertyProviderImpl.getInstance().getProperty(PROPERTY_ALLOW_CUSTOM_PROPERTIES, true);
	}

	@Override
	public Iterator<MetadataDefinition> iterator() {
		return getMetadataDefinitions().iterator();
	}
	
	@Override
	public Collection<MetadataDefinition> getMetadataDefinitions() {
		ArrayList<MetadataDefinition> result = new ArrayList<MetadataDefinition>(values());
		Collections.sort(result);
		return result;
	}

	@Override
	public Collection<MetadataDefinition> getMandatoryMetadataDefinitions() {
		ArrayList<MetadataDefinition> result = new ArrayList<MetadataDefinition>(values());
		for(Iterator<MetadataDefinition> iter = result.iterator(); iter.hasNext();)
		{
			if(iter.next().getType() != MetadataDefinition.Type.MANDATORY)
				iter.remove();
		}
		Collections.sort(result);
		return result;
	}
	
	@Override
	public Collection<MetadataDefinition> getDefaultMetadataDefinitions() {
		ArrayList<MetadataDefinition> result = new ArrayList<MetadataDefinition>(values());
		for(Iterator<MetadataDefinition> iter = result.iterator(); iter.hasNext();)
		{
			MetadataDefinition.Type type = iter.next().getType();
			if(type != MetadataDefinition.Type.MANDATORY && type != MetadataDefinition.Type.DEFAULT)
				iter.remove();
		}
		Collections.sort(result);
		return result;
	}

	@Override
	public MetadataDefinition getMetadataDefinition(String name) {
		return get(name);
	}

	private XStream getSerializerInstance()
	{
		XStream xstream = new XStream();
		xstream.aliasSystemAttribute("type", "class");
		// Process the annotations of the classes needed to know.
		xstream.processAnnotations(new Class<?>[] {MetadataDefinition.class, MetadataDefinition.Type.class});
		return xstream;
	}
	private boolean loadMetadataDefinitions()
	{
		File folder = new File(METADATA_FOLDER_NAME);
		if(!folder.exists() || !folder.isDirectory())
			return false;
		File[] files = folder.listFiles(new FilenameFilter()
		{

			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
			
		});
		XStream xstream = getSerializerInstance();
		for(File file : files)
		{
			try(FileInputStream fis = new FileInputStream(file);)
			{
				MetadataDefinition definition = (MetadataDefinition)xstream.fromXML(fis);
				put(definition.getName(), definition);
			} catch (Throwable e) {
				ClientSystem.err.println("Could not load measurement metadata definition file "+file.getAbsolutePath()+".", e);
			}
		}
		return true;
	}
	@Override
	public void setMetadataDefinition(MetadataDefinition property) throws YouScopeClientException
	{
		XStream xstream = getSerializerInstance();

		File folder = new File(METADATA_FOLDER_NAME);
		if(!folder.exists())
			folder.mkdirs();
		File file = new File(folder, TextTools.convertToFileName(property.getName())+".xml");
		try(FileOutputStream fos = new FileOutputStream(file))
		{
			fos.write(new String("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n").getBytes()); 
			fos.flush();
			
			xstream.toXML(property, fos); 
		} 
		catch (IOException e) 
		{
			throw new YouScopeClientException("Could not save measurement metadata configuration " + property.getName()+" to file "+file.getAbsolutePath()+".", e);
		} 		
		
		put(property.getName(), property);  
	}

	@Override
	public boolean deleteMetadataDefinition(String name) 
	{
		if(remove(name) == null)
			return false;
		File folder = new File(METADATA_FOLDER_NAME);
		if(!folder.exists())
			return true;
		File file = new File(folder, TextTools.convertToFileName(name)+".xml");
		if(!file.exists())
			return true;
		file.delete();
		return true;
	}

	@Override
	public int getNumMetadataDefinitions() {
		return size();
	}
}
