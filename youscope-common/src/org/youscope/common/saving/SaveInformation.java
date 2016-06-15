package org.youscope.common.saving;

import java.io.Serializable;

/**
 * General information of the measurement which is currently saved, intended to be used by instances of SaveSettings
 * to calculate the names of the folders in which files of the measurement should be saved.
  * <br>
 * The class is immutable.
 * @author mlang
 *
 */
public final class SaveInformation implements Serializable 
{
	/**
	 * Serial Version UID.
	 */
	private static final long serialVersionUID = -8822349015437087498L;
	private final String measurementName;
	private final long measurementStartTime;
	
	/**
	 * Constructor.
	 * @param measurementName Name of the measurement.
	 * @param measurementStartTime Time in ms when measurement was started. See {@link System#currentTimeMillis()}.
	 */
	public SaveInformation(String measurementName, long measurementStartTime)
	{
		this.measurementName = measurementName;
		this.measurementStartTime = measurementStartTime;
	}

	/**
	 * Returns the name of the measurement.
	 * @return Name of the measurement.
	 */
	public String getMeasurementName() {
		return measurementName;
	}

	/**
	 * Returns the start time of the measurement in ms. See {@link System#currentTimeMillis()}.
	 * @return Start time in ms of the measurement.
	 */
	public long getMeasurementStartTime() {
		return measurementStartTime;
	}
}
