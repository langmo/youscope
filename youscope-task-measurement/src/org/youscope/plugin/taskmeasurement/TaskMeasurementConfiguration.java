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

import java.util.ArrayList;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.measurement.MeasurementConfiguration;
import org.youscope.common.task.TaskConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This class represents the configuration of a user configurable measurement.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("task-measurement")
public class TaskMeasurementConfiguration extends MeasurementConfiguration
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -6934063013631887402L;

	/**
	 * The identifier for this measurement type.
	 */
	public static final String	TYPE_IDENTIFIER		= "YouScope.ConfiguratableMeasurement";

	@Override 
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}

	/**
	 * A list of all the jobs which should be done during the measurement.
	 */
	@XStreamAlias("tasks")
	private ArrayList<TaskConfiguration>	tasks	= new ArrayList<TaskConfiguration>();

	/**
     * Returns the tasks in the measurement.
     * 
     * @return Tasks.
     */
	public TaskConfiguration[] getTasks()
	{
		return tasks.toArray(new TaskConfiguration[tasks.size()]);
	}

	/**
	 * @param tasks the tasks to set
	 */
	public void setTasks(TaskConfiguration[] tasks)
	{
		this.tasks.clear();
		for(TaskConfiguration task : tasks)
			this.tasks.add(task);
	}

	@Override
	public void checkConfiguration() throws ConfigurationException {
		super.checkConfiguration();
		if(tasks == null)
			throw new ConfigurationException("Tasks are null.");
		for(TaskConfiguration task: tasks)
			task.checkConfiguration();
		
	}

	/**
     * Returns the number of tasks in the measurement.
     * @return number of tasks.
     */
	public int getNumTasks() {
		return tasks.size();
	}

	/**
     * Returns the task at the given position.
     * @param taskID task position.
     * @return task at given position.
     * @throws IndexOutOfBoundsException thrown if task index is invalid.
     */
	public TaskConfiguration getTask(int taskID) throws IndexOutOfBoundsException {
		return tasks.get(taskID);
	}

	/**
     * Removes the task at the given index.
     * @param taskID Task id to remove.
     * @throws IndexOutOfBoundsException
     */
	public void removeTask(int taskID) throws IndexOutOfBoundsException {
		tasks.remove(taskID);
	}

	/**
     * Adds a new task at the end of the tasks.
     * @param taskConfiguration Task to add.
     */
	public void addTask(TaskConfiguration taskConfiguration) {
		tasks.add(taskConfiguration);
	}

	/**
     * Inserts a task at the given position in the task list. The indices of the task having previously the given index and of all tasks with a higher index is increased by one.
     * @param taskConfiguration Task to add.
     * @param taskID Index where to insert the task.
     * @throws IndexOutOfBoundsException
     */
	public void insertTask(TaskConfiguration taskConfiguration, int taskID) throws IndexOutOfBoundsException {
		tasks.add(taskID, taskConfiguration);
	}
}
