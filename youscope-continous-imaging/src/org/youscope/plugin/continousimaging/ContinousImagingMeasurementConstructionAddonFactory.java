/**
 * 
 */
package org.youscope.plugin.continousimaging;

import org.youscope.addon.measurement.MeasurementConstructionAddon;
import org.youscope.addon.measurement.MeasurementConstructionAddonFactory;

/**
 * @author Moritz Lang
 * 
 */
public class ContinousImagingMeasurementConstructionAddonFactory implements MeasurementConstructionAddonFactory
{

	@Override
	public MeasurementConstructionAddon createMeasurementConstructionAddon(String ID)
	{
		if(ID.equals(ContinousImagingMeasurementConfiguration.TYPE_IDENTIFIER))
			return new ContinousImagingMeasurementConstructionAddon();
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		String[] ids = new String[] {ContinousImagingMeasurementConfiguration.TYPE_IDENTIFIER};
		return ids;
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		if(ID.equals(ContinousImagingMeasurementConfiguration.TYPE_IDENTIFIER))
			return true;
		return false;
	}

}
