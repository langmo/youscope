/**
 * 
 */
package org.youscope.plugin.multicameracontinuousimaging;

import javax.swing.ImageIcon;

import org.youscope.addon.measurement.MeasurementConfigurationAddon;
import org.youscope.addon.measurement.MeasurementAddonFactory;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.configuration.MeasurementConfiguration;
import org.youscope.serverinterfaces.YouScopeServer;


/**
 * @author langmo
 *
 */
public class MultiCameraContinousImagingConfigurationAddonFactory implements MeasurementAddonFactory
{

	@Override
	public MeasurementConfigurationAddon<? extends MeasurementConfiguration> createMeasurementUI(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(isSupportingTypeIdentifier(ID))
			return new MultiCameraContinousImagingConfigurationAddon(client, server);
		return null;
	}

	@Override
	public String[] getSupportedTypeIdentifiers()
	{
		return new String[]{MultiCameraContinousImagingConfigurationDTO.TYPE_IDENTIFIER};
	}

	@Override
	public boolean isSupportingTypeIdentifier(String ID)
	{
		return (ID.compareToIgnoreCase(MultiCameraContinousImagingConfigurationDTO.TYPE_IDENTIFIER) == 0);
	}

	@Override
	public String getMeasurementName(String ID)
	{
		if(isSupportingTypeIdentifier(ID))
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
