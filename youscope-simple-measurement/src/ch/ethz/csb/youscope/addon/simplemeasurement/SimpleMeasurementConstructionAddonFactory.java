/**
 * 
 */
package ch.ethz.csb.youscope.addon.simplemeasurement;

import ch.ethz.csb.youscope.server.addon.measurement.MeasurementConstructionAddon;
import ch.ethz.csb.youscope.server.addon.measurement.MeasurementConstructionAddonFactory;

/**
 * @author langmo
 * 
 */
public class SimpleMeasurementConstructionAddonFactory implements MeasurementConstructionAddonFactory
{

	@Override
	public MeasurementConstructionAddon createMeasurementConstructionAddon(String ID)
	{
		if(ID.compareToIgnoreCase(SimpleMeasurementConfigurationDTO.TYPE_IDENTIFIER) == 0)
			return new SimpleMeasurementConstructionAddon();
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		String[] ids = new String[] {SimpleMeasurementConfigurationDTO.TYPE_IDENTIFIER};
		return ids;
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		if(ID.compareToIgnoreCase(SimpleMeasurementConfigurationDTO.TYPE_IDENTIFIER) == 0)
			return true;
		return false;
	}

}
