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
package org.youscope.plugin.onix;

import org.youscope.common.table.ColumnDefinition;
import org.youscope.common.table.TableDefinition;

/**
 * Class to provide information about the layout of the table consumed by the Onix job.
 * @author Moritz Lang
 *
 */
public class OnixTable
{
	static final ColumnDefinition<Double> XPressure = ColumnDefinition.createDoubleColumnDefinition("X-Pressure (psi)", "Pressure for the wells controlled by the x-valves (Valve 1 and 2).", true);
	static final ColumnDefinition<Double> YPressure = ColumnDefinition.createDoubleColumnDefinition("Y-Pressure (psi)", "Pressure for the wells controlled by the y-valves (Valves 3 to 6).", true);
	static final ColumnDefinition<Boolean> Valve1 = ColumnDefinition.createBooleanColumnDefinition("Valve 1", "Set to false to deactivate valve, and to true to activate valve.", true);
	static final ColumnDefinition<Boolean> Valve2 = ColumnDefinition.createBooleanColumnDefinition("Valve 2", "Set to false to deactivate valve, and to true to activate valve.", true);
	static final ColumnDefinition<Boolean> Valve3 = ColumnDefinition.createBooleanColumnDefinition("Valve 3", "Set to false to deactivate valve, and to true to activate valve.", true);
	static final ColumnDefinition<Boolean> Valve4 = ColumnDefinition.createBooleanColumnDefinition("Valve 4", "Set to false to deactivate valve, and to true to activate valve.", true);
	static final ColumnDefinition<Boolean> Valve5 = ColumnDefinition.createBooleanColumnDefinition("Valve 5", "Set to false to deactivate valve, and to true to activate valve.", true);
	static final ColumnDefinition<Boolean> Valve6 = ColumnDefinition.createBooleanColumnDefinition("Valve 6", "Set to false to deactivate valve, and to true to activate valve.", true);
	static final ColumnDefinition<Boolean> Valve7 = ColumnDefinition.createBooleanColumnDefinition("Valve 7", "Set to false to deactivate valve, and to true to activate valve.", true);
	static final ColumnDefinition<Boolean> Valve8 = ColumnDefinition.createBooleanColumnDefinition("Valve 8", "Set to false to deactivate valve, and to true to activate valve.", true);
	static final ColumnDefinition<Integer> XPWMPeriod = ColumnDefinition.createIntegerColumnDefinition("X-PWM Period (ms)", "Period of pulse width modulation in ms for x-wells.", true);
	static final ColumnDefinition<Double>  XPWMFraction = ColumnDefinition.createDoubleColumnDefinition("X-PWM Fraction", "Fraction of time (between zero and one) Valve 1 is open during pulse-width-modulation. The rest of the time Valve 2 is open.", true);
	static final ColumnDefinition<Integer> YPWMPeriod = ColumnDefinition.createIntegerColumnDefinition("Y-PWM Period (ms)", "Period of pulse width modulation in ms for y-wells.", true);
	static final ColumnDefinition<Double>  YPWMFraction3 = ColumnDefinition.createDoubleColumnDefinition("Y-PWM Fraction 3 [0-1]", "Fraction of time (between zero and one) Valve 3 is open during pulse-width-modulation.\nFractions for Valve 4-6 should add up to one.", true);
	static final ColumnDefinition<Double>  YPWMFraction4 = ColumnDefinition.createDoubleColumnDefinition("Y-PWM Fraction 4 [0-1]", "Fraction of time (between zero and one) Valve 3 is open during pulse-width-modulation.\nFractions for Valve 4-6 should add up to one.", true);
	static final ColumnDefinition<Double>  YPWMFraction5 = ColumnDefinition.createDoubleColumnDefinition("Y-PWM Fraction 5 [0-1]", "Fraction of time (between zero and one) Valve 3 is open during pulse-width-modulation.\nFractions for Valve 4-6 should add up to one.", true);
	static final ColumnDefinition<Double>  YPWMFraction6 = ColumnDefinition.createDoubleColumnDefinition("Y-PWM Fraction 6 [0-1]", "Fraction of time (between zero and one) Valve 3 is open during pulse-width-modulation.\nFractions for Valve 4-6 should add up to one.", true);
 	
 	private static TableDefinition tableDefinition = null;
 	
 	/**
 	 * Returns the Onix table layout.
 	 * @return Onix table layout.
 	 */
 	public synchronized static TableDefinition getTableDefinition()
 	{
 		if(tableDefinition != null)
 			return tableDefinition;
 		
 		tableDefinition = new TableDefinition("Onix control table.", "Only first row is evaluated. Set values in columns to null to not change values. If any column referring to the pulse-width-modulation of the x-valves is non-null, all columns referring to the PWM of the x-valves have to be set, and accordingly for the y PWM.",
 				XPressure,
		 		YPressure,
		 		Valve1,
		 		Valve2,
		 		Valve3,
		 		Valve4,
		 		Valve5,
		 		Valve6,
		 		Valve7,
		 		Valve8,
		 		XPWMPeriod,
		 		XPWMFraction,
		 		YPWMPeriod,
		 		YPWMFraction3,
		 		YPWMFraction4,
		 		YPWMFraction5,
		 		YPWMFraction6);
 		
 		return tableDefinition;
 	}
}
