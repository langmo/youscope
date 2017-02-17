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
package org.youscope.plugin.customsavesettings;

import org.youscope.common.configuration.Configuration;
import org.youscope.common.configuration.ConfigurationException;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Configuration of a custom save setting type.
 * @author mlang
 *
 */
@XStreamAlias("custom-save-setting-type")
public class CustomSaveSettingType implements Configuration 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 5993546712768193776L;
	
	private static final String TYPE_IDENTIFIER = "YouScope.CustomSaveSettingConfiguration";
	
	/**
	 * The folder where the measurement (information and images made during the measurement) should be stored.
	 * Set to null to let the user decide.
	 */
	@XStreamAlias("base-folder")
	private String					baseFolder					= null;
	
	@XStreamAlias("save-setting-name")
	private String saveSettingName = "unnamed";
	/**
	 * Get the name of this save setting type.
	 * @return Name of save setting type.
	 */
	public String getSaveSettingName() {
		return saveSettingName;
	}

	/**
	 * Sets the name of this save setting type.
	 * @param saveSettingName Name of type.
	 */
	public void setSaveSettingName(String saveSettingName) {
		this.saveSettingName = saveSettingName;
	}
	
	/**
	 * Returns the folder where all measurements should be saved. Returns null if the user should decide.
	 * @return the folder of the measurement.
	 */
	public String getBaseFolder()
	{
		return baseFolder;
	}

	/**
	 * Sets the folder where all measurements should be saved.
	 * Returns null if the user should decide.
	 * @param folder the folder of the measurement.
	 */
	public void setBaseFolder(String folder)
	{
		this.baseFolder = folder;
	}

	@Override
	public String getTypeIdentifier() {
		return TYPE_IDENTIFIER;
	}
	
	@Override
	public void checkConfiguration() throws ConfigurationException {
		// do nothing.
	}
	
	
	
	private String baseFolderExtension = "%xN/%4xy-%2xm-%2xd_%2xH-%2xM-%2xs";
	/**
     * Returns the name of the base folder/path where the measurement is saved. All other paths are given relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}. Has to contain some time related part with a high enough granularity such that no two runs of the same
     * measurement will have the same time identifier (typically implies at least granularity down down to the minute, if not to the second).  
     * @return Base path extension unique for every run of the measurement.
     */
	public String getBaseFolderExtension()
	{
		return baseFolderExtension;
	}
	/**
	 * Sets the name of the base folder/path where the measurement is saved. All other paths are given relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}. Has to contain some time related part with a high enough granularity such that no two runs of the same
     * measurement will have the same time identifier (typically implies at least granularity down down to the minute, if not to the second).  
	 * @param baseFolderExtension
	 */
	public void setBaseFolderExtension(String baseFolderExtension)
	{
		this.baseFolderExtension = baseFolderExtension;
	}
    
	private String measurementConfigurationFilePath = "configuration.csb"; 
	/**
     * Path of the file (with extension) where the measurement configuration should be saved into. Typically "configuration.csb". Note: by
     * convention, all measurement configurations should have the file extension "csb".
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
     * @return Measurement configuration file path.
     */
	public String getMeasurementConfigurationFilePath() {
		return measurementConfigurationFilePath;
	}
	/**
     * Path of the file (with extension) where the measurement configuration should be saved into. Typically "configuration.csb". Note: by
     * convention, all measurement configurations should have the file extension "csb".
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
	 * @param measurementConfigurationFilePath Measurement configuration file path.
     */
	public void setMeasurementConfigurationFilePath(String measurementConfigurationFilePath) {
		this.measurementConfigurationFilePath = measurementConfigurationFilePath;
	}

	private String microscopeConfigurationFilePath ="YSConfig_Microscope.cfg";
	/**
     * Path of the file (with extension) where the microscope configuration should be saved into. Typically "YSConfig_Microscope.cfg".
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
     * @return Microscope configuration file path.
     */
	public String getMicroscopeConfigurationFilePath() {
		return microscopeConfigurationFilePath;
	}
	/**
     * Path of the file (with extension) where the microscope configuration should be saved into. Typically "YSConfig_Microscope.cfg".
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
	 * @param microscopeConfigurationFilePath Microscope configuration file path.
     */
	public void setMicroscopeConfigurationFilePath(String microscopeConfigurationFilePath) {
		this.microscopeConfigurationFilePath = microscopeConfigurationFilePath;
	}

	private String logErrFilePath="measurement_err.txt";
	/**
     * Returns the file path for the error log file (with extension). Typically "measurement_err.txt".
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
     * @return Error file path.
     */
	public String getLogErrFilePath() {
		return logErrFilePath;
	}
	/**
     * Sets the file path for the error log file (with extension). Typically "measurement_err.txt".
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
	 * @param logErrFilePath Error file path.
     */
	public void setLogErrFilePath(String logErrFilePath) {
		this.logErrFilePath = logErrFilePath;
	}

	private String logOutFilePath ="measurement_log.txt";
	/**
     * Returns the file path for the standard output log file (with extension). Typically "measurement_log.txt".
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
     * @return Log file path.
     */
	public String getLogOutFilePath() {
		return logOutFilePath;
	}
	/**
     * Sets the file path for the standard output log file (with extension). Typically "measurement_log.txt".
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
	 * @param logOutFilePath  Log file path.
     */
	public void setLogOutFilePath(String logOutFilePath) {
		this.logOutFilePath = logOutFilePath;
	}

	private String tableFilePath = "%N.csv";
	/**
     * Returns a file path (with extension) where a table with a given name should be saved. By convention, the file type should be "csv".
     * Note that, different to images which are stored in one file per image, tables are usually stored one file for every named table.
     * Identifiers when and where a given row of the table is produced is automatically added by YouScope.
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
     * @return table file path.
     */
	public String getTableFilePath() {
		return tableFilePath;
	}
	/**
     * Sets a file path (with extension) where a table with a given name should be saved. By convention, the file type should be "csv".
     * Note that, different to images which are stored in one file per image, tables are usually stored one file for every named table.
     * Identifiers when and where a given row of the table is produced is automatically added by YouScope.
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
	 * @param tableFilePath table file path.
     */
	public void setTableFilePath(String tableFilePath) {
		this.tableFilePath = tableFilePath;
	}

	private String imageMetadataTableFilePath = "images.csv";
	/**
     * Returns a file path (with extension) where the table storing metadata about the images should be stored. Typically "images.csv". By convention,
     * the file type should be "csv".
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
     * @return image table path.
     */
	public String getImageMetadataTableFilePath() {
		return imageMetadataTableFilePath;
	}
	/**
     * Sets a file path (with extension) where the table storing metadata about the images should be stored. Typically "images.csv". By convention,
     * the file type should be "csv".
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
	 * @param imageMetadataTableFilePath mage table path.
     */
	public void setImageMetadataTableFilePath(String imageMetadataTableFilePath) {
		this.imageMetadataTableFilePath = imageMetadataTableFilePath;
	}

	private String imageFilePath="%N_position%4w%2p_time%4n.tif";
	/**
     * Returns a file path (with extension) where an image with a given name should be saved. The extension (e.g. "tif" for "gfp_pos01_time003.tif") should usually match
     * the return value of {@link #getImageExtension()}.
     * Note that, images are stored in one file per image. Therefore, the image path should be different for images produced by the same image producer
     * at a different iteration. Therefore, the image creation information stored in the image event should be used to determine the path.
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
     * @return image path, with extension.
     */
	public String getImageFilePath() 
	{
		return imageFilePath;
	}
	/**
     * Sets a file path (with extension) where an image with a given name should be saved. The extension (e.g. "tif" for "gfp_pos01_time003.tif") should usually match
     * the return value of {@link #getImageExtension()}.
     * Note that, images are stored in one file per image. Therefore, the image path should be different for images produced by the same image producer
     * at a different iteration. Therefore, the image creation information stored in the image event should be used to determine the path.
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
	 * @param imageFilePath image path, with extension.
     */
	public void setImageFilePath(String imageFilePath) 
	{
		this.imageFilePath = imageFilePath;
	}

	private String imageExtension = "tif";
	/**
     * Returns a extension/file type (without a dot, e.g. "tif" and not ".tif") of an image. Typically, this is the same as the extension of the file returned by
     * {@link #getImageFilePath()}. This extension is internally used to determine how the image should be saved, and should match
     * to a supported image type (for more, see YouScope server details).
     * @return image extension, without a dot.
     */
	public String getImageExtension() {
		return imageExtension;
	}
	/**
     * Sets a extension/file type (without a dot, e.g. "tif" and not ".tif") of an image. Typically, this is the same as the extension of the file returned by
     * {@link #getImageFilePath()}. This extension is internally used to determine how the image should be saved, and should match
     * to a supported image type (for more, see YouScope server details).
	 * @param imageExtension image extension, without a dot.
     */
	public void setImageExtension(String imageExtension) {
		this.imageExtension = imageExtension;
	}

	private String xmlInformationFilePath = "information.xml";
	/**
     * Path of the XML file (with extension) where the measurement metadata, and the scope and channel settings are saved into. Typically "information.xml".
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
     * @return Measurement XML information file path
     */
	public String getXMLInformationFilePath() {
		return xmlInformationFilePath;
	}
	
	/**
     * Path of the XML file (with extension) where the measurement metadata, and the scope and channel settings are saved into. Typically "information.xml".
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
	 * @param xmlInformationFilePath Measurement XML information file path
     */
	public void setXMLInformationFilePath(String xmlInformationFilePath) 
	{
		this.xmlInformationFilePath = xmlInformationFilePath;
	}

	private String htmlInformationFilePath = "information.html";
	/**
     * Path of the HTML file (with extension) where the measurement metadata, and the scope and channel settings are saved into. Typically "information.html".
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
     * @return Measurement HTML information file path
     */
	public String getHTMLInformationFilePath() {
		return htmlInformationFilePath;
	}
	/**
     * Path of the HTML file (with extension) where the measurement metadata, and the scope and channel settings are saved into. Typically "information.html".
     * The path is relative to {@link #getBaseFolder()}/{@link #getBaseFolderExtension()}.
	 * @param htmlInformationFilePath Measurement HTML information file path
     */
	public void setHTMLInformationFilePath(String htmlInformationFilePath) 
	{
		this.htmlInformationFilePath = htmlInformationFilePath;
	}
}
