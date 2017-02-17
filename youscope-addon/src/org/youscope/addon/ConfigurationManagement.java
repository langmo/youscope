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
/* Copyright 2011 ETH Zuerich, CISD
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. */

package org.youscope.addon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ServiceLoader;

import org.youscope.addon.component.ComponentAddonFactory;
import org.youscope.addon.component.ComponentMetadata;
import org.youscope.addon.measurement.MeasurementAddonFactory;
import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.FocusConfiguration;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.MetadataProperty;
import org.youscope.common.Well;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.saving.SaveSettingsConfiguration;
import org.youscope.common.task.PeriodConfiguration;
import org.youscope.common.task.RegularPeriodConfiguration;
import org.youscope.common.task.TaskConfiguration;
import org.youscope.common.task.VaryingPeriodConfiguration;

import com.thoughtworks.xstream.XStream;

/**
 * This class exposes methods which load/save configurations.
 * @author gpawel
 */
public class ConfigurationManagement
{
	/**
	 * Loads configuration from given file.
	 * 
	 * @param fileName
	 *            path to configuration file.
	 * @return configuration loaded from file
	 * @throws IOException
	 */
	public static Configuration loadConfiguration(String fileName) throws IOException
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(fileName);
			XStream xstream = getSerializerInstance();
			return (Configuration)xstream.fromXML(fis);
		}
		finally
		{
			if(fis != null)
				fis.close();
		}
	}

	/**
	 * Saves given configuration under given file name.
	 * 
	 * @param fileName
	 *            destination file name (path) for configuration
	 * @param configuration
	 *            configuration to save
	 * @throws IOException
	 */
	public static void saveConfiguration(String fileName, Configuration configuration) throws IOException
	{
		XStream xstream = getSerializerInstance();

		File folder = new File(fileName).getParentFile();
		if(!folder.exists())
			folder.mkdirs();
		
		FileOutputStream fos = null;
		OutputStreamWriter writer = null;
		try
		{
			fos = new FileOutputStream(new File(fileName));
			writer = new OutputStreamWriter(fos, "UTF-8");
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			writer.flush();
			xstream.toXML(configuration, writer);
		}
		finally
		{
			if(fos != null)
				fos.close();
			if(writer != null)
				writer.close();
		}
	}

	private static XStream singleton = null;
	private synchronized static XStream getSerializerInstance() throws IOException
	{
		if(singleton != null)
			return singleton;
		XStream xstream = new XStream();
		xstream.aliasSystemAttribute("type", "class");

		// First, process the annotations of the classes in this package.
		xstream.processAnnotations(new Class<?>[] {SaveSettingsConfiguration.class, Well.class, FocusConfiguration.class, JobConfiguration.class, MeasurementConfiguration.class, PeriodConfiguration.class, RegularPeriodConfiguration.class, TaskConfiguration.class, VaryingPeriodConfiguration.class, DeviceSetting.class, MetadataProperty.class});

		// Now, process all classes provided by the service providers
		ServiceLoader<ComponentAddonFactory> componentAddonFactories = ServiceLoader.load(ComponentAddonFactory.class, ConfigurationManagement.class.getClassLoader());
		for(ComponentAddonFactory provider : componentAddonFactories)
		{
			for(String typeIdentifier : provider.getSupportedTypeIdentifiers())
			{
				ComponentMetadata<?> metadata;
				try {
					metadata = provider.getComponentMetadata(typeIdentifier);
				} catch (@SuppressWarnings("unused") AddonException e) {
					continue;
				}
				xstream.processAnnotations(metadata.getConfigurationClass());
			}
			
		}
		ServiceLoader<MeasurementAddonFactory> measurementAddonFactories = ServiceLoader.load(MeasurementAddonFactory.class, ConfigurationManagement.class.getClassLoader());
		for(MeasurementAddonFactory provider : measurementAddonFactories)
		{
			for(String typeIdentifier : provider.getSupportedTypeIdentifiers())
			{
				ComponentMetadata<?> metadata;
				try {
					metadata = provider.getComponentMetadata(typeIdentifier);
				} catch (@SuppressWarnings("unused") AddonException e) {
					continue;
				}
				xstream.processAnnotations(metadata.getConfigurationClass());
			}
			
		}

		return xstream;
	}
}
