/*******************************************************************************
 * Copyright (c) 2017 Moritz Lang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Moritz Lang - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package org.youscope.plugin.taskmeasurement;

import org.youscope.addon.AddonException;
import org.youscope.addon.component.ComponentMetadataAdapter;
import org.youscope.addon.measurement.MeasurementAddonUIAdapter;
import org.youscope.addon.measurement.pages.GeneralSettingsPage;
import org.youscope.addon.measurement.pages.MetadataPage;
import org.youscope.addon.measurement.pages.StartAndEndSettingsPage;
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
		addPage(new MetadataPage<>(client));
		addPage(new GeneralSettingsPage<TaskMeasurementConfiguration>(client, TaskMeasurementConfiguration.class));
		addPage(new StartAndEndSettingsPage(client, server));
		addPage(new TaskDefinitionPage(client, server));
	}
	static ComponentMetadataAdapter<TaskMeasurementConfiguration> getMetadata()
	{
		String description = "A task measurement helps to perform several different tasks a microscope should do in parallel .\n\n" +
				"Every task is repeated regularly with a given period and start time, and is composed of several subelements, called jobs." +
				"One job thereby corresponds to a single action of the microscope, like taking a bright-field image or changing the stage position.\n\n" +
				"Every task either has a fixed period length, meaning that its jobs are executed e.g. every two minutes, or a variable period length, meaning that its jobs are executed a given time span after they finished."+
				"The latter one is useful if a task of lower priority should be executed with a high frequency, but without blocking the exectution of tasks of higher priority.";
		return new ComponentMetadataAdapter<TaskMeasurementConfiguration>(TaskMeasurementConfiguration.TYPE_IDENTIFIER, 
				TaskMeasurementConfiguration.class, 
				Measurement.class, "Task Measurement", new String[0], 
				description,
				"icons/arrow-split.png");
	}
}
