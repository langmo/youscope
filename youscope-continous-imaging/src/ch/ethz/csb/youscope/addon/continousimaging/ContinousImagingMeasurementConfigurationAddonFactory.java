/**
 * 
 */
package ch.ethz.csb.youscope.addon.continousimaging;

import javax.swing.ImageIcon;

import ch.ethz.csb.youscope.client.addon.YouScopeClient;
import ch.ethz.csb.youscope.client.addon.measurement.MeasurementConfigurationAddon;
import ch.ethz.csb.youscope.client.addon.measurement.MeasurementConfigurationAddonFactory;
import ch.ethz.csb.youscope.client.uielements.ImageLoadingTools;
import ch.ethz.csb.youscope.shared.YouScopeServer;

/**
 * @author langmo
 *
 */
public class ContinousImagingMeasurementConfigurationAddonFactory implements MeasurementConfigurationAddonFactory
{

	@Override
	public MeasurementConfigurationAddon createMeasurementConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(supportsConfigurationID(ID))
			return new ContinousImagingMeasurementConfigurationAddon(client, server);
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		return new String[]{ContinousImagingMeasurementConfiguration.TYPE_IDENTIFIER};
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		return (ID.compareToIgnoreCase(ContinousImagingMeasurementConfiguration.TYPE_IDENTIFIER) == 0);
	}

	@Override
	public String getMeasurementName(String ID)
	{
		if(supportsConfigurationID(ID))
			return "Continuous Imaging Measurement";
		return null;
	}

	@Override
	public ImageIcon getMeasurementIcon(String ID)
	{
		return ImageLoadingTools.getResourceIcon("icons/camcorder.png", "Continuous Imaging Measurement");
	}
}
