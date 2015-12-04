/**
 * 
 */
package org.youscope.plugin.taskmeasurement;

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
public class TaskMeasurementConfigurationAddonFactory implements MeasurementAddonFactory
{

	@Override
	public MeasurementConfigurationAddon<? extends MeasurementConfiguration> createMeasurementUI(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(isSupportingTypeIdentifier(ID))
			return new TaskMeasurementConfigurationAddon(client, server);
		return null;
	}

	@Override
	public String[] getSupportedTypeIdentifiers()
	{
		return new String[]{TaskMeasurementConfiguration.TYPE_IDENTIFIER};
	}

	@Override
	public boolean isSupportingTypeIdentifier(String ID)
	{
		return (ID.compareToIgnoreCase(TaskMeasurementConfiguration.TYPE_IDENTIFIER) == 0);
	}

	@Override
	public String getMeasurementName(String ID)
	{
		if(isSupportingTypeIdentifier(ID))
			return "Task Measurement";
		return null;
	}

	@Override
	public ImageIcon getMeasurementIcon(String ID)
	{
		return ImageLoadingTools.getResourceIcon("icons/arrow-split.png", "Task Measurement");
	}
}
