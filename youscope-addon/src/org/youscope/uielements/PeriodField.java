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
package org.youscope.uielements;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Arrays;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

/**
 * Field which allows to define a period in any unit. Internally uses ms.
 * @author Moritz Lang
 *
 */
public class PeriodField extends JPanel
{
	/**
	 * Serial Version UID.
	 */
	private static final long	serialVersionUID	= 7888892868550101052L;
	private final JFormattedTextField			durationField				= new JFormattedTextField(StandardFormats.getDoubleFormat());
	private final TimeUnit[] units;
	private final JComboBox<TimeUnit>					unitField;
	private int lastValidUnitIdx = 0;
	/**
	 * Shortcut to create a period field with all periods from milli second to days.
	 */
	public PeriodField()
	{
		this(new TimeUnit[]{TimeUnit.MILLI_SECOND, TimeUnit.SECOND, TimeUnit.MINUTE, TimeUnit.HOUR, TimeUnit.DAY});
		
	}	
	/**
	 * Constructor.
	 * Constructs a period field with the given time units to choose from.
	 * @param units Units of time to choose from.
	 */
	public PeriodField(TimeUnit[] units)
	{
		super(new BorderLayout(5, 0));
		if(units == null)
			throw new IllegalArgumentException("At least one unit has to be defined.");
		this.units = units;
		Arrays.sort(this.units);
		unitField = new JComboBox<TimeUnit>(this.units);
		
		setOpaque(false);
		add(durationField, BorderLayout.CENTER);
		add(unitField, BorderLayout.EAST);
		
		unitField.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				// Change value to new unit.
				int newUnitIdx = unitField.getSelectedIndex();
				if(lastValidUnitIdx == newUnitIdx || newUnitIdx < 0)
					return;
				durationField.setValue(PeriodField.this.units[newUnitIdx].toUnit(PeriodField.this.units[lastValidUnitIdx].toMs(((Number)durationField.getValue()).doubleValue())));
				lastValidUnitIdx = newUnitIdx;
			}
		});
	}	
	
	/**
	 * Adds an action listener when value changes
	 * @param listener Listener to add
	 */
	public void addActionListener(ActionListener listener)
	{
		durationField.addActionListener(listener);
	}
	/**
	 * Removes a previously added action listener.
	 * @param listener listener to remove.
	 */
	public void removeActionListener(ActionListener listener)
	{
		durationField.removeActionListener(listener);
	}
	
	/**
	 * Sets the duration in ms. Automatically adjusts the unit to represent the time in the "highest" unit with which it is representable as an integer value.
	 * @param timeInMs Time duration, measured in ms.
	 */
	public void setDuration(int timeInMs)
	{
		setDuration((long)timeInMs);
	}
	
	/**
	 * Sets the duration in ms. Automatically adjusts the unit to represent the time in the "highest" unit with which it is representable as an integer value.
	 * @param timeInMs Time duration, measured in ms.
	 */
	public void setDuration(long timeInMs)
	{
		lastValidUnitIdx = 0;
		
		// Search for highest unit in which the duration can be represented as an integer value.
		if(timeInMs != 0)
		{
			for(int i=1; i<units.length; i++)
			{
				if(units[i].isIntegerInUnit(timeInMs))
					lastValidUnitIdx = i;
				else
					break;
			}
		}
		
		// Set unit field
		unitField.setSelectedIndex(lastValidUnitIdx);
		durationField.setValue(units[lastValidUnitIdx].toUnit(timeInMs));
	}
	
	/**
	 * Returns the duration in ms. Automatically handles the conversion from the currently selected unit.
	 * @return Duration in ms.
	 */
	public int getDuration()
	{
		return (int)units[unitField.getSelectedIndex()].toMs(((Number)durationField.getValue()).doubleValue());
	}
	
	/**
	 * Returns the duration in ms. Automatically handles the conversion from the currently selected unit.
	 * @return Duration in ms.
	 */
	public long getDurationLong()
	{
		return units[unitField.getSelectedIndex()].toMs(((Number)durationField.getValue()).doubleValue());
	}
	
	/**
	 * Forces the current value to be taken and set as the current value
	 * @throws ParseException
	 */
	public void commitEdit() throws ParseException
	{
		durationField.commitEdit();
	}
	
	/**
	 * Returns the currently selected time unit.
	 * @return Currently selected time unit.
	 */
	public TimeUnit getActiveTimeUnit()
	{
		return units[unitField.getSelectedIndex()];
	}
}
