/**
 * 
 */
package org.youscope.plugin.composedimaging;

import org.youscope.addon.measurement.MeasurementAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class ComposedImagingMeasurementAddonFactory  extends MeasurementAddonFactoryAdapter
{

	/**
	 * Constructor.
	 */
	public ComposedImagingMeasurementAddonFactory()
	{
		super(ComposedImagingMeasurementAddonUI.class, new ComposedImagingMeasurementInitializer(), ComposedImagingMeasurementAddonUI.getMetadata());
	}
}
