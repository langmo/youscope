/**
 * 
 */
package org.youscope.plugin.microplatemeasurement;

import javax.swing.ImageIcon;

import org.youscope.addon.measurement.MeasurementConfigurationAddon;
import org.youscope.addon.measurement.MeasurementAddonFactory;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;

/**
 * @author langmo
 *
 */
public class MicroplateMeasurementConfigurationAddonFactory implements MeasurementAddonFactory
{

	@Override
	public MeasurementConfigurationAddon<? extends MeasurementConfiguration> createMeasurementUI(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(isSupportingTypeIdentifier(ID))
			return new MicroplateMeasurementConfigurationAddon(client, server);
		return null;
	}

	@Override
	public String[] getSupportedTypeIdentifiers()
	{
		return new String[]{MicroplateMeasurementConfigurationDTO.TYPE_IDENTIFIER};
	}

	@Override
	public boolean isSupportingTypeIdentifier(String ID)
	{
		return (ID.compareToIgnoreCase(MicroplateMeasurementConfigurationDTO.TYPE_IDENTIFIER) == 0);
	}

	@Override
	public String getMeasurementName(String ID)
	{
		if(isSupportingTypeIdentifier(ID))
			return "Microplate Measurement";
		return null;
	}

	@Override
	public ImageIcon getMeasurementIcon(String ID)
	{
		return ImageLoadingTools.getResourceIcon("icons/map.png", "Microplate Measurement");
	}
}
