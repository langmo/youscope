/**
 * 
 */
package org.youscope.plugin.lifecelldetection;

import org.youscope.addon.measurement.MeasurementAddonFactoryAdapter;

/**
 * @author langmo
 *
 */
public class CellDetectionMeasurementAddonFactory extends MeasurementAddonFactoryAdapter
{

	/**
	 * Constructor.
	 */
	public CellDetectionMeasurementAddonFactory()
	{
		super(CellDetectionMeasurementAddonUI.class, new CellDetectionMeasurementInitializer(), CellDetectionMeasurementAddonUI.getMetadata());
	}
}
