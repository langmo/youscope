/**
 * 
 */
package ch.ethz.csb.youscope.addon.multicameracontinuousimaging;

import javax.swing.ImageIcon;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.measurement.MeasurementConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.measurement.MeasurementConfigurationAddonFactory;
import ch.ethz.csb.youscope.shared.YouScopeServer;


/**
 * @author langmo
 *
 */
public class MultiCameraContinousImagingConfigurationAddonFactory implements MeasurementConfigurationAddonFactory
{

	@Override
	public MeasurementConfigurationAddon createMeasurementConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(supportsConfigurationID(ID))
			return new MultiCameraContinousImagingConfigurationAddon(client, server);
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		return new String[]{MultiCameraContinousImagingConfigurationDTO.TYPE_IDENTIFIER};
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		return (ID.compareToIgnoreCase(MultiCameraContinousImagingConfigurationDTO.TYPE_IDENTIFIER) == 0);
	}

	@Override
	public String getMeasurementName(String ID)
	{
		if(supportsConfigurationID(ID))
			return "Multi-Camera/Multi-Camera Continuous Imaging";
		return null;
	}

	@Override
	public ImageIcon getMeasurementIcon(String ID)
	{
		// TODO Add icon
		return null;
	}
}
