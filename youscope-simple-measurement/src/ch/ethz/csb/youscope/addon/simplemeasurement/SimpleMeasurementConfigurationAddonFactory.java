/**
 * 
 */
package ch.ethz.csb.youscope.addon.simplemeasurement;

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
public class SimpleMeasurementConfigurationAddonFactory implements MeasurementConfigurationAddonFactory
{

	@Override
	public MeasurementConfigurationAddon createMeasurementConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(supportsConfigurationID(ID))
			return new SimpleMeasurementConfigurationAddon(client, server);
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		return new String[]{SimpleMeasurementConfigurationDTO.TYPE_IDENTIFIER};
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		return (ID.compareToIgnoreCase(SimpleMeasurementConfigurationDTO.TYPE_IDENTIFIER) == 0);
	}

	@Override
	public String getMeasurementName(String ID)
	{
		if(supportsConfigurationID(ID))
			return "Simple Measurement";
		return null;
	}

	@Override
	public ImageIcon getMeasurementIcon(String ID)
	{
		return ImageLoadingTools.getResourceIcon("icons/paper-plane.png", "Simple Measurement");
	}
}
