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

import java.awt.event.ActionListener;

/**
 * Model of the quick logger.
 * @author mlang
 *
 */
public interface QuickLoggerModel 
{
	/**
	 * Returns current number of stored lines.
	 * @return Current number of stored lines.
	 */
	public int getNumLines();
	/**
	 * Returns the maximal number of stored lines.
	 * @return maximal number of stored lines.
	 */
	public int getMaxNumLines();
	/**
	 * Returns the message at the line with the given index. The line = 0 corresponds to the last message.
	 * @param line Line to return.
	 * @return Message at given line.
	 */
	public String getLine(int line);
	/**
	 * Returns a string describing the creation time of the message, or null if not the first line of a message.
	 * @param line Line to return the creation line of.
	 * @return Creation time of message.
	 */
	public String getLineTime(int line);
	/**
	 * Returns the maximal number of characters the date string consists of.
	 * @return Maximal number of characters.
	 */
	public int getTimeMaxChar();
	/**
	 * Adds a listener which gets notified if the logged data changed.
	 * @param listener Listener to add.
	 */
	public void addActionListener(ActionListener listener);
	/**
	 * Removes a previously added listener.
	 * @param listener listener to remove.
	 */
	public void removeActionListener(ActionListener listener);
	
	/**
	 * Adds a message to the log. Line breaks in the message are converted into multiple lines.
	 * @param message Message to add.
	 * @param time Time of message.
	 */
	public void addMessage(String message, long time);
	
	/**
	 * Clears all messages/lines from the model.
	 */
	public void clearMessages();
}
