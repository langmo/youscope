/**
 * 
 */
package org.youscope.plugin.microplatemeasurement;


import org.youscope.addon.measurement.MeasurementAddonFactoryAdapter;
/**
 * @author Moritz Lang
 *
 */
public class MicroplateMeasurementAddonFactory extends MeasurementAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public MicroplateMeasurementAddonFactory()
	{
		super(MicroplateMeasurementAddonUI.class, new MicroplateMeasurementInitializer(), MicroplateMeasurementAddonUI.getMetadata());
	}
}
