/**
 * 
 */
package ch.ethz.csb.youscope.addon.usercontrolmeasurement;

import ch.ethz.csb.youscope.server.addon.measurement.MeasurementConstructionAddon;
import ch.ethz.csb.youscope.server.addon.measurement.MeasurementConstructionAddonFactory;

/**
 * @author langmo
 * 
 */
public class UserControlMeasurementConstructionAddonFactory implements MeasurementConstructionAddonFactory
{

	@Override
	public MeasurementConstructionAddon createMeasurementConstructionAddon(String ID)
	{
		if(ID.compareToIgnoreCase(UserControlMeasurementConfigurationDTO.TYPE_IDENTIFIER) == 0)
			return new UserControlMeasurementConstructionAddon();
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		String[] ids = new String[] {UserControlMeasurementConfigurationDTO.TYPE_IDENTIFIER};
		return ids;
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		if(ID.compareToIgnoreCase(UserControlMeasurementConfigurationDTO.TYPE_IDENTIFIER) == 0)
			return true;
		return false;
	}

}
