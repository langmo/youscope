/**
 * 
 */
package org.youscope.plugin.microplatemeasurement;

import org.youscope.addon.measurement.MeasurementConstructionAddon;
import org.youscope.addon.measurement.MeasurementConstructionAddonFactory;

/**
 * @author langmo
 * 
 */
public class MicroplateMeasurementConstructionAddonFactory implements MeasurementConstructionAddonFactory
{

	@Override
	public MeasurementConstructionAddon createMeasurementConstructionAddon(String ID)
	{
		if(ID.compareToIgnoreCase(MicroplateMeasurementConfigurationDTO.TYPE_IDENTIFIER) == 0)
			return new MicroplateMeasurementConstructionAddon();
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		String[] ids = new String[] {MicroplateMeasurementConfigurationDTO.TYPE_IDENTIFIER};
		return ids;
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		if(ID.compareToIgnoreCase(MicroplateMeasurementConfigurationDTO.TYPE_IDENTIFIER) == 0)
			return true;
		return false;
	}

}
