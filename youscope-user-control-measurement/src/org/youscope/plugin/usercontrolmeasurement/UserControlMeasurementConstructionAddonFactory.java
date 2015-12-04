/**
 * 
 */
package org.youscope.plugin.usercontrolmeasurement;

import org.youscope.addon.measurement.MeasurementConstructionAddon;
import org.youscope.addon.measurement.MeasurementConstructionAddonFactory;

/**
 * @author langmo
 * 
 */
public class UserControlMeasurementConstructionAddonFactory implements MeasurementConstructionAddonFactory
{

	@Override
	public MeasurementConstructionAddon createMeasurementConstructionAddon(String ID)
	{
		if(ID.compareToIgnoreCase(UserControlMeasurementConfiguration.TYPE_IDENTIFIER) == 0)
			return new UserControlMeasurementConstructionAddon();
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		String[] ids = new String[] {UserControlMeasurementConfiguration.TYPE_IDENTIFIER};
		return ids;
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		if(ID.compareToIgnoreCase(UserControlMeasurementConfiguration.TYPE_IDENTIFIER) == 0)
			return true;
		return false;
	}

}
