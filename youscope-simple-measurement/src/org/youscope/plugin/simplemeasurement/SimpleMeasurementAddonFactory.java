/**
 * 
 */
package org.youscope.plugin.simplemeasurement;

import org.youscope.addon.measurement.MeasurementAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class SimpleMeasurementAddonFactory  extends MeasurementAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public SimpleMeasurementAddonFactory()
	{
		super(SimpleMeasurementAddonUI.class, new SimpleMeasurementInitializer(), SimpleMeasurementAddonUI.getMetadata());
	}
}
