/**
 * 
 */
package org.youscope.plugin.usercontrolmeasurement;

import java.io.Serializable;

import org.youscope.common.measurement.MeasurementConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class represents the configuration of user-control measurement.
 * In this measurement type, the user can decide when to take where an image.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("user-control-measurement")
public class UserControlMeasurementConfiguration extends MeasurementConfiguration implements Cloneable, Serializable
{
	/**
	 * Serial version UID.
	 */
	private static final long						serialVersionUID	= 4443810800791783902L;

	
	/**
	 * The identifier for this measurement type.
	 */
	public static final String								TYPE_IDENTIFIER		= "YouScope.UserControlMeasurement";

	@Override
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	/**
	 * Sets the stage device which should be monitored.
	 * If the stage device moves less than the tolerance between two successive images, the images are assumed to be made
	 * at the same position. Set to null to use the default stage.
	 * @param stageDevice ID of the stage to monitor, or null to use the default stage.
	 */
	public void setStageDevice(String stageDevice)
	{
		this.stageDevice = stageDevice;
	}

	/**
	 * Returns the stage device which should be monitored.
	 * If the stage device moves less than the tolerance between two successive images, the images are assumed to be made
	 * at the same position. Returns null if the default stage should be used.
	 * @return ID of the stage to monitor, or null to use the default stage.
	 */
	public String getStageDevice()
	{
		return stageDevice;
	}

	/**
	 * Sets the tolerance for the stage monitoring (in muM).
	 * If the stage device moves less than the tolerance between two successive images, the images are assumed to be made
	 * at the same position. Set to a negative value to not monitor the stage.
	 * @param stageTolerance Tolerance for stage in muM, or negative number to not monitor stage.
	 */
	public void setStageTolerance(double stageTolerance)
	{
		this.stageTolerance = stageTolerance;
	}

	/**
	 * Returns the tolerance for the stage monitoring (in muM).
	 * If the stage device moves less than the tolerance between two successive images, the images are assumed to be made
	 * at the same position. Returns a negative value if the stage is not monitored.
	 * @return Tolerance for stage in muM, or negative number if stage is not monitored.
	 */
	public double getStageTolerance()
	{
		return stageTolerance;
	}

	@XStreamAlias("stage-device")
	private String stageDevice = null;
	
	@XStreamAlias("stage-tolerance-um")
	private double stageTolerance = 10;
}
