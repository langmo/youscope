package org.youscope.plugin.measurementappender;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.saving.MeasurementSaver;
import org.youscope.common.saving.SaveSettingsConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * A save setting to append a previous measurement.
 * @author Moritz Lang
 *
 */
public class AppenderSaveSettingsConfiguration  extends SaveSettingsConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = 113704186394412351L;

	private SaveSettingsConfiguration encapsulatedSaveSettings = null;
	private long previousRuntime = 3600*1000;
	/**
	 * Returns the save settings masked by this setting.
	 * @return Encapsulated save settings.
	 */
	public SaveSettingsConfiguration getEncapsulatedSaveSettings() {
		return encapsulatedSaveSettings;
	}

	/**
	 * Sets the save settings masked by this setting.
	 * @param encapsulatedSaveSettings Encapsulated save settings. 
	 */
	public void setEncapsulatedSaveSettings(SaveSettingsConfiguration encapsulatedSaveSettings) {
		this.encapsulatedSaveSettings = encapsulatedSaveSettings;
	}
	
	
	
	/**
	 * Type identifier of the save settings.
	 */
	public static final String TYPE_IDENTIFIER = "YouScope.Appender.SaveSettings";
	
	/**
	 * The folder where the measurement (information and images made during the measurement) should be stored (same folder as the one of the measurement which should be appended.
	 */
	@XStreamAlias("base-folder")
	private String					baseFolder					= "";

	/**
	 * Returns the folder where all measurements should be saved.
	 * @return the folder of the measurement.
	 */
	public String getBaseFolder()
	{
		return baseFolder;
	}

	/**
	 * Sets the folder where all measurements should be saved.
	 * Be aware that an additional folder is created inside this folder when the measurement starts,
	 * which indicates the specific time when the measurement was started. This is done since one
	 * measurement can be started multiple times. To obtain the full path to this subfolder, use the
	 * respective function in {@link MeasurementSaver}.
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

	@XStreamAlias("delta-evaluation-number")
	private long deltaEvaluationNumber = 0;	
	
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
}
