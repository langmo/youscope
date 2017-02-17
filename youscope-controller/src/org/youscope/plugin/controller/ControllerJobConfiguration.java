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
package org.youscope.plugin.controller;

import org.youscope.common.configuration.ConfigurationException;
import org.youscope.common.job.JobConfiguration;
import org.youscope.common.table.ColumnDefinition;
import org.youscope.common.table.TableConsumerConfiguration;
import org.youscope.common.table.TableDefinition;
import org.youscope.common.table.TableProducerConfiguration;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * This job takes takes an input from a table data producer and generates an output, i.e. acting as a contoller unit in feedback measurements.
 * 
 * @author Moritz Lang
 */
@XStreamAlias("controller-job")
public class ControllerJobConfiguration implements JobConfiguration, TableProducerConfiguration
{

	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= -8465192595299726516L;

	/**
	 * The identifier for this measurement type.
	 */
	public static final String	TYPE_IDENTIFIER		= "YouScope.ControllerJob";
	
	@XStreamAlias("input-job")
	private JobConfiguration inputJob = null;
	
	@XStreamAlias("output-job")
	private JobConfiguration outputJob = null;
	
	@XStreamAlias("script")
	private String controllerScript = "";
	
	@XStreamAlias("script-engine")
	private String controllerScriptEngine = "Mozilla Rhino";
	
	/**
	 * The name under which the cell table is saved.
	 */
	@XStreamAlias("controller-table-save-name")
	private String controllerTableSaveName = "controller-table";
	
	@Override 
	public String getTypeIdentifier()
	{
		return TYPE_IDENTIFIER;
	}
	
	
	
	/**
	 * Returns the job which is used to produce the input for the controller.
	 * It is guaranteed that this job implements the TableDataProducerConfiguration interface.
	 * @return Table data producing used as input, or null.
	 */
	public JobConfiguration getInputJob()
	{
		return inputJob;
	}

	/**
	 * Sets the job which should be used to create the table data used as input for the controller.
	 * The job must implement {@link TableProducerConfiguration}. If this is not given, an IllegalArgumentException will be thrown.
	 * @param inputJob Table data producing job for control algorithm, or null.
	 * @throws IllegalArgumentException If inputJob does not implement TableDataProducerConfiguration.
	 */
	public void setInputJob(JobConfiguration inputJob) throws IllegalArgumentException
	{
		if(inputJob == null)
			this.inputJob = null;
		else if(TableProducerConfiguration.class.isAssignableFrom(inputJob.getClass()))
			this.inputJob = inputJob;
		else
			throw new IllegalArgumentException("Input job type must implement the table data producer configuration interface.");
	}
	
	/**
	 * Returns the job which is used to consume the output of the controller.
	 * It is guaranteed that this job implements the OutputConsumerConfiguration interface.
	 * @return Output consumer job configuration, or null.
	 */
	public JobConfiguration getOutputJob()
	{
		return outputJob;
	}

	/**
	 * Sets the job which should be used to consume the output from the controller.
	 * The job must implement {@link TableConsumerConfiguration}. If this is not given, an IllegalArgumentException will be thrown.
	 * @param outputJob output consumer job configuration, or null.
	 * @throws IllegalArgumentException If outputJob does not implement TableConsumerConfiguration.
	 */
	public void setOutputJob(JobConfiguration outputJob) throws IllegalArgumentException
	{
		if(outputJob == null)
			this.outputJob = null;
		else if(TableConsumerConfiguration.class.isAssignableFrom(outputJob.getClass()))
			this.outputJob = outputJob;
		else
			throw new IllegalArgumentException("Output job type must implement TableConsumerConfiguration.");
	}



