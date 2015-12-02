/**
 * 
 */
package ch.ethz.csb.youscope.addon.multicameracontinuousimaging;

import ch.ethz.csb.youscope.server.addon.measurement.MeasurementConstructionAddon;
import ch.ethz.csb.youscope.server.addon.measurement.MeasurementConstructionAddonFactory;

/**
 * @author langmo
 * 
 */
public class MultiCameraContinousImagingMeasurementConstructionAddonFactory implements MeasurementConstructionAddonFactory
{

	@Override
	public MeasurementConstructionAddon createMeasurementConstructionAddon(String ID)
	{
		if(ID.compareToIgnoreCase(MultiCameraContinousImagingConfigurationDTO.TYPE_IDENTIFIER) == 0)
			return new MultiCameraContinousImagingMeasurementConstructionAddon();
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		String[] ids = new String[] {MultiCameraContinousImagingConfigurationDTO.TYPE_IDENTIFIER};
		return ids;
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		if(ID.compareToIgnoreCase(MultiCameraContinousImagingConfigurationDTO.TYPE_IDENTIFIER) == 0)
			return true;
		return false;
	}

}
