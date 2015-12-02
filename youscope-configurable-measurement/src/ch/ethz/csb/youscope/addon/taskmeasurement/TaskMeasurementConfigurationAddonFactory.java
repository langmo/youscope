/**
 * 
 */
package ch.ethz.csb.youscope.addon.taskmeasurement;

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
public class TaskMeasurementConfigurationAddonFactory implements MeasurementConfigurationAddonFactory
{

	@Override
	public MeasurementConfigurationAddon createMeasurementConfigurationAddon(String ID, YouScopeClient client, YouScopeServer server)
	{
		if(supportsConfigurationID(ID))
			return new TaskMeasurementConfigurationAddon(client, server);
		return null;
	}

	@Override
	public String[] getSupportedConfigurationIDs()
	{
		return new String[]{TaskMeasurementConfiguration.TYPE_IDENTIFIER};
	}

	@Override
	public boolean supportsConfigurationID(String ID)
	{
		return (ID.compareToIgnoreCase(TaskMeasurementConfiguration.TYPE_IDENTIFIER) == 0);
	}

	@Override
	public String getMeasurementName(String ID)
	{
		if(supportsConfigurationID(ID))
			return "Task Measurement";
		return null;
	}

	@Override
	public ImageIcon getMeasurementIcon(String ID)
	{
		return ImageLoadingTools.getResourceIcon("icons/arrow-split.png", "Task Measurement");
	}
}
