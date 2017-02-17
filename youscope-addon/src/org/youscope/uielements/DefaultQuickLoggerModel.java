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
package org.youscope.uielements;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Default model for the quick logger.
 * @author mlang
 *
 */
public class DefaultQuickLoggerModel implements QuickLoggerModel 
{
	private final String[] dateLines;
	private final String[] messageLines;
	private final int maxLines;
	private volatile int firstLine = 0;
	private volatile int lastLine = -1;
	private final Calendar calendar = GregorianCalendar.getInstance();
	private final ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
	/**
	 * Constructor.
	 * @param maxLines Maximal number of lines stored in the logger.
	 */
	public DefaultQuickLoggerModel(int maxLines)
	{
		this.maxLines = maxLines;
		dateLines = new String[maxLines];
		messageLines = new String[maxLines];
	}
	/**
	 * Constructor. Sets number of stored lines to 200.
	 */
	public DefaultQuickLoggerModel()
	{
		this(200);
	}
	@Override
	public int getNumLines() 
	{
		if(lastLine < 0)
			return 0;
		else if(firstLine != 0)
			return maxLines;
		else 
			return lastLine+1;
	}

	@Override
	public String getLine(int line) {
		return messageLines[(maxLines + lastLine - line) % maxLines];
	}

	@Override
	public String getLineTime(int line) {
		return dateLines[(maxLines + lastLine - line) % maxLines];
	}
	@Override
	public synchronized void clearMessages()
    {
    	firstLine = 0;
    	lastLine = -1;
    	fireMessagesChanged();
    }
	@Override
	public void addMessage(String message, long time)
    {
    	if(time <0)
    		time = System.currentTimeMillis();
    	
    	// Get time
    	int hour;
    	int minute;
    	int second;
    	synchronized(calendar)
    	{
	    	calendar.setTime(new Date(time));
	    	hour = calendar.get(Calendar.HOUR_OF_DAY);
	    	minute = calendar.get(Calendar.MINUTE);
	    	second = calendar.get(Calendar.SECOND);
    	} 
    	    	
    	// prepare strings
    	String[] lines = message.split("\n");
    	String date = (hour<10 ? "0" : "") + Integer.toString(hour)+":"
    		+ (minute<10 ? "0" : "") + Integer.toString(minute)+":"
    		+ (second<10 ? "0" : "") + Integer.toString(second) +" >";
    	
    	// Update elements
    	synchronized(this)
    	{
    		for(int i=lines.length-1; i>=0; i--)
    		{
    			// Update line index in buffer
    			if(lastLine < 0)
    				lastLine++;
    			else
    			{
    				lastLine++;
    				if(lastLine >= maxLines)
    				{
    					lastLine = 0;
    				}
    				if(lastLine == firstLine)
    				{
    					firstLine++;
    					if(firstLine >= maxLines)
    						firstLine = 0;
    				}
    			}
    			
    			messageLines[lastLine] = lines[i];
    			if(i==0)
    				dateLines[lastLine] = date;
    			else
    				dateLines[lastLine] = null;
    		}
    	}
    	fireMessagesChanged();
    }
	protected void fireMessagesChanged()
	{
		synchronized (actionListeners) 
		{
			for(ActionListener listener : actionListeners)
			{
				listener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_FIRST, "lines changed"));
			}
		}
	}
	@Override
	public int getTimeMaxChar() {
		return 11;
	}
	@Override
	public void addActionListener(ActionListener listener) {
		synchronized(actionListeners)
		{
			actionListeners.add(listener);
		}
	}
	@Override
	public void removeActionListener(ActionListener listener) {
		synchronized (actionListeners) {
			actionListeners.remove(listener);
		}
	}
	@Override
	public int getMaxNumLines() {
		return maxLines;
	}
}
