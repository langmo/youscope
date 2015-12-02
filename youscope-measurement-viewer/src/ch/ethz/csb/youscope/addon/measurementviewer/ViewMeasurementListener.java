/**
 * 
 */
package ch.ethz.csb.youscope.addon.measurementviewer;

import java.util.EventListener;

/**
 * @author Moritz Lang
 *
 */
interface ViewMeasurementListener extends EventListener
{
	public void viewMeasurement(String measurementPath);
}
