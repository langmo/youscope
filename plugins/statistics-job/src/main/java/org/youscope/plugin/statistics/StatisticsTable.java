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
package org.youscope.plugin.statistics;

import org.youscope.common.table.ColumnDefinition;
import org.youscope.common.table.TableDefinition;

/**
 * Class to provide information about the layout of the table filled with statistics.
 * @author Moritz Lang
 *
 */
public class StatisticsTable
{
	/**
	 * Private constructor. Use static methods.
	 */
	private StatisticsTable()
	{
		// do nothing.
	}
	/**
	 * Column containing job name.
	 */
	public final static ColumnDefinition<String> COLUMN_JOB_NAME = ColumnDefinition.createStringColumnDefinition("Job Name", "Name of executed job.", false);
	
	/**
	 * Column containing position information of job.
	 */
	public final static ColumnDefinition<String> COLUMN_JOB_POSITION_INFORMATION = ColumnDefinition.createStringColumnDefinition("Job Position Information", "Logical position of job in measurement hierachy.", false);
	
	/**
	 * Column containing execution information of job.
	 */
	public final static ColumnDefinition<String> COLUMN_JOB_EXECUTION_INFORMATION = ColumnDefinition.createStringColumnDefinition("Job Execution Information", "Logical execution number of job.", false);
	
	/**
	 * Column containing execution start time of job.
	 */
	public final static ColumnDefinition<Long> COLUMN_START_TIME = ColumnDefinition.createLongColumnDefinition("Start Time (ms)", "Time in ms after measurement start when job execution was started.", false);
	/**
	 * Column containing execution end time of job.
	 */
	public final static ColumnDefinition<Long> COLUMN_END_TIME = ColumnDefinition.createLongColumnDefinition("End Time (ms)", "Time in ms after measurement start when job execution was finished.", false);
	/**
	 * Column containing time needed to execute job.
	 */
	public final static ColumnDefinition<Long> COLUMN_DURATION = ColumnDefinition.createLongColumnDefinition("Duration (ms)", "Duration in ms the job needed to execute.", false);
	
	private static TableDefinition tableDefinition = null;
	
	/**
	 * Returns the statistics table layout.
	 * @return Statistics table layout.
	 */
	public synchronized static TableDefinition getTableDefinition()
	{
		if(tableDefinition != null)
			return tableDefinition;
		tableDefinition = new TableDefinition("Statistics table", "Table containing the start and end times of the execution of each sub-job and sub-sub-job of a statistics job.",
				COLUMN_JOB_NAME, COLUMN_JOB_POSITION_INFORMATION, COLUMN_JOB_EXECUTION_INFORMATION, COLUMN_START_TIME, COLUMN_END_TIME, COLUMN_DURATION);
		return tableDefinition;
	}

}
