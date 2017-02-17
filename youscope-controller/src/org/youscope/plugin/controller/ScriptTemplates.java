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

import org.youscope.common.table.ColumnDefinition;

/**
 * @author Moritz Lang
 *
 */
class ScriptTemplates
{
	public static String generateJavaScriptTemplate(ColumnDefinition<?>[] inputColumns, ColumnDefinition<?>[] outputColumns)
	{
		String returnValue =
			"/**\n" +
			" ** Control algorithm.\n" +
			" ** @param evaluationNumber Number of times the algorithm has been evaluated, starting at zero.\n" +
			" ** @param controller connection with which inputs can be obtained, outputs be set, and controller variables be stored.\n" +
			" ** @param wellY y-ID of current well (zero based). -1 if wells are not defined for the controller." +
			" ** @param wellX x-ID of current well (zero based). -1 if wells are not defined for the controller." +
			" ** @param position array of zero based positions. Might e.g. indicate focus plane. Often empty." +
			" **/\n" +
			"function controllerAlgorithm(evaluationNumber, controller)\n" + 
			"{\n" +
			"	// Load input values\n";
		returnValue += 
			"	u = new Array(controller.getNumInputRows());\n" +
			"	for(r=0; r<controller.getNumInputRows(); r++)\n" +
			"	{\n" +
			"		u[r] = new Array(" + Integer.toString(inputColumns.length) + ");\n";
		
		for(int i=0; i<inputColumns.length; i++)
		{
			
			if(Integer.class.isAssignableFrom(inputColumns[i].getValueType()) || Long.class.isAssignableFrom(inputColumns[i].getValueType()))
			{
				returnValue += 
					"		u[r][" + Integer.toString(i) + "] = controller.getInputAsLong(r, \"" + inputColumns[i].getColumnName() + "\"); // type = integer/long\n";
			}
			else if(Double.class.isAssignableFrom(inputColumns[i].getValueType()) || Float.class.isAssignableFrom(inputColumns[i].getValueType()))
			{
				returnValue += 
					"		u[r][" + Integer.toString(i) + "] = controller.getInputAsDouble(r, \"" + inputColumns[i].getColumnName() + "\"); // type = double/float\n";
			}
			else if(Boolean.class.isAssignableFrom(inputColumns[i].getValueType()))
			{
				returnValue += 
					"		u[r][" + Integer.toString(i) + "] = controller.getInputAsBoolean(r, \"" + inputColumns[i].getColumnName() + "\"); // type = boolean\n";
			}			
			else if(String.class.isAssignableFrom(inputColumns[i].getValueType()))
			{
				returnValue += 
					"		u[r][" + Integer.toString(i) + "] = controller.getInputAsString(r, \"" + inputColumns[i].getColumnName() + "\"); // type = String\n";
			}
			else
			{
				returnValue += 
					"		u[r][" + Integer.toString(i) + "] = controller.getInput(r, \"" + inputColumns[i].getColumnName() + "\"); // type = "+inputColumns[i].getValueType().getName()+"\n";
			}
		}
		returnValue += "}\n" +
			"\n"+
			"	// Load a state variable. Set it to the value it was in the last evaluation\n" +
			"	// set to, or to 1 if it did not yet exist.\n" +
			"	x = controller.getStateAsInteger(\"myVarName\", 1); \n" +
			"\n"+
			"	// Calculate the values of the outputs.\n" +
			"	// Enter here your control algorithm.\n" +
			"	x++;\n";
		returnValue += "	y = new Array(" + Integer.toString(outputColumns.length) + ");\n";
		for(int i=0; i<outputColumns.length; i++)
		{
			if(Double.class.isAssignableFrom(outputColumns[i].getValueType()) || Float.class.isAssignableFrom(outputColumns[i].getValueType()))
			{
				returnValue += 
					"	y[" + Integer.toString(i) + "] = 0." + Integer.toString(i) + " / 3.141 * x; // output = "+outputColumns[i].getColumnName()+", type = double/float\n";
			}
			else if(Integer.class.isAssignableFrom(outputColumns[i].getValueType()) || Long.class.isAssignableFrom(outputColumns[i].getValueType()))
			{
				returnValue += 
					"	y[" + Integer.toString(i) + "] = " + Integer.toString(i+7) + " - 2 * x; // output = "+outputColumns[i].getColumnName()+", type = integer/long\n";
			}
			else if(Boolean.class.isAssignableFrom(outputColumns[i].getValueType()))
			{
				returnValue += 
					"	y[" + Integer.toString(i) + "] = true; // output = "+outputColumns[i].getColumnName()+", type = boolean\n";
			}
			else if(String.class.isAssignableFrom(outputColumns[i].getValueType()))
			{
				returnValue += 
					"	y[" + Integer.toString(i) + "] = \"output_" + Integer.toString(i+1) + "\"; // output = "+outputColumns[i].getColumnName()+", type = String\n";
			}
			else
			{
				returnValue += 
					"	y[" + Integer.toString(i) + "] = new "+outputColumns[i].getValueType().getName()+"(); // output = "+outputColumns[i].getColumnName()+", type = "+outputColumns[i].getValueType().getName()+"\n";
			}
		}
		returnValue += 
			"\n" +
			"	// Save states\n" +
			"	controller.setState(\"myVarName\", x);\n" +
			"\n" +
			"	// Set outputs\n";
		for(int i=0; i<outputColumns.length; i++)
		{
			returnValue += "	controller.setOutput(\"" + outputColumns[i].getColumnName() + "\", y[" + Integer.toString(i) + "]);\n";
		}
		returnValue += 
			"}\n" +
			"\n" +
			"// Call algorithm\n" +
			"controllerAlgorithm(evaluationNumber, controller, wellY, wellX, position);\n";
		
		return returnValue;
	}
	public static String generateMatlabTemplate(ColumnDefinition<?>[] inputColumns, ColumnDefinition<?>[] outputColumns)
	{
		String returnValue =
			"% Control algorithm.\n" +
			"% The following variables are available by default:\n"+
			"% - evaluationNumber: number of times the algorithm has been evaluated, starting at zero.\n" +
			"% - controller:       connection with which inputs can be obtained, outputs be set, and controller variables be stored.\n" +
			"% - wellY:            y-ID of current well (zero based). -1 if wells are not defined for the controller.\n" +
			"% - wellX:            x-ID of current well (zero based). -1 if wells are not defined for the controller.\n" +
			"% - position:         array of zero based positions. Might e.g. indicate focus plane. Often empty.\n" +
			"\n" +
			"%% Load input values\n";
		returnValue += 
			"u = cell(controller.getNumInputRows(), " + Integer.toString(inputColumns.length) + ");\n" +
			"for r=1 : controller.getNumInputRows()\n";
		for(int i=0; i<inputColumns.length; i++)
		{
			if(Integer.class.isAssignableFrom(inputColumns[i].getValueType()) || Long.class.isAssignableFrom(inputColumns[i].getValueType()))
			{
				returnValue += 
					"	u{r, " + Integer.toString(i+1) + "} = controller.getInputAsLong(r-1,'" + inputColumns[i].getColumnName() + "'); % type = int/long\n";
			}
			else if(Double.class.isAssignableFrom(inputColumns[i].getValueType()) || Float.class.isAssignableFrom(inputColumns[i].getValueType()))
			{
				returnValue += 
					"	u{r, " + Integer.toString(i+1) + "} = controller.getInputAsDouble(r-1,'" + inputColumns[i].getColumnName() + "'); % type = double/float\n";
			}
			else if(Boolean.class.isAssignableFrom(inputColumns[i].getValueType()))
			{
				returnValue += 
					"	u{r, " + Integer.toString(i+1) + "} = controller.getInputAsBoolean(r-1, '" + inputColumns[i].getColumnName() + "'); % type = boolean\n";
			}	
			else if(String.class.isAssignableFrom(inputColumns[i].getValueType()))
			{
				returnValue += 
					"	u{r, " + Integer.toString(i+1) + "} = controller.getInputAsString(r-1,'" + inputColumns[i].getColumnName() + "'); % type = String\n";
			}
			else if(String.class.isAssignableFrom(outputColumns[i].getValueType()))
			{
				returnValue += 
					"	u{r, " + Integer.toString(i+1) + "} = controller.getInput(r-1,'" + inputColumns[i].getColumnName() + "'); % type = "+inputColumns[i].getValueType().getName()+"\n";
			}
		}
		returnValue +=
			"end\n" +
			"\n" +
			"%% Load a state variables.\n" +
			"% Set x to the value it was in the last evaluation\n" +
			"% set to, or to 1 if it did not yet exist.\n" +
			"x = controller.getStateAsInteger('myVarName', 1); \n" +
			"\n"+
			"%% Calculate the values of the outputs.\n" +
			"% Enter here your control algorithm.\n" +
			"x = x + 1;\n";
		returnValue += "y = cell(1, " + Integer.toString(outputColumns.length) + ");\n";
		for(int i=0; i<outputColumns.length; i++)
		{
			if(Double.class.isAssignableFrom(outputColumns[i].getValueType()) || Float.class.isAssignableFrom(outputColumns[i].getValueType()))
			{
				returnValue +=
					"y{" + Integer.toString(i+1) + "} = 0." + Integer.toString(i) + " / 3.141 * x; % output = "+outputColumns[i].getColumnName()+", type = double/float\n";
			}
			else if(Integer.class.isAssignableFrom(inputColumns[i].getValueType()) || Long.class.isAssignableFrom(inputColumns[i].getValueType()))
			{
				returnValue +=
					"y{" + Integer.toString(i+1) + "} = " + Integer.toString(i+7) + " - 2 * x; % output = "+outputColumns[i].getColumnName()+", type = integer/long\n";
			}
			else if(Boolean.class.isAssignableFrom(inputColumns[i].getValueType()))
			{
				returnValue +=
						"y{" + Integer.toString(i+1) + "} = true; % output = "+outputColumns[i].getColumnName()+", type = boolean\n";
			}
			else if(String.class.isAssignableFrom(outputColumns[i].getValueType()))
			{
				returnValue +=
					"y{" + Integer.toString(i+1) + "} = sprintf('value=%g', " + Integer.toString(i+1) + " * x); % output = "+outputColumns[i].getColumnName()+", type = String\n";
			}
			else
			{
				returnValue +=
					"y{" + Integer.toString(i+1) + "} = "+outputColumns[i].getValueType()+"(); % output = "+outputColumns[i].getColumnName()+", type = "+outputColumns[i].getValueType()+"\n";
			}
		}
		returnValue += 
			"\n" +
			"%% Save states\n" +
			"controller.setState('myVarName', x);\n" +
			"\n" +
			"%% Set outputs\n";
		for(int i=0; i<outputColumns.length; i++)
		{
			returnValue += "controller.setOutput('" + outputColumns[i].getColumnName() + "', y{" + Integer.toString(i+1) + "});\n";
		}
		
		return returnValue;
	}
}
