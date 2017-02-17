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

import java.rmi.RemoteException;

import org.youscope.common.table.RowView;
import org.youscope.common.table.Table;
import org.youscope.common.table.TableException;

/**
 * Helper class to consume table data.
 * @author Moritz Lang
 *
 */
class OnixJobTableDataInterpreter
{
	static void runTableData(Table table, OnixAddon onix) throws OnixException, TableException, RemoteException, OnixProtocolRunningException
	{
		if(table.getNumRows()==0)
			return;
		RowView rowView = table.getRowView(0);
		// first, set pressure
		if(!rowView.get(OnixTable.XPressure).isNull())
		{
			onix.setXPressureSetpoint(rowView.getValue(OnixTable.XPressure).floatValue());
		}
		if(!rowView.get(OnixTable.YPressure).isNull())
		{
			onix.setYPressureSetpoint(rowView.getValue(OnixTable.YPressure).floatValue());
		}
		
		// Valves
		if(!rowView.get(OnixTable.Valve1).isNull())
		{
			onix.setValve(0, rowView.getValue(OnixTable.Valve1));
		}
		if(!rowView.get(OnixTable.Valve2).isNull())
		{
			onix.setValve(1, rowView.getValue(OnixTable.Valve2));
		}
		if(!rowView.get(OnixTable.Valve3).isNull())
		{
			onix.setValve(2, rowView.getValue(OnixTable.Valve3));
		}
		if(!rowView.get(OnixTable.Valve4).isNull())
		{
			onix.setValve(3, rowView.getValue(OnixTable.Valve4));
		}
		if(!rowView.get(OnixTable.Valve5).isNull())
		{
			onix.setValve(4, rowView.getValue(OnixTable.Valve5));
		}
		if(!rowView.get(OnixTable.Valve6).isNull())
		{
			onix.setValve(5, rowView.getValue(OnixTable.Valve6));
		}
		if(!rowView.get(OnixTable.Valve7).isNull())
		{
			onix.setValve(6, rowView.getValue(OnixTable.Valve7));
		}
		if(!rowView.get(OnixTable.Valve8).isNull())
		{
			onix.setValve(7, rowView.getValue(OnixTable.Valve8));
		}
		
		
		// X-PWM
		if(!rowView.get(OnixTable.XPWMPeriod).isNull())
		{
			int period = rowView.getValue(OnixTable.XPWMPeriod);
			if(period <= 0)
				onix.stopPWMX();
			else
			{
				if(rowView.get(OnixTable.XPWMFraction).isNull())
					throw new OnixException("Column "+OnixTable.XPWMPeriod.getColumnName()+" is non-null, but column "+OnixTable.XPWMFraction.getColumnName()+" is null.");
				double fraction = rowView.getValue(OnixTable.XPWMFraction);
				onix.startPWMX(period, fraction);
			}
		}
		
		// Y-PWM
		if(!rowView.get(OnixTable.YPWMPeriod).isNull())
		{
			int period = rowView.getValue(OnixTable.YPWMPeriod);
			if(period <= 0)
				onix.stopPWMX();
			else
			{
				if(rowView.get(OnixTable.YPWMFraction3).isNull())
					throw new OnixException("Column "+OnixTable.YPWMPeriod.getColumnName()+" is non-null, but column "+OnixTable.YPWMFraction3.getColumnName()+" is null.");
				double fraction3 = rowView.getValue(OnixTable.YPWMFraction3);
				
				if(rowView.get(OnixTable.YPWMFraction4).isNull())
					throw new OnixException("Column "+OnixTable.YPWMPeriod.getColumnName()+" is non-null, but column "+OnixTable.YPWMFraction4.getColumnName()+" is null.");
				double fraction4 = rowView.getValue(OnixTable.YPWMFraction4);
				
				if(rowView.get(OnixTable.YPWMFraction5).isNull())
					throw new OnixException("Column "+OnixTable.YPWMPeriod.getColumnName()+" is non-null, but column "+OnixTable.YPWMFraction5.getColumnName()+" is null.");
				double fraction5 = rowView.getValue(OnixTable.YPWMFraction5);
				
				if(rowView.get(OnixTable.YPWMFraction6).isNull())
					throw new OnixException("Column "+OnixTable.YPWMPeriod.getColumnName()+" is non-null, but column "+OnixTable.YPWMFraction6.getColumnName()+" is null.");
				double fraction6 = rowView.getValue(OnixTable.YPWMFraction6);
				
				onix.startPWMY(period, fraction3, fraction4, fraction5, fraction6);
			}
		}		
	}
}
