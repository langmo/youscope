/**
 * 
 */
package org.youscope.plugin.continuationmeasurement;

import org.youscope.addon.measurement.MeasurementAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class ContinuationMeasurementAddonFactory  extends MeasurementAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public ContinuationMeasurementAddonFactory()
	{
		super(ContinuationMeasurementAddonUI.class, new ContinuationMeasurementInitializer(), ContinuationMeasurementAddonUI.getMetadata());
	}
}
