/**
 * 
 */
package org.youscope.plugin.usercontrolmeasurement;

import org.youscope.addon.measurement.MeasurementAddonFactoryAdapter;

/**
 * @author langmo
 *
 */
public class UserControlMeasurementAddonFactory extends MeasurementAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public UserControlMeasurementAddonFactory()
	{
		super(UserControlMeasurementAddonUI.class, new UserControlMeasurementInitializer(), UserControlMeasurementAddonUI.getMetadata());
	}
}
