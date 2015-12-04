/**
 * 
 */
package org.youscope.plugin.taskmeasurement;

import org.youscope.addon.measurement.MeasurementConstructionAddon;
import org.youscope.addon.measurement.MeasurementConstructionAddonFactory;

/**
 * @author langmo
 * 
 */
public class TaskMeasurementConstructionAddonFactory implements MeasurementConstructionAddonFactory
{

	@Override
	public MeasurementConstructionAddon createMeasurementConstructionAddon(String ID)
	{
		if(ID.compareToIgnoreCase(TaskMeasurementConfiguration.TYPE_IDENTIFIER) == 0)
			return new TaskMeasurementConstructionAddon();
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		String[] ids = new String[] {TaskMeasurementConfiguration.TYPE_IDENTIFIER};
		return ids;
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		if(ID.compareToIgnoreCase(TaskMeasurementConfiguration.TYPE_IDENTIFIER) == 0)
			return true;
		return false;
	}

}
