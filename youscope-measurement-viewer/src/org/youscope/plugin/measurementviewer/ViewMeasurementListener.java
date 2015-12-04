/**
 * 
 */
package org.youscope.plugin.measurementviewer;

import java.util.EventListener;

/**
 * @author Moritz Lang
 *
 */
interface ViewMeasurementListener extends EventListener
{
	public void viewMeasurement(String measurementPath);
}
