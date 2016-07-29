/**
 * 
 */
package org.youscope.plugin.taskmeasurement;

import javax.swing.ImageIcon;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.addon.measurement.pages.DescriptionPage;
import org.youscope.addon.measurement.pages.GeneralSettingsPage;
import org.youscope.addon.measurement.pages.StartAndEndSettingsPage;
import org.youscope.clientinterfaces.YouScopeClient;
import org.youscope.common.measurement.Measurement;
import org.youscope.serverinterfaces.YouScopeServer;
import org.youscope.uielements.ImageLoadingTools;

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
		
		String description = "A task measurement helps to perform several different tasks a microscope should do in parallel .\n\n" +
				"Every task is repeated regularly with a given period and start time, and is composed of several subelements, called jobs." +
				"One job thereby corresponds to a single action of the microscope, like taking a bright-field image or changing the stage position.\n\n" +
				"Every task either has a fixed period length, meaning that its jobs are executed e.g. every two minutes, or a variable period length, meaning that its jobs are executed a given time span after they finished."+
				"The latter one is useful if a task of lower priority should be executed with a high frequency, but without blocking the exectution of tasks of higher priority.";
		ImageIcon image = ImageLoadingTools.getResourceIcon("org/youscope/plugin/taskmeasurement/images/taskMeasurement.jpg", "Task Measurement");
		String imageLegend = "Flowchart of a task measurement.";
		addPage(new DescriptionPage(null, description, image, imageLegend));
		addPage(new GeneralSettingsPage<TaskMeasurementConfiguration>(client, TaskMeasurementConfiguration.class));
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