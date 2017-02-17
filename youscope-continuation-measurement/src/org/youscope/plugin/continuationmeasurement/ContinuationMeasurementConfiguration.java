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
/**
 * 
 */
package org.youscope.plugin.continuationmeasurement;


import java.util.ArrayList;
import java.util.Collection;

import org.youscope.common.MetadataProperty;
import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.microscope.DeviceSetting;
import org.youscope.common.saving.SaveSettingsConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class represents the configuration of simple measurement, e.g. one repeated task with several jobs.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("continuation-measurement")
public class ContinuationMeasurementConfiguration extends MeasurementConfiguration
{
	/**
	 * Serial version UID.
	 */
	private static final long						serialVersionUID	= 3333810800791783902L;

	/**
	 * The original (eventually changed) configuration
	 */
	@XStreamAlias("encapsulated-configuration")
	private MeasurementConfiguration encapsulatedConfiguration = null;
	/**
	 * The identifier for this measurement type.
	 */
	public static final String								TYPE_IDENTIFIER		= "YouScope.ContinuationMeasurement";

	
	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	/**
	 * Returns the original (eventually changed) configuration, or null if yet not set.
	 * @return The original (eventually changed) configuration.
	 */
	public MeasurementConfiguration getEncapsulatedConfiguration() {
		return encapsulatedConfiguration;
	}

	/**
	 * Sets the original (eventually changed) configuration, or null if yet not set.
	 * @param encapsulatedConfiguration The original (eventually changed) configuration.
	 */
	public void setEncapsulatedConfiguration(MeasurementConfiguration encapsulatedConfiguration) {
		this.encapsulatedConfiguration = encapsulatedConfiguration;
	}

	@XStreamAlias("measurement-folder")
	private String measurementFolder = null;

	/**
	 * Returns the folder where the measurement should be saved.
	 * @return the folder of the measurement.
	 */
	public String getMeasurementFolder()
	{
		return measurementFolder;
	}
	/**
	 * Sets the folder where the measurement should be saved.
	 * @param measurementFolder the folder of the measurement.
	 */
	public void setMeasurementFolder(String measurementFolder)
	{
		this.measurementFolder = measurementFolder;
	}
	@XStreamAlias("delta-evaluation-number")
	private long deltaEvaluationNumber = 0;	
	
	@XStreamAlias("previous-runtime")
	private long previousRuntime = 3600*1000;
	
	/**
	 * The delta for which the evaluation numbers of all images should be increased.
	 * @return Evaluation number delta.
	 */
	public long getDeltaEvaluationNumber() {
		return deltaEvaluationNumber;
	}
	/**
	 * The delta for which the evaluation numbers of all images should be increased.
	 * @param deltaEvaluationNumber Evaluation number delta.
	 */
	public void setDeltaEvaluationNumber(long deltaEvaluationNumber) 
	{
		this.deltaEvaluationNumber = deltaEvaluationNumber;
	}

	/**
	 * Returns the runtime of the previous measurement.
	 * @return Runtime of previous measurement in ms.
	 */
	public long getPreviousRuntime() {
		return previousRuntime;
	}
	/**
	 * Sets the runtime of the previous measurement.
	 * @param previousRuntime Runtime of previous measurement in ms.
	 */
	public void setPreviousRuntime(long previousRuntime) {
		this.previousRuntime = previousRuntime;
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(previousRuntime <= 0)
			throw new ConfigurationException("Runtime of previous measurement must be greater than zero.");
	}

	@Override
	public String getMetadataPropertyValue(String propertyName) throws IllegalArgumentException 
	{
		return encapsulatedConfiguration == null ? null : encapsulatedConfiguration.getMetadataPropertyValue(propertyName);
	}

	@Override
	public String getDescription() {
		return encapsulatedConfiguration == null ? "" : encapsulatedConfiguration.getDescription();
	}

	@Override
	public void setDescription(String description) {
		if(encapsulatedConfiguration != null)
			encapsulatedConfiguration.setDescription(description);
	}

	@Override
	public MetadataProperty getMetadataProperty(String propertyName) throws IllegalArgumentException {
		return encapsulatedConfiguration == null ? null : encapsulatedConfiguration.getMetadataProperty(propertyName);
	}

	@Override
	public Collection<MetadataProperty> getMetadataProperties() {
		return encapsulatedConfiguration == null ? new ArrayList<MetadataProperty>() : encapsulatedConfiguration.getMetadataProperties();
	}

	@Override
	public void setMetadataProperties(Collection<MetadataProperty> properties) throws IllegalArgumentException {
		if(encapsulatedConfiguration != null)
			encapsulatedConfiguration.setMetadataProperties(properties);
	}

	@Override
	public void setMetadataProperty(MetadataProperty property) throws IllegalArgumentException {
		if(encapsulatedConfiguration != null)
			encapsulatedConfiguration.setMetadataProperty(property);
	}

	@Override
	public void setMetadataProperty(String propertyName, String propertyValue) throws IllegalArgumentException {
		if(encapsulatedConfiguration != null)
			encapsulatedConfiguration.setMetadataProperty(propertyName, propertyValue);
	}

	@Override
	public void clearMetadataProperties() {
		if(encapsulatedConfiguration != null)
			encapsulatedConfiguration.clearMetadataProperties();
	}

	@Override
	public boolean deleteMetadataProperty(String propertyName) throws IllegalArgumentException {
		if(encapsulatedConfiguration != null)
			return encapsulatedConfiguration.deleteMetadataProperty(propertyName);
		return false;
	}

	@Override
	public String getName() {
		return encapsulatedConfiguration == null ? "unnamed" : encapsulatedConfiguration.getName();
	}

	@Override
	public void setName(String name) throws IllegalArgumentException {
		if(encapsulatedConfiguration != null)
			encapsulatedConfiguration.setName(name);
	}

	@Override
	public int getMaxRuntime() {
		return encapsulatedConfiguration == null ? -1 : encapsulatedConfiguration.getMaxRuntime();
	}

	@Override
	public void setMaxRuntime(int maxRuntime) {
		if(encapsulatedConfiguration != null)
			encapsulatedConfiguration.setMaxRuntime(maxRuntime);
	}

	@Override
	public DeviceSetting[] getDeviseSettingsOn() {
		return encapsulatedConfiguration == null ? new DeviceSetting[0] : encapsulatedConfiguration.getDeviseSettingsOn();
	}

	@Override
	public void setDeviseSettingsOn(DeviceSetting[] deviceSettings) throws IllegalArgumentException {
		if(encapsulatedConfiguration != null)
			encapsulatedConfiguration.setDeviseSettingsOn(deviceSettings);
	}

	@Override
	public DeviceSetting[] getDeviseSettingsOff() {
		return encapsulatedConfiguration == null ? new DeviceSetting[0] : encapsulatedConfiguration.getDeviseSettingsOff();
	}

	@Override
	public void setDeviseSettingsOff(DeviceSetting[] deviceSettings) {
		if(encapsulatedConfiguration != null)
			encapsulatedConfiguration.setDeviseSettingsOff(deviceSettings);
	}

	@Override
	public void setSaveSettings(SaveSettingsConfiguration saveSettings) {
		if(encapsulatedConfiguration != null)
			encapsulatedConfiguration.setSaveSettings(saveSettings);
	}

	@Override
	public SaveSettingsConfiguration getSaveSettings() {
		return encapsulatedConfiguration == null ? null : encapsulatedConfiguration.getSaveSettings();
	}
}
