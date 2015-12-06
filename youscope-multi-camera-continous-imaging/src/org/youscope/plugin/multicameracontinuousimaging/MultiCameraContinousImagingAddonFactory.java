/**
 * 
 */
package org.youscope.plugin.multicameracontinuousimaging;

import org.youscope.addon.measurement.MeasurementAddonFactoryAdapter;

/**
 * @author langmo
 *
 */
public class MultiCameraContinousImagingAddonFactory  extends MeasurementAddonFactoryAdapter
{

	/**
	 * Constructor.
	 */
	public MultiCameraContinousImagingAddonFactory()
	{
		super(MultiCameraContinousImagingAddonUI.class, new MultiCameraContinousImagingInitializer(), MultiCameraContinousImagingAddonUI.getMetadata());
	}
}
