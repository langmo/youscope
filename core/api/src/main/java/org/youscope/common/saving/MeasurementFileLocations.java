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
package org.youscope.common.saving;

import java.io.Serializable;

/**
 * Provides information where the meta files of a measurement are stored.
 * @author Moritz Lang
 *
 */
public class MeasurementFileLocations implements Serializable 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 139851674370268614L;
	private final String imageTablePath;
	private final String measurementConfigurationFilePath;
	private final String logErrFilePath;
	private final String logOutFilePath;
	private final String microscopeConfigurationFilePath;
	private final String measurementBaseFolder;
	private final String xmlInformationPath;
	private final String htmlInformationPath;
	/**
	 * Constructor.
	 * @param measurementBaseFolder Base folder of the measurement.
	 * @param imageTablePath Path to the image table.
	 * @param measurementConfigurationFilePath Path to the measurement configuration
	 * @param logErrFilePath Path to the error log file.
	 * @param logOutFilePath Path to the normal log file.
	 * @param microscopeConfigurationFilePath path to the microscope configuration file.
	 * @param xmlInformationPath Path to the measurement information as XML.
	 * @param htmlInformationPath Path to the measurement information as HTML.
	 */
	public MeasurementFileLocations(String measurementBaseFolder, String imageTablePath, String measurementConfigurationFilePath, String logErrFilePath, String logOutFilePath, String microscopeConfigurationFilePath, String xmlInformationPath, String htmlInformationPath)
	{
		this.measurementBaseFolder =measurementBaseFolder;
		this.imageTablePath = imageTablePath;
		this.measurementConfigurationFilePath = measurementConfigurationFilePath;
		this.logErrFilePath = logErrFilePath;
		this.logOutFilePath = logOutFilePath;
		this.microscopeConfigurationFilePath = microscopeConfigurationFilePath;
		this.xmlInformationPath = xmlInformationPath;
		this.htmlInformationPath = htmlInformationPath;
	}
	
	/**
	 * Base measurement folder.
	 * @return main folder of measurement.
	 */
	public String getMeasurementBaseFolder()
	{
		return measurementBaseFolder;
	}
	/**
	 * Path to the image table.
	 * @return image table path.
	 */
	public String getImageTablePath()
	{
		return imageTablePath;
	}
	/**
	 * Path to the measurement configuration file.
	 * @return measurement configuration file
	 */
	public String getMeasurementConfigurationFilePath()
	{
		return measurementConfigurationFilePath;
	}
	/**
	 * Path to the error log.
	 * @return error log file.
	 */
	public String getLogErrFilePath()
	{
		return logErrFilePath;
	}
	/**
	 * Path to the normal log file.
	 * @return normal log file.
	 */
	public String getLogOutFilePath()
	{
		return logOutFilePath;
	}
	/**
	 * Path to the microscope configuration.
	 * @return microscope configuration path.
	 */
	public String getMicroscopeConfigurationFilePath()
	{
		return microscopeConfigurationFilePath;
	}

	/**
	 * Path to the measurement information as XML.
	 * @return measurement information file.
	 */
	public String getXmlInformationPath() {
		return xmlInformationPath;
	}

	/**
	 * Path to the measurement information as HTML.
	 * @return measurement information file.
	 */
	public String getHtmlInformationPath() {
		return htmlInformationPath;
	}

}
