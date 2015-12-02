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

package ch.ethz.csb.youscope.shared.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ServiceLoader;

import ch.ethz.csb.youscope.shared.Microplate;
import ch.ethz.csb.youscope.shared.Well;
import ch.ethz.csb.youscope.shared.addon.XMLConfigurationProvider;
import ch.ethz.csb.youscope.shared.configuration.FocusConfiguration;
import ch.ethz.csb.youscope.shared.configuration.ImageFolderStructure;
import ch.ethz.csb.youscope.shared.configuration.JobConfiguration;
import ch.ethz.csb.youscope.shared.configuration.MeasurementConfiguration;
import ch.ethz.csb.youscope.shared.configuration.Period;
import ch.ethz.csb.youscope.shared.configuration.RegularPeriod;
import ch.ethz.csb.youscope.shared.configuration.TaskConfiguration;
import ch.ethz.csb.youscope.shared.configuration.VaryingPeriodDTO;
import ch.ethz.csb.youscope.shared.measurement.MeasurementSaveSettings;
import ch.ethz.csb.youscope.shared.microscope.DeviceSettingDTO;

import com.thoughtworks.xstream.XStream;

/**
 * This class exposes methods which load/save configuration of a measurement.
 * @author gpawel
 */
public class ConfigurationManagement
{
	/**
	 * Loads measurement configuration from given file.
	 * 
	 * @param fileName
	 *            path to configuration file.
	 * @return configuration loaded from file
	 * @throws IOException
	 */
	public static MeasurementConfiguration loadConfiguration(String fileName) throws IOException
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(fileName);
			XStream xstream = getSerializerInstance();
			return (MeasurementConfiguration)xstream.fromXML(fis);
		}
		finally
		{
			if(fis != null)
				fis.close();
		}
	}

	/**
	 * Saves given measurement configuration under given file name.
	 * 
	 * @param fileName
	 *            destination file name (path) for configuration
	 * @param measurement
	 *            configuration to save
	 * @throws IOException
	 */
	public static void saveConfiguration(String fileName, MeasurementConfiguration measurement) throws IOException
	{
		XStream xstream = getSerializerInstance();

		FileOutputStream fos = null;
		OutputStreamWriter writer = null;
		try
		{
			fos = new FileOutputStream(new File(fileName));
			writer = new OutputStreamWriter(fos, "UTF-8");
			writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
			xstream.toXML(measurement, writer);
		}
		finally
		{
			if(fos != null)
				fos.close();
			if(writer != null)
				writer.close();
		}
	}

	private static XStream getSerializerInstance() throws IOException
	{
		XStream xstream = new XStream();
		xstream.aliasSystemAttribute("type", "class");

		// First, process the annotations of the classes in this package.
		xstream.processAnnotations(new Class<?>[] {MeasurementSaveSettings.class, Well.class, Microplate.class, FocusConfiguration.class, FocusConfiguration.class, ImageFolderStructure.class, JobConfiguration.class, MeasurementConfiguration.class, Period.class, RegularPeriod.class, TaskConfiguration.class, VaryingPeriodDTO.class, DeviceSettingDTO.class});

		// Now, process all classes provided by the service providers
		ServiceLoader<XMLConfigurationProvider> xmlConfigurationProviders = ServiceLoader.load(XMLConfigurationProvider.class, ConfigurationManagement.class.getClassLoader());

		for(XMLConfigurationProvider provider : xmlConfigurationProviders)
		{
			try
			{
				xstream.processAnnotations(provider.getConfigurationClasses().toArray(new Class<?>[0]));
				
			}
			catch(Throwable e)
			{
				throw new IOException("Cannot process addon provider " + provider.getClass().getName() + ". Repair or delete respective plug-in.", e);
			}
		}

		return xstream;
	}
}
