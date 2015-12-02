/**
 * 
 */
package ch.ethz.csb.youscope.addon.lifecelldetection;

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
public class CellDetectionMeasurementConfigurationAddonFactory implements MeasurementConfigurationAddonFactory
{

	@Override
	public MeasurementConfigurationAddon createMeasurementConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(supportsConfigurationID(ID))
			return new ContinuousLifeCellDetectionMeasurementConfigurationAddon(client, server);
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		return new String[]{CellDetectionMeasurementConfiguration.TYPE_IDENTIFIER};
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		return (ID.compareToIgnoreCase(CellDetectionMeasurementConfiguration.TYPE_IDENTIFIER) == 0);
	}

	@Override
	public String getMeasurementName(String ID)
	{
		if(supportsConfigurationID(ID))
			return "Misc/Cell Detection";
		return null;
	}

	@Override
	public ImageIcon getMeasurementIcon(String ID)
	{
		if(supportsConfigurationID(ID))
			return ImageLoadingTools.getResourceIcon("icons/smiley-mr-green.png", "Cell Detection");
		return null;
	}
}
