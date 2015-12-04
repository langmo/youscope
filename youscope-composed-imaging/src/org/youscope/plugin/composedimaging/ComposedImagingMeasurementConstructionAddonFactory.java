/**
 * 
 */
package org.youscope.plugin.composedimaging;

import org.youscope.addon.measurement.MeasurementConstructionAddon;
import org.youscope.addon.measurement.MeasurementConstructionAddonFactory;

/**
 * @author langmo
 * 
 */
public class ComposedImagingMeasurementConstructionAddonFactory implements MeasurementConstructionAddonFactory
{

	@Override
	public MeasurementConstructionAddon createMeasurementConstructionAddon(String ID)
	{
		if(ID.compareToIgnoreCase(ComposedImagingMeasurementConfiguration.TYPE_IDENTIFIER) == 0)
			return new ComposedImagingMeasurementConstructionAddon();
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		String[] ids = new String[] {ComposedImagingMeasurementConfiguration.TYPE_IDENTIFIER};
		return ids;
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		if(ID.compareToIgnoreCase(ComposedImagingMeasurementConfiguration.TYPE_IDENTIFIER) == 0)
			return true;
		return false;
	}

}
