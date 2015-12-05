/**
 * 
 */
package org.youscope.plugin.taskmeasurement;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.Measurement;
import org.youscope.serverinterfaces.YouScopeServer;

/**
 * @author Moritz Lang
 */
class TaskMeasurementAddonUI  extends MeasurementAddonUIAdapter<TaskMeasurementConfiguration>
{
	/**
	 * Constructor.
	 * @param server YouScope server.
	 * @param YouScope client.
	 * @throws AddonException 
	 */
	TaskMeasurementAddonUI(YouScopeClient client, YouScopeServer server) throws AddonException
	{
		super(getMetadata(), client, server);

		setTitle("Task Measurement");
		addPage(new StartPage());
		addPage(new GeneralSettingsPage(client, server));
		addPage(new StartAndEndSettingsPage(client, server));
		addPage(new TaskDefinitionPage(client, server));
	}
	static ComponentMetadataAdapter<TaskMeasurementConfiguration> getMetadata()
	{
		return new ComponentMetadataAdapter<TaskMeasurementConfiguration>(TaskMeasurementConfiguration.TYPE_IDENTIFIER, 
				TaskMeasurementConfiguration.class, 
				Measurement.class, "Task Measurement", new String[0], "icons/arrow-split.png");
	}
}
