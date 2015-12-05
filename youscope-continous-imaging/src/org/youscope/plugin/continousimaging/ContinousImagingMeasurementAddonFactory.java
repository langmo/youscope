/**
 * 
 */
package org.youscope.plugin.continousimaging;

import org.youscope.addon.measurement.MeasurementAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class ContinousImagingMeasurementAddonFactory extends MeasurementAddonFactoryAdapter
{

	/**
	 * Constructor.
	 */
	public ContinousImagingMeasurementAddonFactory()
	{
		super(ContinousImagingMeasurementAddonUI.class, new ContinousImagingMeasurementInitializer(), ContinousImagingMeasurementAddonUI.getMetadata());
	}

}
