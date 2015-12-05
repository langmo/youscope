/**
 * 
 */
package org.youscope.plugin.taskmeasurement;

import org.youscope.addon.measurement.MeasurementAddonFactoryAdapter;

/**
 * @author Moritz Lang
 *
 */
public class TaskMeasurementAddonFactory extends MeasurementAddonFactoryAdapter
{
	/**
	 * Constructor.
	 */
	public TaskMeasurementAddonFactory()
	{
		super(TaskMeasurementAddonUI.class, new TaskMeasurementInitializer(), TaskMeasurementAddonUI.getMetadata());
	}
}