	@Override
	public String getDescription()
	{
		String description = "<p>Controller</p>";
		description += "<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
		description += "<li>Input job:<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
		if(inputJob != null)
		{
			ColumnDefinition<?>[] inputColumns;
			if(inputJob instanceof TableProducerConfiguration)
			{
				inputColumns = ((TableProducerConfiguration)inputJob).getProducedTableDefinition().getColumnDefinitions();
			}
			else
				inputColumns = new ColumnDefinition<?>[0];
			description += "<li>" + inputJob.getDescription() + "</li></ul></li><li>Input columns:<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
			if(inputColumns.length > 0)
			{
				for(ColumnDefinition<?> column : inputColumns)
				{
					description += "<li>" + column.getColumnName() + "</li>"; 
				}
			}
			else
				description += "<li>No input columns set.</li>";
		}
		else
		{
			description += "<li>No input defined.</li>";
		}
		description += "</ul></li>";
		
		description += "<li>Output job:<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
		if(outputJob != null)
		{
			ColumnDefinition<?>[] outputColumns;
			if(outputJob instanceof TableConsumerConfiguration)
			{
				outputColumns = ((TableConsumerConfiguration)outputJob).getConsumedTableDefinition().getColumnDefinitions();
			}
			else
				outputColumns = new ColumnDefinition<?>[0];
			description += "<li>" + outputJob.getDescription() + "</li></ul></li><li>Output columns:<ul style=\"margin-top:0px;margin-bottom:0px;margin-left:12px;list-style-type:none\">";
			if(outputColumns.length > 0)
			{
				for(ColumnDefinition<?> column : outputColumns)
				{
					description += "<li>" + column.getColumnName() + "</li>"; 
				}
			}
			else
				description += "<li>No output columns set.</li>";
		}
		else
		{
			description += "<li>No output defined.</li>";
		}
		description += "</ul></li>";
		description += "</ul><p>end</p>";
		return description;
	}

	@Override
	public TableDefinition getProducedTableDefinition()
	{
		return ControllerTable.getTableDefinition(inputJob==null || !(inputJob instanceof TableProducerConfiguration) ?null : ((TableProducerConfiguration)inputJob).getProducedTableDefinition().getColumnDefinitions(), 
				outputJob==null || !(outputJob instanceof TableConsumerConfiguration) ?null : ((TableConsumerConfiguration)outputJob).getConsumedTableDefinition().getColumnDefinitions());
	}

	/**
	 * Sets the script of the controller, i.e. how the controller sets the outputs depending on the inputs.
	 * @param controllerScript The script to set.
	 */
	public void setControllerScript(String controllerScript)
	{
		if(controllerScript == null)
			this.controllerScript = "";
		else
			this.controllerScript = controllerScript;
	}

	/**
	 * Returns the script of the controller, i.e. how the controller sets the outputs depending on the inputs.
	 * @return Controller script.
	 */
	public String getControllerScript()
	{
		return controllerScript;
	}

	/**
	 * Sets the name of the script engine with which the controller script should be executed.
	 * @param controllerScriptEngine Name of the script engine.
	 */
	public void setControllerScriptEngine(String controllerScriptEngine)
	{
		if(controllerScriptEngine == null)
			this.controllerScriptEngine = "Mozilla Rhino";
		else
			this.controllerScriptEngine = controllerScriptEngine;
	}

	/**
	 * Returns the name of the script engine with which the controller script should be executed.
	 * @return Name of the script engine.
	 */
	public String getControllerScriptEngine()
	{
		return controllerScriptEngine;
	}


	/**
	 * Sets the name of the file (without file extension) under which the input/output data of the controller should be saved.
	 * @param controllerTableSaveName Name of the file (without extension) for controller I/O saving, or null if it should not be saved.
	 */
	public void setControllerTableSaveName(String controllerTableSaveName)
	{
		this.controllerTableSaveName = controllerTableSaveName;
	}

	/**
	 * Returns the name of the file (without file extension) under which the input/output data of the controller should be saved.
	 * @return Name of the file (without extension) where the controller I/O is saved, or null if it is not saved.
	 */
	public String getControllerTableSaveName()
	{
		return controllerTableSaveName;
	}



	@Override
	public void checkConfiguration() throws ConfigurationException {
		if(inputJob == null)
			throw new ConfigurationException("No input job selected.");
		inputJob.checkConfiguration();
		
		if(outputJob == null)
			throw new ConfigurationException("No output job selected.");
		outputJob.checkConfiguration();
		
		if(controllerScript == null || controllerScript.length() <= 0)
			throw new ConfigurationException("Controller script is empty.");
		if(controllerScriptEngine == null || controllerScriptEngine.length() <= 0)
			throw new ConfigurationException("No script engine selected.");
	}	
}
